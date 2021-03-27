package net.creeperhost.minetogether.gui.order;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.gui.element.ButtonMap;
import net.creeperhost.minetogether.gui.element.FancyButton;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GuiLocationMap extends GuiGetServer
{
    private final ResourceLocation siv = new ResourceLocation(CreeperHost.MOD_ID, "textures/guisiv.png");
    private ButtonMap currentFocus;
    private Map<String, String> regions;
    private Map<String, String> dataCenters;
    private String distance = "";

    public GuiLocationMap(int stepId, Order order)
    {
        super(stepId, order);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int y = (this.height / 2) + 20;
        regions = Callbacks.getRegionMap();
        dataCenters = Callbacks.getDataCentres();

        float scalingFactor = 8;

        int nawestX = 413;
        int nawestY = 398;
        int nasouthX = 422;
        int nasouthY = 266;
        int naeastX = 420;
        int naeastY = 416;
        int southamericaX = 492;
        int southamericaY = 768;
        int greenlandX = 407;
        int greenlandY = 194;
        int euwestX = 422;
        int euwestY = 354;
        int eumiddleeastX = 567;
        int eumiddleeastY = 547;
        int russiaX = 1150;
        int russiaY = 540;
        int asiaX = 687;
        int asiaY = 602;
        int australiaX = 673;
        int australiaY = 511;
        int subsaharanafricaX = 664;
        int subsaharanafricaY = 518;
        int northafricaX = 669;
        int northafricaY = 288;

        double halfWidth = (double)width / 2;

        addButton(new ButtonMap(21, halfWidth - 228, y - 94, (int) (nawestX / scalingFactor), (int) (nawestY / scalingFactor),             (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-west", false, this::updateSelected));
        addButton(new ButtonMap(22, halfWidth - 207, y - 44.8, (int) (nasouthX / scalingFactor), (int) (nasouthY / scalingFactor),            (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-south", false, this::updateSelected));
        addButton(new ButtonMap(23, halfWidth - 176.5, y - 96.5, (int) (naeastX / scalingFactor), (int) (naeastY / scalingFactor),             (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-east", false, this::updateSelected));
        addButton(new ButtonMap(24, halfWidth - 171.8, y - 19.5, (int) (southamericaX / scalingFactor), (int) (southamericaY / scalingFactor),   (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "south-america", false, this::updateSelected));

        addButton(new ButtonMap(25, halfWidth - 115, y - 122, (int) (greenlandX / scalingFactor), (int) (greenlandY / scalingFactor),      (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "greenland", false, this::updateSelected));

        addButton(new ButtonMap(26, halfWidth - 30.8, y - 101.2, (int) (euwestX / scalingFactor), (int) (euwestY / scalingFactor),              (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-west", false, this::updateSelected));
        addButton(new ButtonMap(27, halfWidth - 0.5 , y - 95, (int) (eumiddleeastX / scalingFactor), (int) (eumiddleeastY / scalingFactor),        (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-middle-east", false, this::updateSelected));
        addButton(new ButtonMap(28, halfWidth + 24, y - 114, (int) (russiaX / scalingFactor), (int) (russiaY / scalingFactor),             (int) (2048 / scalingFactor), (int) (2048 / scalingFactor), "russia", false, this::updateSelected));

        addButton(new ButtonMap(29, halfWidth + 64.5, y - 69, (int) (asiaX / scalingFactor), (int) (asiaY / scalingFactor),                  (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "asia", false, this::updateSelected));
        addButton(new ButtonMap(30, halfWidth + 119.5, y - 5.5, (int) (australiaX / scalingFactor), (int) (australiaY / scalingFactor),                 (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "australia", false, this::updateSelected));

        addButton(new ButtonMap(31, halfWidth - 35.8, y - 23.2, (int) (subsaharanafricaX / scalingFactor), (int) (subsaharanafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "sub-saharan-africa", false, this::updateSelected));
        addButton(new ButtonMap(32, halfWidth - 41.4, y - 57.7, (int) (northafricaX / scalingFactor), (int) (northafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "north-africa", false, this::updateSelected));

        //TODO remove this
        addButton(new FancyButton(33, this.width - 180, height - 30, 80, 20, "REFRESH", (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLocationMap(stepId, order));
        }));

        //Should never be null but /shrug its Minecraft
        if(regions != null && !regions.isEmpty())
        {
            try
            {
                regions.forEach((s, s2) ->
                {
                    buttonList.forEach(widget ->
                    {
                        if (widget.displayString.equalsIgnoreCase(s))
                        {
                            widget.enabled = true;
                        }
                        if(order.serverLocation != null)
                        {
                            if(widget.displayString.equalsIgnoreCase(datacentreToRegion(order.serverLocation)))
                            {
                                updateSelected((GuiButton) widget);
                                if (dataCenters != null && distance.isEmpty())
                                {
                                    distance = dataCenters.get(order.serverLocation);
                                }
                            }
                        }
                    });
                });
            } catch (Exception ignored)
            {
                CreeperHost.logger.error("Failed to get region map data");
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(currentFocus != null)
        {
            mc.getTextureManager().bindTexture(siv);
            int x = fontRendererObj.getStringWidth(ttl(currentFocus.displayString));
            int buffer = 10;

//            drawGradientRect(this.width - 140, 40, width, 20, 0x99000000, 0x99000000);
            drawTexturedModalRect((this.width - buffer) - x - 5, 20, 0, 0, x + buffer, 20);
            drawString(fontRendererObj, ttl(currentFocus.displayString), (this.width - x) - 10, 26, -1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ICONS);

            //16 = 4 bars
            //32 = 3 bars
            //40 = 2 bars
            //48 = 1 bars
//            if(!distance.isEmpty()) Gui.drawModalRectWithCustomSizedTexture(this.width - 18, 22, 16, 16, 0, distanceConvert(Integer.parseInt(distance)), 8, 8);
        }
    }

    @Override
    public String getStepName()
    {
        return Util.localize("gui.server_location");
    }

    private void updateSelected(GuiButton button)
    {
        try
        {
            if(button != null && button instanceof ButtonMap)
            {
                ButtonMap buttonMap = (ButtonMap) button;
                if(currentFocus != null) currentFocus.setFocus(false);
                    this.currentFocus = buttonMap;
                    buttonMap.setFocus(true);
                if (dataCenters != null && !dataCenters.isEmpty())
                    distance = dataCenters.get(regionToDataCentre(currentFocus.displayString));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button instanceof FancyButton)
        {
            FancyButton fancyButton = (FancyButton) button;
            fancyButton.onPress();
        }
        super.actionPerformed(button);
    }

    public String ttl(String input)
    {
        String key = "minetogether.region." + input.toLowerCase(Locale.ROOT);
        if(I18n.hasKey(key)) return I18n.format(key);
        return input;
    }

    public int distanceConvert(int distance)
    {
        if(distance < 1000) return 16;
        if(distance > 1000 && distance < 5000) return 32;
        if(distance > 5000 && distance < 6000) return 40;

        return 48;
    }

    public String datacentreToRegion(String centre)
    {
        switch (centre)
        {
            case "grantham":
                return "eu-west";
            case "buffalo":
                return "na-east";
            case "chicago":
                return "na-east";
            case "miami":
                return "na-south";
            case "dallas":
                return "na-south";
            case "seattle":
                return "na-west";
            case "losangeles":
                return "na-west";
            case "johannesburg":
                return "sub-saharan-africa";
            case "tokyo":
                return "asia";
            case "saopaulo":
                return "south-america";
            case "hongkong":
                return "asia";
            case "sydney":
                return "australia";
            case "istanbul":
                return "eu-middle-east";
            default:
                return "";
        }
    }

    //This is only used to get an Approximate distance
    public String regionToDataCentre(String region)
    {
        switch (region)
        {
            case "eu-west":
                return "grantham";
            case "na-east":
                return "buffalo";
            case "na-west":
                return "losangeles";
            case "na-south":
                return "dallas";
            case "sub-saharan-africa":
                return "johannesburg";
            case "south-america":
                return "saopaulo";
            case "asia":
                return "hongkong";
            case "australia":
                return "sydney";
            case "eu-middle-east":
                return "istanbul";
            default:
                return "";
        }
    }
}
