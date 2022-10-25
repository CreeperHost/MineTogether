package net.creeperhost.minetogether.util;

import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by covers1624 on 25/10/22.
 */
public class GetCountryRequest extends ApiRequest<GetCountryRequest.Response> {

    public GetCountryRequest() {
        super("GET", CH + "json/datacentre/closest", Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response extends ApiResponse {

        public Customer customer = new Customer();

        public static class Customer {
            public String country = "US";
        }
    }
}
