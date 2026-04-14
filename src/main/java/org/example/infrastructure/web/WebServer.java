package org.example.infrastructure.web;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;
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

    private final Map<String, Long> requestCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long MIN_INTERVAL_MS = 100;

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

            setupSecurity();
            setupRoutes();

            try {
                app.start(port);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                    LOGGER.warn("Port {} is busy, trying to find an available port...", port);
                    app.start(0);
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
        // Rate limiting
        app.before(ctx -> {
            String ip = ctx.ip();
            long now = System.currentTimeMillis();

            Long lastTime = lastRequestTime.get(ip);
            if (lastTime != null && (now - lastTime) < MIN_INTERVAL_MS) {
                ctx.status(HttpStatus.TOO_MANY_REQUESTS).result(messages.getString("error.too_many_requests"));
                return;
            }
            lastRequestTime.put(ip, now);

            long minute = now / 60000;
            String key = ip + ":" + minute;
            long count = requestCounts.compute(key, (k, v) -> v == null ? 1L : v + 1L);
            if (count > MAX_REQUESTS_PER_MINUTE) {
                ctx.status(HttpStatus.TOO_MANY_REQUESTS).result(messages.getString("error.too_many_requests"));
                return;
            }

            // Cleanup old data
            if (requestCounts.size() > 1000) {
                requestCounts.entrySet().removeIf(entry -> {
                    String[] parts = entry.getKey().split(":");
                    try {
                        return Long.parseLong(parts[1]) < minute;
                    } catch (Exception e) {
                        return true;
                    }
                });
                lastRequestTime.entrySet().removeIf(entry -> (now - entry.getValue()) > 60000);
            }
        });

        // Security headers
        app.after(ctx -> {
            ctx.header("X-Content-Type-Options", "nosniff");
            ctx.header("X-Frame-Options", "DENY");
            ctx.header("X-XSS-Protection", "1; mode=block");
            ctx.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            ctx.header("Content-Security-Policy", "default-src 'self'; img-src 'self' data: https://via.placeholder.com; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'");
        });

        // CSRF check
        app.before(ctx -> {
            if ("POST".equals(ctx.method())) {
                String referer = ctx.header("Referer");
                String host = ctx.header("Host");
                if (referer != null && host != null && !referer.contains(host)) {
                    ctx.status(HttpStatus.FORBIDDEN).result(messages.getString("error.csrf_detected"));
                }
            }
        });
    }

    private void setupRoutes() {
        // --- Главная ---
        app.get("/", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) { ctx.redirect("/shop"); return; }
            Map<String, Object> model = createModel(ctx);
            model.put("books", bookRepository.findOwnedBooksByUserId(user.getId()));
            render(ctx, "templates/library.ftl", model);
        });

        app.get("/admin/user/{id}", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }

            int targetUserId = Integer.parseInt(ctx.pathParam("id"));

            // Доступ разрешен если: ты админ ИЛИ ты смотришь свой собственный профиль
            if (currentUser.isAdmin() || currentUser.getId() == targetUserId) {
                userRepository.findById(targetUserId).ifPresentOrElse(user -> {
                    Map<String, Object> model = createModel(ctx);
                    model.put("targetUser", user);
                    model.put("userOrders", orderRepository.findByUserId(targetUserId));
                    render(ctx, "templates/user_profile.ftl", model);
                }, () -> ctx.status(404).result("Пользователь не найден"));
            } else {
                ctx.status(403).result("Доступ запрещен");
            }
        });

        app.post("/admin/user/update-points", ctx -> {
            User admin = ctx.sessionAttribute("currentUser");
            if (admin != null && admin.isAdmin()) {
                int id = Integer.parseInt(ctx.formParam("userId"));
                int points = Integer.parseInt(ctx.formParam("points"));
                userRepository.updatePoints(id, points);
                ctx.redirect("/admin/user/" + id);
            } else ctx.status(403);
        });

        app.post("/admin/user/delete", ctx -> {
            User admin = ctx.sessionAttribute("currentUser");
            if (admin != null && admin.isAdmin()) {
                int id = Integer.parseInt(ctx.formParam("userId"));
                if (id != admin.getId()) {
                    userRepository.deleteById(id);
                    ctx.redirect("/admin");
                } else ctx.status(400).result("Нельзя удалить себя");
            } else ctx.status(403);
        });

        // --- Авторизация ---
        app.get("/login", ctx -> render(ctx, "templates/login.ftl", createModel(ctx)));
        app.post("/login", ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            try {
                if (authService.login(email, password)) {
                    ctx.sessionAttribute("currentUser", authService.getCurrentUser());
                    ctx.redirect("/");
                } else {
                    Map<String,Object> model = createModel(ctx);
                    model.put("error","Неверный email или пароль");
                    render(ctx,"templates/login.ftl",model);
                }
            } catch(Exception e) {
                Map<String,Object> model = createModel(ctx);
                model.put("error",e.getMessage());
                render(ctx,"templates/login.ftl",model);
            }
        });

        app.get("/register", ctx -> render(ctx,"templates/register.ftl",createModel(ctx)));
        app.post("/register", ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            if (authService.register(email,password,false)) ctx.redirect("/login");
            else {
                Map<String,Object> model = createModel(ctx);
                model.put("error","Пользователь с таким email уже существует");
                render(ctx,"templates/register.ftl",model);
            }
        });

        app.get("/logout", ctx -> { ctx.consumeSessionAttribute("currentUser"); ctx.redirect("/"); });

        // --- Книги ---
        app.get("/book/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            bookRepository.findById(id).ifPresentOrElse(book -> {
                Map<String,Object> model = createModel(ctx);
                model.put("book",book);
                User user = ctx.sessionAttribute("currentUser");
                boolean isOwned = user != null && (bookRepository.findOwnedBooksByUserId(user.getId()).stream().anyMatch(b->b.getId()==id) || user.isAdmin());
                model.put("isOwned",isOwned);
                render(ctx,"templates/book_details.ftl",model);
            }, () -> ctx.status(404).result("Книга не найдена"));
        });

        app.get("/book/{id}/cover", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            bookRepository.findById(id).ifPresent(book -> { if(book.getCover()!=null) ctx.contentType("image/jpeg").result(book.getCover()); });
        });

        app.get("/book/{id}/download", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if(user==null){ctx.redirect("/login"); return;}
            int id = Integer.parseInt(ctx.pathParam("id"));
            List<StoredBook> owned = bookRepository.findOwnedBooksByUserId(user.getId());
            if(owned.stream().noneMatch(b->b.getId()==id) && !user.isAdmin()){ ctx.status(403).result("Доступ запрещен"); return;}
            byte[] content = bookRepository.getBookContent(id);
            if(content!=null){ bookRepository.findById(id).ifPresent(book -> ctx.contentType("application/octet-stream").header("Content-Disposition","attachment; filename=\""+book.getOriginalName()+"\"").result(content)); }
        });

        // --- Магазин ---
        app.get("/shop", ctx -> {
            Map<String,Object> model = createModel(ctx);
            User user = ctx.sessionAttribute("currentUser");
            String q = ctx.queryParam("q"), g=ctx.queryParam("genre"), l=ctx.queryParam("language"), s=ctx.queryParam("sort");
            int page = ctx.queryParamAsClass("page",Integer.class).getOrDefault(1), pageSize=12;
            List<StoredBook> books = bookRepository.findPublicBooks(q,g,l,s,(page-1)*pageSize,pageSize);
            long total = bookRepository.countPublicBooks(q,g,l);
            if(user!=null) model.put("ownedIds",bookRepository.findOwnedBooksByUserId(user.getId()).stream().map(StoredBook::getId).toList());
            model.put("books",books); model.put("genres",bookRepository.findAllGenres()); model.put("languages",bookRepository.findAllLanguages());
            model.put("currentPage",page); model.put("totalPages",(int)Math.ceil((double)total/pageSize));
            render(ctx,"templates/shop.ftl",model);
        });

        app.post("/shop/buy", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if(user==null){ctx.redirect("/login");return;}
            int bookId=Integer.parseInt(ctx.formParam("bookId"));
            bookRepository.findById(bookId).ifPresent(book -> {
                if(book.getBookType()==StoredBook.BookType.PHYSICAL){ orderService.placeOrder(user.getId(),book.getId()); }
                else if(user.getPoints()>=1){
                    orderRepository.save(new Order(null,user.getId(),book.getId(),Order.Status.DELIVERED,java.time.LocalDateTime.now()));
                    authService.updateCurrentUserPoints(user.getPoints()-1);
                    ctx.sessionAttribute("currentUser",authService.getCurrentUser());
                }
            });
            ctx.redirect("/shop");
        });

        // --- Админка ---
        app.get("/admin", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if(user!=null && user.isAdmin()){
                Map<String,Object> model = createModel(ctx);
                model.put("totalBooks",dashboardService.getTotalBookCount());
                model.put("totalVolume",String.format("%.2f",dashboardService.getTotalDataVolumeGB()));
                model.put("users",userRepository.findAll());
                model.put("notifications",notificationRepository.findAll());
                render(ctx,"templates/admin_dashboard.ftl",model);
            }else ctx.status(403);
        });

        app.get("/admin/book/edit/{id}", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null && user.isAdmin()) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                bookRepository.findById(id).ifPresentOrElse(book -> {
                    Map<String, Object> model = createModel(ctx);
                    model.put("book", book);
                    render(ctx, "templates/edit_book.ftl", model);
                }, () -> ctx.status(404).result("Книга не найдена"));
            } else ctx.status(403);
        });

        app.post("/admin/book/edit", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null && user.isAdmin()) {
                int id = Integer.parseInt(ctx.formParam("id"));
                bookRepository.findById(id).ifPresent(book -> {
                    book.setTitle(ctx.formParam("title"));
                    book.setAuthor(ctx.formParam("author"));
                    book.setGenre(ctx.formParam("genre"));
                    book.setLanguage(ctx.formParam("language"));
                    book.setYear(ctx.formParam("year"));
                    book.setDescription(ctx.formParam("description"));
                    book.setBookType(StoredBook.BookType.valueOf(ctx.formParam("bookType")));

                    UploadedFile file = ctx.uploadedFile("coverFile");
                    if (file != null && file.size() > 0) {
                        try {
                            book.setCover(file.content().readAllBytes());
                        } catch (Exception e) {
                            LOGGER.error("Error reading uploaded cover", e);
                        }
                    }
                    bookRepository.update(book);
                });
                ctx.redirect("/shop");
            } else ctx.status(403);
        });

        app.post("/admin/add-notification", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if(user!=null && user.isAdmin()){
                dashboardService.addNotification(null,ctx.formParam("message"));
                ctx.redirect("/admin");
            }else ctx.status(403);
        });
        // --- Пополнение баллов (Тест) ---
        app.post("/user/add-point", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user != null) {
                int newPoints = user.getPoints() + 1;
                userRepository.updatePoints(user.getId(), newPoints);
                user.setPoints(newPoints);
                ctx.sessionAttribute("currentUser", user);

                ctx.redirect(ctx.header("Referer") != null ? ctx.header("Referer") : "/");
            } else {
                ctx.redirect("/login");
            }
        });

        // --- Глобальный обработчик ошибок ---
        app.exception(Exception.class,(e,ctx)->{
            LOGGER.error("Unhandled exception in web server",e);
            ctx.status(500).result("Internal Server Error: "+e.getMessage());
        });
    }

    private Map<String,Object> createModel(Context ctx){
        Map<String,Object> model = new HashMap<>();
        model.put("currentUser",ctx.sessionAttribute("currentUser"));
        return model;
    }

    private void render(Context ctx,String templatePath,Map<String,Object> model){
        try{
            Template template = freeMarkerCfg.getTemplate(templatePath);
            StringWriter writer = new StringWriter();
            template.process(model,writer);
            ctx.contentType("text/html").result(writer.toString());
        }catch(Exception e){
            LOGGER.error("Error rendering template: "+templatePath,e);
            ctx.status(500).result("Error rendering template: "+e.getMessage());
        }
    }

    public void stop(){ if(app!=null) app.stop(); }
}
