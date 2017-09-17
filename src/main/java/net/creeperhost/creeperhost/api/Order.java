package net.creeperhost.creeperhost.api;

import net.creeperhost.creeperhost.paul.Constants;

public class Order
{

    public String name = "";
    public int playerAmount = Constants.DEF_PLAYER_COUNT;
    public String serverLocation = "";

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

    public boolean pregen = false;
}
