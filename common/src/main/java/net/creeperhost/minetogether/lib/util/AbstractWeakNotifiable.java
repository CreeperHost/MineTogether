package net.creeperhost.minetogether.lib.util;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * A very simple weakly notifiable system.
 * <p>
 * Created by covers1624 on 11/7/22.
 */
public class AbstractWeakNotifiable<T> implements WeakNotifiable<T> {

    private final LinkedList<Entry> listeners = new LinkedList<>();

    @Override
    public <W> Object addListener(W watch, WeakListener<W, T> listener) {
        synchronized (listeners) {
            listeners.add(new Entry(new SoftReference<>(watch), listener));
            return listener;
        }
    }

    @Override
    public void removeListener(Object key) {
        synchronized (listeners) {
            listeners.removeIf(e -> e.listener == key);
        }
    }

    protected void fire(T thing) {
        synchronized (listeners) {
            Iterator<Entry> itr = listeners.iterator();
            while (itr.hasNext()) {
                Entry entry = itr.next();

                // It is important to keep these get's from SoftReference in the same scope
                // as firing the listener, there will be edge cases where the GC free's the object
                // between checking if its dead and firing the listener.
                Object watch = entry.watch.get();
                if (watch == null) {
                    itr.remove();
                    continue;
                }
                entry.listener.fire(unsafeCast(watch), thing);
            }
        }
    }

    private class Entry {

        public final SoftReference<Object> watch;
        public final WeakListener<?, T> listener;

        private Entry(SoftReference<Object> watch, WeakListener<?, T> listener) {
            this.watch = watch;
            this.listener = listener;
        }
    }
}
