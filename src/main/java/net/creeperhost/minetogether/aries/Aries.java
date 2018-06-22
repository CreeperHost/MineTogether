package net.creeperhost.minetogether.aries;

import com.google.gson.Gson;
import net.creeperhost.minetogether.common.WebUtils;

import java.util.HashMap;
import java.util.Map;

public class Aries {
    private final Map<String, String> credentials;

    public Aries(String key, String secret)
    {
        credentials = new HashMap<>();
        credentials.put("key", key);
        credentials.put("secret", secret);
    }

    public Map doApiCall(String daemon, String action, Map<String, String> extraData)
    {
        extraData.putAll(credentials);
        return new Gson().fromJson(WebUtils.postWebResponse("https://api.creeper.host/" + daemon + "/" + action, extraData), Map.class);
    }

    public Map doApiCall(String daemon, String action)
    {
        return doApiCall(daemon, action, new HashMap<>());
    }
}