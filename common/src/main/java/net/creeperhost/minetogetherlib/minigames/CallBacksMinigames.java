package net.creeperhost.minetogether.minetogetherlib.minigames;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.minetogetherlib.util.WebUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallBacksMinigames
{
    public static ArrayList<Minigame> getMinigames(boolean isModded, String minecraftVersion, String curseProjectID)
    {

        Map<String, String> sendMap = new HashMap<>();
        {
            sendMap.put("mc", minecraftVersion);
            sendMap.put("project", isModded ? curseProjectID : String.valueOf(0));
        }

        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/mgtemplates", new Gson().toJson(sendMap), true, false);

        JsonParser parser = new JsonParser();

        Gson gson = new Gson();

        JsonElement parse = parser.parse(resp);
        if (parse.isJsonObject())
        {
            JsonObject obj = parse.getAsJsonObject();
            if (obj.get("status").getAsString().equals("success"))
            {
                return gson.fromJson(obj.get("templates"), new TypeToken<List<Minigame>>() {}.getType());
            }
        }

        return null;
    }
}
