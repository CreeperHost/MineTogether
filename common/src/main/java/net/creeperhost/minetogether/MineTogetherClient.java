package net.creeperhost.minetogether;

import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.datafixers.util.Either;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.MineTogetherConnect;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.orderform.OrderForm;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.serverlist.web.GetServerRequest;
import net.creeperhost.minetogether.session.JWebToken;
import net.creeperhost.minetogether.session.MineTogetherSession;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Initialize on a client.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class MineTogetherClient {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService SESSION_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("MT Session Executor").build());

    private static boolean first = true;
    @Nullable
    private static MineTogetherSession session;

    public static void init() {
        LOGGER.info("Initializing MineTogetherClient!");

        Minecraft mc = Minecraft.getInstance();
        GameProfile profile = mc.getUser().getGameProfile();
        // Profile id may be null if none is specified when starting the game (dev)
        // Version 4 is 'random', version 3 is offline (md5 hash based).
        if (profile.getId() != null && profile.getId().version() == 4) {
            session = new MineTogetherSession(
                    Path.of("./.mtsession"),
                    profile.getId(),
                    profile.getName(),
                    () -> {
                        String serverId = Hashing.sha1().hashString(UUID.randomUUID().toString(), UTF_8).toString();
                        try {
                            mc.getMinecraftSessionService().joinServer(mc.getUser().getGameProfile(), mc.getUser().getAccessToken(), serverId);
                            return serverId;
                        } catch (AuthenticationException ex) {
                            LOGGER.error("Failed to send 'joinServer' request.", ex);
                        }
                        return null;
                    }
            );
        }

        MineTogetherChat.init();
        MineTogetherServerList.init();
        MineTogetherConnect.init();

        ClientGuiEvent.INIT_POST.register(MineTogetherClient::onScreenOpen);
    }

    public static CompletableFuture<Either<JWebToken, String>> getSession() {
        // TODO, store the CompletableFuture, so we can preserve the api errors in the Either.
        // TODO, also detect the token becoming expired between calls to getSession and prepare a new one.
        // TODO, Would a wrapper around the CompletableFuture be better? which lazily does everything internally when queried for a result?
        if (session == null)
            return CompletableFuture.completedFuture(Either.right("Offline mode. No session available."));
        if (session.isValid()) return CompletableFuture.completedFuture(Either.left(session.getToken()));
        return CompletableFuture.supplyAsync(() -> {
            try {
                session.validate();
                if (!session.isValid()) {
                    LOGGER.error("Got invalid session after validate.. Api failure?");
                    return Either.right("Got invalid session after validate.. Api failure?");
                }
                return Either.left(session.getToken());
            } catch (Throwable ex) {
                LOGGER.error("Failed to validate session.", ex);
                return Either.right("Failed to acquire session token. See logs.");
            }
        }, SESSION_EXECUTOR);
    }

    private static void onScreenOpen(Screen screen, ScreenAccess screenAccess) {
        if (screen instanceof TitleScreen && first) {
            first = false;
            String serverProp = System.getProperty("mt.server");
            if (serverProp == null) return;

            Server server;
            try {
                ApiClientResponse<GetServerRequest.Response> resp = MineTogether.API.execute(new GetServerRequest(serverProp));
                if (resp.apiResponse().getStatus().equals("error")) {
                    LOGGER.error("Failed to load server with id: {}. Message: {}", serverProp, resp.apiResponse().getMessageOrNull());
                    return;
                }
                server = resp.apiResponse().server;
                if (server == null) {
                    LOGGER.error("Returned empty server?");
                    return;
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to query server.", ex);
                return;
            }

            ServerData serverData = new ServerData(server.ip, String.valueOf(server.port), false);
            ConnectScreen.startConnecting(new JoinMultiplayerScreen(screen), Minecraft.getInstance(), ServerAddress.parseString(serverData.ip), serverData, false);
        } else if (screen instanceof PauseScreen) {
            @SuppressWarnings("unchecked")
            List<GuiEventListener> children = (List<GuiEventListener>) screen.children();
            List<Renderable> renderables = screenAccess.getRenderables();
            List<NarratableEntry> narratables = screenAccess.getNarratables();

            // Replace bugs button with our own button.
            AbstractWidget bugs = ButtonHelper.findButton("menu.reportBugs", screen);
            if (bugs != null && Config.instance().issueTrackerUrl != null) {
                Button ourBugsButton = Button.builder(Component.translatable("menu.reportBugs"), (button) -> {
                            String s = Config.instance().issueTrackerUrl;
                            Minecraft.getInstance().setScreen(new ConfirmLinkScreen((p_213069_2_) -> {
                                if (p_213069_2_) {
                                    Util.getPlatform().openUri(s);
                                }

                                Minecraft.getInstance().setScreen(screen);
                            }, s, true));
                        })
                        .bounds(bugs.getX(), bugs.getY(), bugs.getWidth(), bugs.getHeight())
                        .build();
                // We have to keep these indexes the same and remove the old button due to how Mod Menu works...
                children.set(children.indexOf(bugs), ourBugsButton);
                renderables.set(renderables.indexOf(bugs), ourBugsButton);
                narratables.set(narratables.indexOf(bugs), ourBugsButton);
                bugs = ourBugsButton;
            }
        }
    }
}
