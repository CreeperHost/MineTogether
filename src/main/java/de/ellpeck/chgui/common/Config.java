package de.ellpeck.chgui.common;

/**
 * Created by Aaron on 05/05/2017.
 */
public class Config
{
    private final String version;
    private final String promoCode;

    private static Config instance;

    private Config(String version, String promoCode) {
        this.version = version;
        this.promoCode = promoCode;
    }

    public static Config getInstance() {
        return instance;
    }

    public String getVersion() {
        return version;
    }

    public String getPromo() {
        return promoCode;
    }

    public static void makeConfig(String version, String promoCode) {
        if (instance != null) {
            return;
        }

        instance = new Config(version, promoCode);
    }
}
