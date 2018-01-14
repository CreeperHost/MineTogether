package net.creeperhost.minetogether;

import net.creeperhost.minetogether.gui.hacky.IBufferProxy;
import net.creeperhost.minetogether.gui.hacky.IBufferProxyGetter;
import net.creeperhost.minetogether.gui.hacky.IServerListEntryWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeVersion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Util
{

    private static List<String> cookies;
    private static boolean logHide;
    private static Random random = new Random();
    private static ArrayList<String> oldVersions = new ArrayList<String>()
    {{
        add("1.9");
        add("1.9.4");
        add("1.10");
        add("1.10.2");
        add("1.11");
        add("1.11.2");
    }};
    private static IBufferProxyGetter proxyGetter;
    private static IServerListEntryWrapper wrapper;

    public static String localize(String key, Object... format)
    {
        return I18n.format((CreeperHost.instance.getImplementation() == null ? "creeperhost" : CreeperHost.instance.getImplementation().getLocalizationRoot()) + "." + key, format);
    }

    public static String getWebResponse(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
            // lul
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (cookies != null)
            {
                for (String cookie : cookies)
                {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.138 Safari/537.36 Vivaldi/1.8.770.56");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder respData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                respData.append(line);
            }

            List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

            if (setCookies != null)
            {
                cookies = setCookies;
            }

            rd.close();
            return respData.toString();
        }
        catch (Throwable t)
        {
            CreeperHost.logger.warn("An error occurred while fetching " + urlString, t);
        }

        return "error";

    }

    private static String mapToFormString(Map<String, String> map)
    {
        StringBuilder postDataStringBuilder = new StringBuilder();

        String postDataString;

        try
        {
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                postDataStringBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            postDataString = postDataStringBuilder.toString();
        }
        return postDataString;
    }

    public static String postWebResponse(String urlString, Map<String, String> postDataMap)
    {
        return postWebResponse(urlString, mapToFormString(postDataMap));
    }

    public static String methodWebResponse(String urlString, String postDataString, String method, boolean isJson, boolean silent)
    {
        try
        {
            postDataString.substring(0, postDataString.length() - 1);

            byte[] postData = postDataString.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.138 Safari/537.36 Vivaldi/1.8.770.56");
            conn.setRequestMethod(method);
            if (cookies != null)
            {
                for (String cookie : cookies)
                {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            conn.setRequestProperty("Content-Type", isJson ? "application/json" : "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setConnectTimeout(5000);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try
            {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(postData);
            }
            catch (Throwable t)
            {
                if (!silent)
                {
                    t.printStackTrace();
                }
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder respData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                respData.append(line);
            }

            List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

            if (setCookies != null)
            {
                cookies = setCookies;
            }

            rd.close();
            logHide = false;
            return respData.toString();
        }
        catch (Throwable t)
        {
            if (silent || logHide)
            {
                return "error";
            }
            logHide = true;
            CreeperHost.logger.warn("An error occurred while fetching " + urlString + ". Will hide repeated errors.", t);
        }

        return "error";
    }

    public static String postWebResponse(String urlString, String postDataString)
    {
        return methodWebResponse(urlString, postDataString, "POST", false, false);
    }

    public static String putWebResponse(String urlString, String body, boolean isJson, boolean isSilent)
    {
        return methodWebResponse(urlString, body, "PUT", isJson, isSilent);
    }

    public static String getDefaultName()
    {
        String[] nm1 = {"amber", "angel", "spirit", "basin", "lagoon", "basin", "arrow", "autumn", "bare", "bay", "beach", "bear", "bell", "black", "bleak", "blind", "bone", "boulder", "bridge", "brine", "brittle", "bronze", "castle", "cave", "chill", "clay", "clear", "cliff", "cloud", "cold", "crag", "crow", "crystal", "curse", "dark", "dawn", "dead", "deep", "deer", "demon", "dew", "dim", "dire", "dirt", "dog", "dragon", "dry", "dusk", "dust", "eagle", "earth", "east", "ebon", "edge", "elder", "ember", "ever", "fair", "fall", "false", "far", "fay", "fear", "flame", "flat", "frey", "frost", "ghost", "glimmer", "gloom", "gold", "grass", "gray", "green", "grim", "grime", "hazel", "heart", "high", "hollow", "honey", "hound", "ice", "iron", "kil", "knight", "lake", "last", "light", "lime", "little", "lost", "mad", "mage", "maple", "mid", "might", "mill", "mist", "moon", "moss", "mud", "mute", "myth", "never", "new", "night", "north", "oaken", "ocean", "old", "ox", "pearl", "pine", "pond", "pure", "quick", "rage", "raven", "red", "rime", "river", "rock", "rogue", "rose", "rust", "salt", "sand", "scorch", "shade", "shadow", "shimmer", "shroud", "silent", "silk", "silver", "sleek", "sleet", "sly", "small", "smooth", "snake", "snow", "south", "spring", "stag", "star", "steam", "steel", "steep", "still", "stone", "storm", "summer", "sun", "swamp", "swan", "swift", "thorn", "timber", "trade", "west", "whale", "whit", "white", "wild", "wilde", "wind", "winter", "wolf"};
        String[] nm2 = {"acre", "band", "barrow", "bay", "bell", "born", "borough", "bourne", "breach", "break", "brook", "burgh", "burn", "bury", "cairn", "call", "chill", "cliff", "coast", "crest", "cross", "dale", "denn", "drift", "fair", "fall", "falls", "fell", "field", "ford", "forest", "fort", "front", "frost", "garde", "gate", "glen", "grasp", "grave", "grove", "guard", "gulch", "gulf", "hall", "hallow", "ham", "hand", "harbor", "haven", "helm", "hill", "hold", "holde", "hollow", "horn", "host", "keep", "land", "light", "maw", "meadow", "mere", "mire", "mond", "moor", "more", "mount", "mouth", "pass", "peak", "point", "pond", "port", "post", "reach", "rest", "rock", "run", "scar", "shade", "shear", "shell", "shield", "shore", "shire", "side", "spell", "spire", "stall", "wich", "minster", "star", "storm", "strand", "summit", "tide", "town", "vale", "valley", "vault", "vein", "view", "ville", "wall", "wallow", "ward", "watch", "water", "well", "wharf", "wick", "wind", "wood", "yard"};

        int rnd = random.nextInt(nm1.length);
        int rnd2 = random.nextInt(nm2.length);
        while (nm1[rnd] == nm2[rnd2])
        {
            rnd2 = random.nextInt(nm2.length);
        }
        String name = nm1[rnd] + nm2[rnd2] + random.nextInt(999);
        return name;
    }

    // Stolen from ReflectionHelper as is deprecated and could be removed
    public static <E> Method findMethod(Class<? super E> clazz, String[] methodNames, Class<?>... methodTypes)
    {
        for (String methodName : methodNames)
        {
            try
            {
                Method m = clazz.getDeclaredMethod(methodName, methodTypes);
                m.setAccessible(true);
                return m;
            }
            catch (Throwable e)
            {
            }
        }
        return null;
    }

    public static IBufferProxy getBufferProxy()
    {
        if (proxyGetter == null)
        {
            String className = "net.creeperhost.minetogether.gui.hacky.BufferProxyGetterNew";
            String mcVersion;
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            }
            catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
            if (oldVersions.contains(mcVersion))
            {
                className = "net.creeperhost.minetogether.gui.hacky.BufferProxyGetterOld";
            }

            try
            {
                Class clazz = Class.forName(className);
                proxyGetter = (IBufferProxyGetter) clazz.newInstance();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

        return proxyGetter.get();
    }

    public static IServerListEntryWrapper getWrapper()
    {
        if (wrapper == null)
        {
            String className = "net.creeperhost.minetogether.gui.hacky.ServerListEntryWrapperNew";
            String mcVersion;
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            }
            catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
            if (oldVersions.contains(mcVersion))
            {
                className = "net.creeperhost.minetogether.gui.hacky.ServerListEntryWrapperOld";
            }

            try
            {
                Class clazz = Class.forName(className);
                wrapper = (IServerListEntryWrapper) clazz.newInstance();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

        return wrapper;
    }

    public static class CachedValue<T>
    {
        private long invalidTime;
        private ICacheCallback<T> callback;
        private T cachedValue;
        private long validTime;

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
            cachedValue = callback.get(args);
            invalidTime = validTime + System.currentTimeMillis();
            return cachedValue;
        }

        public interface ICacheCallback<T>
        {
            T get(Object... args);

            boolean needsRefresh(Object... args);
        }
    }
}
