package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogether.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogether.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogether.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MineTogether
{
    public static final String MOD_ID = "minetogether";
    public static Logger logger = LogManager.getLogger();
    private static CompletableFuture chatThread = null;
    private static MineTogetherChat mineTogetherChat;

    public static void init()
    {
        Path configFile = Platform.getConfigFolder().resolve(MOD_ID + ".json");
        Config.init(configFile.toFile());
    }

    public static void clientInit()
    {
        EnvExecutor.runInEnv(EnvType.CLIENT, () -> MineTogetherClient::init);
        startChat();
    }

    public static void startChat()
    {
        mineTogetherChat = new MineTogetherChat();
        MineTogetherChat.INSTANCE.ourNick = "MT" + ChatCallbacks.getPlayerHash(getUUID()).substring(0, 28);
        MineTogetherChat.INSTANCE.online = true;
        MineTogetherChat.INSTANCE.realName = "{\"p\": \"-1\"}";
        MineTogetherChat.INSTANCE.signature = new SignatureVerifier(Platform.getGameFolder().resolve("mods").toFile()).verify();
        MineTogetherChat.INSTANCE.serverID = getServerIDAndVerify();
        MineTogetherChat.INSTANCE.uuid = getUUID();

        if(chatThread != null) {
            chatThread.cancel(true);
            chatThread = null;
        }
        if (MineTogetherChat.INSTANCE.profile.get() == null) {
            MineTogetherChat.INSTANCE.profile.set(new Profile(MineTogetherChat.INSTANCE.ourNick));
            CompletableFuture.runAsync(() ->
            {
                while (MineTogetherChat.INSTANCE.profile.get().getLongHash().isEmpty()) {
                    MineTogetherChat.INSTANCE.profile.get().loadProfile();
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, MineTogetherChat.INSTANCE.profileExecutor);
        }
        chatThread = CompletableFuture.runAsync(() -> ChatHandler.init(MineTogetherChat.INSTANCE.ourNick, MineTogetherChat.INSTANCE.realName, MineTogetherChat.INSTANCE.online, MineTogetherChat.INSTANCE), MineTogetherChat.profileExecutor); // start in thread as can hold up the UI thread for some reason.
    }

    //TODO fix session checking
    public static UUID getUUID()
    {
        User session = Minecraft.getInstance().getUser();
        UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
//        MineTogether.instance.online = !uuid.equals(PlayerEntity.getOfflineUUID(session.getUsername()));

        return uuid;
    }

    public static String getServerIDAndVerify() {
        Minecraft mc = Minecraft.getInstance();
        String serverId = DigestUtils.sha1Hex(String.valueOf(new Random().nextInt()));
        try {
            mc.getMinecraftSessionService().joinServer(mc.getUser().getGameProfile(), mc.getUser().getAccessToken(), serverId);
        } catch (AuthenticationException e) {
            return null;
        }
        return serverId;
    }
}
