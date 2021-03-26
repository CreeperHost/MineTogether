package net.creeperhost.minetogether.data;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class KnownUsers
{
    private AtomicReference<List<Profile>> profiles = new AtomicReference<List<Profile>>();

    public KnownUsers()
    {
        this.profiles.set(new ArrayList<Profile>());
    }

    public void clean()
    {
        List<String> remove = new ArrayList<>();
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        for(Profile profile : profilesCopy)
        {
            if(profile.getUserDisplay().isEmpty()) continue;
            if(profile.isFriend()) continue;
            if(profile.isBanned()) continue;
            LimitedSizeQueue<Message> tempMessages = ChatHandler.messages.get(ChatHandler.CHANNEL);
            boolean skip = false;
            for(Message message : tempMessages)
            {
                if(message.sender.equals("System")) continue;
                if(message.sender.equalsIgnoreCase(profile.getUserDisplay()) || message.sender.equalsIgnoreCase(profile.getMediumHash()) || message.sender.equalsIgnoreCase(profile.getShortHash()))
                {
                    skip = true;
                    break;
                }
            }
            if(!skip)
            {
                //Cleanup legacy code curseSync list...
                if(ChatHandler.curseSync.containsKey(profile.getMediumHash()))
                {
                    ChatHandler.curseSync.remove(profile.getMediumHash());
                } else if(ChatHandler.curseSync.containsKey(profile.getShortHash()))
                {
                    ChatHandler.curseSync.remove(profile.getShortHash());
                }
                if(profile.getLongHash().length() > 0) remove.add(profile.getLongHash());
            }
        }
        for(String hash : remove)
        {
            removeByHash(hash, false);
        }
    }

    public Profile add(String hash)
    {
        if(CreeperHost.profile.get().getLongHash().startsWith(hash.substring(2))) return null;

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
                try
                {
                    Thread.sleep(5000);
                } catch (InterruptedException e) { e.printStackTrace(); }
                profileFuture.loadProfile();
            }, CreeperHost.profileExecutor);
            return profile;
        }
        return null;
    }
    public boolean update(Profile updatedProfile)
    {
        Profile finalProfile = null;
        if(updatedProfile.getLongHash().length() > 0) {
            profiles.updateAndGet((curProfiles) -> {
                Profile existingProfile = findByHash(updatedProfile.getLongHash());
                if(existingProfile == null) return curProfiles;
                curProfiles.remove(existingProfile);
                curProfiles.add(updatedProfile);
                return curProfiles;
            });
            finalProfile = findByHash(updatedProfile.getLongHash());
        } else if(updatedProfile.getMediumHash().length() > 0)
        {
            profiles.updateAndGet((curProfiles) -> {
                Profile existingProfile = findByNick(updatedProfile.getMediumHash());
                if(existingProfile == null) return curProfiles;
                curProfiles.remove(existingProfile);
                curProfiles.add(updatedProfile);
                return curProfiles;
            });
            finalProfile = findByNick(updatedProfile.getMediumHash());
        }
        return (finalProfile != null && finalProfile == updatedProfile);
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

    public Profile findByHash(String search)
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        Profile returnProfile = null;
        for(Profile profile : profilesCopy) {
            if(profile == null || profile.getShortHash() == null || profile.getMediumHash() == null) continue;
            if (profile.getLongHash().equalsIgnoreCase(search))
            {
                returnProfile = profile;
                break;
            }
        }
        return returnProfile;
    }

    public Profile findByDisplay(String search)
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        Profile returnProfile = null;
        for(Profile profile : profilesCopy)
        {
            if(profile == null || profile.getShortHash() == null || profile.getMediumHash() == null) continue;
            if(profile.getUserDisplay().equalsIgnoreCase(search))
            {
                returnProfile = profile;
                break;
            }
        }
        return returnProfile;
    }

    public Profile findByNick(String search)
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        Profile returnProfile = null;
        for(Profile profile : profilesCopy)
        {
            if(profile == null || profile.getShortHash() == null || profile.getMediumHash() == null) continue;
            if(profile.getShortHash().equalsIgnoreCase(search) || profile.getMediumHash().equalsIgnoreCase(search))
            {
                returnProfile = profile;
                break;
            }
        }
        return returnProfile;
    }

    public List<String> getNames()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        return profilesCopy.stream().map(Profile::getUserDisplay).collect(Collectors.toList());
    }

    public List<String> getFriends()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        List<String> returnList = new ArrayList<>();
        for(Profile profile : profilesCopy)
        {
            if(profile.isFriend())
            {
                returnList.add(profile.getMediumHash());
            }
        }
        return returnList;
    }
}
