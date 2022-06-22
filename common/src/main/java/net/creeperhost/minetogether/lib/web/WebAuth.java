package net.creeperhost.minetogether.lib.web;

/**
 * Provides additional Authentication headers to a {@link ApiClient} environment.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public interface WebAuth {

    HeaderList getAuthHeaders();
}
