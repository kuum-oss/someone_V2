package org.example.infrastructure.web;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.core.entity.Notification;
import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import org.example.core.entity.User;
import org.example.core.service.AdminDashboardService;
import org.example.core.service.AuthService;
import org.example.core.service.OrderService;
import org.example.infrastructure.repository.JdbcBookRepository;
import org.example.infrastructure.repository.JdbcNotificationRepository;
import org.example.infrastructure.repository.JdbcOrderRepository;
import org.example.infrastructure.repository.JdbcUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private final JdbcBookRepository bookRepository;
    private final JdbcUserRepository userRepository;
    private final JdbcOrderRepository orderRepository;
    private final JdbcNotificationRepository notificationRepository;
    
    private final AuthService authService;
    private final AdminDashboardService dashboardService;
    private final OrderService orderService;
    
    private final ResourceBundle messages;
    
    private final Configuration freeMarkerCfg;
    private Javalin app;

    // Простая защита от DoS: ограничение количества запросов по IP
    private final Map<String, Long> requestCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long MIN_INTERVAL_MS = 100; // Минимальный интервал между запросами (10 в секунду)

    public WebServer() {
        this.bookRepository = new JdbcBookRepository();
        this.userRepository = new JdbcUserRepository();
        this.orderRepository = new JdbcOrderRepository();
        this.notificationRepository = new JdbcNotificationRepository();
        
        this.authService = new AuthService(userRepository);
        this.dashboardService = new AdminDashboardService(bookRepository, notificationRepository);
        this.orderService = new OrderService(orderRepository, bookRepository, dashboardService);
        
        this.messages = ResourceBundle.getBundle("messages", Locale.getDefault());
        
        this.freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_32);
        this.freeMarkerCfg.setClassForTemplateLoading(WebServer.class, "/");
        this.freeMarkerCfg.setDefaultEncoding("UTF-8");
        this.freeMarkerCfg.setOutputEncoding("UTF-8");
        this.freeMarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.freeMarkerCfg.setLogTemplateExceptions(false);
        this.freeMarkerCfg.setWrapUncheckedExceptions(true);
        this.freeMarkerCfg.setFallbackOnNullLoopVariable(false);
    }

    public int start(int port) {
        try {
            app = Javalin.create(config -> {
                config.staticFiles.add("/public");
                config.showJavalinBanner = false;
            });

            // Базовые меры безопасности
            setupSecurity();

            // Настройка эндпоинтов
            setupRoutes();

            try {
                app.start(port);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                    LOGGER.warn("Port {} is busy, trying to find an available port...", port);
                    app.start(0); // 0 means any available port
                    port = app.port();
                } else {
                    throw e;
                }
            }

            LOGGER.info("Web server started successfully at http://localhost:{}", port);
            return port;
        } catch (Exception e) {
            LOGGER.error("Failed to start web server", e);
            throw new RuntimeException("Web server failed to start", e);
        }
    }

    private void setupSecurity() {
        // 1. Ограничение скорости (Rate Limiting) для защиты от DoS/Brute-force
        app.before(ctx -> {
            String ip = ctx.ip();
            long now = System.currentTimeMillis();
            
            // Проверка интервала
            Long lastTime = lastRequestTime.get(ip);
            if (lastTime != null && (now - lastTime) < MIN_INTERVAL_MS) {
                LOGGER.warn("DoS protection triggered for IP: {}. Too fast requests.", ip);
                ctx.status(HttpStatus.TOO_MANY_REQUESTS).result(messages.getString("error.too_many_requests"));
                return;
            }
            lastRequestTime.put(ip, now);

            // Ограничение по минутам
            long minute = now / 60000;
            String key = ip + ":" + minute;
            long count = requestCounts.compute(key, (k, v) -> v == null ? 1L : v + 1L);
            if (count > MAX_REQUESTS_PER_MINUTE) {
                LOGGER.warn("DoS protection triggered for IP: {}. Minute limit exceeded.", ip);
                ctx.status(HttpStatus.TOO_MANY_REQUESTS).result(messages.getString("error.too_many_requests"));
                return;
            }
            
            // Очистка старых данных каждые 5 минут
            if (requestCounts.size() > 1000) {
                requestCounts.entrySet().removeIf(entry -> {
                    String[] parts = entry.getKey().split(":");
                    if (parts.length < 2) return true;
                    try {
                        long entryMinute = Long.parseLong(parts[parts.length - 1]);
                        return entryMinute < minute;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                });
                lastRequestTime.entrySet().removeIf(entry -> (now - entry.getValue()) > 60000);
            }
        });

        // 2. Безопасные заголовки (Security Headers)
        app.after(ctx -> {
            ctx.header("X-Content-Type-Options", "nosniff");
            ctx.header("X-Frame-Options", "DENY");
            ctx.header("X-XSS-Protection", "1; mode=block");
            ctx.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            ctx.header("Content-Security-Policy", "default-src 'self'; img-src 'self' data: https://via.placeholder.com; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'");
        });

        // 3. Базовая проверка CSRF для POST запросов
        app.before(ctx -> {
            if (ctx.method().toString().equals("POST")) {
                String referer = ctx.header("Referer");
                String host = ctx.header("Host");
                if (referer != null && host != null && !referer.contains(host)) {
                    LOGGER.warn("Potential CSRF attack detected from IP: {}. Referer: {}", ctx.ip(), referer);
                    ctx.status(HttpStatus.FORBIDDEN).result(messages.getString("error.csrf_detected"));
                }
            }
        });
    }

    private void setupRoutes() {
        // Главная страница
        app.get("/", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.redirect("/shop");
                return;
            }
            Map<String, Object> model = createModel(ctx);
            // В библиотеке только купленные книги (или загруженные самим пользователем)
            List<StoredBook> books = bookRepository.findOwnedBooksByUserId(user.getId());
            model.put("books", books);
            render(ctx, "templates/library.ftl", model);
        });

        // Аутентификация
        app.get("/login", ctx -> render(ctx, "templates/login.ftl", createModel(ctx)));
        app.post("/login", ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            try {
                if (authService.login(email, password)) {
                    ctx.sessionAttribute("currentUser", authService.getCurrentUser());
                    ctx.redirect("/");
                } else {
                    Map<String, Object> model = createModel(ctx);
                    model.put("error", "Неверный email или пароль");
                    render(ctx, "templates/login.ftl", model);
                }
            } catch (Exception e) {
                Map<String, Object> model = createModel(ctx);
                model.put("error", e.getMessage());
                render(ctx, "templates/login.ftl", model);
            }
        });

        app.get("/register", ctx -> render(ctx, "templates/register.ftl", createModel(ctx)));
        app.post("/register", ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            if (authService.register(email, password, false)) {
                ctx.redirect("/login");
            } else {
                Map<String, Object> model = createModel(ctx);
                model.put("error", "Пользователь с таким email уже существует");
                render(ctx, "templates/register.ftl", model);
            }
        });

        app.get("/logout", ctx -> {
            ctx.consumeSessionAttribute("currentUser");
            ctx.redirect("/");
        });

        // Книги
        app.get("/book/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            bookRepository.findById(id).ifPresentOrElse(book -> {
                Map<String, Object> model = createModel(ctx);
                model.put("book", book);
                
                User user = ctx.sessionAttribute("currentUser");
                boolean isOwned = false;
                if (user != null) {
                    List<StoredBook> ownedBooks = bookRepository.findOwnedBooksByUserId(user.getId());
                    isOwned = ownedBooks.stream().anyMatch(b -> b.getId() == id) || user.isAdmin();
                }
                model.put("isOwned", isOwned);
                
                render(ctx, "templates/book_details.ftl", model);
            }, () -> ctx.status(404).result("Книга не найдена"));
        });

        app.get("/book/{id}/cover", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                bookRepository.findById(id).ifPresentOrElse(book -> {
                    if (book.getCover() != null) {
                        ctx.contentType("image/jpeg").result(book.getCover());
                    } else {
                        ctx.status(404);
                    }
                }, () -> ctx.status(404));
            } catch (Exception e) {
                LOGGER.error("Error serving cover", e);
                ctx.status(500);
            }
        });

        app.get("/book/{id}/download", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.redirect("/login");
                return;
            }
            int id = Integer.parseInt(ctx.pathParam("id"));
            
            // Проверка владения: либо загрузил сам, либо купил
            List<StoredBook> ownedBooks = bookRepository.findOwnedBooksByUserId(user.getId());
            boolean isOwned = ownedBooks.stream().anyMatch(b -> b.getId() == id);
            
            if (!isOwned && !user.isAdmin()) {
                ctx.status(403).result("Вы должны сначала купить эту книгу");
                return;
            }

            byte[] content = bookRepository.getBookContent(id);
            if (content != null) {
                bookRepository.findById(id).ifPresent(book -> {
                    ctx.contentType("application/octet-stream")
                       .header("Content-Disposition", "attachment; filename=\"" + book.getOriginalName() + "\"")
                       .result(content);
                });
            } else {
                ctx.status(404).result("Файл не найден в базе данных");
            }
        });

        // Магазин
        app.get("/shop", ctx -> {
            Map<String, Object> model = createModel(ctx);
            User user = ctx.sessionAttribute("currentUser");
            
            String selectedGenre = ctx.queryParam("genre");
            String selectedLanguage = ctx.queryParam("language");
            
            List<StoredBook> allPublicBooks = bookRepository.findPublicBooks();
            
            // Фильтрация по жанру и языку
            List<StoredBook> filteredBooks = allPublicBooks.stream()
                    .filter(b -> (selectedGenre == null || selectedGenre.isEmpty() || selectedGenre.equals(b.getGenre())))
                    .filter(b -> (selectedLanguage == null || selectedLanguage.isEmpty() || selectedLanguage.equals(b.getLanguage())))
                    .toList();
            
            if (user != null) {
                List<StoredBook> ownedBooks = bookRepository.findOwnedBooksByUserId(user.getId());
                List<Integer> ownedIds = ownedBooks.stream().map(StoredBook::getId).toList();
                // Показываем в магазине только те книги, которых нет у пользователя
                List<StoredBook> shopBooks = filteredBooks.stream()
                        .filter(b -> !ownedIds.contains(b.getId()))
                        .toList();
                model.put("books", shopBooks);
            } else {
                model.put("books", filteredBooks);
            }
            
            model.put("genres", bookRepository.findAllGenres());
            model.put("languages", bookRepository.findAllLanguages());
            model.put("selectedGenre", selectedGenre);
            model.put("selectedLanguage", selectedLanguage);
            
            render(ctx, "templates/shop.ftl", model);
        });

        app.post("/shop/buy", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.redirect("/login");
                return;
            }
            int bookId = Integer.parseInt(ctx.formParam("bookId"));
            bookRepository.findById(bookId).ifPresent(book -> {
                // Проверяем, не куплена ли уже
                List<StoredBook> ownedBooks = bookRepository.findOwnedBooksByUserId(user.getId());
                if (ownedBooks.stream().anyMatch(b -> b.getId() == bookId)) {
                    return; // Уже куплено
                }

                if (book.getBookType() == StoredBook.BookType.PHYSICAL || user.getPoints() >= 1) {
                    if (book.getBookType() == StoredBook.BookType.PHYSICAL) {
                        orderService.placeOrder(user.getId(), book.getId());
                    } else {
                        // Для электронной книги тоже создаем запись в orders, чтобы findOwnedBooksByUserId ее увидел
                        orderRepository.save(new Order(null, user.getId(), book.getId(), Order.Status.DELIVERED, java.time.LocalDateTime.now()));
                        authService.updateCurrentUserPoints(user.getPoints() - 1);
                    }
                    
                    // Обновляем пользователя в сессии (могли измениться поинты)
                    ctx.sessionAttribute("currentUser", authService.getCurrentUser());
                }
            });
            ctx.redirect("/shop");
        });

        app.post("/user/add-point", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null) {
                authService.updateCurrentUserPoints(user.getPoints() + 1);
                ctx.sessionAttribute("currentUser", authService.getCurrentUser());
            }
            ctx.redirect("/shop");
        });

        // Админка
        app.get("/admin", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null && user.isAdmin()) {
                Map<String, Object> model = createModel(ctx);
                model.put("totalBooks", dashboardService.getTotalBookCount());
                model.put("totalVolume", String.format("%.2f", dashboardService.getTotalDataVolumeGB()));
                model.put("users", userRepository.findAll());
                model.put("notifications", notificationRepository.findAll());
                render(ctx, "templates/admin_dashboard.ftl", model);
            } else {
                ctx.status(403).result("Доступ запрещен");
            }
        });

        app.post("/admin/add-notification", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null && user.isAdmin()) {
                String message = ctx.formParam("message");
                dashboardService.addNotification(null, message);
                ctx.redirect("/admin");
            } else {
                ctx.status(403);
            }
        });

        app.exception(Exception.class, (e, ctx) -> {
            LOGGER.error("Unhandled exception in web server", e);
            ctx.status(500).result("Internal Server Error: " + e.getMessage());
        });
    }

    private Map<String, Object> createModel(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        User currentUser = ctx.sessionAttribute("currentUser");
        model.put("currentUser", currentUser);
        return model;
    }

    private void render(Context ctx, String templatePath, Map<String, Object> model) {
        try {
            Template template = freeMarkerCfg.getTemplate(templatePath);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            ctx.contentType("text/html").result(writer.toString());
        } catch (Exception e) {
            LOGGER.error("Error rendering template: " + templatePath, e);
            ctx.status(500).result("Error rendering template: " + e.getMessage());
        }
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
