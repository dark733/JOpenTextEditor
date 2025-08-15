import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.undo.UndoManager;

public class RetroTextEditor extends JFrame {

    // Native macOS colors
    private static final Color NATIVE_BG = UIManager.getColor(
        "TextArea.background"
    );
    private static final Color NATIVE_FG = UIManager.getColor(
        "TextArea.foreground"
    );
    private static final Color NATIVE_SELECTION = UIManager.getColor(
        "TextArea.selectionBackground"
    );
    private static final Color LINE_NUMBER_BG = new Color(248, 248, 248);
    private static final Color LINE_NUMBER_FG = new Color(128, 128, 128);
    private static final Color STATUS_BG = new Color(242, 242, 242);
    private static final Color TOOLBAR_BG = new Color(245, 245, 245);

    // Components
    private JTextArea textArea;
    private JTextArea lineNumbers;
    private JScrollPane scrollPane;
    private JFileChooser fileChooser;
    private JLabel statusLabel;
    private JToolBar toolBar;

    // File management
    private File currentFile;
    private boolean isModified = false;
    private List<String> recentFiles;
    private static final int MAX_RECENT_FILES = 10;

    // Edit functionality
    private UndoManager undoManager;
    private FindReplaceDialog findReplaceDialog;
    private GoToLineDialog goToLineDialog;
    private boolean wordWrap = true;

    // Auto-save
    private javax.swing.Timer autoSaveTimer;
    private boolean autoSaveEnabled = true;

