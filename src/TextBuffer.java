public class TextBuffer {

        private TextNode head;
        private int size;

        public TextBuffer() {
            head = null;
            size = 0;
        }

        public void append(char c) {
            TextNode newNode = new TextNode(c);
            if (head == null) { head = newNode; }
            else {
                TextNode curr = head;
                while (curr.next != null) curr = curr.next;
                curr.next = newNode;
            }
            size++;
        }


        public void deleteLast() {
            if (head == null) return;
            if (head.next == null) { head = null; }
            else {
                TextNode curr = head;
                while (curr.next.next != null) curr = curr.next;
                curr.next = null;
            }
            size--;
        }

        // Convert linked list to String for display
        public String getText() {
            StringBuilder sb = new StringBuilder();
            TextNode curr = head;
            while (curr != null) {
                sb.append(curr.data);
                curr = curr.next;
            }
            return sb.toString();
        }

        public int getSize() { return size; }
    }

