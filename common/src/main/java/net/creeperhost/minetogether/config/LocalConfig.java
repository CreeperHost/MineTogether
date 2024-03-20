package net.creeperhost.minetogether.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import dev.architectury.platform.Platform;
import net.covers1624.quack.io.IOUtils;
import net.creeperhost.minetogether.chat.ChatTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static net.creeperhost.minetogether.MineTogether.MOD_ID;

/**
 * Created by covers1624 on 20/3/24.
 */
public class LocalConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Jankson JANKSON = Jankson.builder()
            .build();
    private static final JsonGrammar GRAMMAR = JsonGrammar.JSON5;

    private static @Nullable LocalConfig INSTANCE;
    private static @Nullable Path filePath;

    public static LocalConfig instance() {
        if (INSTANCE == null) {
            synchronized (LocalConfig.class) {
                if (INSTANCE != null) return INSTANCE;
                INSTANCE = loadConfig(Platform.getGameFolder().resolve("local/minetogether/" + MOD_ID + ".json"));
                save();
                // Force main config to load, to migrate data.
                // This is safe to do in here as we have set the INSTANCE variable. re-entry will return the instance.
                // Config will call LocalConfig.instance() to migrate. However, also only does this after its config variable
                // has been assigned.
                Config.instance();
            }
        }
        return INSTANCE;
    }

    public static void save() {
        assert INSTANCE != null;
        assert filePath != null;

        try (BufferedWriter writer = Files.newBufferedWriter(IOUtils.makeParents(filePath), StandardCharsets.UTF_8)) {
            writer.append(JANKSON.toJson(INSTANCE).toJson(GRAMMAR));
            writer.flush();
        } catch (IOException ex) {
            LOGGER.error("Failed to save config file to: " + filePath, ex);
        }
    }

    private static synchronized LocalConfig loadConfig(Path file) {
        file = file.toAbsolutePath();
        LocalConfig config;
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                config = JANKSON.fromJson(JANKSON.load(is), LocalConfig.class);
            } catch (IOException | SyntaxError ex) {
                LOGGER.error("Failed to read config file from '" + file + "' - Resetting to default.", ex);
                config = new LocalConfig();
            }
        } else {
            config = new LocalConfig();
        }
        filePath = file;
        return config;
    }

    @Comment ("If the Chat component of MineTogether is enabled.")
    public boolean chatEnabled = true;

    @Comment ("If notifications for friends are enabled.")
    public boolean friendNotifications = true;

    @Comment ("Enable / disable chat settings sliders.")
    public boolean chatSettingsSliders = true;

    @Comment ("If menu buttons are enabled.")
    public boolean mainMenuButtons = true;

    @Comment ("If the Multiplayer server list should have CreeperHost adverts.")
    public boolean mpMenuEnabled = true;

    @Comment ("INTERNAL: Marker for tracking first connections.")
    public Set<String> firstConnect = new HashSet<>();

    @Comment ("Stores the currently selected chat TAB. Either VANILLA or PUBLIC.")
    public ChatTarget selectedTab = ChatTarget.PUBLIC;

}
