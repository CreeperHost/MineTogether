package net.creeperhost.minetogetherlib.serverorder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogetherlib.util.WebUtils;

public class ServerOrderCallbacks
{
    public static AvailableResult getNameAvailable(String name)
    {
        try
        {
            String result = WebUtils.getWebResponse("https://www.creeperhost.net/json/availability/" + name);
            JsonElement jElement = new JsonParser().parse(result);
            JsonObject jObject = jElement.getAsJsonObject();
            String status = jObject.getAsJsonPrimitive("status").getAsString();
            boolean statusBool = status.equals("success");
            String message = jObject.getAsJsonPrimitive("message").getAsString();

            return new AvailableResult(statusBool, message);
        } catch (Throwable t)
        {
            MineTogether.logger.error("Unable to check if name available", t);
        }

        return new AvailableResult(false, "unknown");
    }
}
