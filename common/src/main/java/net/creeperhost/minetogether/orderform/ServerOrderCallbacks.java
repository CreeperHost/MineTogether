package net.creeperhost.minetogether.orderform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.util.ModPackInfo;
import net.minecraft.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

// TODO, This needs to be replaced with the new API request system.
@Deprecated
public class ServerOrderCallbacks {

    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean doesEmailExist(final String email) {
        try {
            //TODO Figure out how to do form url encoding with new request system.
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/exists", new HashMap<String, String>() {{
                put("email", email);
            }});

            if (response.equals("error")) {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error")) {
                    return true; //"Error" status means email "does" exist
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Unable to check if email exists", t);
            return false;
        }
        return false;
    }

    public static String doLogin(final String username, final String password) {
        try {
            //TODO Figure out how to do form url encoding with new request system.
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/login", new HashMap<String, String>() {{
                put("email", username);
                put("password", password);
            }});

            if (response.equals("error")) {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error")) {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                } else {
                    return "success:" + jObject.getAsJsonPrimitive("currency").getAsString() + ":" + jObject.getAsJsonPrimitive("userid").getAsString();
                }
            }
            return "Unknown Error";
        } catch (Throwable t) {
            LOGGER.error("Unable to do login", t);
            return "Unknown Error";
        }
    }

    public static String createOrder(final Order order, String dcId, String pregen, String fallbackName) {
        String response = null;
        try {
            //TODO Figure out how to do form url encoding with new request system.
            response = WebUtils.postWebResponse("https://www.creeperhost.net/json/order/" + order.clientID + "/" + order.productID + "/" + dcId, new HashMap<>() {{
                put("name", order.name);
                put("swid", ModPackInfo.getInfo().websiteID);
                if (order.pregen) put("pregen", pregen);
                if (!StringUtil.isNullOrEmpty(order.worldUrl)) put("worldUrl", order.worldUrl);
                if (!StringUtil.isNullOrEmpty(fallbackName)) put("fallback", fallbackName);
            }});

            if (response.equals("error")) {

            } else {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("success")) {
                    jObject = jObject.getAsJsonObject("more");
                    return "success:" + jObject.getAsJsonPrimitive("invoiceid").getAsString() + ":" + jObject.getAsJsonPrimitive("orderid").getAsString();
                } else {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                }
            }
            return "Unknown error";
        } catch (Throwable t) {
            LOGGER.error("Unable to create order", t);
            LOGGER.error("Server Response:\n{}", response);
            return "Unknown error";
        }
    }

    public static String createAccount(final Order order) {
        try {
            //TODO Figure out how to do form url encoding with new request system.
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/account/create", new HashMap<String, String>() {{
                put("servername", order.name);
                put("modpack", ModPackInfo.getInfo().curseID);
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
            if (response.equals("error")) {
                // Something went wrong, so lets just pretend everything fine and don't change the validation status
            } else {
                JsonElement jElement = new JsonParser().parse(response);
                JsonObject jObject = jElement.getAsJsonObject();
                if (jObject.getAsJsonPrimitive("status").getAsString().equals("error")) {
                    return jObject.getAsJsonPrimitive("message").getAsString();
                } else {
                    return "success:" + jObject.getAsJsonPrimitive("currency").getAsString() + ":" + jObject.getAsJsonPrimitive("userid").getAsString();
                }
            }
            return "Unknown error";
        } catch (Throwable t) {
            LOGGER.error("Unable to create account", t);
            return "Unknown error";
        }
    }

    public static String getPaymentLink(String invoiceID) {
        return "https://billing.creeperhost.net/viewinvoice.php?id=" + invoiceID;
    }
}
