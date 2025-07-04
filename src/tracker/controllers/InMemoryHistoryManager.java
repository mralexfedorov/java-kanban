package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, Node<Task>> history;
    private Node<Task> head = null;
    private Node<Task> tail = null;

    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        removeNode(history.get(id));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }

    private void removeNode(Node<Task> node) {
        history.remove(node.getData().getId());
        Node<Task> prevNode = node.getPrev();
        Node<Task> nextNode = node.getNext();
        if (prevNode != null) {
            prevNode.setNext(nextNode);
        } else {
            head = nextNode;
        }

        if (nextNode != null) {
            nextNode.setPrev(prevNode);
        } else {
            head = prevNode;
        }
    }

    private void linkLast(Task data) {
        Node<Task> prevNode = tail;
        Node<Task> newNode = new Node<>(data, null, prevNode);
        if (prevNode != null) {
            prevNode.setNext(newNode);
        }

        if (history.isEmpty()) {
            head = newNode;
        } else {
            Node<Task> foundNode = history.get(data.getId());
            if (foundNode != null) {
                removeNode(foundNode);
            }
        }
        tail = newNode;
        history.put(newNode.getData().getId(), newNode);
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node<Task> tmpNode = head;
        while (tmpNode != null) {
            tasks.add(tmpNode.getData());
            tmpNode = tmpNode.getNext();
        }
        return tasks;
    }
}

