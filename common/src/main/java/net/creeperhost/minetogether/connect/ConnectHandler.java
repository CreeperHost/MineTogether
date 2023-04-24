package net.creeperhost.minetogether.connect;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;

import java.util.*;

/**
 * Created by brandon3055 on 21/04/2023
 */
public class ConnectHandler {

    private static Map<RemoteServer, Profile> testServerMap = new HashMap<>();


    public static void init() {
    }

    public static boolean isEnabled() {
        return true; //TODO v2
    }

    @Deprecated
    public static void genRandomTestServers() {
        testServerMap.clear();
        //Generate a bunch or random fake servers for testing.
        ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
        List<Profile> profiles =profileManager.getKnownProfiles();
        Random random = new Random();

        for (Profile profile : profiles) {
            if (profile.isFriend()) {
                RemoteServer server = new RemoteServer(String.valueOf(random.nextLong()), String.valueOf(random.nextLong()));
                testServerMap.put(server, profile);
            }
        }

        int count = Math.min(profiles.size(), 5 + random.nextInt(10));
        for (int i = 0; i < count; i++) {
            Profile profile;
            do {
                profile = profiles.get(random.nextInt(profiles.size()));
            } while (testServerMap.containsKey(profile));

            RemoteServer server = new RemoteServer(String.valueOf(random.nextLong()), String.valueOf(random.nextLong()));
            testServerMap.put(server, profile);
        }
    }

    public static Collection<RemoteServer> getRemoteServers() {
        return testServerMap.keySet();
    }

    public static Profile getServerProfile(RemoteServer server) {
        return testServerMap.get(server);
    }

    public static void connect(RemoteServer server) {
        //Do the thing!!!
    }



}
