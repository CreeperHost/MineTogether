package net.creeperhost.minetogether.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.covers1624.quack.gson.JsonUtils;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by covers1624 on 25/10/22.
 */
public class ModPackInfo {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static String curseID = StringUtils.stripToEmpty(Config.instance().curseProjectID);
    public static String base64FTBID = "";
    public static String ftbPackID = "";

    public static String realName = "{\"p\": \"-1\"}";

    public static void init() {
        // TODO off thread?
        readVersionJson();

        Map<String, String> json = new HashMap<>();
        if (ftbPackID.isEmpty()) {
            json.put("p", NumberUtils.isParsable(curseID) ? curseID : "-1");
        } else {
            json.put("p", ftbPackID);
            json.put("b", base64FTBID);
        }

        realName = GSON.toJson(json);
    }

    private static void readVersionJson() {
        Path versionJson = Platform.getGameFolder().resolve("version.json");
        if (Files.exists(versionJson)) {
            try {
                ModpackVersionManifest manifest = JsonUtils.parse(GSON, versionJson, ModpackVersionManifest.class);
                base64FTBID = Base64.getEncoder().encodeToString((String.valueOf(manifest.parent) + manifest.id).getBytes(StandardCharsets.UTF_8));
                String resolvedID = MineTogether.API.execute(new GetCurseForgeVersionRequest(base64FTBID)).apiResponse().id;
                if (resolvedID.isEmpty()) {
                    resolvedID = "-1";
                }
                curseID = resolvedID;
                Config.instance().curseProjectID = resolvedID;
                Config.save();

                ftbPackID = "m" + manifest.parent;
            } catch (IOException ex) {
                LOGGER.error("Failed to load version manifest.", ex);
            }
        }
    }

    public static class ModpackVersionManifest {

        public long id;
        public long parent;
    }
}
