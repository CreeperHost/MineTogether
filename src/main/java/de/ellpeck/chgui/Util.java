package de.ellpeck.chgui;

import net.creeperhost.ingamesale.IngameSale;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Util{

    private static List<String> cookies;

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(CreeperHostGui.MOD_ID,"textures/gui.png");

    public static String localize(String key, Object... format){
        return I18n.format(CreeperHostGui.MOD_ID+"."+key, format);
    }

    public static String getWebResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (cookies != null) {
                for (String cookie : cookies) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder respData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                respData.append(line);
            }

            List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

            if (setCookies != null) {
                cookies = setCookies;
            }

            rd.close();
            return respData.toString();
        } catch (Throwable t) {
            IngameSale.logger.warn("An error occurred while fetching " + urlString, t);
        }

        return "error";

    }

}
