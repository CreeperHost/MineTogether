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
                Profile profileFuture = findByNick(hash);
                profileFuture.loadProfile();
            }, MineTogether.profileExecutor);
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

    public void removeByHash(String hash, boolean ignoreFriend)
    {
        profiles.updateAndGet(profiles1 ->
        {
            Profile profileTarget = findByHash(hash);
            if(profileTarget == null) return profiles1;
            if(profileTarget.isBanned()) return profiles1;
            if(ignoreFriend) {
                if (profileTarget.isFriend()) return profiles1;
            }

            profiles1.remove(profileTarget);
            return profiles1;
        });
    }

    public void removeByNick(String nick, boolean ignoreFriend)
    {
        profiles.updateAndGet(profiles1 ->
        {
            Profile profileTarget = findByNick(nick);
            if(profileTarget == null) return profiles1;
            if(profileTarget.isBanned()) return profiles1;
            if(ignoreFriend) {
                if (profileTarget.isFriend()) return profiles1;
            }

            profiles1.remove(profileTarget);
            return profiles1;
        });
    }
}
