import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {
    private Queue<String> queue;

    public TaskQueue() {
        queue = new LinkedList<>();
    }

    // Add a task (e.g. "auto-save", "spell-check")
    public void enqueue(String task) {
        queue.add(task);
        System.out.println("Task queued: " + task);
    }

    // Process the next task in line
    public void processNext() {
        if (queue.isEmpty()) {
            System.out.println("No pending tasks.");
            return;
        }
        String task = queue.poll();  // removes from front
        System.out.println("Processing: " + task);
        // In real app: dispatch to worker thread here
    }

    // Process all tasks in queue
    public void processAll() {
        while (!queue.isEmpty()) processNext();
    }

    public boolean hasTasks() { return !queue.isEmpty(); }
    public int taskCount() { return queue.size(); }
}