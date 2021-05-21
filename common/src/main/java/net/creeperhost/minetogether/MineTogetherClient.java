package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.multiplayer.MultiPlayerModule;
import net.creeperhost.minetogether.module.serverorder.ServerOrderModule;
import net.creeperhost.minetogether.screen.OfflineScreen;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogether.module.serverorder.screen.OrderServerScreen;
import net.creeperhost.minetogether.module.chat.screen.ChatScreen;
import net.creeperhost.minetogether.module.chat.screen.FriendsListScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogetherlib.Order;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MineTogetherClient
{
    public static ToastHandler toastHandler;
    private static MineTogetherChat mineTogetherChat;
    private static boolean isOnlineUUID = false;

    public static void init()
    {
        toastHandler = new ToastHandler();
        GuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
        GuiEvent.RENDER_POST.register(MineTogetherClient::onScreenRender);
        buildChat();
    }

    public static void buildChat()
    {
        String ourNick = "MT" + ChatCallbacks.getPlayerHash(getUUID()).substring(0, 28);
        UUID uuid = getUUID();
        boolean online = isOnlineUUID;
        String realName = "{\"p\": \"-1\"}";
        String signature = new SignatureVerifier().verify();
        String serverID = getServerIDAndVerify();

        mineTogetherChat = new MineTogetherChat(ourNick, uuid, online, realName, signature, serverID);
        mineTogetherChat.startChat();
    }

    public static MineTogetherChat getMineTogetherChat()
    {
        return mineTogetherChat;
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
        } catch (AuthenticationException e) { return null; }
        return serverId;
    }

    private static void onScreenRender(Screen screen, PoseStack poseStack, int i, int i1, float part)
    {
        if(toastHandler != null) toastHandler.onScreenRender(screen, poseStack, i, i1, part);
    }

    static boolean firstOpen = true;

    private static void onScreenOpen(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
    {
        if(firstOpen && screen instanceof TitleScreen)
        {
            File offline = new File("local/minetogether/offline.txt");

            if(!MineTogetherClient.isOnlineUUID && !offline.exists())
            {
                firstOpen = false;
                Minecraft.getInstance().setScreen(new OfflineScreen());
            }
        }
        MultiPlayerModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
        ServerOrderModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
        ChatModule.onScreenOpen(screen, abstractWidgets, guiEventListeners);
    }
}
