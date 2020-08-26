package net.creeperhost.minetogether.paul;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.AvailableResult;
import net.creeperhost.minetogether.api.IServerHost;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.api.OrderSummary;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.WebUtils;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aaron on 09/05/2017.
 */
public class CreeperHostServerHost implements IServerHost
{
    public static Map<String, String> locations = new HashMap<String, String>();
    
    private ResourceLocation buttonIcon = new ResourceLocation("creeperhost", "textures/gui.png");
    private ResourceLocation menuIcon = new ResourceLocation("creeperhost", "textures/creeperhost.png");
    
    @Override
    public ResourceLocation getButtonIcon()
    {
        return buttonIcon;
    }
    
    @Override
    public ResourceLocation getMenuIcon()
    {
        return menuIcon;
    }
    
    @Override
    public Map<String, String> getAllServerLocations()
    {
        Map<String, String> rawMap = new HashMap<String, String>();
        try
        {
            String jsonData = WebUtils.getWebResponse("https://www.creeperhost.net/json/locations");
            
            Type type = new TypeToken<Map<String, String>>()
            {
            }.getType();
            Gson g = new Gson();
            JsonElement el = new JsonParser().parse(jsonData);
            rawMap = g.fromJson(el.getAsJsonObject().get("nameMap"), type);
        } catch (Exception e)
        {
            CreeperHost.logger.error("Unable to fetch server locations" + e);
            locations.put("no", "Unable to fetch server locations");
        }
        for (Map.Entry<String, String> entry : rawMap.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            locations.put(key, value);
        }
        return locations;
    }
    
    @Override
    public OrderSummary getSummary(Order order)
    {
        if (order.country.isEmpty())
        {
            order.country = Callbacks.getUserCountry();
        }
        
        if (order.serverLocation.isEmpty())
        {
            order.serverLocation = Callbacks.getRecommendedLocation();
        }
        
        try
        {
            String version = Config.getInstance().getVersion();
            if(!CreeperHost.instance.ftbPackID.isEmpty())
            {
                Config.getInstance().setVersion(CreeperHost.instance.requestedID);
            }
            String url = "https://www.creeperhost.net/json/order/mc/" + version + "/recommend/" + order.playerAmount;
            
            String resp = WebUtils.getWebResponse(url);


            String applyPromo = WebUtils.getWebResponse("https://www.creeperhost.net/applyPromo/" + Config.getInstance().getPromo());
//            if (applyPromo.equals("error"))
//            {
//                return new OrderSummary("quote.promoerror");
//            }
            JsonElement jElement = new JsonParser().parse(resp);
            
            JsonObject jObject = jElement.getAsJsonObject();
            String recommended = jObject.getAsJsonPrimitive("recommended").getAsString();
            
            String summary = WebUtils.getWebResponse("https://www.creeperhost.net/json/order/" + order.country + "/" + recommended + "/" + "summary");
            
            jElement = new JsonParser().parse(summary);
            
            jObject = jElement.getAsJsonObject();
            jObject = jObject.getAsJsonObject("0");
            double preDiscount = jObject.getAsJsonPrimitive("PreDiscount").getAsDouble();
            double subTotal = jObject.getAsJsonPrimitive("Subtotal").getAsDouble();
            double discount;
            try
            {
                discount = jObject.getAsJsonPrimitive("Discount").getAsDouble();
            }
            catch (Exception e)
            {
                discount = 0;
            }
            double tax = jObject.getAsJsonPrimitive("Tax").getAsDouble();
            if (tax <= 0)
            {
                tax = 0.00;
            }
            double total = jObject.getAsJsonPrimitive("Total").getAsDouble();
            
            String currency = WebUtils.getWebResponse("https://www.creeperhost.net/json/currency/" + order.country);
            
            jElement = new JsonParser().parse(currency);
            
            jObject = jElement.getAsJsonObject();
            String prefix = jObject.getAsJsonPrimitive("prefix").getAsString();
            String suffix = jObject.getAsJsonPrimitive("suffix").getAsString();
            String id = jObject.getAsJsonPrimitive("id").getAsString();
            
            String product = WebUtils.getWebResponse("https://www.creeperhost.net/json/products/" + recommended);
            
            jElement = new JsonParser().parse(product);
            
            jObject = jElement.getAsJsonObject();
            String vpsDisplay = jObject.getAsJsonPrimitive("displayName").getAsString();
            
            String vpsDescription = jObject.getAsJsonPrimitive("description").getAsString();
            
            String patternStr = "<li>(.*?)<";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(vpsDescription);
            
            ArrayList<String> vpsFeatures = new ArrayList<String>();
            
            while (matcher.find())
            {
                String group = matcher.group(1);
                vpsFeatures.add(group);
            }
            
            List<String> vpsIncluded = new ArrayList<String>();
            vpsIncluded.add(Util.localize("quote.vpsincluded1"));
            vpsIncluded.add(Util.localize("quote.vpsincluded2"));
            vpsIncluded.add(Util.localize("quote.vpsincluded3"));
            vpsIncluded.add(Util.localize("quote.vpsincluded4"));
            vpsIncluded.add(Util.localize("quote.vpsincluded5"));
            vpsIncluded.add(Util.localize("quote.vpsincluded6"));
            vpsIncluded.add(Util.localize("quote.vpsincluded7"));
            
            return new OrderSummary(recommended, vpsDisplay, vpsFeatures, vpsIncluded, preDiscount, subTotal, total, tax, discount, suffix, prefix, id);
            
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to fetch summary", t);
            return null;
        }
        
    }
    
