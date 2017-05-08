package de.ellpeck.creeperhost.gui.mpreplacement;

import de.ellpeck.creeperhost.CreeperHost;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Aaron on 26/04/2017.
 */
public class GuiCHMultiplayer extends GuiMultiplayer
{
    private static Field serverListSelectorField;
    private static Field serverListInternetField;

    public GuiCHMultiplayer(GuiScreen parentScreen)
    {
        super(parentScreen);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        CreeperHostServerSelectionList ourList = new CreeperHostServerSelectionList(this, this.mc, this.width, this.height, 32, this.height - 64, 36);

        try
        {
            if (serverListSelectorField == null) {
                serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
                serverListSelectorField.setAccessible(true);
            }

            if (serverListInternetField == null) {
                serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "field_148198_l", "serverListInternet");
                serverListInternetField.setAccessible(true);
            }

            ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(this); // Get the old selector
            List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
            serverListInternetField.set(ourList, serverListInternet); // shove its list into ours
            serverListSelectorField.set(this, ourList); // Make our list the one in our GUIMultiplayer

        } catch (Throwable e)
        {
            CreeperHost.logger.warn("Reflection to copy server list failed.", e);
        }
    }
}
