package cworks.treefs.server.core;

import org.vertx.java.core.Handler;

import java.util.Iterator;

// # AsyncIterator
public abstract class AsyncIterator<T> implements Handler<T> {

    private final Iterator<T> iterator;
    private boolean end = false;

    public AsyncIterator(Iterable<T> iterable) {
        iterator = iterable.iterator();
        next();
    }

    public AsyncIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        next();
    }

    public final boolean hasNext() {
        return !end;
    }

    public final void next() {
        if (iterator.hasNext()) {
            handle(iterator.next());
        } else {
            end = true;
            handle(null);
        }
    }

    public final void remove() {
        iterator.remove();
    }
}