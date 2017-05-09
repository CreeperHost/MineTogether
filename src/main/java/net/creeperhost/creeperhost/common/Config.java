package net.creeperhost.creeperhost.common;

/**
 * Created by Aaron on 05/05/2017.
 */
public class Config
{
    private final String version;
    private final String promoCode;
    private boolean creeperhostEnabled;
    private boolean mpMenuEnabled;
    private boolean mainMenuEnabled;
    private boolean serverHostButtonImage;
    private boolean serverHostMenuImage;

    private static Config instance;

    private Config(String version, String promoCode, boolean creeperhostEnabled, boolean mpMenuEnabled, boolean mainMenuEnabled, boolean serverHostButtonImage, boolean serverHostMenuImage) {
        this.version = version;
        this.promoCode = promoCode;
        this.creeperhostEnabled = creeperhostEnabled;
        this.mpMenuEnabled = mpMenuEnabled;
        this.mainMenuEnabled = mainMenuEnabled;
        this.serverHostButtonImage = serverHostButtonImage;
        this.serverHostMenuImage = serverHostMenuImage;
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

    public boolean isMpMenuEnabled()
    {
        return mpMenuEnabled;
    }

    public boolean isCreeperhostEnabled()
    {
        return creeperhostEnabled;
    }

    public boolean isMainMenuEnabled()
    {
        return mainMenuEnabled;
    }

    public boolean isServerHostButtonImage()
    {
        return serverHostButtonImage;
    }

    public boolean isServerHostMenuImage()
    {
        return serverHostMenuImage;
    }

    public static void makeConfig(String version, String promoCode, boolean creeperhostEnabled, boolean mpMenuEnabled, boolean mainMenuEnabled, boolean serverHostButtonImage, boolean serverHostMenuImage) {
        if (instance != null) {
            return;
        }

        instance = new Config(version, promoCode, creeperhostEnabled, mpMenuEnabled, mainMenuEnabled, serverHostButtonImage, serverHostMenuImage);
    }
}