    @Override
    public AvailableResult getNameAvailable(String name)
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
            CreeperHost.logger.error("Unable to check if name available", t);
        }
        
        return new AvailableResult(false, "unknown");
    }
    
    @Override
    public boolean doesEmailExist(final String email)
    {
        try
        {
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/exists", new HashMap<String, String>()
            {{
                put("email", email);
            }});
            
            if (response.equals("error"))
            {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else
            {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error"))
                {
                    return false;
                }
            }
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to check if email exists", t);
            return false;
        }
        return true;
    }
    
    @Override
    public String doLogin(final String username, final String password)
    {
        try
        {
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/login", new HashMap<String, String>()
            {{
                put("email", username);
                put("password", password);
            }});
            
            if (response.equals("error"))
            {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else
            {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error"))
                {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                } else
                {
                    return "success:" + jObject.getAsJsonPrimitive("currency").getAsString() + ":" + jObject.getAsJsonPrimitive("userid").getAsString();
                }
            }
            return "Unknown Error";
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to do login", t);
            return "Unknown Error";
        }
    }
    
    @Override
    public String createAccount(final Order order)
    {
        try
        {
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/create", new HashMap<String, String>()
            {{
                put("servername", order.name);
                put("modpack", Config.getInstance().getVersion());
                put("email", order.emailAddress);
                put("password", order.password);
                put("fname", order.firstName);
                put("lname", order.lastName);
                put("addr1", order.address);
                put("city", order.city);
                put("tel", order.phone);
                put("county", order.state);
                put("state", order.state);
                put("country", order.country);
                put("pcode", order.zip);
                put("currency", order.currency);
            }});
            if (response.equals("error"))
            {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else
            {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error"))
                {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                } else
                {
                    return "success:" + jObject.getAsJsonPrimitive("currency").getAsString() + ":" + jObject.getAsJsonPrimitive("userid").getAsString();
                }
            }
            return "Unknown error";
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to create account", t);
            return "Unknown error";
        }
    }
    
    @Override
    public String createOrder(final Order order)
    {
        try
        {
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/order/" + order.clientID + "/" + order.productID + "/" + order.serverLocation, new HashMap<String, String>()
            {{
                put("name", order.name);
                put("swid", Config.getInstance().getVersion());
                if (order.pregen)
                    put("pregen", String.valueOf(Config.getInstance().getPregenDiameter()));
            }});
            
            if (response.equals("error"))
            {
            
            } else
            {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("success"))
                {
                    jObject = jObject.getAsJsonObject("more");
                    return "success:" + jObject.getAsJsonPrimitive("invoiceid").getAsString() + ":" + jObject.getAsJsonPrimitive("orderid").getAsString();
                } else
                {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                }
            }
            return "Unknown error";
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to create order");
            return "Unknown error";
        }
    }
    
    @Override
    public boolean cancelOrder(int orderNum)
    {
        try
        {
            String response = WebUtils.getWebResponse("https://www.creeperhost.net/json/order/" + orderNum + "/cancel");
        } catch (Throwable t)
        {
            CreeperHost.logger.error("Unable to cancel order");
            return false;
        }
        return true;
    }
    
    @Override
    public String getLocalizationRoot()
    {
        return "creeperhost";
    }
    
    @Override
    public String getPaymentLink(String invoiceID)
    {
        return "https://billing.creeperhost.net/viewinvoice.php?id=" + invoiceID;
    }
    
    @Override
    public ServerData getServerEntry(Order order)
    {
        return new ServerData(order.name + ".PlayAt.CH", order.name + ".playat.ch", false);
    }
    
    @Override
    public String getRecommendedLocation()
    {
        try
        {
            String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");
            
            JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();
            
            jObject = jObject.getAsJsonObject("datacentre");
            
            return jObject.getAsJsonPrimitive("name").getAsString();
        } catch (Throwable t)
        {
        }
        return ""; // default
    }
}
