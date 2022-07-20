package net.creeperhost.minetogether.lib.util;

/**
 * A very simple system for attaching listeners which
 * are weakly linked allowing for automatic expiry.
 * <p>
 * Created by covers1624 on 11/7/22.
 */
public interface WeakNotifiable<T> {

    /**
     * Attach the given {@link WeakListener} to this {@link WeakNotifiable}.
     * <p>
     * It is VERY important that the Listener either perform static operations
     * or explicitly use its instance parameter. Pulling in local scope WILL
     * cause GC deadlocks.
     *
     * @param watch    The instance to watch, when this gets GC'd the
     *                 listener will be removed. This is also provided
     *                 as context to the {@link WeakListener}.
     * @param listener The listener.
     * @return A key to use when removing listeners.
     */
    <W> Object addListener(W watch, WeakListener<W, T> listener);

    /**
     * Remove the specified listener by key.
     * <p>
     * If no listener with the key exists, this method does nothing.
     *
     * @param key The key.
     */
    void removeListener(Object key);
}
