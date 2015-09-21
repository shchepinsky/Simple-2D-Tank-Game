package game.world.entities;

import java.util.*;

/**
 * Thread safe list of path returned by path finder.
 * @param <E>
 */
public class PathList<E> {
    private final ArrayList<E> path = new ArrayList<>();
    
    public synchronized int size() {
        return path.size();
    }

    public synchronized boolean isEmpty() {
        return path.isEmpty();
    }

    public synchronized boolean add(E e) {
        return path.add(e);
    }

    public synchronized boolean remove(Object o) {
        return path.remove(0) != null;
    }
    
    public synchronized void clear() {
        path.clear();
    }

    public synchronized E get(int index) {
        return path.get(index);
    }

    public synchronized E set(int index, E element) {
        return path.set(index, element);
    }

    public synchronized void add(int index, E element) {
        path.add(index ,element);
    }

    public synchronized E remove(int index) {
        return path.remove(index);
    }

    public String toString() {
        return path.toString();
    }
}
