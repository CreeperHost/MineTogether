package net.creeperhost.minetogetherlib.serverorder;

import com.google.gson.*;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogetherlib.Order;
import net.creeperhost.minetogetherlib.util.WebUtils;
import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerOrderCallbacks
{
    private static String userCountry;
    private static Map<String, String> countries = new LinkedHashMap<String, String>()
    {{
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
        put("CI", "C�te d'Ivoire");
        put("HR", "Croatia");
        put("CU", "Cuba");
        put("CW", "Cura�ao");
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
        put("RE", "R�union");
        put("RO", "Romania");
        put("RU", "Russian Federation");
        put("RW", "Rwanda");
        put("BL", "Saint Barth�lemy");
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
        put("UNKNOWN", "Unknown");
    }};

    public static Map<String, String> getCountries()
    {
        return countries;
    }

    public static AvailableResult getNameAvailable(String name)
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
            MineTogether.logger.error("Unable to check if name available", t);
        }

        return new AvailableResult(false, "unknown");
    }

    public static OrderSummary getSummary(Order order)
    {
        if (order.country.isEmpty())
        {
            order.country = getUserCountry();
        }

        if (order.serverLocation.isEmpty())
        {
            order.serverLocation = getRecommendedLocation();
        }

        try
        {
            String version = Config.getInstance().getVersion();
            if (version.equals("0"))
            {
                //TODO
//                if (!MineTogether.instance.ftbPackID.isEmpty())
//                {
//                    Config.getInstance().setVersion(MineTogether.instance.requestedID);
//                }
            }
            String url = "https://www.creeperhost.net/json/order/mc/" + version + "/recommend/" + order.playerAmount;

            String resp = WebUtils.getWebResponse(url);

            JsonElement jElement = new JsonParser().parse(resp);

            JsonObject jObject = jElement.getAsJsonObject();
            String recommended = jObject.getAsJsonPrimitive("recommended").getAsString();

            if(!Config.getInstance().getPromo().isEmpty() && !Config.getInstance().getPromo().equalsIgnoreCase("Insert Promo Code here"))
            {
                WebUtils.getWebResponse("https://www.creeperhost.net/applyPromo/" + Config.getInstance().getPromo());
            }

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
            } catch (Exception e)
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
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded1"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded2"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded3"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded4"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded5"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded6"));
            vpsIncluded.add(I18n.get("minetogether.quote.vpsincluded7"));

            return new OrderSummary(recommended, vpsDisplay, vpsFeatures, vpsIncluded, preDiscount, subTotal, total, tax, discount, suffix, prefix, id);

        } catch (Throwable t)
        {
            MineTogether.logger.error("Unable to fetch summary", t);
            return null;
        }
    }

    public static String getRecommendedLocation()
    {
        try
        {
            String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");

            JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();

            jObject = jObject.getAsJsonObject("datacentre");

            return jObject.getAsJsonPrimitive("name").getAsString();
        } catch (Throwable ignored)
        {
        }
        return ""; // default
    }

    public static String getUserCountry()
    {
        if (userCountry == null)
            try
            {
                String freeGeoIP = WebUtils.getWebResponse("https://www.creeperhost.net/json/datacentre/closest");

                JsonObject jObject = new JsonParser().parse(freeGeoIP).getAsJsonObject();

                jObject = jObject.getAsJsonObject("customer");

                userCountry = jObject.getAsJsonPrimitive("country").getAsString();
            } catch (Throwable t)
            {
                MineTogether.logger.error("Unable to get user's country automatically, assuming USA", t);
                userCountry = "US"; // default
            }
        return userCountry;
    }

    public static Map<String, String> getRegionMap()
    {
        Map<String, String> rawMap = new HashMap<String, String>();
        Map<String, String> returnMap = new HashMap<String, String>();

        try
        {
            String jsonData = WebUtils.getWebResponse("https://www.creeperhost.net/json/locations");

            Type type = new com.google.common.reflect.TypeToken<Map<String, String>>() {}.getType();
            Gson g = new Gson();
            JsonElement el = new JsonParser().parse(jsonData);
            rawMap = g.fromJson(el.getAsJsonObject().get("regionMap"), type);
        } catch (Exception e)
        {
            MineTogether.logger.error("Unable to fetch server locations" + e);
        }
        for (Map.Entry<String, String> entry : rawMap.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            returnMap.put(key, value);
        }
        return returnMap;
    }

    public static Map<String, String> getDataCentres()
    {
        String url = "https://www.creeperhost.net/json/datacentre/closest";
        String resp = WebUtils.getWebResponse(url);
        Map<String, String> map = new HashMap<>();

        JsonElement jElement = new JsonParser().parse(resp);

        if (jElement.isJsonObject())
        {
            JsonArray array = jElement.getAsJsonObject().getAsJsonArray("datacentres");

            if (array != null)
            {
                for (JsonElement serverEl : array)
                {
                    JsonObject object = (JsonObject) serverEl;
                    String name = object.get("name").getAsString();
                    String distance = object.get("distance").getAsString();
                    map.put(name, distance);
                }
                return map;
            }
        }
        return null;
    }

    public static boolean doesEmailExist(final String email)
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
            MineTogether.logger.error("Unable to check if email exists", t);
            return false;
        }
        return true;
    }

    public static String doLogin(final String username, final String password)
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
            MineTogether.logger.error("Unable to do login", t);
            return "Unknown Error";
        }
    }

    public static String createOrder(final Order order)
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
            MineTogether.logger.error("Unable to create order");
            return "Unknown error";
        }
    }

    public static String createAccount(final Order order)
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
            MineTogether.logger.error("Unable to create account", t);
            return "Unknown error";
        }
    }

    public static String getPaymentLink(String invoiceID)
    {
        return "https://billing.creeperhost.net/viewinvoice.php?id=" + invoiceID;
    }

    public static boolean cancelOrder(int orderNum)
    {
        try
        {
            String response = WebUtils.getWebResponse("https://www.creeperhost.net/json/order/" + orderNum + "/cancel");
        } catch (Throwable t)
        {
            MineTogether.logger.error("Unable to cancel order");
            return false;
        }
        return true;
    }
}
