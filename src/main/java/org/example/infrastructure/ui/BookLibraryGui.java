package org.example.infrastructure.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.example.application.controller.AuthController;
import org.example.application.controller.BookLibraryController;
import org.example.application.state.LibraryViewState;
import org.example.application.state.ViewMode;
import org.example.core.entity.Book;
import org.example.core.service.*;
import org.example.infrastructure.ui.dialogs.AdminSettingsDialog;
import org.example.infrastructure.ui.dialogs.SeatSelectionDialog;
import org.example.core.usecase.GroupBooksUseCase;
import org.example.core.util.BookFileUtils;
import org.example.infrastructure.ui.components.BookDetailsPanel;
import org.example.infrastructure.ui.components.BookGridView;
import org.example.infrastructure.ui.dialogs.AuthDialog;
import org.example.infrastructure.ui.dialogs.BookPreviewDialog;
import org.example.infrastructure.ui.dialogs.LibraryDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class BookLibraryGui extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookLibraryGui.class);
    private final BookLibraryController controller;
    private final AuthController authController;
    private final LibraryViewState state;
    private final AdminDashboardService dashboardService;
    private final OrderService orderService;
    private final GroupBooksUseCase groupBooksUseCase;
    private final org.example.core.service.FileStorageService storageService; // TEMP for dialogs
    private final org.example.core.service.AdminService adminService; // TEMP for dialogs
    private final org.example.core.service.AuthService authService; // TEMP for dialogs
    private final LibraryService libraryService;
    private final GenreImageService genreImageService = new GenreImageService();

    private final Preferences prefs = Preferences.userNodeForPackage(BookLibraryGui.class);

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree tree;
    private BookGridView gridView;
    private JPanel centerCardPanel;
    private CardLayout centerCardLayout;

    private JLabel statusLabel;
    private JLabel pointsLabel;
    private JProgressBar progressBar;
    private JButton organizeButton;
    private JButton cancelButton;
    private JButton exitButton;
    private JButton searchButton;
    private JButton headerBookInfoButton;
    private JButton shopButton;
    private JButton physicalShopButton;
    private JButton libraryButton;
    private JButton adminPanelButton;
    private JButton addPointButton;
    private JToggleButton listViewButton;
    private JToggleButton gridViewButton;

    private BookDetailsPanel detailsPanel;
    private JTextField searchField;
    private JComboBox<String> groupModeCombo;

    private JMenu libraryMenu;
    private JMenu adminMenu;
    private JMenu accountMenu;
    private JMenu settingsMenu;
    private JMenu langMenu;
    private JMenu themeMenu;
    private JMenu toolsMenu;
    private JMenu addMenu;

    private JMenuItem myLibraryItem;
    private JMenuItem physicalShopItem;
    private JMenuItem adminDashboardItem;
    private JMenuItem userManagementItem;
    private JMenuItem addEBookItem;
    private JMenuItem addPhysicalBookItem;
    private JMenuItem loginItem;
    private JMenuItem registerItem;
    private JMenuItem logoutItem;
    private JMenuItem currentUserItem;
    private JMenuItem uploadItem;
    private JMenuItem uploadAllItem;
    private JMenuItem statsItem;
    private JMenuItem dupsItem;
    private JMenuItem langEnItem;
    private JMenuItem langRuItem;
    private JMenuItem langUkItem;
    private JMenuItem themeLightItem;
    private JMenuItem themeDarkItem;
    private JMenuItem uploadAllContextItem;
    private JMenuItem uploadToShopItem;

    private SwingWorker<?, ?> currentWorker;
    private ResourceBundle messages;
    private Locale currentLocale;

    // Для подсчета времени сканирования
    private long startTime;

    public BookLibraryGui(BookLibraryController controller,
                          AuthController authController,
                          LibraryViewState state,
                          AdminDashboardService dashboardService,
                          OrderService orderService,
                          GroupBooksUseCase groupBooksUseCase,
                          org.example.core.service.FileStorageService storageService,
                          org.example.core.service.AdminService adminService,
                          org.example.core.service.AuthService authService,
                          LibraryService libraryService) {
        this.controller = controller;
        this.authController = authController;
        this.state = state;
        this.dashboardService = dashboardService;
        this.orderService = orderService;
        this.groupBooksUseCase = groupBooksUseCase;
        this.storageService = storageService;
        this.adminService = adminService;
        this.authService = authService;
        this.libraryService = libraryService;

        initLocale(new Locale("en"));
        initLookAndFeel();
        try {
            initUI();
            initMenuBar();
            setupDragAndDrop();
            setupContextMenus();
            setupHotkeys();
            updateAuthUI();
        } catch (Exception e) {
            LOGGER.error("Critical error during UI initialization", e);
            JOptionPane.showMessageDialog(null, "Critical error during UI initialization: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAuthUI() {
        boolean authenticated = state.isAuthenticated();
        if (myLibraryItem != null) myLibraryItem.setEnabled(authenticated);
        if (physicalShopItem != null) physicalShopItem.setVisible(authenticated);
        
        boolean isAdmin = state.isAdmin();
        if (adminMenu != null) adminMenu.setVisible(isAdmin);
        if (adminDashboardItem != null) {
            adminDashboardItem.setVisible(isAdmin);
            adminDashboardItem.setEnabled(isAdmin);
        }
        if (userManagementItem != null) {
            userManagementItem.setVisible(isAdmin);
            userManagementItem.setEnabled(isAdmin);
        }
        if (addMenu != null) {
            addMenu.setVisible(isAdmin);
            addMenu.setEnabled(isAdmin);
        }
        if (uploadToShopItem != null) {
            uploadToShopItem.setVisible(isAdmin);
            uploadToShopItem.setEnabled(isAdmin);
        }
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(isAdmin);
        }
        
        if (loginItem != null) loginItem.setVisible(!authenticated);
        if (registerItem != null) registerItem.setVisible(!authenticated);
        if (logoutItem != null) logoutItem.setVisible(authenticated);
        if (currentUserItem != null) {
            currentUserItem.setVisible(authenticated);
            if (authenticated && state.getCurrentUser() != null) {
                currentUserItem.setText(MessageFormat.format(messages.getString("menu.current_account"), state.getCurrentUser().getEmail()));
            }
        }
        if (uploadItem != null) uploadItem.setVisible(authenticated);
        if (uploadAllItem != null) uploadAllItem.setEnabled(authenticated);
        if (uploadAllContextItem != null) uploadAllContextItem.setVisible(authenticated);

        if (pointsLabel != null) {
            pointsLabel.setVisible(authenticated);
            if (authenticated && state.getCurrentUser() != null) {
                pointsLabel.setText(MessageFormat.format(messages.getString("user.points"), state.getCurrentUser().getPoints()));
            }
        }
        if (addPointButton != null) addPointButton.setVisible(authenticated);

        if (libraryButton != null) libraryButton.setEnabled(authenticated && state.getMode() != ViewMode.LIBRARY);
        if (shopButton != null) shopButton.setEnabled(authenticated && state.getMode() != ViewMode.SHOP);
        if (physicalShopButton != null) {
            physicalShopButton.setVisible(authenticated);
            physicalShopButton.setEnabled(authenticated && state.getMode() != ViewMode.PHYSICAL_SHOP);
        }
    }

    public void refreshAuthState() {
        authController.syncState();
        updateAuthUI();
    }

    public boolean isUserAuthenticated() {
        return state.isAuthenticated();
    }

    /* ===================== INIT ===================== */

    private void initLookAndFeel() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", "BookLibrary");
            }
            String theme = prefs.get("theme", "light");
            if (theme.equals("dark")) {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
            }
        } catch (Exception ignored) {}
    }

    private void setupContextMenus() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem(messages.getString("button.preview"));
        openItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                openBook(book);
            }
        });
        JMenuItem showInFolderItem = new JMenuItem(messages.getString("menu.show_in_folder"));
        showInFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                showInFolder(book);
            }
        });
        JMenuItem removeItem = new JMenuItem(messages.getString("menu.remove_from_list"));
        removeItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                state.getBooks().remove(book);
                updateView();
            }
        });
        uploadItem = new JMenuItem(messages.getString("menu.upload_to_shop"));
        uploadItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                if (state.getMode() == ViewMode.LIBRARY) {
                    uploadBookToServer(book);
                } else {
                    buyBook(book);
                }
            }
        });
        uploadAllContextItem = new JMenuItem(messages.getString("menu.upload_all"));
        uploadAllContextItem.addActionListener(e -> uploadAllBooksToServer());

        uploadToShopItem = new JMenuItem(messages.getString("menu.upload_to_shop"));
        uploadToShopItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                uploadToShop(book);
            }
        });

        popupMenu.add(openItem);
        popupMenu.add(showInFolderItem);
        popupMenu.add(uploadItem);
        popupMenu.add(uploadToShopItem);
        popupMenu.add(uploadAllContextItem);
        popupMenu.addSeparator();
        popupMenu.add(removeItem);

        tree.setComponentPopupMenu(popupMenu);
    }

    private void setupHotkeys() {
        // Ctrl+F or Cmd+F for search
        int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuShortcutKeyMask), "search");
        searchField.getActionMap().put("search", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });

        // Delete key to remove book
        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        tree.getActionMap().put("remove", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node != null && node.getUserObject() instanceof Book book) {
                    state.getBooks().remove(book);
                    updateView();
                }
            }
        });
    }

    private void showInFolder(Book book) {
        try {
            File file = book.getFilePath().toFile();
            if (com.formdev.flatlaf.util.SystemInfo.isWindows) {
                Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
            } else if (com.formdev.flatlaf.util.SystemInfo.isMacOS) {
                Runtime.getRuntime().exec(new String[]{"open", "-R", file.getAbsolutePath()});
            } else {
                Desktop.getDesktop().open(file.getParentFile());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to show file in folder", e);
        }
    }

    private void initUI() {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Initializing UI components");
        setTitle(messages.getString("app.title"));
        UiUtils.setupWindow(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        root = new DefaultMutableTreeNode(messages.getString("tree.root"));
        treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);
        tree.setRowHeight(32);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new BookTreeCellRenderer());

        gridView = new BookGridView(book -> {
            detailsPanel.updateDetails(book);
            headerBookInfoButton.setText(book.getTitle() + " - " + book.getAuthor());
            headerBookInfoButton.setVisible(true);
        }, book -> openBook(book));

        centerCardLayout = new CardLayout();
        centerCardPanel = new JPanel(centerCardLayout);
        centerCardPanel.add(new JScrollPane(tree), "LIST");
        centerCardPanel.add(new JScrollPane(gridView), "GRID");

        detailsPanel = new BookDetailsPanel(messages);
        detailsPanel.setBuyAction(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                buyBook(book);
            }
        });
        detailsPanel.setPreviewAction(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                if (book.getDatabaseId() != null) {
                    String preview = controller.getPreview(book.getDatabaseId());
                    BookPreviewDialog dialog = new BookPreviewDialog(this, messages.getString("dialog.preview.title") + ": " + book.getTitle(), preview, messages);
                    dialog.setVisible(true);
                }

            }
        });

        add(detailsPanel, BorderLayout.EAST);

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                detailsPanel.updateDetails(book);
                detailsPanel.setPreviewButtonVisible(state.getMode() == ViewMode.SHOP || state.getMode() == ViewMode.PHYSICAL_SHOP || (book.getDatabaseId() != null));
                detailsPanel.setBuyButtonVisible(state.getMode() == ViewMode.SHOP || state.getMode() == ViewMode.PHYSICAL_SHOP);
                if (state.getMode() == ViewMode.PHYSICAL_SHOP) {
                    detailsPanel.setBuyButtonText("Замовити");
                } else {
                    detailsPanel.setBuyButtonText(messages.getString("button.buy"));
                }
                headerBookInfoButton.setText(book.getTitle() + " - " + book.getAuthor());
                headerBookInfoButton.setVisible(true);
            } else {
                headerBookInfoButton.setVisible(false);
            }
        });

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject() instanceof Book book) {
                        openBook(book);
                    }
                }
            }
        });

        statusLabel = new JLabel(messages.getString("status.drag.drop"));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new LineBorder(Color.BLUE, 1, true));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        organizeButton = new JButton(messages.getString("button.organize"));
        organizeButton.setEnabled(false);
        organizeButton.addActionListener(e -> startOrganizing());

        cancelButton = new JButton(messages.getString("button.cancel"));
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            if (currentWorker != null) currentWorker.cancel(true);
        });

        exitButton = new JButton(messages.getString("button.exit"));
        exitButton.addActionListener(e -> exitApplication());

        libraryButton = new JButton(messages.getString("button.library"));
        libraryButton.addActionListener(e -> switchToLibrary());
        libraryButton.setEnabled(false);

        shopButton = new JButton(messages.getString("button.shop"));
        shopButton.addActionListener(e -> switchToShop());

        physicalShopButton = new JButton(messages.getString("menu.physical_shop"));
        physicalShopButton.addActionListener(e -> switchToPhysicalShop());
        physicalShopButton.setVisible(false);

        adminPanelButton = new JButton(messages.getString("admin.button.dashboard"));
        adminPanelButton.addActionListener(e -> new org.example.infrastructure.ui.dialogs.AdminDashboardDialog(this, dashboardService, messages).setVisible(true));
        adminPanelButton.setVisible(false);

        pointsLabel = new JLabel();
        pointsLabel.setVisible(false);

        addPointButton = new JButton(messages.getString("button.add_point"));
        addPointButton.setVisible(false);
        addPointButton.addActionListener(e -> addTestPoint());

        headerBookInfoButton = new JButton("");
        headerBookInfoButton.setVisible(false);
        headerBookInfoButton.setToolTipText(messages.getString("button.copy_info"));
        headerBookInfoButton.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            Book book = null;
            if (node != null && node.getUserObject() instanceof Book b) {
                book = b;
            } else if (state.getUiViewMode() == LibraryViewState.UiViewMode.GRID) {
                // В сеточном режиме получаем из другого источника или просто используем тот, 
                // что был выбран последним (уже обновлен в headerBookInfoButton)
            }
            
            if (book != null) {
                detailsPanel.updateDetails(book);
                // Trigger copy logic from detailsPanel or duplicate here
                String info = String.format(
                        "%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                        messages.getString("details.title"), book.getTitle(),
                        messages.getString("details.author"), book.getAuthor(),
                        messages.getString("details.genre"), book.getGenre(),
                        messages.getString("details.year"), book.getYear(),
                        messages.getString("details.series"), book.getSeries(),
                        messages.getString("details.language"), book.getLanguage(),
                        messages.getString("details.path"), book.getFilePath()
                );
                copyToClipboard(info);
                JOptionPane.showMessageDialog(this, messages.getString("details.copy_info") + ": Success", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        listViewButton = new JToggleButton("≡"); // List icon
        gridViewButton = new JToggleButton("⊞"); // Grid icon
        listViewButton.setSelected(true);
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(listViewButton);
        viewGroup.add(gridViewButton);

        listViewButton.addActionListener(e -> {
            state.setUiViewMode(LibraryViewState.UiViewMode.LIST);
            centerCardLayout.show(centerCardPanel, "LIST");
            updateView();
        });
        gridViewButton.addActionListener(e -> {
            state.setUiViewMode(LibraryViewState.UiViewMode.GRID);
            centerCardLayout.show(centerCardPanel, "GRID");
            updateView();
        });

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(libraryButton);
        modePanel.add(shopButton);
        modePanel.add(physicalShopButton);
        modePanel.add(adminPanelButton);
        modePanel.add(new JSeparator(SwingConstants.VERTICAL));
        modePanel.add(listViewButton);
        modePanel.add(gridViewButton);
        modePanel.add(new JSeparator(SwingConstants.VERTICAL));
        modePanel.add(pointsLabel);
        modePanel.add(addPointButton);
        modePanel.add(headerBookInfoButton);

        JPanel top = new JPanel(new BorderLayout());
        top.add(modePanel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(exitButton);
        buttons.add(cancelButton);
        buttons.add(organizeButton);

        top.add(buttons, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", messages.getString("search.placeholder"));
        searchField.addActionListener(e -> filterBooks(searchField.getText()));

        searchButton = new JButton(messages.getString("button.search"));
        searchButton.addActionListener(e -> filterBooks(searchField.getText()));

        JButton clearButton = new JButton("X");
        clearButton.setToolTipText(messages.getString("button.clear"));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterBooks("");
        });

        String[] modes = {
                messages.getString("filter.genre"),
                messages.getString("filter.author"),
                messages.getString("filter.year")
        };
        groupModeCombo = new JComboBox<>(modes);
        groupModeCombo.addActionListener(e -> updateView());

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearButton, BorderLayout.WEST);
        searchBar.add(searchButton, BorderLayout.EAST);

        searchPanel.add(new JLabel(messages.getString("search.label")), BorderLayout.WEST);
        searchPanel.add(searchBar, BorderLayout.CENTER);
        searchPanel.add(groupModeCombo, BorderLayout.EAST);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(centerCardPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    /* ===================== MENU ===================== */

    private void initMenuBar() {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Initializing menu bar");
        JMenuBar bar = getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        } else {
            bar.removeAll();
        }

        // Библиотека
        libraryMenu = new JMenu(messages.getString("menu.library.title"));
        myLibraryItem = new JMenuItem(messages.getString("menu.my_library"));
        myLibraryItem.addActionListener(e -> {
            if (state.isAuthenticated()) {
                new LibraryDialog(this, state.getCurrentUser().getId(), storageService).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, messages.getString("menu.msg.auth_required"), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
            }
        });
        libraryMenu.add(myLibraryItem);

        uploadAllItem = new JMenuItem(messages.getString("menu.upload_all"));
        uploadAllItem.addActionListener(e -> uploadAllBooksToServer());
        libraryMenu.add(uploadAllItem);

        physicalShopItem = new JMenuItem(messages.getString("menu.physical_shop"));
        physicalShopItem.addActionListener(e -> new org.example.infrastructure.ui.dialogs.PhysicalShopDialog(this, orderService, authService).setVisible(true));
        libraryMenu.add(physicalShopItem);

        bar.add(libraryMenu);

        // Адмін
        adminMenu = new JMenu(messages.getString("menu.admin"));
        adminDashboardItem = new JMenuItem(messages.getString("admin.button.dashboard"));
        adminDashboardItem.addActionListener(e -> new org.example.infrastructure.ui.dialogs.AdminDashboardDialog(this, dashboardService, messages).setVisible(true));
        adminMenu.add(adminDashboardItem);

        userManagementItem = new JMenuItem(messages.getString("menu.users"));
        userManagementItem.addActionListener(e -> new org.example.infrastructure.ui.dialogs.UserManagementDialog(this, adminService, messages).setVisible(true));
        adminMenu.add(userManagementItem);

        addMenu = new JMenu(messages.getString("menu.add_book"));
        addEBookItem = new JMenuItem(messages.getString("menu.ebook"));
        addEBookItem.addActionListener(e -> new org.example.infrastructure.ui.dialogs.AddBookDialog(this, adminService, false, messages).setVisible(true));
        addPhysicalBookItem = new JMenuItem(messages.getString("menu.physical_book"));
        addPhysicalBookItem.addActionListener(e -> new org.example.infrastructure.ui.dialogs.AddBookDialog(this, adminService, true, messages).setVisible(true));
        addMenu.add(addEBookItem);
        addMenu.add(addPhysicalBookItem);
        adminMenu.add(addMenu);

        uploadToShopItem = new JMenuItem(messages.getString("menu.upload_to_shop"));
        uploadToShopItem.addActionListener(e -> uploadAllBooksToServer());
        adminMenu.add(uploadToShopItem);

        JMenuItem librarySettingsItem = new JMenuItem("Налаштування бібліотеки");
        librarySettingsItem.addActionListener(e -> new AdminSettingsDialog(this, libraryService).setVisible(true));
        adminMenu.add(librarySettingsItem);

        bar.add(adminMenu);

        // Аккаунт
        accountMenu = new JMenu(messages.getString("menu.account"));
        loginItem = new JMenuItem(messages.getString("menu.login"));
        loginItem.addActionListener(e -> {
            AuthDialog dialog = new AuthDialog(this, authService, messages);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                authController.syncState();
                updateAuthUI();
            }
        });
        registerItem = new JMenuItem(messages.getString("menu.register"));
        registerItem.addActionListener(e -> {
            // В AuthDialog уже есть вкладка регистрации
            AuthDialog dialog = new AuthDialog(this, authService, messages);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                authController.syncState();
                updateAuthUI();
            }
        });
        logoutItem = new JMenuItem(messages.getString("menu.logout"));
        logoutItem.addActionListener(e -> {
            authController.logout();
            updateAuthUI();
            JOptionPane.showMessageDialog(this, messages.getString("menu.msg.logout_success"));
        });
        currentUserItem = new JMenuItem("");
        currentUserItem.setEnabled(false);

        accountMenu.add(currentUserItem);
        accountMenu.add(loginItem);
        accountMenu.add(registerItem);
        accountMenu.add(logoutItem);
        bar.add(accountMenu);

        settingsMenu = new JMenu(messages.getString("menu.settings"));

        langMenu = new JMenu(messages.getString("menu.language"));
        langEnItem = new JMenuItem(messages.getString("lang.en"));
        langEnItem.addActionListener(e -> changeLanguage(Locale.ENGLISH));
        langRuItem = new JMenuItem(messages.getString("lang.ru"));
        langRuItem.addActionListener(e -> changeLanguage(new Locale("ru")));
        langUkItem = new JMenuItem(messages.getString("lang.uk"));
        langUkItem.addActionListener(e -> changeLanguage(new Locale("uk")));
        langMenu.add(langEnItem);
        langMenu.add(langRuItem);
        langMenu.add(langUkItem);

        themeMenu = new JMenu(messages.getString("menu.theme"));
        themeLightItem = new JMenuItem(messages.getString("theme.light"));
        themeLightItem.addActionListener(e -> changeTheme(false));
        themeDarkItem = new JMenuItem(messages.getString("theme.dark"));
        themeDarkItem.addActionListener(e -> changeTheme(true));

        themeMenu.add(themeLightItem);
        themeMenu.add(themeDarkItem);

        settingsMenu.add(langMenu);
        settingsMenu.add(themeMenu);
        bar.add(settingsMenu);

        toolsMenu = new JMenu(messages.getString("menu.tools"));
        statsItem = new JMenuItem(messages.getString("menu.stats"));
        statsItem.addActionListener(e -> showStatistics());
        dupsItem = new JMenuItem(messages.getString("menu.duplicates"));
        dupsItem.addActionListener(e -> findDuplicates());

        toolsMenu.add(statsItem);
        toolsMenu.add(dupsItem);

        bar.add(toolsMenu);
        if (getJMenuBar() != bar) {
            setJMenuBar(bar);
        }
        bar.revalidate();
        bar.repaint();
    }

    private void showStatistics() {
        if (state.getBooks().isEmpty()) return;

        long genresCount = state.getBooks().stream().map(Book::getGenre).distinct().count();
        long authorsCount = state.getBooks().stream().map(Book::getAuthor).distinct().count();
        Map<String, Long> formats = state.getBooks().stream()
                .collect(java.util.stream.Collectors.groupingBy(Book::getFormat, java.util.stream.Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(messages.getString("stats.total_books"), state.getBooks().size())).append("\n");
        sb.append(MessageFormat.format(messages.getString("stats.genres"), genresCount)).append("\n");
        sb.append(MessageFormat.format(messages.getString("stats.authors"), authorsCount)).append("\n\n");
        sb.append(messages.getString("stats.formats")).append("\n");
        formats.forEach((f, c) -> sb.append(f.isEmpty() ? "Unknown" : f).append(": ").append(c).append("\n"));

        JOptionPane.showMessageDialog(this, sb.toString(), messages.getString("stats.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void findDuplicates() {
        if (state.getBooks().isEmpty()) return;

        Map<String, List<Book>> map = state.getBooks().stream()
                .collect(java.util.stream.Collectors.groupingBy(b -> (b.getTitle() + "|" + b.getAuthor()).toLowerCase()));

        List<String> duplicates = map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getValue().get(0).getTitle() + " (" + e.getValue().get(0).getAuthor() + ") x" + e.getValue().size())
                .toList();

        if (duplicates.isEmpty()) {
            JOptionPane.showMessageDialog(this, messages.getString("duplicates.none"), messages.getString("duplicates.title"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String msg = MessageFormat.format(messages.getString("duplicates.found"), duplicates.size()) + "\n\n" + String.join("\n", duplicates);
            JOptionPane.showMessageDialog(this, msg, messages.getString("duplicates.title"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /* ===================== THEME / LANG ===================== */

    private void changeTheme(Boolean dark) {
        FlatAnimatedLafChange.showSnapshot();
        try {
            if (dark != null && dark) {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
                prefs.put("theme", "dark");
            } else {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
                prefs.put("theme", "light");
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }
    }

    private void initLocale(Locale locale) {
        this.currentLocale = locale;
        this.messages = ResourceBundle.getBundle("messages", locale, new ResourceBundle.Control() {
            @Override
            public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, "properties");
                try (java.io.InputStream stream = loader.getResourceAsStream(resourceName)) {
                    if (stream != null) {
                        return new PropertyResourceBundle(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8));
                    }
                }
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        });
    }

    private void changeLanguage(Locale locale) {
        initLocale(locale);
        prefs.put("language", locale.getLanguage());
        setTitle(messages.getString("app.title"));
        statusLabel.setText(messages.getString("status.drag.drop"));
        organizeButton.setText(messages.getString("button.organize"));
        cancelButton.setText(messages.getString("button.cancel"));
        exitButton.setText(messages.getString("button.exit"));
        searchButton.setText(messages.getString("button.search"));
        libraryButton.setText(messages.getString("button.library"));
        shopButton.setText(messages.getString("button.shop"));
        physicalShopButton.setText(messages.getString("menu.physical_shop"));
        addPointButton.setText(messages.getString("button.add_point"));
        if (adminPanelButton != null) {
            adminPanelButton.setText(messages.getString("admin.button.dashboard"));
        }
        headerBookInfoButton.setToolTipText(messages.getString("button.copy_info"));
        root.setUserObject(messages.getString("tree.root"));
        detailsPanel.setMessages(messages);
        if (searchField != null) {
            searchField.putClientProperty("JTextField.placeholderText", messages.getString("search.placeholder"));
        }
        initMenuBar(); // Re-initialize menu bar to update labels
        updateAuthUI(); // Ensure visibility of elements matches auth state after menu re-initialization

        // Update groupModeCombo items
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{
                messages.getString("filter.genre"),
                messages.getString("filter.author"),
                messages.getString("filter.year")
        });
        int selectedIndex = groupModeCombo.getSelectedIndex();
        groupModeCombo.setModel(model);
        if (selectedIndex >= 0 && selectedIndex < model.getSize()) {
            groupModeCombo.setSelectedIndex(selectedIndex);
        }

        treeModel.reload();
    }

    private void filterBooks(String query) {
        if (query == null || query.isBlank()) {
            updateView();
            return;
        }
        String q = query.toLowerCase();
        List<Book> filtered = state.getBooks().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q) ||
                        b.getAuthor().toLowerCase().contains(q) ||
                        b.getGenre().toLowerCase().contains(q))
                .toList();
        updateTree(filtered);
        updateGridView(filtered);
    }

    private void openBook(Book book) {
        if (state.getMode() == ViewMode.SHOP) {
            buyBook(book);
            return;
        }

        if (book.getFilePath() == null) {
            // If no file path, try to show preview (likely a shop book)
            if (book.getDatabaseId() != null) {
                String preview = controller.getPreview(book.getDatabaseId());
                BookPreviewDialog dialog = new BookPreviewDialog(this,
                    messages.getString("dialog.preview.title") + ": " + book.getTitle(),
                    preview, messages);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    messages.getString("error.no_file_path"),
                    messages.getString("error.title"),
                    JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(book.getFilePath().toFile());
            } else {
                copyToClipboard(book.getFilePath().toString());
                JOptionPane.showMessageDialog(this,
                    "Desktop is not supported. File path copied to clipboard.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    messages.getString("error.open_file") + "\n" + e.getMessage(),
                    messages.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    /* ===================== DRAG & DROP ===================== */

    private void setupDragAndDrop() {
        new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    if (state.getMode() == ViewMode.SHOP) {
                        e.rejectDrop();
                        JOptionPane.showMessageDialog(BookLibraryGui.this,
                                "Загрузка локальних файлів неможлива у режимі магазину.",
                                messages.getString("error.title"),
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (e.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        List<File> files = (List<File>) e.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        processFiles(files);
                    } else {
                        e.rejectDrop();
                        JOptionPane.showMessageDialog(BookLibraryGui.this,
                                messages.getString("error.unsupported_flavor"),
                                messages.getString("error.title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Drop failed", ex);
                    e.rejectDrop();
                }
            }
        });
    }

    /* ===================== SCAN FILES ===================== */

    private void processFiles(List<File> files) {
        state.getLocalBooks().clear();
        organizeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        startTime = System.currentTimeMillis();
        statusLabel.setText(messages.getString("status.preparing"));

        int approxTotal = estimateFileCount(files);
        progressBar.setIndeterminate(approxTotal == 0);
        progressBar.setMaximum(approxTotal > 0 ? approxTotal : 100);
        progressBar.setValue(0);
        progressBar.setVisible(true);

        controller.scanLibrary(files,
            books -> {
                updateView();
            },
            processed -> {
                int total = approxTotal;
                int remaining = Math.max(0, total - processed);
                long elapsed = System.currentTimeMillis() - startTime;

                if (!progressBar.isIndeterminate()) {
                    progressBar.setValue(processed);
                }

                statusLabel.setText(
                        MessageFormat.format(
                                messages.getString("status.processed"),
                                processed,
                                Math.max(processed, total),
                                remaining,
                                formatTime(elapsed)
                        )
                );
            },
            () -> {
                cancelButton.setEnabled(false);
                organizeButton.setEnabled(!state.getBooks().isEmpty());
                statusLabel.setText(
                        MessageFormat.format(messages.getString("status.found"), state.getBooks().size())
                );
                progressBar.setVisible(false);
            }
        );
    }

    private int estimateFileCount(List<File> files) {
        int count = 0;
        for (File f : files) {
            if (f.isFile() && BookFileUtils.isBookFile(f.toPath())) count++;
            else if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) count += estimateFileCount(Arrays.asList(children));
            }
        }
        return count;
    }

    private String formatTime(long millis) {
        long sec = millis / 1000;
        long min = sec / 60;
        sec %= 60;

        if (min > 0) {
            return MessageFormat.format(messages.getString("status.time_format"), min, sec);
        } else {
            return MessageFormat.format(messages.getString("status.time_format_sec"), sec);
        }
    }

    /* ===================== ORGANIZE ===================== */

    private void startOrganizing() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(messages.getString("chooser.title"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String lastDir = prefs.get("lastExportDir", null);
        Path defaultDir;
        if (lastDir != null) {
            defaultDir = Paths.get(lastDir);
        } else {
            defaultDir = Paths.get(System.getProperty("user.home"), "Desktop", "collection");
        }

        try { java.nio.file.Files.createDirectories(defaultDir); } catch (IOException ignored) {}

        chooser.setSelectedFile(defaultDir.toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path targetDir = chooser.getSelectedFile().toPath();
        prefs.put("lastExportDir", targetDir.toString());

        cancelButton.setEnabled(true);
        organizeButton.setEnabled(false);

        progressBar.setIndeterminate(false);
        progressBar.setMaximum(state.getBooks().size());
        progressBar.setValue(0);
        progressBar.setVisible(true);

        controller.organizeBooks(state.getBooks(), targetDir,
            processed -> {
                progressBar.setValue(processed);
                statusLabel.setText(
                        MessageFormat.format(
                                messages.getString("status.copying"),
                                processed,
                                state.getBooks().size()
                        )
                );
            },
            () -> {
                cancelButton.setEnabled(false);
                organizeButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText(
                        MessageFormat.format(messages.getString("status.done"), targetDir.toString())
                );
                JOptionPane.showMessageDialog(BookLibraryGui.this, messages.getString("dialog.finished"));
            }
        );
    }

    /* ===================== TREE ===================== */

    private void updateView() {
        updateTree(state.getBooks());
        updateGridView(state.getBooks());
    }

    private void updateGridView(List<Book> books) {
        if (gridView != null) {
            gridView.updateBooks(books);
        }
    }

    private void updateTree(List<Book> books) {
        root.removeAllChildren();
        if (books != null) {
            GroupBooksUseCase.GroupMode mode = switch (groupModeCombo.getSelectedIndex()) {
                case 1 -> GroupBooksUseCase.GroupMode.AUTHOR;
                case 2 -> GroupBooksUseCase.GroupMode.YEAR;
                default -> GroupBooksUseCase.GroupMode.GENRE;
            };

            Map<String, List<Book>> grouped = groupBooksUseCase.execute(books, mode);

            for (Map.Entry<String, List<Book>> entry : grouped.entrySet()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(entry.getKey());
                for (Book b : entry.getValue()) {
                    groupNode.add(new DefaultMutableTreeNode(b));
                }
                root.add(groupNode);
            }
        }
        treeModel.reload();
    }


    /* ===================== RENDERER ===================== */

    private class BookTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Map<Book, ImageIcon> coverCache = new WeakHashMap<>();

        // Панель-контейнер: Центр = Иконка+Текст, Восток = Бейдж формата
        private final JPanel panel = new JPanel(new BorderLayout(5, 0));
        // Наш новый красивый бейдж
        private final FormatBadge formatBadge = new FormatBadge();

        public BookTreeCellRenderer() {
            // Делаем панель прозрачной по умолчанию, чтобы видеть фон дерева
            panel.setOpaque(false);

            // 'this' - это сам DefaultTreeCellRenderer (иконка + название)
            // Кладем его в центр
            panel.add(this, BorderLayout.CENTER);

            // Кладем бейдж справа
            panel.add(formatBadge, BorderLayout.EAST);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean exp, boolean leaf, int row, boolean focus) {

            // 1. Настраиваем стандартную часть (текст, иконка, фокус)
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);

            // 2. Сбрасываем бейдж (скрываем, если это не книга)
            formatBadge.setVisible(false);

            // 3. Заполняем данными
            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();
                if (userObject instanceof Book book) {
                    setText(book.getTitle());

                    // --- Логика обложки (как была у тебя) ---
                    ImageIcon icon = null;
                    if (book.getCover() != null && book.getCover().length > 0) {
                        icon = coverCache.computeIfAbsent(book, b -> {
                            try {
                                ImageIcon original = new ImageIcon(b.getCover());
                                if (original.getIconWidth() > 0) {
                                    Image img = original.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                                    return new ImageIcon(img);
                                }
                            } catch (Exception ignored) {}
                            return null;
                        });
                    }
                    if (icon == null) {
                        icon = genreImageService.getDefaultBookIcon();
                    }
                    setIcon(icon);
                    // -----------------------------------------

                    // --- НОВАЯ ЛОГИКА: ФОРМАТ ---
                    String fmt = book.getFormat();
                    if (fmt != null && !fmt.isBlank()) {
                        formatBadge.setFormat(fmt);
                        formatBadge.setVisible(true);
                    }

                } else if (userObject instanceof String groupName) {
                    setText(groupName);
                    setIcon(genreImageService.getGenreIcon(groupName));
                }
            }

            // 4. Коррекция фона при выделении (Selection)
            // Чтобы вся полоска (включая пустое место до бейджа) подсвечивалась синим
            if (sel) {
                panel.setBackground(getBackgroundSelectionColor());
                panel.setOpaque(true);
            } else {
                panel.setBackground(getBackgroundNonSelectionColor());
                panel.setOpaque(false);
            }

            return panel;
        }
    }

    /**
     * Вспомогательный класс для отрисовки цветного тега (бейджа)
     */
    private static class FormatBadge extends JLabel {
        private static final int ARC = 10; // Радиус скругления углов
        private Color badgeColor = Color.GRAY;

        public FormatBadge() {
            setOpaque(false); // Фон рисуем сами в paintComponent
            setForeground(Color.WHITE); // Белый текст
            setHorizontalAlignment(CENTER);
            // Шрифт чуть меньше основного и жирный
            setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 10f));
            // Внутренние отступы: Сверху, Слева, Снизу, Справа
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        }

        public void setFormat(String format) {
            String text = format.toUpperCase();
            setText(text);
            this.badgeColor = getFormatColor(text);
        }

        private Color getFormatColor(String format) {
            // Подбираем цвет под формат
            return switch (format) {
                case "PDF" -> new Color(220, 53, 69);   // Красный
                case "EPUB" -> new Color(40, 167, 69);  // Зеленый
                case "FB2" -> new Color(0, 123, 255);   // Синий
                case "MOBI" -> new Color(253, 126, 20); // Оранжевый
                case "TXT" -> new Color(108, 117, 125); // Серый
                case "DJVU" -> new Color(23, 162, 184); // Бирюзовый
                default -> new Color(119, 119, 119);    // Нейтральный
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Включаем сглаживание, чтобы углы были мягкими
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем цветную подложку
            g2.setColor(badgeColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

            g2.dispose();

            // Рисуем текст поверх подложки
            super.paintComponent(g);
        }
    }

    /* ===================== EXIT ===================== */

    private void exitApplication() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        dispose();
        System.exit(0);
    }

    private void uploadBookToServer(Book book) {
        if (!state.isAuthenticated()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, авторизуйтесь для загрузки книг", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            controller.uploadBook(book);
            JOptionPane.showMessageDialog(this, "Книга успешно загружена на сервер!");
        } catch (IOException e) {
            LOGGER.error("Error uploading book", e);
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке книги: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uploadAllBooksToServer() {
        if (!state.isAuthenticated()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, авторизуйтесь для загрузки книг", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (state.getBooks().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Список книг пуст");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите загрузить все книги (" + state.getBooks().size() + ") на сервер?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(state.getBooks().size());
        uploadAllItem.setEnabled(false);
        uploadAllContextItem.setEnabled(false);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            private int successCount = 0;
            private int failCount = 0;
            private String lastError = "";

            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < state.getBooks().size(); i++) {
                    if (isCancelled()) break;
                    Book book = state.getBooks().get(i);
                    try {
                        controller.uploadBook(book);
                        successCount++;
                    } catch (Exception e) {
                        LOGGER.error("Error uploading book: {}", book.getTitle(), e);
                        failCount++;
                        lastError = e.getMessage();
                    }
                    publish(i + 1);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                progressBar.setValue(latest);
                statusLabel.setText("Загрузка книг: " + latest + " из " + state.getBooks().size());
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                uploadAllItem.setEnabled(true);
                uploadAllContextItem.setEnabled(true);
                statusLabel.setText("Загрузка завершена. Успешно: " + successCount + ", Ошибок: " + failCount);

                String message = "Загрузка завершена!\nУспешно: " + successCount;
                if (failCount > 0) {
                    message += "\nОшибок: " + failCount + "\nПоследняя ошибка: " + lastError;
                }
                JOptionPane.showMessageDialog(BookLibraryGui.this, message);
            }
        };
        worker.execute();
    }

    private void switchToLibrary() {
        controller.switchMode(ViewMode.LIBRARY);
        updateAuthUI();
        detailsPanel.setBuyButtonVisible(false);
        libraryButton.setEnabled(false);
        shopButton.setEnabled(true);
        physicalShopButton.setEnabled(true);
        headerBookInfoButton.setVisible(false);
        updateView();
    }

    private void switchToShop() {
        if (!state.isAuthenticated()) return;
        controller.switchMode(ViewMode.SHOP);
        updateAuthUI();
        detailsPanel.setBuyButtonVisible(true);
        detailsPanel.setBuyButtonText(messages.getString("button.buy"));
        libraryButton.setEnabled(true);
        shopButton.setEnabled(false);
        physicalShopButton.setEnabled(true);
        headerBookInfoButton.setVisible(false);
        loadShopBooks();
    }

    private void switchToPhysicalShop() {
        if (!state.isAuthenticated()) return;
        controller.switchMode(ViewMode.PHYSICAL_SHOP);
        updateAuthUI();
        detailsPanel.setBuyButtonVisible(true);
        detailsPanel.setBuyButtonText("Замовити");
        libraryButton.setEnabled(true);
        shopButton.setEnabled(true);
        physicalShopButton.setEnabled(false);
        headerBookInfoButton.setVisible(false);
        loadPhysicalShopBooks();
    }

    private void loadShopBooks() {
        controller.loadShopBooks(books -> {
            updateView();
        });
    }

    private void loadPhysicalShopBooks() {
        controller.loadPhysicalShopBooks(books -> {
            updateView();
        });
    }

    private void buyBook(Book book) {
        if (state.getMode() == ViewMode.PHYSICAL_SHOP) {
            placeOrder(book);
            return;
        }
        if (state.getCurrentUser().getPoints() <= 0) {
            JOptionPane.showMessageDialog(this, messages.getString("msg.not_enough_points"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        authController.updatePoints(state.getCurrentUser().getPoints() - 1);
        updateAuthUI();

        controller.buyBook(book, () -> {
            JOptionPane.showMessageDialog(BookLibraryGui.this, messages.getString("msg.buy_success"));
        });
    }

    private void placeOrder(Book book) {
        SeatSelectionDialog dialog = new SeatSelectionDialog(this, libraryService);
        dialog.setVisible(true);

        String seat = dialog.getSelectedSeat();
        if (seat != null) {
            System.out.println("[DEBUG] Placing order for seat: " + seat + " from " + dialog.getStartTime() + " to " + dialog.getEndTime());
            controller.placeOrder(book, seat, dialog.getStartTime(), dialog.getEndTime(), () -> {
                System.out.println("[DEBUG] Order placed successfully");
                JOptionPane.showMessageDialog(BookLibraryGui.this, "Замовлення оформлено! Місце: " + seat);
            });
        } else {
            System.out.println("[DEBUG] Seat selection cancelled or no seat selected");
        }
    }

    private void addTestPoint() {
        if (state.isAuthenticated()) {
            authController.updatePoints(state.getCurrentUser().getPoints() + 1);
            updateAuthUI();
        }
    }

    private void uploadToShop(Book book) {
        if (!state.isAdmin()) return;

        try {
            controller.uploadToShop(book);
            JOptionPane.showMessageDialog(this, "Книга успешно загружена в магазин!");
        } catch (IOException e) {
            LOGGER.error("Error uploading to shop", e);
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
