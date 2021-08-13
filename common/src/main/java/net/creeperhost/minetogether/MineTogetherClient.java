package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.creeperhost.minetogether.handler.AutoServerConnectHandler;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.mixin.MixinScreen;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.social.MineTogetherSocialInteractionsScreen;
import net.creeperhost.minetogether.module.connect.ConnectModule;
import net.creeperhost.minetogether.module.multiplayer.MultiPlayerModule;
import net.creeperhost.minetogether.module.serverorder.ServerOrderModule;
import net.creeperhost.minetogether.screen.OfflineScreen;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
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
        ClientGuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
        ClientGuiEvent.RENDER_POST.register(MineTogetherClient::onScreenRender);
        ClientGuiEvent.RENDER_HUD.register(MineTogetherClient::onHudRender);
        ClientRawInputEvent.KEY_PRESSED.register(MineTogetherClient::onRawInput);
        ConnectModule.init();
        MineTogetherClient.getUUID();
        ChatModule.init();
        registerKeybindings();

        if (!isOnlineUUID) MineTogether.logger.info(Constants.MOD_ID + " Has detected profile is in offline mode");
    }


    private static EventResult onRawInput(Minecraft minecraft, int i, int i1, int i2, int i3)
    {
        if (minecraft.screen == null)
        {
            if (!MineTogetherClient.toastHandler.isActiveToast() && mtSocialKey.isDown())
            {
                minecraft.setScreen(new MineTogetherSocialInteractionsScreen());
                return EventResult.pass();
            }
        }

        if (MineTogetherClient.toastHandler.toastMethod != null && mtSocialKey.isDown())
        {
            MineTogetherClient.toastHandler.toastMethod.run();
            return EventResult.pass();
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
        isOnlineUUID = !uuid.equals(Player.createPlayerUUID(session.getName()));
        return uuid;
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

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess)
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
        MultiPlayerModule.onScreenOpen(screen, getWidgetList(screen));
        ServerOrderModule.onScreenOpen(screen, getWidgetList(screen));
        ChatModule.onScreenOpen(screen, getWidgetList(screen));
        AutoServerConnectHandler.onScreenOpen(screen, getWidgetList(screen));
    }

    public static List<AbstractWidget> getWidgetList(Screen screen)
    {
        return ((MixinScreen) screen).getRenderables();
    }
}
