package net.creeperhost.minetogether.util;

import com.google.gson.annotations.SerializedName;
import net.creeperhost.minetogether.lib.web.ApiRequest;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.creeperhost.minetogether.lib.web.WebConstants.CH;

/**
 * Created by covers1624 on 25/10/22.
 */
public class GetClosestDCRequest extends ApiRequest<GetClosestDCRequest.Response> {

    public GetClosestDCRequest() {
        super("GET", CH + "json/datacentre/closest", Response.class);
        requiredAuthHeaders.add("Fingerprint");
        requiredAuthHeaders.add("Identifier");
    }

    public static class Response extends ApiResponse {

        @Nullable
        @SerializedName ("datacentre")
        private DataCenter dataCentre;
        @Nullable
        private Customer customer;
        @SerializedName ("datacentres")
        private List<DataCenter> dataCenters = new LinkedList<>();

        // @formatter:off
        public DataCenter getDataCenter() { return requireNonNull(dataCentre); }
        public Customer getCustomer() { return requireNonNull(customer); }
        public List<DataCenter> getDataCenters() { return dataCenters; }
        // @formatter:on
    }

    public static class DataCenter {

        @Nullable
        private String name;
        @Nullable
        private Long distance;
        @Nullable
        private String longitude;
        @Nullable
        private String latitude;

        // @formatter:off
        public String getName() { return requireNonNull(name); }
        public Long getDistance() { return requireNonNull(distance); }
        public String getLongitude() { return requireNonNull(longitude); }
        public String getLatitude() { return requireNonNull(latitude); }
        // @formatter:on
    }

    public static class Customer {

        @Nullable
        private String country;
        @Nullable
        private String source;
        @Nullable
        private String longitude;
        @Nullable
        private String latitude;
        @Nullable
        private String ip;

        // @formatter:off
        public String getCountry() { return requireNonNull(country); }
        public String getSource() { return requireNonNull(source); }
        public String getLongitude() { return requireNonNull(longitude); }
        public String getLatitude() { return requireNonNull(latitude); }
        public String getIp() { return requireNonNull(ip); }
        // @formatter:on
    }
}
