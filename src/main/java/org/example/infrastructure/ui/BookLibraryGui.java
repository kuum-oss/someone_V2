package org.example.infrastructure.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.usecase.GroupBooksUseCase;
import org.example.core.usecase.OrganizeBooksUseCase;
import org.example.core.util.BookFileUtils;
import org.example.infrastructure.ui.components.BookDetailsPanel;
import org.example.core.service.AdminService;
import org.example.core.service.AuthService;
import org.example.core.service.FileStorageService;
import org.example.infrastructure.ui.dialogs.AuthDialog;
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
    private final ExtractMetadataUseCase extractMetadataUseCase;
    private final OrganizeBooksUseCase organizeBooksUseCase;
    private final GroupBooksUseCase groupBooksUseCase;
    private final AuthService authService;
    private final FileStorageService storageService;
    private final AdminService adminService;
    private final GenreImageService genreImageService = new GenreImageService();

    private final List<Book> currentBooks = new ArrayList<>();
    private final Preferences prefs = Preferences.userNodeForPackage(BookLibraryGui.class);

    private enum ViewMode { LIBRARY, SHOP }
    private ViewMode viewMode = ViewMode.LIBRARY;

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree tree;

    private JLabel statusLabel;
    private JLabel pointsLabel;
    private JProgressBar progressBar;
    private JButton organizeButton;
    private JButton cancelButton;
    private JButton exitButton;
    private JButton searchButton;
    private JButton headerBookInfoButton;
    private JButton shopButton;
    private JButton libraryButton;
    private JButton addPointButton;

    private BookDetailsPanel detailsPanel;
    private JTextField searchField;
    private JComboBox<String> groupModeCombo;

    private JMenuItem myLibraryItem;
    private JMenu accountMenu;
    private JMenuItem loginItem;
    private JMenuItem registerItem;
    private JMenuItem logoutItem;
    private JMenuItem currentUserItem;
    private JMenuItem uploadItem;
    private JMenuItem uploadAllItem;
    private JMenuItem uploadAllContextItem;
    private JMenuItem uploadToShopItem;

    private SwingWorker<?, ?> currentWorker;
    private ResourceBundle messages;
    private Locale currentLocale;

    // Для подсчета времени сканирования
    private long startTime;

    public BookLibraryGui(ExtractMetadataUseCase extractMetadataUseCase,
                          OrganizeBooksUseCase organizeBooksUseCase,
                          GroupBooksUseCase groupBooksUseCase,
                          AuthService authService,
                          FileStorageService storageService,
                          AdminService adminService) {
        this.extractMetadataUseCase = extractMetadataUseCase;
        this.organizeBooksUseCase = organizeBooksUseCase;
        this.groupBooksUseCase = groupBooksUseCase;
        this.authService = authService;
        this.storageService = storageService;
        this.adminService = adminService;

        initLocale(new Locale(prefs.get("language", "en")));
        initLookAndFeel();
        initUI();
        initMenuBar();
        setupDragAndDrop();
        setupContextMenus();
        setupHotkeys();
        updateAuthUI();
    }

    private void updateAuthUI() {
        boolean authenticated = authService.isAuthenticated();
        if (myLibraryItem != null) myLibraryItem.setEnabled(authenticated);
        if (loginItem != null) loginItem.setVisible(!authenticated);
        if (registerItem != null) registerItem.setVisible(!authenticated);
        if (logoutItem != null) logoutItem.setVisible(authenticated);
        if (currentUserItem != null) {
            currentUserItem.setVisible(authenticated);
            if (authenticated && authService.getCurrentUser() != null) {
                currentUserItem.setText("Аккаунт: " + authService.getCurrentUser().getEmail());
            }
        }
        if (uploadItem != null) uploadItem.setVisible(authenticated);
        if (uploadAllItem != null) uploadAllItem.setEnabled(authenticated);
        if (uploadAllContextItem != null) uploadAllContextItem.setVisible(authenticated);

        if (pointsLabel != null) {
            pointsLabel.setVisible(authenticated);
            if (authenticated && authService.getCurrentUser() != null) {
                pointsLabel.setText(MessageFormat.format(messages.getString("user.points"), authService.getCurrentUser().getPoints()));
            }
        }
        if (addPointButton != null) addPointButton.setVisible(authenticated);
        if (shopButton != null) shopButton.setEnabled(authenticated);
        if (libraryButton != null) libraryButton.setEnabled(authenticated && viewMode == ViewMode.SHOP);
        if (shopButton != null) shopButton.setEnabled(authenticated && viewMode == ViewMode.LIBRARY);
        
        if (uploadToShopItem != null) {
            uploadToShopItem.setVisible(authenticated && authService.getCurrentUser().isAdmin());
        }
    }

    /* ===================== INIT ===================== */

    private void initLookAndFeel() {
        try {
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
        JMenuItem openItem = new JMenuItem("Open"); // Localize later if needed
        openItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                openBook(book);
            }
        });
        JMenuItem showInFolderItem = new JMenuItem("Show in folder");
        showInFolderItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                showInFolder(book);
            }
        });
        JMenuItem removeItem = new JMenuItem("Remove from list");
        removeItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                currentBooks.remove(book);
                updateTree(currentBooks);
            }
        });
        uploadItem = new JMenuItem("Загрузить на сервер");
        uploadItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                uploadBookToServer(book);
            }
        });
        uploadAllContextItem = new JMenuItem("Загрузить всё на сервер");
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
                    currentBooks.remove(book);
                    updateTree(currentBooks);
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

        detailsPanel = new BookDetailsPanel(messages);
        detailsPanel.setBuyAction(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                buyBook(book);
            }
        });
        add(detailsPanel, BorderLayout.EAST);

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Book book) {
                detailsPanel.updateDetails(book);
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
            if (node != null && node.getUserObject() instanceof Book book) {
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

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(libraryButton);
        modePanel.add(shopButton);
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
        groupModeCombo.addActionListener(e -> updateTree(currentBooks));

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearButton, BorderLayout.WEST);
        searchBar.add(searchButton, BorderLayout.EAST);

        searchPanel.add(new JLabel(messages.getString("search.label") + " "), BorderLayout.WEST);
        searchPanel.add(searchBar, BorderLayout.CENTER);
        searchPanel.add(groupModeCombo, BorderLayout.EAST);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(tree), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    /* ===================== MENU ===================== */

    private void initMenuBar() {
        JMenuBar bar = new JMenuBar();

        // Библиотека
        JMenu libraryMenu = new JMenu("Библиотека");
        myLibraryItem = new JMenuItem("Моя библиотека");
        myLibraryItem.addActionListener(e -> {
            if (authService.isAuthenticated()) {
                new LibraryDialog(this, authService.getCurrentUser().getId(), storageService).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Пожалуйста, авторизуйтесь", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        libraryMenu.add(myLibraryItem);
        
        uploadAllItem = new JMenuItem("Загрузить все книги на сервер");
        uploadAllItem.addActionListener(e -> uploadAllBooksToServer());
        libraryMenu.add(uploadAllItem);

        bar.add(libraryMenu);

        // Аккаунт
        accountMenu = new JMenu("Аккаунт");
        loginItem = new JMenuItem("Войти");
        loginItem.addActionListener(e -> {
            AuthDialog dialog = new AuthDialog(this, authService);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                updateAuthUI();
            }
        });
        registerItem = new JMenuItem("Регистрация");
        registerItem.addActionListener(e -> {
            // В AuthDialog уже есть вкладка регистрации
            AuthDialog dialog = new AuthDialog(this, authService);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                updateAuthUI();
            }
        });
        logoutItem = new JMenuItem("Выйти");
        logoutItem.addActionListener(e -> {
            authService.logout();
            updateAuthUI();
            JOptionPane.showMessageDialog(this, "Вы вышли из аккаунта");
        });
        currentUserItem = new JMenuItem("");
        currentUserItem.setEnabled(false);

        accountMenu.add(currentUserItem);
        accountMenu.add(loginItem);
        accountMenu.add(registerItem);
        accountMenu.add(logoutItem);
        bar.add(accountMenu);

        JMenu settings = new JMenu(messages.getString("menu.settings"));

        JMenu lang = new JMenu(messages.getString("menu.language"));
        JMenuItem en = new JMenuItem(messages.getString("lang.en"));
        en.addActionListener(e -> changeLanguage(Locale.ENGLISH));
        JMenuItem ru = new JMenuItem(messages.getString("lang.ru"));
        ru.addActionListener(e -> changeLanguage(new Locale("ru")));
        JMenuItem uk = new JMenuItem(messages.getString("lang.uk"));
        uk.addActionListener(e -> changeLanguage(new Locale("uk")));
        lang.add(en);
        lang.add(ru);
        lang.add(uk);

        JMenu theme = new JMenu(messages.getString("menu.theme"));
        JMenuItem light = new JMenuItem(messages.getString("theme.light"));
        light.addActionListener(e -> changeTheme(false));
        JMenuItem dark = new JMenuItem(messages.getString("theme.dark"));
        dark.addActionListener(e -> changeTheme(true));

        theme.add(light);
        theme.add(dark);

        settings.add(lang);
        settings.add(theme);

        JMenu tools = new JMenu(messages.getString("menu.tools"));
        JMenuItem stats = new JMenuItem(messages.getString("menu.stats"));
        stats.addActionListener(e -> showStatistics());
        JMenuItem dups = new JMenuItem(messages.getString("menu.duplicates"));
        dups.addActionListener(e -> findDuplicates());
        
        tools.add(stats);
        tools.add(dups);

        bar.add(settings);
        bar.add(tools);
        setJMenuBar(bar);
    }

    private void showStatistics() {
        if (currentBooks.isEmpty()) return;

        long genresCount = currentBooks.stream().map(Book::getGenre).distinct().count();
        long authorsCount = currentBooks.stream().map(Book::getAuthor).distinct().count();
        Map<String, Long> formats = currentBooks.stream()
                .collect(java.util.stream.Collectors.groupingBy(Book::getFormat, java.util.stream.Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(messages.getString("stats.total_books"), currentBooks.size())).append("\n");
        sb.append(MessageFormat.format(messages.getString("stats.genres"), genresCount)).append("\n");
        sb.append(MessageFormat.format(messages.getString("stats.authors"), authorsCount)).append("\n\n");
        sb.append(messages.getString("stats.formats")).append("\n");
        formats.forEach((f, c) -> sb.append(f.isEmpty() ? "Unknown" : f).append(": ").append(c).append("\n"));

        JOptionPane.showMessageDialog(this, sb.toString(), messages.getString("stats.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void findDuplicates() {
        if (currentBooks.isEmpty()) return;

        Map<String, List<Book>> map = currentBooks.stream()
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
        headerBookInfoButton.setToolTipText(messages.getString("button.copy_info"));
        root.setUserObject(messages.getString("tree.root"));
        detailsPanel.setMessages(messages);
        if (searchField != null) {
            searchField.putClientProperty("JTextField.placeholderText", messages.getString("search.placeholder"));
        }
        initMenuBar(); // Re-initialize menu bar to update labels
        
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
            updateTree(currentBooks);
            return;
        }
        String q = query.toLowerCase();
        List<Book> filtered = currentBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q) ||
                        b.getAuthor().toLowerCase().contains(q) ||
                        b.getGenre().toLowerCase().contains(q))
                .toList();
        updateTree(filtered);
    }

    private void openBook(Book book) {
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
                    ex.printStackTrace();
                    e.rejectDrop();
                }
            }
        });
    }

    /* ===================== SCAN FILES ===================== */

    private void processFiles(List<File> files) {
        currentBooks.clear();
        organizeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        startTime = System.currentTimeMillis();
        statusLabel.setText(messages.getString("status.preparing"));
        
        int approxTotal = estimateFileCount(files);
        progressBar.setIndeterminate(approxTotal == 0);
        progressBar.setMaximum(approxTotal > 0 ? approxTotal : 100);
        progressBar.setValue(0);
        progressBar.setVisible(true);

        LibraryScanner scanner = new LibraryScanner(files, extractMetadataUseCase, 
            books -> {
                currentBooks.addAll(books);
                updateTree(currentBooks);
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
                organizeButton.setEnabled(!currentBooks.isEmpty());
                statusLabel.setText(
                        MessageFormat.format(messages.getString("status.found"), currentBooks.size())
                );
                progressBar.setVisible(false);
            }
        );
        scanner.setStartTime(startTime);
        currentWorker = scanner;
        scanner.execute();
    }

    private int estimateFileCount(List<File> files) {
        int count = 0;
        for (File f : files) {
            if (f.isFile() && BookFileUtils.isBookFile(f.getName())) count++;
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
        progressBar.setMaximum(currentBooks.size());
        progressBar.setValue(0);
        progressBar.setVisible(true);

        currentWorker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                int i = 0;
                for (Book book : currentBooks) {
                    if (isCancelled()) break;
                    try {
                        organizeBooksUseCase.execute(book, targetDir);
                        publish(++i);
                    } catch (IOException e) { e.printStackTrace(); }
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int processed = chunks.get(chunks.size() - 1);
                progressBar.setValue(processed);
                statusLabel.setText(
                        MessageFormat.format(
                                messages.getString("status.copying"),
                                processed,
                                currentBooks.size()
                        )
                );
            }

            @Override
            protected void done() {
                cancelButton.setEnabled(false);
                organizeButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText(
                        MessageFormat.format(messages.getString("status.done"), targetDir.toString())
                );
                JOptionPane.showMessageDialog(BookLibraryGui.this, messages.getString("dialog.finished"));
            }
        };

        currentWorker.execute();
    }

    /* ===================== TREE ===================== */

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

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean exp, boolean leaf, int row, boolean focus) {

            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);

            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();
                if (userObject instanceof Book book) {
                    setText(book.getTitle());
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
                } else if (userObject instanceof String groupName) {
                    setText(groupName);
                    setIcon(genreImageService.getGenreIcon(groupName));
                }
            }
            return this;
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
        if (!authService.isAuthenticated()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, авторизуйтесь для загрузки книг", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            storageService.uploadBook(authService.getCurrentUser().getId(), book, false);
            JOptionPane.showMessageDialog(this, "Книга успешно загружена на сервер!");
        } catch (IOException e) {
            LOGGER.error("Error uploading book", e);
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке книги: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uploadAllBooksToServer() {
        if (!authService.isAuthenticated()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, авторизуйтесь для загрузки книг", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentBooks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Список книг пуст");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите загрузить все книги (" + currentBooks.size() + ") на сервер?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(currentBooks.size());
        uploadAllItem.setEnabled(false);
        uploadAllContextItem.setEnabled(false);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            private int successCount = 0;
            private int failCount = 0;
            private String lastError = "";

            @Override
            protected Void doInBackground() throws Exception {
                Integer userId = authService.getCurrentUser().getId();
                for (int i = 0; i < currentBooks.size(); i++) {
                    if (isCancelled()) break;
                    Book book = currentBooks.get(i);
                    try {
                        storageService.uploadBook(userId, book, false);
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
                statusLabel.setText("Загрузка книг: " + latest + " из " + currentBooks.size());
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
        viewMode = ViewMode.LIBRARY;
        updateAuthUI();
        detailsPanel.setBuyButtonVisible(false);
        updateTree(currentBooks);
    }

    private void switchToShop() {
        if (!authService.isAuthenticated()) return;
        viewMode = ViewMode.SHOP;
        updateAuthUI();
        detailsPanel.setBuyButtonVisible(true);
        loadShopBooks();
    }

    private void loadShopBooks() {
        SwingWorker<List<Book>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Book> doInBackground() {
                return storageService.getPublicBooks().stream()
                        .map(sb -> Book.builder()
                                .title(sb.getTitle())
                                .author(sb.getAuthor())
                                .genre(sb.getGenre())
                                .year(sb.getYear())
                                .series(sb.getSeries())
                                .seriesIndex(sb.getSeriesIndex())
                                .language(sb.getLanguage())
                                .description(sb.getDescription())
                                .cover(sb.getCover())
                                .authorPhoto(sb.getAuthorPhoto())
                                .databaseId(sb.getId())
                                .isPublic(true)
                                .build())
                        .toList();
            }

            @Override
            protected void done() {
                try {
                    updateTree(get());
                } catch (Exception e) {
                    LOGGER.error("Error loading shop books", e);
                }
            }
        };
        worker.execute();
    }

    private void buyBook(Book book) {
        if (authService.getCurrentUser().getPoints() <= 0) {
            JOptionPane.showMessageDialog(this, messages.getString("msg.not_enough_points"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int newPoints = authService.getCurrentUser().getPoints() - 1;
        authService.updateCurrentUserPoints(newPoints);
        updateAuthUI();

        // Copy book to user's library in DB
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                org.example.core.entity.StoredBook sb = org.example.core.entity.StoredBook.builder()
                        .id(book.getDatabaseId())
                        .build();
                storageService.purchaseBook(authService.getCurrentUser().getId(), sb);
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(BookLibraryGui.this, messages.getString("msg.buy_success"));
            }
        };
        worker.execute();
    }

    private void addTestPoint() {
        if (authService.isAuthenticated()) {
            authService.updateCurrentUserPoints(authService.getCurrentUser().getPoints() + 1);
            updateAuthUI();
        }
    }

    private void uploadToShop(Book book) {
        if (!adminService.isAdmin()) return;
        
        try {
            adminService.uploadToShop(book);
            JOptionPane.showMessageDialog(this, "Книга успешно загружена в магазин!");
        } catch (IOException e) {
            LOGGER.error("Error uploading to shop", e);
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
