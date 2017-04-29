package de.ellpeck.chgui.paul;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.common.AvailableResult;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

public final class Callbacks {
    public static Map<Integer, String> locations = new HashMap<Integer, String>();

    public static Map<Integer, String> getAllServerLocations(){
        Map<String, Integer> rawMap = new HashMap<String,Integer>();
        try {

            // This is going to be done a lot. Probably best in some kind of util method.
            String jsonData = Util.getWebResponse("https://www.creeperhost.net/json/locations");

            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Gson g = new Gson();
            rawMap = g.fromJson(jsonData.toString(), type);
        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        for(Map.Entry<String, Integer> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            locations.put(value, Character.toUpperCase(key.charAt(0)) + key.substring(1));
        }
        return locations;
    }

    public static Map<String, String> getCountries() {
        return countries;
    }

    //Not called yet, but you should process the order here
    public static void onOrderComplete(Order order){

    }

    public static AvailableResult getNameAvailable(String name) {
        try
        {
            String result = Util.getWebResponse("https://www.creeperhost.net/json/availability/" + name);
            JsonElement jElement = new JsonParser().parse(result);
            JsonObject jObject = jElement.getAsJsonObject();
            String status = jObject.getAsJsonPrimitive("status").getAsString();
            boolean statusBool = status.equals("success");
            String message = jObject.getAsJsonPrimitive("message").getAsString();

            return new AvailableResult(statusBool, message);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return new AvailableResult(false, "unknown");
    }

    public static String getUserCountry() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        ResourceLocation geoipRes = new ResourceLocation("chgui", "GeoLite2-Country.mmdb");

        try {

            String ip = Util.getWebResponse("https://api.ipify.org");

            InputStream geoip = Minecraft.getMinecraft().getResourceManager().getResource(geoipRes).getInputStream();
            DatabaseReader reader = new DatabaseReader.Builder(geoip).build();

            InetAddress ipAddress = InetAddress.getByName(ip);

            CountryResponse response = reader.country(ipAddress);

            Country country = response.getCountry();

            return country.getIsoCode();
        } catch (Throwable t) {
        }
        return "US"; // default
    }

    public static OrderSummary getSummary(Order order) {

        if (order.country == "") {
            order.country = getUserCountry();
        }

        String url = "https://www.creeperhost.net/json/order/mc/" + order.version + "/recommend/" + order.playerAmount;

        String resp = Util.getWebResponse(url);

        JsonElement jElement = new JsonParser().parse(resp);

        JsonObject jObject = jElement.getAsJsonObject();
        String recommended = jObject.getAsJsonPrimitive("recommended").getAsString();

        String applyPromo = Util.getWebResponse("https://www.creeperhost.net/applyPromo/" + order.promo);

        String summary = Util.getWebResponse("https://www.creeperhost.net/json/order/" + order.country + "/" + recommended + "/" + "summary");

        jElement = new JsonParser().parse(summary);

        jObject = jElement.getAsJsonObject();
        jObject = jObject.getAsJsonObject("0");
        double preDiscount = jObject.getAsJsonPrimitive("PreDiscount").getAsDouble();
        double subTotal = jObject.getAsJsonPrimitive("Subtotal").getAsDouble();
        double discount = jObject.getAsJsonPrimitive("Discount").getAsDouble();
        double tax = jObject.getAsJsonPrimitive("Tax").getAsDouble();
        double total = jObject.getAsJsonPrimitive("Total").getAsDouble();

        String currency = Util.getWebResponse("https://www.creeperhost.net/json/currency/" + order.country);

        jElement = new JsonParser().parse(currency);

        jObject = jElement.getAsJsonObject();
        String prefix = jObject.getAsJsonPrimitive("prefix").getAsString();
        String suffix = jObject.getAsJsonPrimitive("suffix").getAsString();

        String product = Util.getWebResponse("https://www.creeperhost.net/json/products/" + recommended);

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

        return new OrderSummary(vpsDisplay, vpsFeatures, preDiscount, subTotal, total, tax, discount, suffix, prefix);
    }


    private static Map<String, String> countries = new LinkedHashMap<String, String>() {{
        put("GB", "United Kingdom");
        put("US", "United States");
        put("NZ", "New Zealand");
        put("AU", "Australia");
        put("DK", "Denmark");
        put("CA", "Canada");
        put("SE", "Sweden");
        put("NO", "Norway");
        put("BR", "Brazil");
        put("ES", "Spain");
        put("AF", "Afghanistan");
        put("AX", "Aland Islands");
        put("AL", "Albania");
        put("DZ", "Algeria");
        put("AS", "American Samoa");
        put("AD", "Andorra");
        put("AO", "Angola");
        put("AI", "Anguilla");
        put("AQ", "Antarctica");
        put("AG", "Antigua and Barbuda");
        put("AR", "Argentina");
        put("AM", "Armenia");
        put("AW", "Aruba");
        put("AT", "Austria");
        put("AZ", "Azerbaijan");
        put("BS", "Bahamas");
        put("BH", "Bahrain");
        put("BD", "Bangladesh");
        put("BB", "Barbados");
        put("BY", "Belarus");
        put("BE", "Belgium");
        put("BZ", "Belize");
        put("BJ", "Benin");
        put("BM", "Bermuda");
        put("BT", "Bhutan");
        put("BO", "Bolivia, Plurinational State of");
        put("BQ", "Bonaire, Sint Eustatius and Saba");
        put("BA", "Bosnia and Herzegovina");
        put("BW", "Botswana");
        put("BV", "Bouvet Island");
        put("IO", "British Indian Ocean Territory");
        put("BN", "Brunei Darussalam");
        put("BG", "Bulgaria");
        put("BF", "Burkina Faso");
        put("BI", "Burundi");
        put("KH", "Cambodia");
        put("CM", "Cameroon");
        put("CV", "Cape Verde");
        put("KY", "Cayman Islands");
        put("CF", "Central African Republic");
        put("TD", "Chad");
        put("CL", "Chile");
        put("CN", "China");
        put("CX", "Christmas Island");
        put("CC", "Cocos (Keeling) Islands");
        put("CO", "Colombia");
        put("KM", "Comoros");
        put("CG", "Congo");
        put("CD", "Congo, the Democratic Republic of the");
        put("CK", "Cook Islands");
        put("CR", "Costa Rica");
        put("CI", "C?te d'Ivoire");
        put("HR", "Croatia");
        put("CU", "Cuba");
        put("CW", "Cura?ao");
        put("CY", "Cyprus");
        put("CZ", "Czech Republic");
        put("DJ", "Djibouti");
        put("DM", "Dominica");
        put("DO", "Dominican Republic");
        put("EC", "Ecuador");
        put("EG", "Egypt");
        put("SV", "El Salvador");
        put("GQ", "Equatorial Guinea");
        put("ER", "Eritrea");
        put("EE", "Estonia");
        put("ET", "Ethiopia");
        put("FK", "Falkland Islands (Malvinas)");
        put("FO", "Faroe Islands");
        put("FJ", "Fiji");
        put("FI", "Finland");
        put("FR", "France");
        put("GF", "French Guiana");
        put("PF", "French Polynesia");
        put("TF", "French Southern Territories");
        put("GA", "Gabon");
        put("GM", "Gambia");
        put("GE", "Georgia");
        put("DE", "Germany");
        put("GH", "Ghana");
        put("GI", "Gibraltar");
        put("GR", "Greece");
        put("GL", "Greenland");
        put("GD", "Grenada");
        put("GP", "Guadeloupe");
        put("GU", "Guam");
        put("GT", "Guatemala");
        put("GG", "Guernsey");
        put("GN", "Guinea");
        put("GW", "Guinea-Bissau");
        put("GY", "Guyana");
        put("HT", "Haiti");
        put("HM", "Heard Island and McDonald Islands");
        put("VA", "Holy Sea (Vatican City State)");
        put("HN", "Honduras");
        put("HK", "Hong Kong");
        put("HU", "Hungary");
        put("IS", "Iceland");
        put("IN", "India");
        put("ID", "Indonesia");
        put("IR", "Iran, Islamic Republic of");
        put("IQ", "Iraq");
        put("IE", "Ireland");
        put("IM", "Isle of Man");
        put("IL", "Israel");
        put("IT", "Italy");
        put("JM", "Jamaica");
        put("JP", "Japan");
        put("JE", "Jersey");
        put("JO", "Jordan");
        put("KZ", "Kazakhstan");
        put("KE", "Kenya");
        put("KI", "Kiribati");
        put("KP", "Korea, Democratic People's Republic of");
        put("KR", "Korea, Republic of");
        put("KW", "Kuwait");
        put("KG", "Kyrgyzstan");
        put("LA", "Lao People's Democratic Republic");
        put("LV", "Latvia");
        put("LB", "Lebanon");
        put("LS", "Lesotho");
        put("LR", "Liberia");
        put("LY", "Libya");
        put("LI", "Liechtenstein");
        put("LT", "Lithuania");
        put("LU", "Luxembourg");
        put("MO", "Macao");
        put("MK", "Macedonia, the former Yugoslav Republic of");
        put("MG", "Madagascar");
        put("MW", "Malawi");
        put("MY", "Malaysia");
        put("MV", "Maldives");
        put("ML", "Mali");
        put("MT", "Malta");
        put("MH", "Marshall Islands");
        put("MQ", "Martinique");
        put("MR", "Mauritania");
        put("MU", "Mauritius");
        put("YT", "Mayotte");
        put("MX", "Mexico");
        put("FM", "Micronesia, Federated States of");
        put("MD", "Moldova, Republic of");
        put("MC", "Monaco");
        put("MN", "Mongolia");
        put("ME", "Montenegro");
        put("MS", "Montserrat");
        put("MA", "Morocco");
        put("MZ", "Mozambique");
        put("MM", "Myanmar");
        put("NA", "Namibia");
        put("NR", "Nauru");
        put("NP", "Nepal");
        put("NL", "Netherlands");
        put("NC", "New Caledonia");
        put("NI", "Nicaragua");
        put("NE", "Niger");
        put("NG", "Nigeria");
        put("NU", "Niue");
        put("NF", "Norfolk Island");
        put("MP", "Northern Mariana Islands");
        put("OM", "Oman");
        put("PK", "Pakistan");
        put("PW", "Palau");
        put("PS", "Palestinian Territory, Occupied");
        put("PA", "Panama");
        put("PG", "Papua New Guinea");
        put("PY", "Paraguay");
        put("PE", "Peru");
        put("PH", "Philippines");
        put("PN", "Pitcairn");
        put("PL", "Poland");
        put("PT", "Portugal");
        put("PR", "Puerto Rico");
        put("QA", "Qatar");
        put("RE", "R?union");
        put("RO", "Romania");
        put("RU", "Russian Federation");
        put("RW", "Rwanda");
        put("BL", "Saint Barth?lemy");
        put("SH", "Saint Helena, Ascension and Tristan da Cunha");
        put("KN", "Saint Kitts and Nevis");
        put("LC", "Saint Lucia");
        put("MF", "Saint Martin (French part)");
        put("PM", "Saint Pierre and Miquelon");
        put("VC", "Saint Vincent and the Grenadines");
        put("WS", "Samoa");
        put("SM", "San Marino");
        put("ST", "Sao Tome and Principe");
        put("SA", "Saudi Arabia");
        put("SN", "Senegal");
        put("RS", "Serbia");
        put("SC", "Seychelles");
        put("SL", "Sierra Leone");
        put("SG", "Singapore");
        put("SX", "Sint Maarten (Dutch part)");
        put("SK", "Slovakia");
        put("SI", "Slovenia");
        put("SB", "Solomon Islands");
        put("SO", "Somalia");
        put("ZA", "South Africa");
        put("GS", "South Georgia and the South Sandwich Islands");
        put("SS", "South Sudan");
        put("LK", "Sri Lanka");
        put("SD", "Sudan");
        put("SR", "Suriname");
        put("SJ", "Svalbard and Jan Mayen");
        put("SZ", "Swaziland");
        put("CH", "Switzerland");
        put("SY", "Syrian Arab Republic");
        put("TW", "Taiwan, Province of China");
        put("TJ", "Tajikistan");
        put("TZ", "Tanzania, United Republic of");
        put("TH", "Thailand");
        put("TL", "Timor-Leste");
        put("TG", "Togo");
        put("TK", "Tokelau");
        put("TO", "Tonga");
        put("TT", "Trinidad and Tobago");
        put("TN", "Tunisia");
        put("TR", "Turkey");
        put("TM", "Turkmenistan");
        put("TC", "Turks and Caicos Islands");
        put("TV", "Tuvalu");
        put("UG", "Uganda");
        put("UA", "Ukraine");
        put("AE", "United Arab Emirates");
        put("UM", "United States Minor Outlying Islands");
        put("UY", "Uruguay");
        put("UZ", "Uzbekistan");
        put("VU", "Vanuatu");
        put("VE", "Venezuela, Bolivarian Republic of");
        put("VN", "Viet Nam");
        put("VG", "Virgin Islands, British");
        put("VI", "Virgin Islands, U.S.");
        put("WF", "Wallis and Futuna");
        put("EH", "Western Sahara");
        put("YE", "Yemen");
        put("ZM", "Zambia");
        put("ZW", "Zimbabwe");
    }};

}