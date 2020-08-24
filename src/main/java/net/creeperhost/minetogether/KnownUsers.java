package net.creeperhost.minetogether;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class KnownUsers
{
    private AtomicReference<List<Profile>> profiles = new AtomicReference<List<Profile>>();
    private static Executor profileExecutor = new ThreadPoolExecutor(100, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    Logger logger = LogManager.getLogger(KnownUsers.class.getName());

    public KnownUsers()
    {
        this.profiles.set(new ArrayList<Profile>());
    }

    public Profile add(String hash)
    {
        Profile profile = new Profile(hash);
        if(findByNick(hash) == null)
        {
            profiles.updateAndGet(profiles1 ->
            {
                profiles1.add(profile);
                return profiles1;
            });
            CompletableFuture.runAsync(() -> {
                logger.error("Loading profile for "+hash+"...");
                Profile profileFuture = findByNick(hash);
                profileFuture.loadProfile();
                logger.error("Loaded profile for "+hash+"...");
            }, profileExecutor);
            return profile;
        }
        return null;
    }

    public Profile findByHash(String search)
    {
        for(Profile profile : profiles.get())
        {
            if(profile.getLongHash().equalsIgnoreCase(search))
                return profile;
        }
        return null;
    }

    public Profile findByDisplay(String search)
    {
        for(Profile profile : profiles.get())
        {
            if(profile.getUserDisplay().equalsIgnoreCase(search))
                return profile;
        }
        return null;
    }

    public Profile findByNick(String search)
    {
        for(Profile profile : profiles.get())
        {
            if(profile.getShortHash().equalsIgnoreCase(search) || profile.getMediumHash().equalsIgnoreCase(search))
                return profile;
        }
        return null;
    }
}
