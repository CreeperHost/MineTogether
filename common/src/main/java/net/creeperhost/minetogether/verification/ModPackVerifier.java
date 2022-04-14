package net.creeperhost.minetogether.verification;

import com.google.gson.*;
import dev.architectury.platform.Platform;
import io.sentry.Sentry;
import net.creeperhost.minetogether.MineTogetherCommon;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class ModPackVerifier
{
    String curseID = "";
    String base64 = "";
    String requestedID = "";
    String ftbPackID = "";

    public ModPackVerifier()
    {
        curseID = Config.getInstance().getCurseProjectID();
    }

    public String verify()
    {
        updateFtbPackID();

        int packID;
        String realName;
        HashMap<String, String> jsonObj = new HashMap<>();
        if (ftbPackID.length() <= 1)
        {
            try
            {
                packID = Integer.parseInt(curseID);
            } catch (NumberFormatException e)
            {
                packID = -1;
            }
            jsonObj.put("p", String.valueOf(packID));
        }
        else
        {
            jsonObj.put("p", ftbPackID);
            jsonObj.put("b", base64);
            MineTogetherCommon.base64 = base64;
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try
        {
            realName = gson.toJson(jsonObj);
            return realName;
        } catch (Exception e)
        {
            Sentry.captureException(e);
        }
        return "{\"p\": \"-1\"}";
    }

    public void updateFtbPackID()
    {
        File versions = new File(Platform.getGameFolder() + File.separator + "version.json");
        if (versions.exists())
        {
            try (InputStream stream = new FileInputStream(versions))
            {
                try
                {
                    JsonElement json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (json.isJsonObject())
                    {
                        JsonObject object = json.getAsJsonObject();
                        int versionID = object.getAsJsonPrimitive("id").getAsInt();
                        int ftbPackID = object.getAsJsonPrimitive("parent").getAsInt();

                        base64 = Base64.getEncoder().encodeToString((String.valueOf(ftbPackID) + String.valueOf(versionID)).getBytes());
                        requestedID = ChatCallbacks.getVersionFromApi(base64);
                        if (requestedID.isEmpty()) requestedID = "-1";

                        Config.instance.curseProjectID = requestedID;
                        Config.saveConfigToFile(MineTogetherCommon.configFile.toFile());
                        this.ftbPackID = "m" + ftbPackID;
                    }
                } catch (Exception MalformedJsonException)
                {
                    Sentry.captureException(MalformedJsonException);
                    MineTogetherCommon.logger.error("version.json is not valid returning to curse ID");
                }
            } catch (IOException e)
            {
                Sentry.captureException(e);
                MineTogetherCommon.logger.info("version.json not found returning to curse ID");
            }
        }
    }
}
