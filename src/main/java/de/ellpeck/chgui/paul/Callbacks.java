package de.ellpeck.chgui.paul;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.common.AvailableResult;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.compress.utils.IOUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

public final class Callbacks {
    public static Map<Integer, String> locations = new HashMap<Integer, String>();
    public static Map<Integer, String> getAllServerLocations(){
        Map<String, Integer> rawMap = new HashMap<String,Integer>();
        try {

            // This is going to be done a lot. Probably best in some kind of util method.
            URL url = new URL( "https://www.creeperhost.net/json/locations" );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder jsonData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                jsonData.append(line);
            }
            rd.close();

            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Gson g = new Gson();
            rawMap = g.fromJson(jsonData.toString(), type);
        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        for(Map.Entry<String, Integer> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            locations.put(value, Character.toUpperCase(key.charAt(0)) + key.substring(1));
        }
        return locations;
    }

    //Not called yet, but you should process the order here
    public static void onOrderComplete(Order order){

    }

    public static AvailableResult getNameAvailable(String name) {
        try
        {
            String result = Util.getWebResponse("https://www.creeperhost.net/json/availability/" + name);
            JsonElement jElement = new JsonParser().parse(result);
            JsonObject jObject = jElement.getAsJsonObject();
            String status = jObject.getAsJsonPrimitive("status").getAsString();
            boolean statusBool = status.equals("success");
            String message = jObject.getAsJsonPrimitive("message").getAsString();

            return new AvailableResult(statusBool, message);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return new AvailableResult(false, "unknown");
    }

    public static String getUserCountry() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        ResourceLocation geoipRes = new ResourceLocation("creeperhostigs", "GeoLite2-Country.mmdb");

        try {

            String ip = Util.getWebResponse("https://api.ipify.org");

            InputStream geoip = Minecraft.getMinecraft().getResourceManager().getResource(geoipRes).getInputStream();
            DatabaseReader reader = new DatabaseReader.Builder(geoip).build();

            InetAddress ipAddress = InetAddress.getByName(ip);

            CountryResponse response = reader.country(ipAddress);

            Country country = response.getCountry();
            return country.getIsoCode();
        } catch (Throwable t) {
        }
        return "US"; // default
    }

    public static OrderSummary getSummary(Order order) {

        String url = "https://www.creeperhost.net/json/order/mc/" + order.version + "/recommend/" + order.playerAmount;

        String resp = Util.getWebResponse(url);

        JsonElement jElement = new JsonParser().parse(resp);

        JsonObject jObject = jElement.getAsJsonObject();
        String recommended = jObject.getAsJsonPrimitive("recommended").getAsString();

        System.out.println(recommended);

        String applyPromo = Util.getWebResponse("https://www.creeperhost.net/applyPromo/" + order.promo);

        System.out.println(applyPromo);

        String summary = Util.getWebResponse("https://www.creeperhost.net/json/order/" + order.country + "/" + recommended + "/" + "summary");

        System.out.println(summary);

        String currency = Util.getWebResponse("https://www.creeperhost.net/json/currency/" + order.country);

        System.out.println(currency);

        String product = Util.getWebResponse("https://www.creeperhost.net/json/products/" + recommended);

        System.out.println(product);


        jElement = new JsonParser().parse(product);

        jObject = jElement.getAsJsonObject();
        String vpsDisplay = jObject.getAsJsonPrimitive("displayName").getAsString();

        // We could parse the json, but no need - this gets us what we want.
        String patternStr = "<li>(.*?)<";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(product);

        ArrayList<String> vpsFeatures = new ArrayList<String>();

        while (matcher.find()) {
            String group = matcher.group(1);
            vpsFeatures.add(group);
        }

        return new OrderSummary(vpsDisplay, vpsFeatures);
    }
}