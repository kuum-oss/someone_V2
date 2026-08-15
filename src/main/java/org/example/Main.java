package org.example;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.example.infrastructure.ui.BookLibraryGui;
import org.example.core.usecase.impl.ExtractMetadataUseCaseImpl;
import org.example.core.usecase.impl.OrganizeBooksUseCaseImpl;
import org.example.core.usecase.impl.GroupBooksUseCaseImpl;
import org.example.core.usecase.GroupBooksUseCase;
import org.example.adapter.gateway.TikaMetadataAdapter;
import org.example.adapter.gateway.NioFileAdapter;
import org.example.adapter.gateway.ExternalMetadataGatewayImpl;
import org.example.adapter.gateway.ThumbnailCacheService;
import org.example.infrastructure.db.DatabaseInitializer;
import org.example.infrastructure.repository.JdbcUserRepository;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.repository.JdbcBookRepository;
import org.example.infrastructure.repository.CachedBookRepository;
import org.example.core.service.AdminService;
import org.example.core.service.AuthService;
import org.example.core.service.FileStorageService;
import org.example.core.service.LibraryService;
import org.example.infrastructure.repository.JdbcLibrarySettingsRepository;
import org.example.infrastructure.ui.dialogs.AuthDialog;
import org.example.infrastructure.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting application...");
            
            // Инициализация БД
            DatabaseInitializer.initialize();

            // Чтение параметров принудительного запуска
            String envMode = System.getenv("START_MODE");
            boolean forceGui = (envMode != null && envMode.equalsIgnoreCase("gui")) 
                    || (args.length > 0 && args[0].equalsIgnoreCase("--gui"));
            boolean forceWeb = (envMode != null && envMode.equalsIgnoreCase("web")) 
                    || (args.length > 0 && args[0].equalsIgnoreCase("--web"));

            // Если запущен в безголовом режиме или форсирован режим Web, запускаем веб-сервер
            if (java.awt.GraphicsEnvironment.isHeadless() || forceWeb) {
                startWebServer();
                return;
            }

            if (forceGui) {
                startGui();
                return;
            }

            // Выбор режима запуска
            String[] options = {"Приложение (GUI)", "Сайт (Web)"};
            int selection = JOptionPane.showOptionDialog(null, 
                    "Выберите режим запуска:", 
                    "Smart Organizer Setup", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, options, options[0]);

            if (selection == 1) {
                startWebServer();
            } else if (selection == 0) {
                startGui();
            } else {
                System.exit(0);
            }

        } catch (Exception e) {
            LOGGER.error("Fatal error during startup", e);
        }
    }

    private static void startWebServer() {
        LOGGER.info("Starting Web Server mode...");
        try {
            WebServer server = new WebServer();
            int actualPort = server.start(8081);
            
            // Автоматически открываем браузер, если мы не в безголовом режиме
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                try {
                    if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                        String url = "http://localhost:" + actualPort;
                        LOGGER.info("Opening browser: {}", url);
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } else {
                        LOGGER.warn("Desktop or BROWSE action is not supported. Please open http://localhost:{} manually.", actualPort);
                        JOptionPane.showMessageDialog(null, 
                            "Сервер запущен на http://localhost:" + actualPort + "\nПожалуйста, откройте этот адрес в браузере вручную.",
                            "Сервер запущен", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to open browser", e);
                    JOptionPane.showMessageDialog(null, 
                        "Сервер запущен на http://localhost:" + actualPort + "\nНо не удалось открыть браузер автоматически.",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to start web server", e);
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                String message = "Не удалось запустить веб-сервер: " + e.getMessage();
                JOptionPane.showMessageDialog(null, 
                    message, 
                    "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void startGui() {
        LOGGER.info("Starting GUI mode...");
        // Подавляем предупреждения о Log4j2 и нативном доступе
        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("apple.awt.application.appearance", "system");
        
        // Настройка темы в зависимости от системы (особенно важно для Mac M1)
        UIManager.put("FlatLaf.setPreferredAppearance", "system");
        
        if (isSystemDarkMode()) {
            FlatMacDarkLaf.setup();
        } else {
            FlatMacLightLaf.setup();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("Initializing controllers and services...");
                    TikaMetadataAdapter metadataAdapter = new TikaMetadataAdapter();
                    NioFileAdapter fileAdapter = new NioFileAdapter();
                    ThumbnailCacheService cacheService = new ThumbnailCacheService();
                    ExternalMetadataGatewayImpl externalAdapter = new ExternalMetadataGatewayImpl(cacheService);

                    ExtractMetadataUseCaseImpl extractMetadataUseCase = new ExtractMetadataUseCaseImpl(metadataAdapter, externalAdapter);
                    OrganizeBooksUseCaseImpl organizeBooksUseCase = new OrganizeBooksUseCaseImpl(fileAdapter);
                    GroupBooksUseCaseImpl groupBooksUseCase = new GroupBooksUseCaseImpl();
                    
                    JdbcUserRepository userRepository = new JdbcUserRepository();
                    BookRepository bookRepo = new CachedBookRepository(new JdbcBookRepository());
                    org.example.infrastructure.repository.JdbcOrderRepository orderRepository = new org.example.infrastructure.repository.JdbcOrderRepository();
                    org.example.infrastructure.repository.JdbcNotificationRepository notificationRepository = new org.example.infrastructure.repository.JdbcNotificationRepository();
                    JdbcLibrarySettingsRepository settingsRepository = new JdbcLibrarySettingsRepository();
                    org.example.infrastructure.repository.JdbcReadingRepository readingRepository = new org.example.infrastructure.repository.JdbcReadingRepository();

                    AuthService authService = new AuthService(userRepository);
                    FileStorageService storageService = new FileStorageService(bookRepo, authService, metadataAdapter, orderRepository);
                    org.example.core.service.AdminDashboardService dashboardService = new org.example.core.service.AdminDashboardService(bookRepo, notificationRepository);
                    org.example.core.service.QrCodeService qrCodeService = new org.example.core.service.QrCodeService();
                    org.example.core.service.EmailService emailService = new org.example.core.service.EmailService();
                    org.example.core.service.OrderService orderService = new org.example.core.service.OrderService(orderRepository, bookRepo, dashboardService, qrCodeService, emailService);
                    AdminService adminService = new AdminService(storageService, authService, userRepository, bookRepo);
                    LibraryService libraryService = new LibraryService(settingsRepository, orderRepository);
                    org.example.core.service.ReadingService readingService = new org.example.core.service.ReadingService(readingRepository);

                    org.example.application.state.LibraryViewState state = new org.example.application.state.LibraryViewState();
                    org.example.application.controller.AuthController authController = new org.example.application.controller.AuthController(authService, state);
                    org.example.application.controller.BookLibraryController controller = new org.example.application.controller.BookLibraryController(
                            extractMetadataUseCase, organizeBooksUseCase, groupBooksUseCase, storageService, adminService, orderService, readingService, state);

                    BookLibraryGui gui = new BookLibraryGui(controller, authController, state, dashboardService, orderService, groupBooksUseCase,
                            storageService, adminService, authService, libraryService);
                    
                    gui.setVisible(true);
                    LOGGER.info("GUI is visible.");
                } catch (Exception e) {
                    LOGGER.error("Error during GUI creation", e);
                    JOptionPane.showMessageDialog(null, "Ошибка при запуске интерфейса: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });
    }

    private static boolean isSystemDarkMode() {
        // FlatLaf 3.x+ умеет определять темную тему на macOS
        return com.formdev.flatlaf.util.SystemInfo.isMacOS &&
               com.formdev.flatlaf.FlatLaf.isLafDark();
    }
}
//may