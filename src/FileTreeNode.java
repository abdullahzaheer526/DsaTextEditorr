import java.util.ArrayList;

public class FileTreeNode {
    String name;
    boolean isFolder;
    ArrayList<FileTreeNode> children;

    public FileTreeNode(String name, boolean isFolder) {
        this.name = name;
        this.isFolder = isFolder;
        this.children = new ArrayList<>();
    }

    // Add a child file or folder
    public void addChild(FileTreeNode child) {
        children.add(child);
    }

    // Print the tree (recursive DFS traversal)
    public void printTree(int level) {
        String indent = "  ".repeat(level);
        String icon = isFolder ? "[+]" : "[f]";
        System.out.println(indent + icon + " " + name);
        for (FileTreeNode child : children) {
            child.printTree(level + 1);  // recurse
        }
    }

    // Find a file by name (DFS search)
    public FileTreeNode find(String targetName) {
        if (this.name.equals(targetName)) return this;
        for (FileTreeNode child : children) {
            FileTreeNode result = child.find(targetName);
            if (result != null) return result;
        }
        return null;
    }
}