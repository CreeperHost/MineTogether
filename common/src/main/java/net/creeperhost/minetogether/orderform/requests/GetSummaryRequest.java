package net.creeperhost.minetogether.orderform.requests;

import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.lib.web.ApiRequest;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetSummaryRequest extends ApiRequest<GetSummaryRequest.Response> {

    public GetSummaryRequest(String country, String product) {
        super("GET", CH + "json/order/" + country + "/" + product + "/summary", GetSummaryRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response {
        @SerializedName("0")
        public Option option0;
//        @SerializedName("1")
//        public Option option1;
//        @SerializedName("2")
//        public Option option2;
//        @SerializedName("3")
//        public Option option3;
//        @SerializedName("4")
//        public Option option4;
//        @SerializedName("5")
//        public Option option5;
    }

    public static class Option {
        @SerializedName("Name")
        public String name;
        @SerializedName("Total")
        public double total;
        @SerializedName("Subtotal")
        public double subtotal;
        @SerializedName("Currency")
        public String currency;
        @SerializedName("Tax")
        public double tax;
        @SerializedName("hasTax")
        public boolean hasTax;
        @SerializedName("Discount")
        public Double discount;
        @SerializedName("PreTax")
        public double preTax;
        @SerializedName("PreDiscount")
        public double preDiscount;
    }
}
