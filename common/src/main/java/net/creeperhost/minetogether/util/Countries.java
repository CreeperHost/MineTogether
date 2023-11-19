package net.creeperhost.minetogether.util;

import com.google.common.collect.ImmutableMap;
import net.creeperhost.minetogether.MineTogether;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * Created by covers1624 on 25/10/22.
 */
public class Countries {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Map<String, String> COUNTRIES = buildMap(new String[] {
            "GB", "United Kingdom",
            "US", "United States",
            "NZ", "New Zealand",
            "AU", "Australia",
            "DK", "Denmark",
            "CA", "Canada",
            "SE", "Sweden",
            "NO", "Norway",
            "BR", "Brazil",
            "ES", "Spain",
            "AF", "Afghanistan",
            "AX", "Aland Islands",
            "AL", "Albania",
            "DZ", "Algeria",
            "AS", "American Samoa",
            "AD", "Andorra",
            "AO", "Angola",
            "AI", "Anguilla",
            "AQ", "Antarctica",
            "AG", "Antigua and Barbuda",
            "AR", "Argentina",
            "AM", "Armenia",
            "AW", "Aruba",
            "AT", "Austria",
            "AZ", "Azerbaijan",
            "BS", "Bahamas",
            "BH", "Bahrain",
            "BD", "Bangladesh",
            "BB", "Barbados",
            "BY", "Belarus",
            "BE", "Belgium",
            "BZ", "Belize",
            "BJ", "Benin",
            "BM", "Bermuda",
            "BT", "Bhutan",
            "BO", "Bolivia, Plurinational State of",
            "BQ", "Bonaire, Sint Eustatius and Saba",
            "BA", "Bosnia and Herzegovina",
            "BW", "Botswana",
            "BV", "Bouvet Island",
            "IO", "British Indian Ocean Territory",
            "BN", "Brunei Darussalam",
            "BG", "Bulgaria",
            "BF", "Burkina Faso",
            "BI", "Burundi",
            "KH", "Cambodia",
            "CM", "Cameroon",
            "CV", "Cape Verde",
            "KY", "Cayman Islands",
            "CF", "Central African Republic",
            "TD", "Chad",
            "CL", "Chile",
            "CN", "China",
            "CX", "Christmas Island",
            "CC", "Cocos (Keeling) Islands",
            "CO", "Colombia",
            "KM", "Comoros",
            "CG", "Congo",
            "CD", "Congo, the Democratic Republic of the",
            "CK", "Cook Islands",
            "CR", "Costa Rica",
            "CI", "C�te d'Ivoire",
            "HR", "Croatia",
            "CU", "Cuba",
            "CW", "Cura�ao",
            "CY", "Cyprus",
            "CZ", "Czech Republic",
            "DJ", "Djibouti",
            "DM", "Dominica",
            "DO", "Dominican Republic",
            "EC", "Ecuador",
            "EG", "Egypt",
            "SV", "El Salvador",
            "GQ", "Equatorial Guinea",
            "ER", "Eritrea",
            "EE", "Estonia",
            "ET", "Ethiopia",
            "FK", "Falkland Islands (Malvinas)",
            "FO", "Faroe Islands",
            "FJ", "Fiji",
            "FI", "Finland",
            "FR", "France",
            "GF", "French Guiana",
            "PF", "French Polynesia",
            "TF", "French Southern Territories",
            "GA", "Gabon",
            "GM", "Gambia",
            "GE", "Georgia",
            "DE", "Germany",
            "GH", "Ghana",
            "GI", "Gibraltar",
            "GR", "Greece",
            "GL", "Greenland",
            "GD", "Grenada",
            "GP", "Guadeloupe",
            "GU", "Guam",
            "GT", "Guatemala",
            "GG", "Guernsey",
            "GN", "Guinea",
            "GW", "Guinea-Bissau",
            "GY", "Guyana",
            "HT", "Haiti",
            "HM", "Heard Island and McDonald Islands",
            "VA", "Holy Sea (Vatican City State)",
            "HN", "Honduras",
            "HK", "Hong Kong",
            "HU", "Hungary",
            "IS", "Iceland",
            "IN", "India",
            "ID", "Indonesia",
            "IR", "Iran, Islamic Republic of",
            "IQ", "Iraq",
            "IE", "Ireland",
            "IM", "Isle of Man",
            "IL", "Israel",
            "IT", "Italy",
            "JM", "Jamaica",
            "JP", "Japan",
            "JE", "Jersey",
            "JO", "Jordan",
            "KZ", "Kazakhstan",
            "KE", "Kenya",
            "KI", "Kiribati",
            "KP", "Korea, Democratic People's Republic of",
            "KR", "Korea, Republic of",
            "KW", "Kuwait",
            "KG", "Kyrgyzstan",
            "LA", "Lao People's Democratic Republic",
            "LV", "Latvia",
            "LB", "Lebanon",
            "LS", "Lesotho",
            "LR", "Liberia",
            "LY", "Libya",
            "LI", "Liechtenstein",
            "LT", "Lithuania",
            "LU", "Luxembourg",
            "MO", "Macao",
            "MK", "Macedonia, the former Yugoslav Republic of",
            "MG", "Madagascar",
            "MW", "Malawi",
            "MY", "Malaysia",
            "MV", "Maldives",
            "ML", "Mali",
            "MT", "Malta",
            "MH", "Marshall Islands",
            "MQ", "Martinique",
            "MR", "Mauritania",
            "MU", "Mauritius",
            "YT", "Mayotte",
            "MX", "Mexico",
            "FM", "Micronesia, Federated States of",
            "MD", "Moldova, Republic of",
            "MC", "Monaco",
            "MN", "Mongolia",
            "ME", "Montenegro",
            "MS", "Montserrat",
            "MA", "Morocco",
            "MZ", "Mozambique",
            "MM", "Myanmar",
            "NA", "Namibia",
            "NR", "Nauru",
            "NP", "Nepal",
            "NL", "Netherlands",
            "NC", "New Caledonia",
            "NI", "Nicaragua",
            "NE", "Niger",
            "NG", "Nigeria",
            "NU", "Niue",
            "NF", "Norfolk Island",
            "MP", "Northern Mariana Islands",
            "OM", "Oman",
            "PK", "Pakistan",
            "PW", "Palau",
            "PS", "Palestinian Territory, Occupied",
            "PA", "Panama",
            "PG", "Papua New Guinea",
            "PY", "Paraguay",
            "PE", "Peru",
            "PH", "Philippines",
            "PN", "Pitcairn",
            "PL", "Poland",
            "PT", "Portugal",
            "PR", "Puerto Rico",
            "QA", "Qatar",
            "RE", "R�union",
            "RO", "Romania",
            "RU", "Russian Federation",
            "RW", "Rwanda",
            "BL", "Saint Barth�lemy",
            "SH", "Saint Helena, Ascension and Tristan da Cunha",
            "KN", "Saint Kitts and Nevis",
            "LC", "Saint Lucia",
            "MF", "Saint Martin (French part)",
            "PM", "Saint Pierre and Miquelon",
            "VC", "Saint Vincent and the Grenadines",
            "WS", "Samoa",
            "SM", "San Marino",
            "ST", "Sao Tome and Principe",
            "SA", "Saudi Arabia",
            "SN", "Senegal",
            "RS", "Serbia",
            "SC", "Seychelles",
            "SL", "Sierra Leone",
            "SG", "Singapore",
            "SX", "Sint Maarten (Dutch part)",
            "SK", "Slovakia",
            "SI", "Slovenia",
            "SB", "Solomon Islands",
            "SO", "Somalia",
            "ZA", "South Africa",
            "GS", "South Georgia and the South Sandwich Islands",
            "SS", "South Sudan",
            "LK", "Sri Lanka",
            "SD", "Sudan",
            "SR", "Suriname",
            "SJ", "Svalbard and Jan Mayen",
            "SZ", "Swaziland",
            "CH", "Switzerland",
            "SY", "Syrian Arab Republic",
            "TW", "Taiwan, Province of China",
            "TJ", "Tajikistan",
            "TZ", "Tanzania, United Republic of",
            "TH", "Thailand",
            "TL", "Timor-Leste",
            "TG", "Togo",
            "TK", "Tokelau",
            "TO", "Tonga",
            "TT", "Trinidad and Tobago",
            "TN", "Tunisia",
            "TR", "Turkey",
            "TM", "Turkmenistan",
            "TC", "Turks and Caicos Islands",
            "TV", "Tuvalu",
            "UG", "Uganda",
            "UA", "Ukraine",
            "AE", "United Arab Emirates",
            "UM", "United States Minor Outlying Islands",
            "UY", "Uruguay",
            "UZ", "Uzbekistan",
            "VU", "Vanuatu",
            "VE", "Venezuela, Bolivarian Republic of",
            "VN", "Viet Nam",
            "VG", "Virgin Islands, British",
            "VI", "Virgin Islands, U.S.",
            "WF", "Wallis and Futuna",
            "EH", "Western Sahara",
            "YE", "Yemen",
            "ZM", "Zambia",
            "ZW", "Zimbabwe",
            "XK", "Kosovo",
            "UNKNOWN", "Unknown" }
    );

    @Nullable
    private static String ourCountry;

    public static String getOurCountry() {
        if (ourCountry == null) {
            try {
                ourCountry = MineTogether.API.execute(new GetClosestDCRequest())
                        .apiResponse().getCustomer().getCountry();
            } catch (IOException ex) {
                LOGGER.error("Failed to get country. Assuming US.", ex);
                ourCountry = "US";
            }
        }
        return ourCountry;
    }

    private static Map<String, String> buildMap(String[] args) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (int i = 0; i < args.length; i += 2) {
            builder.put(args[i], args[i + 1]);
        }
        return builder.build();
    }
}
