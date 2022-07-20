package net.creeperhost.minetogether.lib.util;

/**
 * A very simple system for attaching listeners which
 * are weakly linked allowing for automatic expiry.
 * <p>
 * Created by covers1624 on 8/7/22.
 *
 * @see #fire(Object, Object)
 */
public interface WeakListener<W, T> {

    /**
     * Called when the object this is attached to decides that an
     * update/event/whatever has happened.
     *
     * @param instance The watched instance. It is VERY important that all
     *                 operations performed inside the listener are either
     *                 static, or through this parameter. Pulling local scope
     *                 inside the listener lambda/anon class WILL cause a GC deadlock.
     * @param thing    The event.
     */
    void fire(W instance, T thing);
}