    public RetroTextEditor() {
        recentFiles = new ArrayList<>();
        loadRecentFiles();

        initializeComponents();
        setupUI();
        setupNativeMenus();
        setupToolbar();
        setupKeyBindings();
        setupAutoSave();

        setTitle("Text Editor - New Document");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Handle window closing
        addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    exitApplication();
                }
            }
        );

        setLocationRelativeTo(null);
        newDocument();
    }

    private void initializeComponents() {
        // Use system font
        Font systemFont = new Font(".SF NS Text", Font.PLAIN, 14);
        if (!systemFont.getFamily().equals(".SF NS Text")) {
            systemFont = new Font("San Francisco", Font.PLAIN, 14);
            if (!systemFont.getFamily().equals("San Francisco")) {
                systemFont = new Font("Helvetica Neue", Font.PLAIN, 14);
                if (!systemFont.getFamily().equals("Helvetica Neue")) {
                    systemFont = new Font("Lucida Grande", Font.PLAIN, 14);
                }
            }
        }

        Font monoFont = new Font("SF Mono", Font.PLAIN, 14);
        if (!monoFont.getFamily().equals("SF Mono")) {
            monoFont = new Font("Monaco", Font.PLAIN, 14);
            if (!monoFont.getFamily().equals("Monaco")) {
                monoFont = new Font("Menlo", Font.PLAIN, 14);
                if (!monoFont.getFamily().equals("Menlo")) {
                    monoFont = new Font("Courier New", Font.PLAIN, 14);
                }
            }
        }

        // Main text area
        textArea = new JTextArea();
        textArea.setFont(monoFont);
        textArea.setBackground(NATIVE_BG);
        textArea.setForeground(NATIVE_FG);
        textArea.setCaretColor(NATIVE_FG);
        textArea.setSelectionColor(NATIVE_SELECTION);
        textArea.setLineWrap(wordWrap);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Line numbers
        lineNumbers = new JTextArea("1");
        lineNumbers.setFont(new Font(monoFont.getName(), Font.PLAIN, 13));
        lineNumbers.setBackground(LINE_NUMBER_BG);
        lineNumbers.setForeground(LINE_NUMBER_FG);
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setBorder(new EmptyBorder(10, 10, 10, 5));

        // Scroll pane
        scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(NATIVE_BG);

        // Status bar
        statusLabel = new JLabel(
            " Ready | Line: 1, Col: 1 | Length: 0 | Words: 0"
        );
        statusLabel.setFont(new Font(systemFont.getName(), Font.PLAIN, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(STATUS_BG);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(new EmptyBorder(5, 15, 5, 15));

        // File chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
            new FileNameExtensionFilter("Text Files (*.txt)", "txt")
        );
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("Java Files (*.java)", "java")
        );
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("All Files", "*")
        );

        // Undo manager
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Add document change listener
        textArea
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        documentChanged();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        documentChanged();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        documentChanged();
                    }
                }
            );

        // Add caret listener for status updates
        textArea.addCaretListener(e -> updateStatusBar());

        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupNativeMenus() {
        // Create native macOS menu bar
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK)
        );
        newItem.addActionListener(e -> newDocument());

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK)
        );
        openItem.addActionListener(e -> openFile());

        JMenu recentMenu = new JMenu("Recent Files");
        updateRecentFilesMenu(recentMenu);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK)
        );
        saveItem.addActionListener(e -> saveFile());

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_S,
                InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            )
        );
        saveAsItem.addActionListener(e -> saveFileAs());

        JMenuItem printItem = new JMenuItem("Print...");
        printItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_DOWN_MASK)
        );
        printItem.addActionListener(e -> printDocument());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(recentMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(printItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK)
        );
        undoItem.addActionListener(e -> undo());

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            )
        );
        redoItem.addActionListener(e -> redo());

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_DOWN_MASK)
        );
        cutItem.addActionListener(e -> textArea.cut());

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK)
        );
        copyItem.addActionListener(e -> textArea.copy());

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK)
        );
        pasteItem.addActionListener(e -> textArea.paste());

        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.META_DOWN_MASK)
        );
        selectAllItem.addActionListener(e -> textArea.selectAll());

        JMenuItem findItem = new JMenuItem("Find & Replace...");
        findItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK)
        );
        findItem.addActionListener(e -> showFindReplace());

        JMenuItem goToLineItem = new JMenuItem("Go to Line...");
        goToLineItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_DOWN_MASK)
        );
        goToLineItem.addActionListener(e -> showGoToLine());

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(goToLineItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        JCheckBoxMenuItem wordWrapItem = new JCheckBoxMenuItem(
            "Word Wrap",
            wordWrap
        );
        wordWrapItem.addActionListener(e -> toggleWordWrap());

        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        zoomInItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.META_DOWN_MASK)
        );
        zoomInItem.addActionListener(e -> zoomIn());

        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        zoomOutItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_DOWN_MASK)
        );
        zoomOutItem.addActionListener(e -> zoomOut());

        JMenuItem resetZoomItem = new JMenuItem("Actual Size");
        resetZoomItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.META_DOWN_MASK)
        );
        resetZoomItem.addActionListener(e -> resetZoom());

        viewMenu.add(wordWrapItem);
        viewMenu.addSeparator();
        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetZoomItem);

        // Format Menu
        JMenu formatMenu = new JMenu("Format");
        formatMenu.setMnemonic('o');

        JMenuItem fontItem = new JMenuItem("Font...");
        fontItem.addActionListener(e -> showFontDialog());

        JMenuItem dateTimeItem = new JMenuItem("Insert Date and Time");
        dateTimeItem.setAccelerator(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_SEMICOLON,
                InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            )
        );
        dateTimeItem.addActionListener(e -> insertDateTime());

        formatMenu.add(fontItem);
        formatMenu.addSeparator();
        formatMenu.add(dateTimeItem);

        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');

        JMenuItem wordCountItem = new JMenuItem("Word Count");
        wordCountItem.addActionListener(e -> showWordCount());

        JCheckBoxMenuItem autoSaveItem = new JCheckBoxMenuItem(
            "Auto Save",
            autoSaveEnabled
        );
        autoSaveItem.addActionListener(e -> toggleAutoSave());

        toolsMenu.add(wordCountItem);
        toolsMenu.addSeparator();
        toolsMenu.add(autoSaveItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(formatMenu);
        menuBar.add(toolsMenu);

        setJMenuBar(menuBar);
    }

    private void setupToolbar() {
        toolBar = new JToolBar();
        toolBar.setBackground(TOOLBAR_BG);
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Use native-style buttons
        JButton newBtn = createNativeButton(
            "New",
            "New Document",
            this::newDocument
        );
        JButton openBtn = createNativeButton(
            "Open",
            "Open File",
            this::openFile
        );
        JButton saveBtn = createNativeButton(
            "Save",
            "Save File",
            this::saveFile
        );

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setMaximumSize(new Dimension(1, 24));

        JButton undoBtn = createNativeButton("Undo", "Undo", this::undo);
        JButton redoBtn = createNativeButton("Redo", "Redo", this::redo);

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setMaximumSize(new Dimension(1, 24));

        JButton findBtn = createNativeButton(
            "Find",
            "Find & Replace",
            this::showFindReplace
        );

        toolBar.add(newBtn);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(openBtn);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(saveBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(sep1);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(undoBtn);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(redoBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(sep2);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(findBtn);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton createNativeButton(
        String text,
        String tooltip,
        Runnable action
    ) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font(".SF NS Text", Font.PLAIN, 13));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(255, 255, 255, 0));
        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            )
        );
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());

        // Add hover effect
        button.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(240, 240, 240));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(255, 255, 255, 0));
                }
            }
        );

        return button;
    }

    private void setupKeyBindings() {
        // Use Cmd key for Mac
        bindKey("meta shift N", this::newDocument);
        bindKey("meta W", this::closeCurrentDocument);
        bindKey("F3", this::findNext);
        bindKey("shift F3", this::findPrevious);
        bindKey("meta EQUALS", this::zoomIn);
    }

    private void setupAutoSave() {
        autoSaveTimer = new javax.swing.Timer(30000, e -> {
            if (autoSaveEnabled && isModified && currentFile != null) {
                saveFile();
                updateStatusWithMessage("Auto-saved");
            }
        });
        autoSaveTimer.start();
    }

    private void bindKey(String keyStroke, Runnable action) {
        textArea
            .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(keyStroke), keyStroke);
        textArea
            .getActionMap()
            .put(
                keyStroke,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        action.run();
                    }
                }
            );
    }

    // Document management
    private void documentChanged() {
        if (!isModified) {
            isModified = true;
            updateTitle();
        }
        updateLineNumbers();
        updateStatusBar();
    }

    private void updateTitle() {
        String title = "Text Editor - ";
        if (currentFile != null) {
            title += currentFile.getName();
        } else {
            title += "New Document";
        }
        if (isModified) {
            title += " *";
        }
        setTitle(title);
    }

    private void updateLineNumbers() {
        String text = textArea.getText();
        int lineCount = text.isEmpty() ? 1 : text.split("\n", -1).length;

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            sb.append(i).append("\n");
        }

        lineNumbers.setText(sb.toString());
    }

    private void updateStatusBar() {
        try {
            int pos = textArea.getCaretPosition();
            int line = textArea.getLineOfOffset(pos) + 1;
            int col = pos - textArea.getLineStartOffset(line - 1) + 1;

            String text = textArea.getText();
            int length = text.length();
            int words = countWords(text);

            String status = String.format(
                " Line: %d, Col: %d | Length: %d | Words: %d",
                line,
                col,
                length,
                words
            );

            if (isModified) {
                status = " Modified |" + status;
            } else {
                status = " Ready |" + status;
            }

            statusLabel.setText(status);
        } catch (BadLocationException e) {
            statusLabel.setText(" Ready");
        }
    }

    private void updateStatusWithMessage(String message) {
        String originalText = statusLabel.getText();
        statusLabel.setText(originalText + " (" + message + ")");

        javax.swing.Timer timer = new javax.swing.Timer(3000, e ->
            updateStatusBar()
        );
        timer.setRepeats(false);
        timer.start();
    }

    private int countWords(String text) {
        if (text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    // File operations
    private void newDocument() {
        if (checkSaveChanges()) {
            textArea.setText("");
            currentFile = null;
            isModified = false;
            undoManager.discardAllEdits();
            updateTitle();
            updateStatusBar();
        }
    }

    private void openFile() {
        if (!checkSaveChanges()) return;

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadFile(file);
        }
    }

    private void loadFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            textArea.setText(content.toString());
            currentFile = file;
            isModified = false;
            undoManager.discardAllEdits();
            updateTitle();
            updateStatusBar();
            addToRecentFiles(file.getAbsolutePath());
        } catch (IOException e) {
            showError("Error opening file: " + e.getMessage());
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            saveToFile(currentFile);
        }
    }

    private void saveFileAs() {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "File already exists. Overwrite?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            saveToFile(file);
            currentFile = file;
            addToRecentFiles(file.getAbsolutePath());
        }
    }

    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
            isModified = false;
            updateTitle();
            updateStatusBar();
        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
        }
    }

    private boolean checkSaveChanges() {
        if (!isModified) return true;

        String fileName = currentFile != null
            ? currentFile.getName()
            : "Untitled";
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Save changes to " + fileName + "?",
            "Save Changes",
            JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            saveFile();
            return !isModified;
        } else if (choice == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    // Recent files management
    private void addToRecentFiles(String filePath) {
        recentFiles.remove(filePath);
        recentFiles.add(0, filePath);

        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles = recentFiles.subList(0, MAX_RECENT_FILES);
        }

        saveRecentFiles();
    }

    private void updateRecentFilesMenu(JMenu recentMenu) {
        recentMenu.removeAll();

        if (recentFiles.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("(Empty)");
            emptyItem.setEnabled(false);
            recentMenu.add(emptyItem);
        } else {
            for (String filePath : recentFiles) {
                File file = new File(filePath);
                JMenuItem item = new JMenuItem(file.getName());
                item.setToolTipText(filePath);
                item.addActionListener(e -> {
                    if (checkSaveChanges()) {
                        loadFile(file);
                    }
                });
                recentMenu.add(item);
            }

            recentMenu.addSeparator();
            JMenuItem clearItem = new JMenuItem("Clear Menu");
            clearItem.addActionListener(e -> {
                recentFiles.clear();
                saveRecentFiles();
                updateRecentFilesMenu(recentMenu);
            });
            recentMenu.add(clearItem);
        }
    }

    private void saveRecentFiles() {
        try {
            File configDir = new File(
                System.getProperty("user.home"),
                ".texteditor"
            );
            if (!configDir.exists()) configDir.mkdirs();

            File recentFile = new File(configDir, "recent.txt");
            try (PrintWriter writer = new PrintWriter(recentFile)) {
                for (String path : recentFiles) {
                    writer.println(path);
                }
            }
        } catch (IOException e) {
            // Ignore errors when saving recent files
        }
    }

    private void loadRecentFiles() {
        try {
            File configDir = new File(
                System.getProperty("user.home"),
                ".texteditor"
            );
            File recentFile = new File(configDir, "recent.txt");

            if (recentFile.exists()) {
                try (
                    BufferedReader reader = new BufferedReader(
                        new FileReader(recentFile)
                    )
                ) {
                    String line;
                    while (
                        (line = reader.readLine()) != null &&
                        recentFiles.size() < MAX_RECENT_FILES
                    ) {
                        if (new File(line).exists()) {
                            recentFiles.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Ignore errors when loading recent files
        }
    }

    // Edit operations
    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    // View operations
    private void toggleWordWrap() {
        wordWrap = !wordWrap;
        textArea.setLineWrap(wordWrap);
        textArea.setWrapStyleWord(wordWrap);
    }

    private void zoomIn() {
        Font currentFont = textArea.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() + 2f);
        textArea.setFont(newFont);
        lineNumbers.setFont(newFont.deriveFont(newFont.getSize() - 1f));
    }

    private void zoomOut() {
        Font currentFont = textArea.getFont();
        if (currentFont.getSize() > 8) {
            Font newFont = currentFont.deriveFont(currentFont.getSize() - 2f);
            textArea.setFont(newFont);
            lineNumbers.setFont(newFont.deriveFont(newFont.getSize() - 1f));
        }
    }

    private void resetZoom() {
        Font monoFont = new Font("SF Mono", Font.PLAIN, 14);
        if (!monoFont.getFamily().equals("SF Mono")) {
            monoFont = new Font("Monaco", Font.PLAIN, 14);
        }
        textArea.setFont(monoFont);
        lineNumbers.setFont(monoFont.deriveFont(13f));
    }

    // Dialog operations
    private void showFindReplace() {
        if (findReplaceDialog == null) {
            findReplaceDialog = new FindReplaceDialog(this);
        }
        findReplaceDialog.setVisible(true);
    }

    private void showGoToLine() {
        if (goToLineDialog == null) {
            goToLineDialog = new GoToLineDialog(this);
        }
        goToLineDialog.setVisible(true);
    }

    private void findNext() {
        if (findReplaceDialog != null) {
            findReplaceDialog.findNext();
        }
    }

    private void findPrevious() {
        if (findReplaceDialog != null) {
            findReplaceDialog.findPrevious();
        }
    }

    // Tool operations
    private void showWordCount() {
        String text = textArea.getText();
        int chars = text.length();
        int charsNoSpaces = text.replaceAll("\\s", "").length();
        int words = countWords(text);
        int lines = textArea.getLineCount();

        String message = String.format(
            "Characters: %d\n" +
            "Characters (no spaces): %d\n" +
            "Words: %d\n" +
            "Lines: %d",
            chars,
            charsNoSpaces,
            words,
            lines
        );

        JOptionPane.showMessageDialog(
            this,
            message,
            "Document Statistics",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showFontDialog() {
        Font currentFont = textArea.getFont();
        Font newFont = FontChooser.showDialog(this, "Choose Font", currentFont);
        if (newFont != null) {
            textArea.setFont(newFont);
            lineNumbers.setFont(newFont.deriveFont(newFont.getSize() - 1f));
        }
    }

    private void toggleAutoSave() {
        autoSaveEnabled = !autoSaveEnabled;
    }

    private void insertDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
            "MMMM d, yyyy 'at' h:mm:ss a"
        );
        String dateTime = sdf.format(new Date());
        textArea.insert(dateTime, textArea.getCaretPosition());
    }

    private void printDocument() {
        try {
            textArea.print();
        } catch (PrinterException e) {
            showError("Error printing document: " + e.getMessage());
        }
    }

    private void closeCurrentDocument() {
        newDocument();
    }

    // Utility methods
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void exitApplication() {
        if (checkSaveChanges()) {
            System.exit(0);
        }
    }

    // Getter methods for dialogs
    public JTextArea getTextArea() {
        return textArea;
    }

    public void highlightText(int start, int end) {
        textArea.select(start, end);
    }

    public void goToLine(int lineNumber) {
        try {
            int offset = textArea.getLineStartOffset(lineNumber - 1);
            textArea.setCaretPosition(offset);
            textArea.requestFocus();
        } catch (BadLocationException e) {
            showError("Invalid line number: " + lineNumber);
        }
    }

    public static void main(String[] args) {
        // Use macOS system menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty(
            "com.apple.mrj.application.apple.menu.about.name",
            "Text Editor"
        );

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                // Use default look and feel
            }
            new RetroTextEditor().setVisible(true);
        });
    }
}

