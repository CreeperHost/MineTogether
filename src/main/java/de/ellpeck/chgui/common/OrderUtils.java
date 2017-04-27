package de.ellpeck.chgui.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Aaron on 26/04/2017.
 */
public class OrderUtils
{
    public static AvailableResult isNameAvailable(String name) {
        StringBuilder result = new StringBuilder();
        try
        {
            URL url = new URL("https://www.creeperhost.net/json/availability/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null)
            {
                result.append(line);
            }
            rd.close();

            JsonElement jElement = new JsonParser().parse(String.valueOf(result));
            JsonObject jObject = jElement.getAsJsonObject();
            String status = jObject.getAsJsonPrimitive("status").getAsString();
            boolean statusBool = status.equals("success");
            String message = "";
            message = jObject.getAsJsonPrimitive("message").getAsString();

            return new AvailableResult(statusBool, message);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return new AvailableResult(false, "unknown");
    }
}
