package net.creeperhost.minetogether.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by covers1624 on 20/6/22.
 */
public class Config {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Jankson JANKSON = Jankson.builder()
            .registerSerializer(Path.class, (p, m) -> JsonPrimitive.of(p.toAbsolutePath().toString()))
            .registerDeserializer(JsonPrimitive.class, Path.class, (p, m) -> Paths.get(p.asString()))
            .build();
    private static final JsonGrammar GRAMMAR = JsonGrammar.JSON5;

    @Nullable
    private static Config INSTANCE;
    @Nullable
    private static Path filePath;

    public static Config instance() {
        if (INSTANCE == null) throw new IllegalStateException("Config not loaded.");

        return INSTANCE;
    }

    public static void save() {
        assert INSTANCE != null;
        assert filePath != null;

        try {
            Files.createDirectories(filePath.toAbsolutePath().getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.append(JANKSON.toJson(INSTANCE).toJson(GRAMMAR));
                writer.flush();
            }
        } catch (IOException ex) {
            LOGGER.fatal("Failed to save config file to: " + filePath.toAbsolutePath(), ex);
        }
    }

    public static void loadConfig(Path file) {
        Config config;
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                config = JANKSON.fromJson(JANKSON.load(is), Config.class);
            } catch (IOException | SyntaxError ex) {
                LOGGER.fatal("Failed to read config file from '" + file.toAbsolutePath() + "' - Resetting to default.", ex);
                config = new Config();
            }
        } else {
            config = new Config();
        }

        filePath = file;
        INSTANCE = config;
        save();
    }

    @Nullable
    @Comment ("For modpack creators. Enter your CurseForge project id here.")
    public String curseProjectID = null;

    @Nullable
    @Comment ("For modpack creators. Enter your 'Promo Code' here.")
    public String promoCode = null;

    @Comment("Create _VERY_ verbose logs. May create hugenorums log files.")
    public boolean debugMode = false;
}
