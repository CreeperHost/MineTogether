package net.creeperhost.minetogether;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class KnownUsers
{
    private AtomicReference<List<Profile>> profiles = new AtomicReference<List<Profile>>();

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
//            profiles.get().add(profile);
//            profiles.getAndSet(profiles.get());
            profiles.updateAndGet(profiles1 ->
            {
                profiles1.add(profile);
               return profiles1;
            });
//            profile = findByHash(hash);
            CompletableFuture.runAsync(() -> {
                Profile profileFuture = findByNick(hash);
                profileFuture.loadProfile();
            }).thenRun(() -> {
//                profiles.get().add(profile);
            });
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
