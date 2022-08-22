package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.sentry.Sentry;
import net.creeperhost.minetogether.handler.AutoServerConnectHandler;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.irc.IrcHandler;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.social.MineTogetherSocialInteractionsScreen;
import net.creeperhost.minetogether.module.connect.ConnectModule;
import net.creeperhost.minetogether.module.multiplayer.MultiPlayerModule;
import net.creeperhost.minetogether.module.serverorder.ServerOrderModule;
import net.creeperhost.minetogether.screen.OfflineScreen;
import net.creeperhost.minetogether.threads.FriendUpdateThread;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.Random;
import java.util.UUID;

public class MineTogetherClient
{
    public static boolean isOnlineUUID = false;
    public static final KeyMapping mtSocialKey = new KeyMapping(I18n.get("minetogether.keybindings.social"), InputConstants.Type.KEYSYM, 80, I18n.get("minetogether.keybindings.category"));

    public static void init()
    {
        try
        {
            ClientGuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
            ClientRawInputEvent.KEY_PRESSED.register(MineTogetherClient::onRawInput);
            ConnectModule.init();
            MineTogetherClient.getUUID();
            ChatModule.init();
            registerKeybindings();

            if (!isOnlineUUID) MineTogetherCommon.logger.info(Constants.MOD_ID + " Has detected profile is in offline mode");

            Runtime.getRuntime().addShutdownHook(new Thread(() ->
            {
                MineTogetherCommon.logger.info("Shutdown called, Stopping our threads");
                IrcHandler.sendString("QUIT Game closed", false);
                //Kill the IRC Thread
                IrcHandler.stop(true);
                //Kill Friend Thread
                FriendUpdateThread.stop();
            }));
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
    }

    private static EventResult onRawInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers)
    {
        try
        {
            if (minecraft.screen == null)
            {
                if (mtSocialKey.isDown())
                {
                    minecraft.setScreen(new MineTogetherSocialInteractionsScreen());
                    return EventResult.pass();
                }
            }
            return EventResult.pass();
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
        return EventResult.pass();
    }

    public static void registerKeybindings()
    {
        KeyMappingRegistry.register(mtSocialKey);
    }

    public static void removeVanillaSocialKeybinding()
    {
        Minecraft.getInstance().options.keySocialInteractions.setKey(InputConstants.UNKNOWN);
        //This should really be called updateMappings
        KeyMapping.resetMapping();
    }

    public static UUID getUUID()
    {
        User session = Minecraft.getInstance().getUser();
        UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
        isOnlineUUID = !uuid.equals(UUIDUtil.createOfflinePlayerUUID(session.getName()));
        return uuid;
    }

    public static String getPlayerHash()
    {
        return ChatCallbacks.getPlayerHash(getUUID());
    }

    public static String getServerIDAndVerify()
    {
        Minecraft mc = Minecraft.getInstance();
        String serverId = DigestUtils.sha1Hex(String.valueOf(new Random().nextInt()));
        try
        {
            mc.getMinecraftSessionService().joinServer(mc.getUser().getGameProfile(), mc.getUser().getAccessToken(), serverId);
        } catch (AuthenticationException e)
        {
            MineTogetherCommon.logger.error("Failed to get serverID from Mojang", e);
            return null;
        }
        MineTogetherCommon.logger.info("new ServerID requested");
        return serverId;
    }

    static boolean firstOpen = true;

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess)
    {
        try
        {
            if (firstOpen && screen instanceof TitleScreen)
            {
                //Lets get this value early so we can cache it
                ChatCallbacks.updateOnlineCount();
                removeVanillaSocialKeybinding();

                File offline = new File("local/minetogether/offline.txt");

                if (!MineTogetherClient.isOnlineUUID && !offline.exists())
                {
                    Minecraft.getInstance().setScreen(new OfflineScreen());
                }
                firstOpen = false;
            }
            MultiPlayerModule.onScreenOpen(screen, screenAccess);
            ServerOrderModule.onScreenOpen(screen, screenAccess);
            ChatModule.onScreenOpen(screen, screenAccess);
            AutoServerConnectHandler.onScreenOpen(screen, screenAccess);
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
    }
}
