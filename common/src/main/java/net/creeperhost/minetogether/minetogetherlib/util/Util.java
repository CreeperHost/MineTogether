package net.creeperhost.minetogether.minetogetherlib.util;

public final class Util
{
    public static class CachedValue<T>
    {
        private long invalidTime;
        private ICacheCallback<T> callback;
        private T cachedValue;
        private long validTime;
        private final static Object lock = new Object();
        
        public CachedValue(int validTime, ICacheCallback<T> callback)
        {
            this.validTime = validTime;
            invalidTime = System.currentTimeMillis() + validTime;
            this.callback = callback;
        }
        
        public T get(Object... args)
        {
            if (System.currentTimeMillis() < invalidTime && !callback.needsRefresh(args) && cachedValue != null)
            {
                return cachedValue;
            }
            synchronized (lock)
            {
                T temp = callback.get(args);
                if (temp != null)
                    cachedValue = temp; // make sure only one request at any one time - shouldn't cause much of an issue, but I am working with threads soooo
            }
            invalidTime = validTime + System.currentTimeMillis();
            return cachedValue;
        }

        public void set(T newCachedValue)
        {
            invalidTime = validTime + System.currentTimeMillis();
            cachedValue = newCachedValue;
        }
        
        public T getCachedValue(Object... args)
        {
            return cachedValue;
        }
        
        public interface ICacheCallback<T>
        {
            T get(Object... args);
            
            boolean needsRefresh(Object... args);
        }
    }
}
