package de.ellpeck.chgui.paul;

import java.util.HashMap;
import java.util.Map;

public final class Callbacks{

    public static Map<Integer, String> getAllServerLocations(){
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(0, "Test City");
        map.put(1, "Tester City");
        map.put(2, "Testest City");
        return map;
    }

    //Not called yet, but you should process the order here
    public static void onOrderComplete(Order order){

    }
}
