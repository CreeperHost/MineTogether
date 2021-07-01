package net.creeperhost.minetogether.module.connect;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class FriendDetector extends Thread {
    private FriendsServerList owner;
    private static AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    public FriendDetector(FriendsServerList owner) {
        super("LanServerDetector #" + FriendDetector.UNIQUE_THREAD_ID.incrementAndGet());
        this.owner = owner;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            ConnectHandler.FriendsResponse friends = ConnectHandler.getFriendsBlocking();
            if (ConnectModule.isInitted && friends != null && friends.getFriends() != null) {
                for (ConnectHandler.FriendsResponse.Friend friend : friends.getFriends()) {
                    CompletableFuture.runAsync(() -> {
                        ServerData server = new ServerData(friend.getDisplayName() + "'s server", friend.getAddress(), false);
                        try {
                            this.owner.owner.getPinger().pingServer(server, () -> {
                                owner.addPendingServer(server);
                            });
                        } catch (UnknownHostException var2) {
                            server.ping = -1;
                            server.motd = new TextComponent(ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_resolve"));
                        } catch (Exception var3) {
                            server.ping = -1L;
                            server.motd = new TextComponent(ChatFormatting.DARK_RED + I18n.get("multiplayer.status.cannot_connect"));
                        }
                        if (server.ping > 0) {
                            owner.addPendingServer(server);
                        }
                    }, ConnectModule.connectExecutor);
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static class PendingFriend
    {
        private final String chosenName;
        private final String displayName;
        private final String address;

        public PendingFriend(String chosenName, String displayName, String address)
        {
            this.chosenName = chosenName;
            this.displayName = displayName;
            this.address = address;
        }

        public String getChosenName() {
            return chosenName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAddress() {
            return address;
        }
    }
}
