import java.util.Stack;
public class UndoRedoManager {

        private Stack<String> undoStack;  // past states
        private Stack<String> redoStack;  // future states (after undo)

        public UndoRedoManager() {
            undoStack = new Stack<>();
            redoStack = new Stack<>();
        }

        // Save current state before every edit
        public void saveState(String currentText) {
            undoStack.push(currentText);
            redoStack.clear();  // new edit clears redo history
        }

        // Undo: pop from undoStack, push current to redoStack
        public String undo(String currentText) {
            if (undoStack.isEmpty()) return currentText;
            redoStack.push(currentText);
            return undoStack.pop();
        }

        // Redo: pop from redoStack, push current to undoStack
        public String redo(String currentText) {
            if (redoStack.isEmpty()) return currentText;
            undoStack.push(currentText);
            return redoStack.pop();
        }

        public boolean canUndo() { return !undoStack.isEmpty(); }
        public boolean canRedo() { return !redoStack.isEmpty(); }
    }

