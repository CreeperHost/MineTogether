package net.creeperhost.minetogether.verification;

import com.google.gson.*;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;

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
        if(ftbPackID.length() <= 1)
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
            MineTogether.base64 = base64;
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try
        {
            realName = gson.toJson(jsonObj);
            return realName;
        } catch (Exception ignored) {}
        return "{\"p\": \"-1\"}";
    }

    public void updateFtbPackID()
    {
        File versions = new File(Platform.getGameFolder() + File.separator + "version.json");
        if(versions.exists())
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
                        Config.saveConfig();
                        this.ftbPackID = "m" + ftbPackID;
                    }
                } catch (Exception MalformedJsonException)
                {
                    MineTogether.logger.error("version.json is not valid returning to curse ID");
                }
            } catch (IOException ignored)
            {
                MineTogether.logger.info("version.json not found returning to curse ID");
            }
        }
    }
}
