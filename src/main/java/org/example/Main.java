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
import org.example.infrastructure.repository.JdbcBookRepository;
import org.example.core.service.AdminService;
import org.example.core.service.AuthService;
import org.example.core.service.FileStorageService;
import org.example.infrastructure.ui.dialogs.AuthDialog;
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
            
            // Подавляем предупреждения о Log4j2 и нативном доступе
            System.setProperty("log4j2.disable.jmx", "true");
            System.setProperty("apple.awt.application.appearance", "system");
            
            // Настройка темы в зависимости от системы (особенно важно для Mac M1)
            // Использование FlatPreferences для автоматического определения темы
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
                    JdbcBookRepository bookRepository = new JdbcBookRepository();
                    org.example.infrastructure.repository.JdbcOrderRepository orderRepository = new org.example.infrastructure.repository.JdbcOrderRepository();
                    org.example.infrastructure.repository.JdbcNotificationRepository notificationRepository = new org.example.infrastructure.repository.JdbcNotificationRepository();

                    AuthService authService = new AuthService(userRepository);
                    FileStorageService storageService = new FileStorageService(bookRepository, authService, metadataAdapter);
                    org.example.core.service.AdminDashboardService dashboardService = new org.example.core.service.AdminDashboardService(bookRepository, notificationRepository);
                    org.example.core.service.OrderService orderService = new org.example.core.service.OrderService(orderRepository, bookRepository, dashboardService);
                    AdminService adminService = new AdminService(storageService, authService, userRepository, bookRepository);

                    org.example.application.state.LibraryViewState state = new org.example.application.state.LibraryViewState();
                    org.example.application.controller.AuthController authController = new org.example.application.controller.AuthController(authService, state);
                    org.example.application.controller.BookLibraryController controller = new org.example.application.controller.BookLibraryController(
                            extractMetadataUseCase, organizeBooksUseCase, groupBooksUseCase, storageService, adminService, orderService, state);

                    BookLibraryGui gui = new BookLibraryGui(controller, authController, state, dashboardService, orderService, groupBooksUseCase,
                            storageService, adminService, authService);
                    
                    gui.setVisible(true);
                    LOGGER.info("GUI is visible.");
                } catch (Exception e) {
                    LOGGER.error("Error during GUI creation", e);
                    JOptionPane.showMessageDialog(null, "Ошибка при запуске интерфейса: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Fatal error during startup", e);
        }
    }

    private static boolean isSystemDarkMode() {
        // FlatLaf 3.x+ умеет определять темную тему на macOS
        return com.formdev.flatlaf.util.SystemInfo.isMacOS &&
               com.formdev.flatlaf.FlatLaf.isLafDark();
    }
}
