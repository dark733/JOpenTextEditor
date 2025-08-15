import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class RetroTextEditor extends JFrame {

    private JTextArea textArea;
    private JTextArea lineNumbers;
    private JFileChooser fileChooser;
    private File currentFile;
    private UndoManager undoManager;

    public RetroTextEditor() {
        setTitle("Retro Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font retroFont = new Font("Monospaced", Font.PLAIN, 16);

        textArea = new JTextArea();
        textArea.setFont(retroFont);
        textArea.setBackground(new Color(0xF5E3A1));
        textArea.setForeground(new Color(0x8B4513));
        textArea.setCaretColor(new Color(0xFF4500));

        // Initialize undo manager
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Add line numbers
        lineNumbers = new JTextArea("1");
        lineNumbers.setFont(retroFont);
        lineNumbers.setBackground(new Color(0x8B4513));
        lineNumbers.setForeground(new Color(0xF5E3A1));
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);

        scrollPane.setRowHeaderView(lineNumbers);

        // Add scroll bar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Set up shortcuts
        setShortcuts();

        // Initialize the file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0x8B4513));
        setJMenuBar(menuBar);

        // Create the File menu
        JMenu fileMenu = new JMenu("File");
        customizeMenu(fileMenu);  // Customize the file menu
        menuBar.add(fileMenu);

        // Create menu items for File menu
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        JMenuItem openExistingMenuItem = new JMenuItem("Open Existing File");
        openExistingMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openExistingFile();
            }
        });

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFileAs();
            }
        });

        JMenuItem changeFontMenuItem = new JMenuItem("Change Font");
        changeFontMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFont();
            }
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add additional options to File menu
        JMenuItem additionalMenuItem1 = new JMenuItem("About");
        additionalMenuItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });

        JMenuItem additionalMenuItem2 = new JMenuItem("Version");
        additionalMenuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showVersionDialog();
            }
        });

        // Add menu items to File menu
        fileMenu.add(openMenuItem);
        fileMenu.add(openExistingMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(changeFontMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        fileMenu.addSeparator();  // Add a separator between standard and additional options
        fileMenu.add(additionalMenuItem1);
        fileMenu.add(additionalMenuItem2);

        // Add a document listener to update line numbers when the text changes
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateLineNumbers();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateLineNumbers();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateLineNumbers();
            }

            private void updateLineNumbers() {
                String text = textArea.getText();
                int lineCount = textArea.getLineCount();
                StringBuilder lineNumbersText = new StringBuilder();

                for (int i = 1; i <= lineCount; i++) {
                    lineNumbersText.append(i).append("\n");
                }

                lineNumbers.setText(lineNumbersText.toString());
            }
        });
    }

    private void customizeMenu(JMenu menu) {
        // Customize the menu's appearance
        menu.setFont(new Font("Monospaced", Font.PLAIN, 16));
        menu.setForeground(new Color(0xF5E3A1));
        menu.setBackground(new Color(0x8B4513));
    }

    private void setShortcuts() {
        // Set up shortcuts
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        textArea.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        textArea.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "Cut");
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "Copy");
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "Paste");

        textArea.getActionMap().put("Cut", new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.cut();
            }
        });

        textArea.getActionMap().put("Copy", new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.copy();
            }
        });

        textArea.getActionMap().put("Paste", new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.paste();
            }
        });
    }

    private void openExistingFile() {
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(currentFile));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                textArea.setText(content.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openFile() {
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(currentFile));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                textArea.setText(content.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
                writer.write(textArea.getText());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
        }
    }

    private void changeFont() {
        Font selectedFont = JFontChooser.showDialog(this, "Choose Font", textArea.getFont());
        if (selectedFont != null) {
            textArea.setFont(selectedFont);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Developed By-Juzer Baatwla");
    }

    private void showVersionDialog() {
        JOptionPane.showMessageDialog(this, "1.0");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RetroTextEditor().setVisible(true);
            }
        });
    }
}

class JFontChooser {
    public static Font showDialog(Component parentComponent, String title, Font initialFont) {
        return FontChooser.showDialog(parentComponent, title, initialFont);
    }

    private static class FontChooser extends JPanel {
        private static final long serialVersionUID = 1L;
        private Font font;

        private FontChooser(Font initialFont) {
            font = initialFont;

            JButton chooseButton = new JButton("Choose Font");
            chooseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    font = JFontChooser.showDialog(FontChooser.this, "Choose Font", font);
                    JOptionPane.getRootFrame().repaint();
                }
            });

            add(chooseButton);
        }

        public static Font showDialog(Component parentComponent, String title, Font initialFont) {
            FontChooser panel = new FontChooser(initialFont);
            int result = JOptionPane.showOptionDialog(parentComponent, panel, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new Object[]{}, null);
            return (result == JOptionPane.OK_OPTION) ? panel.font : null;
        }
    }
}
