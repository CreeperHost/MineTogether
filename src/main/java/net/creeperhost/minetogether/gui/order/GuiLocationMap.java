package net.creeperhost.minetogether.gui.order;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.gui.element.ButtonMap;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;
import java.util.Map;

public class GuiLocationMap extends GuiGetServer
{
    private final ResourceLocation siv = new ResourceLocation(CreeperHost.MOD_ID, "textures/guisiv.png");
    private GuiButton currentFocus;
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

        addButton(new ButtonMap(21, (width / 2) - 230, y - 94, 60, 60, "na-west", false, this::updateSelected));
        addButton(new ButtonMap(22,(width / 2) - 204, y - 35, 52, 42, "na-south", false, this::updateSelected));
        addButton(new ButtonMap(23, (width / 2) - 170, y - 97, 60, 62, "na-east", false, this::updateSelected));
        addButton(new ButtonMap(24,(width / 2) - 170, y - 1, 84, 88, "south-america", false, this::updateSelected));

        addButton(new ButtonMap(25, (width / 2) - 115, y - 122, 54, 28, "greenland", false, this::updateSelected));

        addButton(new ButtonMap(26,(width / 2) - 36, y - 104, 55, 55, "eu-west", false, this::updateSelected));
        addButton(new ButtonMap(27, (width / 2 - 4), y - 94, 80, 80, "eu-middle-east", false, this::updateSelected));
        addButton(new ButtonMap(28, (width / 2) + 24, y - 114, 156, 76, "russia", false, this::updateSelected));

        addButton(new ButtonMap(29, (width / 2) + 70, y - 62, 96, 80, "asia", false, this::updateSelected));
        addButton(new ButtonMap(30, (width / 2) + 130, y, 98, 67, "australia", false, this::updateSelected));

        addButton(new ButtonMap(31, (width / 2) - 39, y - 5, 84, 74, "sub-saharan-africa", false, this::updateSelected));
        addButton(new ButtonMap(32, (width / 2) - 45, y - 47, 84, 44, "north-africa", false, this::updateSelected));

        //TODO remove this
//        buttons.add(new Button(this.width - 180, height - 30, 80, 20, new StringTextComponent("REFRESH"), (button) ->
//        {
//            Minecraft.getInstance().displayGuiScreen(new GuiLocationMap(stepId, order));
//        }));

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
//                            widget.active = true;
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
        drawGradientRect(0, this.height - 20, width, 20, 0x99000000, 0x99000000);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(currentFocus != null)
        {
            mc.getTextureManager().bindTexture(siv);
//            fill(matrixStack, this.width - 140, 40, width, 20, 0x99000000);
            drawTexturedModalRect(this.width - 140, 20, 0, 0, 140, 20);
            drawString(fontRendererObj, ttl(currentFocus.displayString), this.width - 138, 26, -1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ICONS);

            //16 = 4 bars
            //32 = 3 bars
            //40 = 2 bars
            //48 = 1 bars
            if(!distance.isEmpty()) Gui.drawModalRectWithCustomSizedTexture(this.width - 18, 22, 16, 16, 0, distanceConvert(Integer.parseInt(distance)), 8, 8);
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
//            if(currentFocus != null) currentFocus.changeFocus(false);
            this.currentFocus = button;
//            button.changeFocus(true);
            if(dataCenters != null && !dataCenters.isEmpty()) distance = dataCenters.get(regionToDataCentre(currentFocus.displayString));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
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
