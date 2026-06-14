import java.util.ArrayList;

public class ClipboardManager {
    private ArrayList<String> history;
    private static final int MAX_HISTORY = 10;

    public ClipboardManager() {
        history = new ArrayList<>();
    }

    // Copy selected text into clipboard history
    public void copy(String selectedText) {
        if (selectedText == null || selectedText.isEmpty()) return;
        history.add(0, selectedText);  // newest first
        if (history.size() > MAX_HISTORY)
            history.remove(history.size() - 1);  // trim old
    }

    // Paste most recent copy
    public String paste() {
        return history.isEmpty() ? "" : history.get(0);
    }

    // Paste from a specific history index
    public String pasteFrom(int index) {
        if (index < 0 || index >= history.size()) return "";
        return history.get(index);
    }

    // Get all clipboard entries
    public ArrayList<String> getHistory() {
        return history;
    }

    public void clearHistory() { history.clear(); }
    public int historySize() { return history.size(); }
}