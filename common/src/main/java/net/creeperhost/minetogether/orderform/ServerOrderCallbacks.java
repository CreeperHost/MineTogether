package net.creeperhost.minetogether.orderform;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.orderform.data.AvailableResult;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.data.OrderSummary;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO, This needs to be replaced with the new API request system.
@Deprecated
public class ServerOrderCallbacks {

    private static final Logger LOGGER = LogManager.getLogger();

    public static AvailableResult getNameAvailable(String name) {
        try {
            String result = WebUtils.getWebResponse("https://www.creeperhost.net/json/availability/" + name);
            JsonElement jElement = new JsonParser().parse(result);
            JsonObject jObject = jElement.getAsJsonObject();
            String status = jObject.getAsJsonPrimitive("status").getAsString();
            boolean statusBool = status.equals("success");
            String message = jObject.getAsJsonPrimitive("message").getAsString();

            return new AvailableResult(statusBool, message);
        } catch (Throwable t) {
            LOGGER.error("Unable to check if name available", t);
        }

        return new AvailableResult(false, "unknown");
    }

    public static OrderSummary getSummary(Order order, String promo) {
        if (order.country.isEmpty()) {
            order.country = Countries.getOurCountry();
        }

        if (order.serverLocation.isEmpty()) {
            order.serverLocation = getRecommendedLocation();
        }

        try {
            String version = "0";
            if (!ModPackInfo.getInfo().curseID.isEmpty()) {
                version = ModPackInfo.getInfo().curseID;
            }
            String url = "https://www.creeperhost.net/json/order/mc/" + version + "/recommend/" + order.playerAmount;

            String resp = WebUtils.getWebResponse(url);

            JsonElement jElement = new JsonParser().parse(resp);

            JsonObject jObject = jElement.getAsJsonObject();
            String recommended = jObject.getAsJsonPrimitive("recommended").getAsString();

            if (StringUtils.isNotEmpty(promo) && !promo.equalsIgnoreCase("Insert Promo Code here")) {
                WebUtils.getWebResponse("https://www.creeperhost.net/applyPromo/" + promo);
            }

            String summary = WebUtils.getWebResponse("https://www.creeperhost.net/json/order/" + order.country + "/" + recommended + "/" + "summary");

            jElement = new JsonParser().parse(summary);

            jObject = jElement.getAsJsonObject();
            jObject = jObject.getAsJsonObject("0");
            double preDiscount = jObject.getAsJsonPrimitive("PreDiscount").getAsDouble();
            double subTotal = jObject.getAsJsonPrimitive("Subtotal").getAsDouble();
            double discount;
            try {
                discount = jObject.getAsJsonPrimitive("Discount").getAsDouble();
            } catch (Exception e) {
                discount = 0;
            }
            double tax = jObject.getAsJsonPrimitive("Tax").getAsDouble();
            if (tax <= 0) {
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

            while (matcher.find()) {
                String group = matcher.group(1);
                vpsFeatures.add(group);
            }

            List<String> vpsIncluded = new ArrayList<String>();
            vpsIncluded.add("minetogether.quote.vpsincluded1");
            vpsIncluded.add("minetogether.quote.vpsincluded2");
            vpsIncluded.add("minetogether.quote.vpsincluded3");
            vpsIncluded.add("minetogether.quote.vpsincluded4");
            vpsIncluded.add("minetogether.quote.vpsincluded5");
            vpsIncluded.add("minetogether.quote.vpsincluded6");
            vpsIncluded.add("minetogether.quote.vpsincluded7");

            return new OrderSummary(recommended, vpsDisplay, vpsFeatures, vpsIncluded, preDiscount, subTotal, total, tax, discount, suffix, prefix, id);

        } catch (Throwable t) {
            LOGGER.error("Unable to fetch summary", t);
            return new OrderSummary("Unable to fetch summary");
        }
    }

    public static String getRecommendedLocation() {
        try {
            String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");

            JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();

            jObject = jObject.getAsJsonObject("datacentre");

            return jObject.getAsJsonPrimitive("name").getAsString();
        } catch (Throwable ignored) {
        }
        return ""; // default
    }

    public static Map<String, String> getRegionMap() {
        Map<String, String> rawMap = new HashMap<String, String>();
        Map<String, String> returnMap = new HashMap<String, String>();

        try {
            String jsonData = WebUtils.getWebResponse("https://www.creeperhost.net/json/locations");

            Type type = new TypeToken<Map<String, String>>() { }.getType();
            Gson g = new Gson();
            JsonElement el = new JsonParser().parse(jsonData);
            rawMap = g.fromJson(el.getAsJsonObject().get("regionMap"), type);
        } catch (Exception e) {
            LOGGER.error("Unable to fetch server locations", e);
        }
        for (Map.Entry<String, String> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            returnMap.put(key, value);
        }
        return returnMap;
    }

    public static Map<String, Integer> getDataCentres() throws IOException, URISyntaxException {
        String url = "https://www.creeperhost.net/json/datacentre/closest";
        String resp = WebUtils.getWebResponse(url);
        Map<String, Integer> map = new HashMap<>();

        JsonElement jElement = new JsonParser().parse(resp);

        if (jElement.isJsonObject()) {
            JsonArray array = jElement.getAsJsonObject().getAsJsonArray("datacentres");

            if (array != null) {
                for (JsonElement serverEl : array) {
                    JsonObject object = (JsonObject) serverEl;
                    String name = object.get("name").getAsString();
                    String distance = object.get("distance").getAsString();
                    try {
                        map.put(name, Integer.parseInt(distance));
                    } catch (NumberFormatException ignored) {
                        map.put(name, -1);
                    }
                }
                return map;
            }
        }
        return null;
    }

    public static Map<String, String> getDataCentreURLs() throws IOException, URISyntaxException {
        String url = "https://api.creeper.host/api/datacentres";
        String resp = WebUtils.getWebResponse(url);
        Map<String, String> map = new HashMap<>();

        JsonElement jElement = new JsonParser().parse(resp);
        if (jElement.isJsonObject()) {
            JsonArray array = jElement.getAsJsonObject().getAsJsonArray("datacentres");
            if (array != null) {
                for (JsonElement serverEl : array) {
                    JsonObject object = (JsonObject) serverEl;
                    String name = object.get("slug").getAsString();
                    String latencyURL = object.get("latencyUrl").getAsString();
                    map.put(name, latencyURL);
                }
                return map;
            }
        }
        return null;
    }

    public static int getDataCentreLatency(String latencyUrl, int distance) throws IOException {
        String resp = WebUtils.getWebResponse(latencyUrl);
        JsonElement jElement = new JsonParser().parse(resp);

        if (jElement.isJsonObject()) {
            JsonObject obj = jElement.getAsJsonObject();
            if ("success".equals(obj.get("status").getAsString()) && obj.has("latency")) {
                double latency = obj.get("latency").getAsDouble();
                double milesPerSecond = 124188; //This is the miles per second value for light using the average refractive index of single mode fibre.
                double minMs = ((distance / milesPerSecond) * 1000) * 1.7; //Figure used against real world RTT time to get close.
                double maxMs = ((distance / milesPerSecond) * 1000) * 5.8; //Figure used against real world RTT time to get close.
                if (latency < minMs) latency = Math.round(minMs);
                if (latency > maxMs) return -2; //Won't retry will just consider this a fail. latency is updated every 10 seconds, so it will retry eventually.
                return (int) latency;
            }
        }
        return -1;
    }

    public static boolean doesEmailExist(final String email) {
        try {
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

    public static String createOrder(final Order order, String pregen) {
        try {
            String response = WebUtils.postWebResponse("https://www.creeperhost.net/json/order/" + order.clientID + "/" + order.productID + "/" + order.serverLocation, new HashMap<String, String>() {{
                put("name", order.name);
                put("swid", ModPackInfo.getInfo().curseID);
                if (order.pregen) { put("pregen", pregen); }
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
            return "Unknown error";
        }
    }

    public static String createAccount(final Order order) {
        try {
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

    public static boolean cancelOrder(int orderNum) {
        try {
            String response = WebUtils.getWebResponse("https://www.creeperhost.net/json/order/" + orderNum + "/cancel");
        } catch (Throwable t) {
            LOGGER.error("Unable to cancel order", t);
            return false;
        }
        return true;
    }
}
