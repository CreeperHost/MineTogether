package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.module.multiplayer.MultiPlayerModule;
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
    private static CompletableFuture chatThread = null;
    private static MineTogetherChat mineTogetherChat;
    private static boolean isOnlineUUID = false;

    public static void init()
    {
        toastHandler = new ToastHandler();
        GuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
        GuiEvent.RENDER_POST.register(MineTogetherClient::onScreenRender);
        startChat();
    }

    @SuppressWarnings("all")
    public static void startChat()
    {
        mineTogetherChat = new MineTogetherChat();
        MineTogetherChat.INSTANCE.ourNick = "MT" + ChatCallbacks.getPlayerHash(getUUID()).substring(0, 28);
        MineTogetherChat.INSTANCE.uuid = getUUID();
        MineTogetherChat.INSTANCE.online = isOnlineUUID;
        MineTogetherChat.INSTANCE.realName = "{\"p\": \"-1\"}";
        MineTogetherChat.INSTANCE.signature = new SignatureVerifier(Platform.getGameFolder().resolve("mods").toFile()).verify();
        MineTogetherChat.INSTANCE.serverID = getServerIDAndVerify();

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
        //Make sure that the screen is not null for any reason
        if(screen == null) return;

        if (MineTogetherClient.toastHandler != null && MineTogetherClient.toastHandler.toastText != null)
        {
            long curTime = System.currentTimeMillis();
            if (MineTogetherClient.toastHandler.fadeTime > curTime)
            {
                long fadeDiff = MineTogetherClient.toastHandler.fadeTime - MineTogetherClient.toastHandler.endTime;
                long curFade = Math.min(MineTogetherClient.toastHandler.fadeTime - curTime, fadeDiff);
                float alpha = (float) curFade / (float) fadeDiff;

                RenderSystem.disableLighting();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                Minecraft.getInstance().getTextureManager().bind(MineTogetherClient.toastHandler.TEXTURE_TOASTS);
                List<FormattedCharSequence> s = ComponentRenderUtils.wrapComponents(MineTogetherClient.toastHandler.toastText, 140, Minecraft.getInstance().font);

                int toastHeight = 32 + (s.size() * 8);
                Screen.blit(poseStack, toastHandler.getX(), toastHandler.getY(), toastHandler.getToastType().getX(), toastHandler.getToastType().getY(), 160, 32, 256, 256);

                RenderSystem.enableBlend();
                int textColour = (0xFFFFFF << 32) | ((int) (alpha * 255) << 24);

                int start = (toastHandler.getY() + 2);
                for(FormattedCharSequence properties : s)
                {
                    Minecraft.getInstance().font.drawShadow(poseStack, properties, toastHandler.getX() + 15, start +=8, textColour);
                }

            } else
            {
                MineTogetherClient.toastHandler.clearToast(false);
            }
        }
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

        if (screen instanceof TitleScreen)
        {
            AbstractWidget relms = ScreenHelpers.removeButton("menu.online", abstractWidgets);
            if(relms != null)
            {
                ScreenHooks.addButton(screen, new Button(relms.x, relms.y, relms.getWidth(), relms.getHeight(), new TranslatableComponent("minetogether.button.getserver"), p ->
                {
                    Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order()));
                }));
            }

            ScreenHooks.addButton(screen, new Button(screen.width - 105, 5, 100, 20, new TranslatableComponent("minetogether.multiplayer.friends"), p ->
            {
                Minecraft.getInstance().setScreen(new FriendsListScreen(screen));
            }));

            ScreenHooks.addButton(screen, new ButtonMultiple(screen.width - 125, 5, 1, p ->
            {
                if (Config.getInstance().isChatEnabled())
                {
                    Minecraft.getInstance().setScreen(new ChatScreen(screen));
                }
                else
                {
                    Minecraft.getInstance().setScreen(new SettingsScreen(screen));
                }
            }));
        }
    }
}
