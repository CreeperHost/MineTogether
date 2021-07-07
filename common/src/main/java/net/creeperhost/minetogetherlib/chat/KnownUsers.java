package net.creeperhost.minetogetherlib.chat;

import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class KnownUsers
{
    private static AtomicReference<List<Profile>> profiles = new AtomicReference<List<Profile>>();

    static
    {
        profiles.set(new ArrayList<Profile>());
    }

    public static void clean()
    {
        List<String> remove = new ArrayList<>();
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        for(Profile profile : profilesCopy)
        {
            if(profile.getUserDisplay().isEmpty()) continue;
            if(profile.isFriend()) continue;
            if(profile.isMuted()) continue;
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
                if(profile.getLongHash().length() > 0) remove.add(profile.getLongHash());
            }
        }
        for(String hash : remove)
        {
            removeByHash(hash, false);
        }
    }

    public static Profile add(String hash)
    {
        if(ChatCallbacks.getPlayerHash(MineTogetherChat.INSTANCE.uuid).startsWith(hash.substring(2))) return null;

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
            }, MineTogetherChat.profileExecutor);
            return profile;
        }
        return null;
    }
    public static boolean update(Profile updatedProfile)
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

    public static void removeByHash(String hash, boolean ignoreFriend)
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
    public static void removeByNick(String nick, boolean ignoreFriend)
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

    public static Profile findByHash(String search)
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

    public static Profile findByDisplay(String search)
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

    public static Profile findByNick(String search)
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

    public static List<String> getNames()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        return profilesCopy.stream().map(Profile::getUserDisplay).collect(Collectors.toList());
    }
    private static long lastFriendsUpdate = 0;

    public static List<Profile> getFriends()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        List<Profile> returnList = new ArrayList<>();
        for(Profile profile : profilesCopy)
        {
            if(profile.isFriend())
            {
                returnList.add(profile);
            }
        }
        return returnList;
    }

    public static List<Profile> getMuted()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        List<Profile> returnList = new ArrayList<>();
        for(Profile profile : profilesCopy)
        {
            if(profile.isMuted())
            {
                returnList.add(profile);
            }
        }
        return returnList;
    }

    public static List<Profile> getPartyMembers()
    {
        List<Profile> profilesCopy = new ArrayList<Profile>(profiles.get());
        List<Profile> returnList = new ArrayList<>();
        for(Profile profile : profilesCopy)
        {
            if(profile.isPartyMember())
            {
                returnList.add(profile);
            }
        }
        return returnList;
    }

    public static AtomicReference<List<Profile>> getProfiles()
    {
        return profiles;
    }
}
