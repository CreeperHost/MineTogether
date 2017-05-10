package net.creeperhost.creeperhost.api;

import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Created by Aaron on 09/05/2017.
 */
public interface IServerHost
{
    ResourceLocation getButtonIcon();
    ResourceLocation getMenuIcon();
    Map<Integer, String> getAllServerLocations();
    OrderSummary getSummary(Order order);
    AvailableResult getNameAvailable(String name);
    boolean doesEmailExist(String email);
    String doLogin(String username, String password);
    String createAccount(Order order);
    String createOrder(Order order);
    String getLocalizationRoot();
}
