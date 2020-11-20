package net.creeperhost.minetogether.trade;

import com.google.gson.*;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudUtils
{

    //addtradable
    //removetradale
    //listtradable
    //iliketrains

    //json send hash(fullhash), item, quantity
    public static boolean sendStack(EntityPlayer player, ItemStack itemStack)
    {
        String itemName = itemStack.getItem().getRegistryName().toString();
        String playerHash = getHashFromPlayer(player);
        int amount = itemStack.getCount();
        System.out.println(itemName);
        System.out.println(playerHash);
        if(itemName.isEmpty() && playerHash.isEmpty()) return false;

        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", playerHash);
            sendMap.put("item", itemName);
            sendMap.put("quantity", String.valueOf(amount));
        }

        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/addtradable", sendStr, true, false);
//        System.out.println(resp);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);

        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();

            if (obj.get("status").getAsString().equals("success"))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean removeStack(EntityPlayer player, ItemStack itemStack)
    {
        String itemName = itemStack.getItem().getRegistryName().toString();
        String playerHash = getHashFromPlayer(player);
        int amount = itemStack.getCount();
        if(itemName.isEmpty() && playerHash.isEmpty()) return false;

        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", playerHash);
            sendMap.put("item", itemName);
            sendMap.put("quantity", String.valueOf(amount));
        }

        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/removetradable", sendStr, true, false);
//        System.out.println(resp);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);

        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();

            if (obj.get("status").getAsString().equals("success"))
            {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> listStacks(EntityPlayer player)
    {
        String playerHash = getHashFromPlayer(player);

        Map<String, String> sendMap = new HashMap<String, String>();
        {
            sendMap.put("hash", playerHash);
        }

        Gson gson = new Gson();
        String sendStr = gson.toJson(sendMap);
        String resp = WebUtils.putWebResponse("https://api.creeper.host/minetogether/listtradable", sendStr, true, false);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(resp);
        List<ItemStack> stackList = new ArrayList<>();

        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();

            if (obj.get("status").getAsString().equals("success"))
            {
                JsonArray jsonArray = obj.getAsJsonArray("inventory");
                if(jsonArray != null)
                {
                    for (JsonElement jsonElement : jsonArray)
                    {
                        JsonObject object = (JsonObject) jsonElement;
                        String name = object.get("name").getAsString();
                        int quantity = object.get("quantity").getAsInt();
                        stackList.add(getStackFromName(name, quantity));
//                        System.out.println(jsonElement);
                    }
                }
            }
        }
        return stackList;
    }

    public static ItemStack getStackFromName(String name, int amount)
    {
        if(Item.getByNameOrId(name) == null) return ItemStack.EMPTY;
        return new ItemStack(Item.getByNameOrId(name), amount);
    }

    public static String getHashFromPlayer(EntityPlayer player)
    {
        return Callbacks.getPlayerHash(player.getUniqueID());
    }
}
