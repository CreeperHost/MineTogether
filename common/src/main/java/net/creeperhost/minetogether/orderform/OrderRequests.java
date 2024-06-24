package net.creeperhost.minetogether.orderform;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.data.OrderSummary;
import net.creeperhost.minetogether.orderform.requests.*;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.minetogether.util.GetClosestDCRequest;
import net.creeperhost.minetogether.util.ModPackInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class OrderRequests {

    private static final Logger LOGGER = LogManager.getLogger();

    public static List<GetDataCentresRequest.DC> getDataCenters(int ram) {
        try {
            GetDataCentresRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetDataCentresRequest(ram)).apiResponse();
            if (!response.getStatus().equals("success")) {
                LOGGER.error("Failed to retrieve datacenter list. Api returned: {}", response.getMessageOrNull());
                return Collections.emptyList();
            }
            return response.dataCentres != null ? response.dataCentres : Collections.emptyList();
        } catch (Throwable e) {
            LOGGER.error("Failed to retrieve datacenter list", e);
            return Collections.emptyList();
        }
    }

    public static Map<String, Integer> getLocations() {
        try {
            GetLocationsRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetLocationsRequest()).apiResponse();
            return response.locMap != null ? response.locMap : Collections.emptyMap();
        } catch (Throwable e) {
            LOGGER.error("Failed to retrieve location list", e);
            return Collections.emptyMap();
        }
    }

    public static GetClosestDCRequest.Response getDCsByDistance() {
        try {
            return MineTogetherChat.CHAT_STATE.api.execute(new GetClosestDCRequest()).apiResponse();
        } catch (Throwable e) {
            LOGGER.error("Failed to retrieve DCs by distance", e);
            return null;
        }
    }

    public static int getDCLatency(String latencyUrl, long distance) {
        try {
            GetLatencyRequest.Response response = MineTogetherChat.CHAT_STATE.api.execute(new GetLatencyRequest(latencyUrl)).apiResponse();
            if (!response.getStatus().equals("success")) {
                LOGGER.error("Failed to check DC Latency. Api returned: {}", response.getMessageOrNull());
                return -1;
            }

            double latency = response.latency;
            double milesPerSecond = 124188; //This is the miles per second value for light using the average refractive index of single mode fibre.
            double minMs = ((distance / milesPerSecond) * 1000) * 1.7; //Figure used against real world RTT time to get close.
            if (latency < minMs) latency = Math.round(minMs);
            return (int) Math.max(latency, 1); //Set hard minimum of 1ms latency.
        } catch (Throwable e) {
            LOGGER.error("Failed to check DC Latency", e);
            return -1;
        }
    }

    public static OrderSummary getSummary(Order order, String promo) {
        if (order.country.isEmpty()) order.country = Countries.getOurCountry();
        if (order.serverLocation.isEmpty()) {
            order.serverLocation = getDCsByDistance().getDataCenter().getName();
        }

        String version = ModPackInfo.getInfo().curseID;
        if (version.isEmpty()) version = "0";

        try {
            var recResponse = MineTogetherChat.CHAT_STATE.api.execute(new GetRecommendRequest(version, order.playerAmount)).apiResponse();
            String recommended = String.valueOf(recResponse.recommended);
            if (StringUtils.isNotEmpty(promo) && !promo.equalsIgnoreCase("Insert Promo Code here")) {
                WebUtils.getWebResponse("https://www.creeperhost.net/applyPromo/" + promo);
            }

            var summary = MineTogetherChat.CHAT_STATE.api.execute(new GetSummaryRequest(order.country, recommended)).apiResponse();
            var op0 = summary.option0;
            double preDiscount = op0.preDiscount;
            double subTotal = op0.subtotal;
            double discount = op0.discount == null ? 0 : op0.discount;
            double tax = op0.tax;
            if (tax <= 0) tax = 0.0;
            double total = op0.total;

            var currencyResponse = MineTogetherChat.CHAT_STATE.api.execute(new GetCurrencyRequest(order.country)).apiResponse();
            String prefix = currencyResponse.prefix;
            String suffix = currencyResponse.suffix;
            String id = currencyResponse.id;

            var productResponse = MineTogetherChat.CHAT_STATE.api.execute(new GetProductRequest(recommended)).apiResponse();
            String vpsDisplay = productResponse.displayName;
            String vpsDescription = productResponse.description;
            String patternStr = "<li>(.*?)<";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(vpsDescription);

            ArrayList<String> vpsFeatures = new ArrayList<>();
            while (matcher.find()) {
                String group = matcher.group(1);
                vpsFeatures.add(group);
            }

            List<String> vpsIncluded = new ArrayList<>();
            vpsIncluded.add("minetogether.quote.vpsincluded1");
            vpsIncluded.add("minetogether.quote.vpsincluded2");
            vpsIncluded.add("minetogether.quote.vpsincluded3");
            vpsIncluded.add("minetogether.quote.vpsincluded4");
            vpsIncluded.add("minetogether.quote.vpsincluded5");
            vpsIncluded.add("minetogether.quote.vpsincluded6");
            vpsIncluded.add("minetogether.quote.vpsincluded7");

            return new OrderSummary(recommended, vpsDisplay, vpsFeatures, vpsIncluded, preDiscount, subTotal, total, tax, discount, suffix, prefix, id, recResponse.ram);
        } catch (Throwable e) {
            LOGGER.error("Unable to fetch summary", e);
            return new OrderSummary("Unable to fetch summary");
        }
    }

    public static ApiResponse getNameAvailable(String name) {
        try {
            return MineTogetherChat.CHAT_STATE.api.execute(new GetNameAvailableRequest(name)).apiResponse();
        } catch (Throwable e) {
            LOGGER.error("Failed to retrieve datacenter list", e);
            return new ApiResponse("error", "unknown");
        }
    }
}
