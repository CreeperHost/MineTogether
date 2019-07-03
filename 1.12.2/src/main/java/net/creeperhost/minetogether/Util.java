package net.creeperhost.minetogether;

import net.creeperhost.minetogether.gui.hacky.IBufferProxy;
import net.creeperhost.minetogether.gui.hacky.IBufferProxyGetter;
import net.creeperhost.minetogether.gui.hacky.IServerListEntryWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeVersion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

public final class Util
{
    private static Random random = new Random();
    @SuppressWarnings("Duplicates")
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
    private static String mcVersion;
    
    public static String localize(String key, Object... format)
    {
        return I18n.format((CreeperHost.instance.getImplementation() == null ? "creeperhost" : CreeperHost.instance.getImplementation().getLocalizationRoot()) + "." + key, format);
    }
    
    @SuppressWarnings("Duplicates")
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
    
    @SuppressWarnings("Duplicates")
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
            } catch (Throwable ignored) {}
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
            } catch (Throwable e)
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
            } catch (Throwable t) { t.printStackTrace(); }
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
            } catch (Throwable e)
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
            } catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        
        return wrapper;
    }
    
    public static String getMinecraftVersion()
    {
        if (mcVersion == null)
            try
            {
                /*
                We need to get this at runtime as Java is smart and interns final fields.
                Certainly not the dirtiest hack we do in this codebase.
                */
                mcVersion = (String) ForgeVersion.class.getField("mcVersion").get(null);
            } catch (Throwable e)
            {
                mcVersion = "unknown"; // will default to new method
            }
        return mcVersion;
    }
    
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
