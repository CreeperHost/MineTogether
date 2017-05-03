package de.ellpeck.chgui.paul;

public class Order{

    public String name = "";
    public int playerAmount = Constants.DEF_PLAYER_COUNT;
    public String serverLocation = Constants.DEF_SERVER_LOCATION;

    public String emailAddress = "";
    public String password = "";
    public String firstName = "";
    public String lastName = "";
    public String address = "";
    public String city = "";
    public String phone = "";
    public String state = "";
    public String country = "";
    public String zip = "";

    public String productID;
    public String clientID;
    public String currency;

    public int version = 35567; // TODO: read from config file
    public String promo = "feedme"; // TODO: read from config file
}
