package net.creeperhost.minetogether.lib.web;

import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 6/7/22.
 */
public class DynamicWebAuth implements WebAuth {

    private final HeaderList headers = new HeaderList();

    @Nullable
    private transient HeaderList cache;

    @Override
    public HeaderList getAuthHeaders() {
        synchronized (headers) {
            // Defensive copy of headers because threads exist.
            if (cache == null) {
                cache = new HeaderList();
                cache.addAll(headers);
            }
            return cache;
        }
    }

    public void setHeader(String name, String value) {
        synchronized (headers) {
            headers.set(name, value);
            cache = null;
        }
    }
}
