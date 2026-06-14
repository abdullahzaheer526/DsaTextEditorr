import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.awt.datatransfer.*;

public class TextEditorApp {

    private TextBuffer       buffer;
    private UndoRedoManager  undoRedo;
    private TaskQueue        taskQueue;
    private ClipboardManager clipboard;
    private FileTreeNode     fileTreeRoot;

    private JFrame    frame;
    private JTextArea textArea;
    private JLabel    statusBar;
    private JTree     fileTree;
    private String    currentFilePath = null;

    public TextEditorApp() {
        buffer    = new TextBuffer();
        undoRedo  = new UndoRedoManager();
        taskQueue = new TaskQueue();
        clipboard = new ClipboardManager();
        buildFileTree();
        buildUI();
    }

    private void buildFileTree() {
        fileTreeRoot = new FileTreeNode("Project", true);
        FileTreeNode src = new FileTreeNode("src", true);
        src.addChild(new FileTreeNode("TextEditorApp.java",   false));
        src.addChild(new FileTreeNode("TextBuffer.java",      false));
        src.addChild(new FileTreeNode("UndoRedoManager.java", false));
        src.addChild(new FileTreeNode("TaskQueue.java",       false));
        src.addChild(new FileTreeNode("ClipboardManager.java",false));
        src.addChild(new FileTreeNode("FileTreeNode.java",    false));
        src.addChild(new FileTreeNode("TextNode.java",        false));
        fileTreeRoot.addChild(src);
        fileTreeRoot.addChild(new FileTreeNode("README.md", false));
    }

    private DefaultMutableTreeNode toSwingNode(FileTreeNode node) {
        DefaultMutableTreeNode swingNode =
                new DefaultMutableTreeNode(node.name);
        for (FileTreeNode child : node.children)
            swingNode.add(toSwingNode(child));
        return swingNode;
    }