// Find and Replace Dialog
class FindReplaceDialog extends JDialog {

    private JTextField findField;
    private JTextField replaceField;
    private JCheckBox caseSensitiveBox;
    private JCheckBox wholeWordBox;
    private RetroTextEditor parent;
    private int lastSearchPos = 0;

    public FindReplaceDialog(RetroTextEditor parent) {
        super(parent, "Find & Replace", false);
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIManager.getColor("Panel.background"));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Find field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Find:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        findField = new JTextField(25);
        mainPanel.add(findField, gbc);

        // Replace field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Replace:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        replaceField = new JTextField(25);
        mainPanel.add(replaceField, gbc);

        // Options
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBackground(UIManager.getColor("Panel.background"));

        caseSensitiveBox = new JCheckBox("Case sensitive");
        wholeWordBox = new JCheckBox("Whole word");
        optionsPanel.add(caseSensitiveBox);
        optionsPanel.add(wholeWordBox);
        mainPanel.add(optionsPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton findNextBtn = new JButton("Find Next");
        JButton findPrevBtn = new JButton("Find Previous");
        JButton replaceBtn = new JButton("Replace");
        JButton replaceAllBtn = new JButton("Replace All");
        JButton closeBtn = new JButton("Done");

        findNextBtn.addActionListener(e -> findNext());
        findPrevBtn.addActionListener(e -> findPrevious());
        replaceBtn.addActionListener(e -> replace());
        replaceAllBtn.addActionListener(e -> replaceAll());
        closeBtn.addActionListener(e -> setVisible(false));

        buttonPanel.add(findNextBtn);
        buttonPanel.add(findPrevBtn);
        buttonPanel.add(replaceBtn);
        buttonPanel.add(replaceAllBtn);
        buttonPanel.add(closeBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();

        // Enter key triggers find next
        findField.addActionListener(e -> findNext());
        replaceField.addActionListener(e -> findNext());
    }

    public void findNext() {
        String searchText = findField.getText();
        if (searchText.isEmpty()) return;

        String text = parent.getTextArea().getText();
        String searchIn = caseSensitiveBox.isSelected()
            ? text
            : text.toLowerCase();
        String searchFor = caseSensitiveBox.isSelected()
            ? searchText
            : searchText.toLowerCase();

        int pos = searchIn.indexOf(searchFor, lastSearchPos);
        if (pos == -1) {
            pos = searchIn.indexOf(searchFor, 0); // Wrap around
            if (pos == -1) {
                JOptionPane.showMessageDialog(
                    this,
                    "Text not found",
                    "Find",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }

        lastSearchPos = pos + searchFor.length();
        parent.highlightText(pos, pos + searchFor.length());
    }

    public void findPrevious() {
        String searchText = findField.getText();
        if (searchText.isEmpty()) return;

        String text = parent.getTextArea().getText();
        String searchIn = caseSensitiveBox.isSelected()
            ? text
            : text.toLowerCase();
        String searchFor = caseSensitiveBox.isSelected()
            ? searchText
            : searchText.toLowerCase();

        int currentPos = parent.getTextArea().getSelectionStart();
        int pos = searchIn.lastIndexOf(searchFor, currentPos - 1);
        if (pos == -1) {
            pos = searchIn.lastIndexOf(searchFor); // Wrap around
            if (pos == -1) {
                JOptionPane.showMessageDialog(
                    this,
                    "Text not found",
                    "Find",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }

        lastSearchPos = pos + searchFor.length();
        parent.highlightText(pos, pos + searchFor.length());
    }

    private void replace() {
        String searchText = findField.getText();
        String replaceText = replaceField.getText();
        if (searchText.isEmpty()) return;

        JTextArea textArea = parent.getTextArea();
        String selectedText = textArea.getSelectedText();

        if (selectedText != null && selectedText.equals(searchText)) {
            textArea.replaceSelection(replaceText);
        }
        findNext();
    }

    private void replaceAll() {
        String searchText = findField.getText();
        String replaceText = replaceField.getText();
        if (searchText.isEmpty()) return;

        JTextArea textArea = parent.getTextArea();
        String text = textArea.getText();

        int flags = caseSensitiveBox.isSelected()
            ? 0
            : Pattern.CASE_INSENSITIVE;
        if (wholeWordBox.isSelected()) {
            searchText = "\\b" + Pattern.quote(searchText) + "\\b";
        } else {
            searchText = Pattern.quote(searchText);
        }

        String newText = text.replaceAll(
            searchText,
            Matcher.quoteReplacement(replaceText)
        );
        textArea.setText(newText);

        int count =
            (text.length() - newText.length()) /
            (searchText.length() - replaceText.length());
        JOptionPane.showMessageDialog(
            this,
            "Replaced " + Math.abs(count) + " occurrences",
            "Replace All",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}

// Go To Line Dialog
class GoToLineDialog extends JDialog {

    private JTextField lineField;
    private RetroTextEditor parent;

    public GoToLineDialog(RetroTextEditor parent) {
        super(parent, "Go to Line", true);
        this.parent = parent;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new FlowLayout());
        mainPanel.setBackground(UIManager.getColor("Panel.background"));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        mainPanel.add(new JLabel("Line number:"));
        lineField = new JTextField(10);
        mainPanel.add(lineField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton goBtn = new JButton("Go");
        JButton cancelBtn = new JButton("Cancel");

        goBtn.addActionListener(e -> goToLine());
        cancelBtn.addActionListener(e -> setVisible(false));
        lineField.addActionListener(e -> goToLine());

        buttonPanel.add(goBtn);
        buttonPanel.add(cancelBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void goToLine() {
        try {
            int lineNumber = Integer.parseInt(lineField.getText());
            if (
                lineNumber > 0 &&
                lineNumber <= parent.getTextArea().getLineCount()
            ) {
                parent.goToLine(lineNumber);
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid line number",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid number",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

// Font Chooser Dialog
class FontChooser {

    public static Font showDialog(
        Component parent,
        String title,
        Font initialFont
    ) {
        FontChooserDialog dialog = new FontChooserDialog(
            (Frame) SwingUtilities.getWindowAncestor(parent),
            title,
            initialFont
        );
        dialog.setVisible(true);
        return dialog.getSelectedFont();
    }

    private static class FontChooserDialog extends JDialog {

        private Font selectedFont;
        private JList<String> fontList;
        private JList<Integer> sizeList;
        private JCheckBox boldBox, italicBox;
        private JTextArea previewArea;

        public FontChooserDialog(Frame parent, String title, Font initialFont) {
            super(parent, title, true);
            this.selectedFont = initialFont;
            initComponents();
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(UIManager.getColor("Panel.background"));
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Font selection panel
            JPanel fontPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            fontPanel.setBackground(UIManager.getColor("Panel.background"));

            // Font family
            JPanel familyPanel = new JPanel(new BorderLayout());
            familyPanel.setBackground(UIManager.getColor("Panel.background"));
            familyPanel.add(new JLabel("Font:"), BorderLayout.NORTH);

            String[] fonts =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            fontList = new JList<>(fonts);
            fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontList.setSelectedValue(selectedFont.getFontName(), true);
            fontList.addListSelectionListener(e -> updatePreview());

            JScrollPane fontScroll = new JScrollPane(fontList);
            fontScroll.setPreferredSize(new Dimension(200, 150));
            familyPanel.add(fontScroll, BorderLayout.CENTER);

            // Font size
            JPanel sizePanel = new JPanel(new BorderLayout());
            sizePanel.setBackground(UIManager.getColor("Panel.background"));
            sizePanel.add(new JLabel("Size:"), BorderLayout.NORTH);

            Integer[] sizes = {
                8,
                9,
                10,
                11,
                12,
                14,
                16,
                18,
                20,
                24,
                28,
                32,
                36,
                48,
                72,
            };
            sizeList = new JList<>(sizes);
            sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            sizeList.setSelectedValue(selectedFont.getSize(), true);
            sizeList.addListSelectionListener(e -> updatePreview());

            JScrollPane sizeScroll = new JScrollPane(sizeList);
            sizeScroll.setPreferredSize(new Dimension(80, 150));
            sizePanel.add(sizeScroll, BorderLayout.CENTER);

            // Font style
            JPanel stylePanel = new JPanel();
            stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.Y_AXIS));
            stylePanel.setBackground(UIManager.getColor("Panel.background"));
            stylePanel.add(new JLabel("Style:"));

            boldBox = new JCheckBox("Bold", selectedFont.isBold());
            italicBox = new JCheckBox("Italic", selectedFont.isItalic());
            boldBox.addActionListener(e -> updatePreview());
            italicBox.addActionListener(e -> updatePreview());

            stylePanel.add(boldBox);
            stylePanel.add(italicBox);

            fontPanel.add(familyPanel);
            fontPanel.add(sizePanel);
            fontPanel.add(stylePanel);

            // Preview
            JPanel previewPanel = new JPanel(new BorderLayout());
            previewPanel.setBackground(UIManager.getColor("Panel.background"));
            previewPanel.add(new JLabel("Preview:"), BorderLayout.NORTH);

            previewArea = new JTextArea(
                "The quick brown fox jumps over the lazy dog"
            );
            previewArea.setEditable(false);
            previewArea.setBackground(
                UIManager.getColor("TextArea.background")
            );
            previewArea.setForeground(
                UIManager.getColor("TextArea.foreground")
            );

            JScrollPane previewScroll = new JScrollPane(previewArea);
            previewScroll.setPreferredSize(new Dimension(400, 100));
            previewPanel.add(previewScroll, BorderLayout.CENTER);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(UIManager.getColor("Panel.background"));

            JButton okBtn = new JButton("OK");
            JButton cancelBtn = new JButton("Cancel");

            okBtn.addActionListener(e -> {
                updateSelectedFont();
                setVisible(false);
            });
            cancelBtn.addActionListener(e -> {
                selectedFont = null;
                setVisible(false);
            });

            buttonPanel.add(okBtn);
            buttonPanel.add(cancelBtn);

            mainPanel.add(fontPanel, BorderLayout.NORTH);
            mainPanel.add(previewPanel, BorderLayout.CENTER);

            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            updatePreview();
        }

        private void updatePreview() {
            updateSelectedFont();
            if (selectedFont != null) {
                previewArea.setFont(selectedFont);
            }
        }

        private void updateSelectedFont() {
            String fontName = fontList.getSelectedValue();
            Integer size = sizeList.getSelectedValue();

            if (fontName != null && size != null) {
                int style = Font.PLAIN;
                if (boldBox.isSelected()) style |= Font.BOLD;
                if (italicBox.isSelected()) style |= Font.ITALIC;

                selectedFont = new Font(fontName, style, size);
            }
        }

        public Font getSelectedFont() {
            return selectedFont;
        }
    }
}
