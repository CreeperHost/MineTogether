package net.creeperhost.minetogether.orderform.requests;

import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.lib.chat.request.v2.GetMeRequest;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import static net.creeperhost.minetogether.lib.web.WebConstants.CH_API;

/**
 * Created by brandon3055 on 24/06/2024
 */
public class GetDataCentresRequest extends ApiRequest<GetDataCentresRequest.Response> {

    public GetDataCentresRequest(int ram) {
        super("GET", CH_API + "api/datacentres?ram=" + ram, GetDataCentresRequest.Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response extends ApiResponse {
        @SerializedName ("datacentres")
        public List<DC> dataCentres = new ArrayList<>();
    }

    public static class DC {
        public String name;
        public String country;
        public String slug;
        public boolean available;
        public String latencyUrl;
        public String countryName;
    }
}