    private void buildUI() {
        frame = new JFrame("DSA Text Editor");
        frame.setSize(900, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(8, 10, 8, 10));

        textArea.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                TextEditorApp self = TextEditorApp.this;
                self.undoRedo.saveState(self.buffer.getText());
                self.buffer = new TextBuffer();
                for (char c : self.textArea.getText().toCharArray())
                    self.buffer.append(c);
                self.taskQueue.enqueue("auto-save");
                self.updateStatusBar();
            }
        });

        fileTree = new JTree(toSwingNode(fileTreeRoot));
        fileTree.setPreferredSize(new Dimension(180, 0));
        fileTree.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setBorder(BorderFactory.createMatteBorder(
                0,0,0,1, Color.LIGHT_GRAY));

        statusBar = new JLabel("  Words: 0  |  Characters: 0");
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4,10,4,10)));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScroll,
                new JScrollPane(textArea));
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(1);

        frame.setLayout(new BorderLayout());
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);
        frame.setJMenuBar(buildMenuBar());
        frame.setVisible(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildFormatMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    private JMenu buildFileMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Start a new file? Unsaved changes will be lost.",
                    "New File", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                textArea.setText("");
                buffer = new TextBuffer();
                currentFilePath = null;
                frame.setTitle("DSA Text Editor");
                updateStatusBar();
            }
        });

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openItem.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    String content = new String(
                            Files.readAllBytes(fc.getSelectedFile().toPath()));
                    textArea.setText(content);
                    currentFilePath = fc.getSelectedFile().getAbsolutePath();
                    frame.setTitle("DSA Text Editor — " +
                            fc.getSelectedFile().getName());
                    updateStatusBar();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Could not open file: " + ex.getMessage());
                }
            }
        });

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> saveFile(false));

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(
                KeyStroke.getKeyStroke("control shift S"));
        saveAsItem.addActionListener(e -> saveFile(true));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        menu.add(newItem);  menu.add(openItem);
        menu.addSeparator();
        menu.add(saveItem); menu.add(saveAsItem);
        menu.addSeparator();
        menu.add(exitItem);
        return menu;
    }

    private JMenu buildEditMenu() {
        JMenu menu = new JMenu("Edit");

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        undoItem.addActionListener(e ->
                textArea.setText(undoRedo.undo(textArea.getText())));

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        redoItem.addActionListener(e ->
                textArea.setText(undoRedo.redo(textArea.getText())));

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke("control X"));
        cutItem.addActionListener(e -> {
            String selected = textArea.getSelectedText();
            if (selected == null) return;

            clipboard.copy(selected);

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(selected), null);

            textArea.replaceSelection("");
        });

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        copyItem.addActionListener(e -> {
            String selected = textArea.getSelectedText();
            if (selected == null) return;


            clipboard.copy(selected);

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(selected), null);
        });

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke("control V"));
        pasteItem.addActionListener(e -> {
            try {

                String text = (String) Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .getData(DataFlavor.stringFlavor);

                if (text != null) {
                    clipboard.copy(text);
                    textArea.insert(text, textArea.getCaretPosition());
                }
            } catch (Exception ex) {

            }
        });

        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke("control A"));
        selectAllItem.addActionListener(e -> textArea.selectAll());

        JMenuItem findItem = new JMenuItem("Find...");
        findItem.setAccelerator(KeyStroke.getKeyStroke("control F"));
        findItem.addActionListener(e -> {
            String word = JOptionPane.showInputDialog(frame, "Find:");
            if (word == null || word.isEmpty()) return;
            String text = textArea.getText();
            int index   = text.indexOf(word);
            if (index >= 0) {
                textArea.setCaretPosition(index);
                textArea.select(index, index + word.length());
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Not found: " + word);
            }
        });

        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.addActionListener(e -> {
            undoRedo.saveState(textArea.getText());
            textArea.setText("");
            buffer = new TextBuffer();
            updateStatusBar();
        });

        menu.add(undoItem);     menu.add(redoItem);
        menu.addSeparator();
        menu.add(cutItem);      menu.add(copyItem);
        menu.add(pasteItem);
        menu.addSeparator();
        menu.add(selectAllItem); menu.add(findItem);
        menu.addSeparator();
        menu.add(clearItem);
        return menu;
    }

    private JMenu buildFormatMenu() {
        JMenu menu = new JMenu("Format");

        JMenuItem fontSizeItem = new JMenuItem("Font Size...");
        fontSizeItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame,
                    "Enter font size:",
                    String.valueOf(textArea.getFont().getSize()));
            if (input == null) return;
            try {
                int size = Integer.parseInt(input.trim());
                textArea.setFont(
                        textArea.getFont().deriveFont((float) size));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter a valid number.");
            }
        });

        JCheckBoxMenuItem wrapItem =
                new JCheckBoxMenuItem("Word Wrap", true);
        wrapItem.addActionListener(e -> {
            textArea.setLineWrap(wrapItem.isSelected());
            textArea.setWrapStyleWord(wrapItem.isSelected());
        });

        menu.add(fontSizeItem);
        menu.add(wrapItem);
        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "DSA Text Editor\n"
                                + "Built with: Linked List, Stack,\n"
                                + "Queue, ArrayList, Tree\n"
                                + "4th Semester DSA Project",
                        "About", JOptionPane.INFORMATION_MESSAGE));
        menu.add(aboutItem);
        return menu;
    }

    private void saveFile(boolean saveAs) {
        if (saveAs || currentFilePath == null) {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
                return;
            currentFilePath = fc.getSelectedFile().getAbsolutePath();
            frame.setTitle("DSA Text Editor — " +
                    fc.getSelectedFile().getName());
        }
        try {
            Files.write(Paths.get(currentFilePath),
                    textArea.getText().getBytes());
            taskQueue.enqueue("file-saved: " + currentFilePath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save: " + ex.getMessage());
        }
    }

    private void updateStatusBar() {
        String text = textArea.getText().trim();
        int chars   = textArea.getText().length();
        int words   = text.isEmpty() ? 0 : text.split("\\s+").length;
        statusBar.setText("  Words: " + words +
                "  |  Characters: " + chars);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditorApp::new);
    }

}

