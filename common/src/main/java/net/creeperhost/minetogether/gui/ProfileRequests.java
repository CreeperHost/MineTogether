package net.creeperhost.minetogether.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.request.IsBannedRequest;
import net.creeperhost.minetogether.lib.chat.request.v2.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Profile requests specifically for the profile gui.
 * Data in this class is only updated while the profile GUI is open.
 * <p>
 * Created by brandon3055 on 13/05/2024
 */
public class ProfileRequests {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setNameFormat("MT Profile Requests Thread %d")
                    .setDaemon(true)
                    .build()
    );

    private static final List<String> NAME_OPTIONS = new ArrayList<>();
    private static final Map<CompletableFuture<?>, Runnable> ACTIVE_REQUESTS = new LinkedHashMap<>();
    private static final AtomicReference<IsBannedRequest.Ban> ACTIVE_BAN = new AtomicReference<>();
    private static final AtomicReference<GetBanRequest.Ban> BAN_INFO = new AtomicReference<>();
    private static final AtomicReference<String> CUSTOM_NAME = new AtomicReference<>();

    private static volatile long nextChange = -1;
    private static Consumer<Component> errorHandler = e -> {};

    public static void guiOpened(Consumer<Component> errorHandler) {
        ProfileRequests.errorHandler = errorHandler;
        updateRequests();
    }

    public static void updateRequests() {
        List<CompletableFuture<?>> futures = new ArrayList<>(ACTIVE_REQUESTS.keySet());
        for (CompletableFuture<?> future : futures) {
            if (future.isDone()) {
                Runnable runnable = ACTIVE_REQUESTS.remove(future);
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    public static List<String> getNameOptions() {
        return NAME_OPTIONS;
    }

    public static String getCustomName() {
        return CUSTOM_NAME.get();
    }

    public static boolean requestsInProgress() {
        return !ACTIVE_REQUESTS.isEmpty();
    }

    public static boolean isBanned() {
        return ACTIVE_BAN.get() != null;
    }

    public static MutableComponent getBanId() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        return Component.literal(ban == null ? "N/A" : ban.id);
    }

    public static MutableComponent getModerator() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        return Component.literal(ban == null ? "N/A" : ban.moderator);
    }

    public static MutableComponent getReason() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        return Component.literal(ban == null ? "N/A" : ban.reason);
    }

    public static MutableComponent getTimestamp() {
        IsBannedRequest.Ban ban = ACTIVE_BAN.get();
        return Component.literal(ban == null ? "N/A" : ban.timestamp);
    }

    public static MutableComponent getAppealStatus() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        if (ban == null || ban.appeal == null) {
            return Component.translatable("minetogether:gui.profile.ban.appeal_no_info");
        }
        return Component.translatable("minetogether:gui.profile.ban.appeal_submitted");
    }

    public static long getNextAppeal() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        return ban == null || ban.appeal == null ? -1L : ban.appeal.nextAppeal;
    }

    public static MutableComponent getNextAppealComp() {
        long nextAppeal = getNextAppeal();
        return nextAppeal == -1 ? Component.literal("N/A") : Component.literal(DATE_FORMAT.format(nextAppeal * 1000));
    }

    public static List<GetBanRequest.Note> getNotes() {
        GetBanRequest.Ban ban = BAN_INFO.get();
        return ban == null || ban.appeal == null || ban.appeal.notes == null ? Collections.emptyList() : ban.appeal.notes;
    }

    public static List<Component> getNotesTooltip() {
        List<Component> list = new ArrayList<>();
        for (GetBanRequest.Note note : getNotes()) {
            list.add(Component.translatable("minetogether:gui.profile.ban.appeal_note_mod", Component.literal(note.moderator).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE));
            list.add(Component.literal(note.message).withStyle(ChatFormatting.GRAY));
            list.add(Component.literal(DATE_FORMAT.format(note.timestamp * 1000)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return list;
    }

    public static boolean canChangeName() {
        return nextChange != -1 && System.currentTimeMillis() > nextChange * 1000;
    }

    public static MutableComponent getCanChangeComp() {
        return nextChange == -1 ? Component.literal("N/A") : Component.literal(DATE_FORMAT.format(nextChange * 1000));
    }

    public static void fetchName(Runnable onComplete) {
        var future = CompletableFuture.runAsync(() -> {
            try {
                GetNameRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetNameRequest()).apiResponse();
                if (response.success) {
                    CUSTOM_NAME.set(response.name);
                    nextChange = response.nextChange;
                } else {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.error.get_name_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.reason).withStyle(ChatFormatting.RED));
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to retrieve current name from API", e);
                errorHandler.accept(Component.literal("An error occurred while retrieving current name from API").withStyle(ChatFormatting.RED));
            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, onComplete);
    }

    public static void fetchNameOptions(Runnable onComplete) {
        if (!NAME_OPTIONS.isEmpty()) {
            onComplete.run();
            return;
        }
        var future = CompletableFuture.runAsync(() -> {
            try {
                GetNamesRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetNamesRequest()).apiResponse();
                if (response.success) {
                    NAME_OPTIONS.clear();
                    NAME_OPTIONS.addAll(response.names);
                } else {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.error.get_names_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.reason).withStyle(ChatFormatting.RED));
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to retrieve name options from API", e);
                errorHandler.accept(Component.literal("An error occurred while retrieving name options from API").withStyle(ChatFormatting.RED));
            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, onComplete);
    }

    public static void fetchBannedStatus(Runnable onComplete) {
        var future = CompletableFuture.runAsync(() -> {
            try {
                IsBannedRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new IsBannedRequest(MineTogetherChat.getOurProfile().getFullHash())).apiResponse();
                if ("success".equals(response.getStatus())) {
                    if (response.banned && response.ban != null) {
                        ACTIVE_BAN.set(response.ban);
                        fetchBanInfo(onComplete);
                    } else {
                        ACTIVE_BAN.set(null);
                    }
                } else {
                    ACTIVE_BAN.set(null);
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.error.get_ban_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.getMessageOrNull() == null ? "Unknown reason" : response.getMessageOrNull()).withStyle(ChatFormatting.RED));
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to retrieve ban status from API", e);
                errorHandler.accept(Component.literal("An error occurred while retrieving ban status from API").withStyle(ChatFormatting.RED));

            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, onComplete);
    }

    public static void fetchBanInfo(Runnable onComplete) {
        IsBannedRequest.Ban ban = ACTIVE_BAN.get();
        if (ban == null) return;
        var future = CompletableFuture.runAsync(() -> {
            try {
                GetBanRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetBanRequest(ban.id)).apiResponse();
                if (response.success) {
                    BAN_INFO.set(response.ban);
                } else {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.error.get_ban_info_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.reason).withStyle(ChatFormatting.RED));
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to retrieve ban info from API", e);
                errorHandler.accept(Component.literal("An error occurred while retrieving ban info from API").withStyle(ChatFormatting.RED));
            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, onComplete);
    }

    public static void setName(String newName, Runnable onComplete) {
        if (newName.trim().isEmpty()) {
            errorHandler.accept(Component.translatable("minetogether:gui.profile.name_is_empty").withStyle(ChatFormatting.RED));
            return;
        }

        errorHandler.accept(Component.translatable("minetogether:gui.profile.name_change_sent"));
        var future = CompletableFuture.runAsync(() -> {
            try {
                PutNameRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new PutNameRequest(newName)).apiResponse();
                if (response.success) {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.name_change_success").append(" ").withStyle(ChatFormatting.GREEN).append(Component.literal(response.name).withStyle(ChatFormatting.GOLD)));
                } else {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.name_change_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.reason).withStyle(ChatFormatting.RED));
                    if (response.nextChange != -1) {
                        errorHandler.accept(Component.translatable("minetogether:gui.profile.next_name_change").withStyle(ChatFormatting.GRAY));
                        errorHandler.accept(Component.literal(DATE_FORMAT.format(response.nextChange * 1000)).withStyle(ChatFormatting.GRAY));
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("An error occurred while trying to set name", e);
                errorHandler.accept(Component.literal("An error occurred while trying to set name").withStyle(ChatFormatting.RED));
            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, onComplete);
    }

    public static void submitAppeal(String appeal, BiConsumer<Boolean, Component> onComplete) {
        if (appeal.trim().isEmpty()) {
            errorHandler.accept(Component.translatable("minetogether:gui.profile.ban.appeal_is_empty").withStyle(ChatFormatting.RED));
            return;
        }

        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<Component> message = new AtomicReference<>();
        errorHandler.accept(Component.translatable("minetogether:gui.profile.ban.submitting"));
        var future = CompletableFuture.runAsync(() -> {
            try {
                Apiv2Response response = MineTogetherChat.CHAT_STATE.api.execute(new DeleteBanRequest(BAN_INFO.get().id, appeal)).apiResponse();
                if (response.success) {
                    success.set(true);
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.ban.submitted").withStyle(ChatFormatting.GREEN));
                    message.set(Component.translatable("minetogether:gui.profile.ban.submitted").withStyle(ChatFormatting.GREEN));
                } else {
                    errorHandler.accept(Component.translatable("minetogether:gui.profile.ban.appeal_fail").withStyle(ChatFormatting.RED));
                    errorHandler.accept(Component.literal(response.reason).withStyle(ChatFormatting.RED));
                    message.set(Component.translatable("minetogether:gui.profile.ban.appeal_fail").withStyle(ChatFormatting.RED).append("\n" + response.reason));
                }
            } catch (Throwable e) {
                LOGGER.error("An error occurred while trying send ban appeal", e);
                errorHandler.accept(Component.literal("An error occurred while trying to send ban appeal").withStyle(ChatFormatting.RED));
                message.set(Component.literal("An error occurred while trying to send ban appeal").withStyle(ChatFormatting.RED));
            }
        }, EXECUTOR);
        ACTIVE_REQUESTS.put(future, () -> {
            if (onComplete != null) {
                onComplete.accept(success.get(), message.get());
            }
        });
    }
}
