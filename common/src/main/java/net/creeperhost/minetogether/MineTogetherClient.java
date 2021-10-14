package net.creeperhost.minetogether;

import com.google.common.hash.Hashing;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import net.creeperhost.minetogether.handler.AutoServerConnectHandler;
import net.creeperhost.minetogether.handler.ToastHandler;
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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MineTogetherClient
{
    public static ToastHandler toastHandler;
    public static boolean isOnlineUUID = false;
    public static final KeyMapping mtSocialKey = new KeyMapping(I18n.get("minetogether.keybindings.social"), InputConstants.Type.KEYSYM, 80, I18n.get("minetogether.keybindings.category"));

    public static void init()
    {
        toastHandler = new ToastHandler();
        GuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
        GuiEvent.RENDER_POST.register(MineTogetherClient::onScreenRender);
        GuiEvent.RENDER_HUD.register(MineTogetherClient::onHudRender);
        ClientRawInputEvent.KEY_PRESSED.register(MineTogetherClient::onRawInput);
        ConnectModule.init();
        MineTogetherClient.getUUID();
        ChatModule.init();
        registerKeybindings();

        if (!isOnlineUUID) MineTogether.logger.info(Constants.MOD_ID + " Has detected profile is in offline mode");

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            //Kill the IRC Thread
            IrcHandler.stop(true);
            //Kill Friend Thread
            FriendUpdateThread.stop();
        }));
    }

    private static InteractionResult onRawInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers)
    {
        if (minecraft.screen == null)
        {
            if (!MineTogetherClient.toastHandler.isActiveToast() && mtSocialKey.isDown())
            {
                minecraft.setScreen(new MineTogetherSocialInteractionsScreen());
                return InteractionResult.SUCCESS;
            }
        }

        if (MineTogetherClient.toastHandler.toastMethod != null && mtSocialKey.isDown())
        {
            MineTogetherClient.toastHandler.toastMethod.run();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static void registerKeybindings()
    {
        KeyBindings.registerKeyBinding(mtSocialKey);
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
        isOnlineUUID = !uuid.equals(Player.createPlayerUUID(session.getName()));
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
            return null;
        }
        return serverId;
    }

    public static void onHudRender(PoseStack poseStack, float partialticks)
    {
        if (toastHandler != null) toastHandler.render(poseStack);
    }

    private static void onScreenRender(Screen screen, PoseStack poseStack, int i, int i1, float part)
    {
        if (toastHandler != null) toastHandler.render(poseStack);
    }

    static boolean firstOpen = true;

    private static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
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
        MultiPlayerModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
        ServerOrderModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
        ChatModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
        AutoServerConnectHandler.onScreenOpen(screen, abstractWidgets, guiEventListeners);
    }
}
