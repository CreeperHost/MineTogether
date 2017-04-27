package de.ellpeck.chgui.paul;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

public final class Callbacks {
    public static Map<Integer, String> locations = new HashMap<Integer, String>();
    public static Map<Integer, String> getAllServerLocations(){
        Map<String, Integer> rawMap = new HashMap<String,Integer>();
        InputStream in;
        try {
            in = new URL( "https://www.creeperhost.net/json/locations" ).openStream();
            String jsonData = "";
            byte[] tmp = IOUtils.toByteArray(in);
            jsonData = new String(tmp);
            IOUtils.closeQuietly(in);
            Gson g = new Gson();
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            rawMap = g.fromJson(jsonData, type);
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
}