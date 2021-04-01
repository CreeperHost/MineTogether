package net.creeperhost.minetogether.client.screen.order;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.element.ButtonMap;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.util.Locale;
import java.util.Map;

public class GuiLocationMap extends GuiGetServer
{
    private final ResourceLocation siv = new ResourceLocation(Constants.MOD_ID, "textures/guisiv.png");
    private Button currentFocus;
    private Map<String, String> regions;
    private Map<String, String> dataCenters;
    private String distance = "";

    public GuiLocationMap(int stepId, Order order)
    {
        super(stepId, order);
    }

    @Override
    public void init()
    {
        super.init();
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

        float halfWidth = (float)width / 2;

        addButton(new ButtonMap(halfWidth - 228F, y - 94F, (int) (nawestX / scalingFactor), (int) (nawestY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-west", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth - 207F, y - 44.8F, (int) (nasouthX / scalingFactor), (int) (nasouthY / scalingFactor),            (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-south", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth - 176.5F, y - 96.5F, (int) (naeastX / scalingFactor), (int) (naeastY / scalingFactor),             (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-east", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth - 171.8F, y - 19.5F, (int) (southamericaX / scalingFactor), (int) (southamericaY / scalingFactor),   (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "south-america", false, this::updateSelected));

        addButton(new ButtonMap(halfWidth - 115F, y - 122F, (int) (greenlandX / scalingFactor), (int) (greenlandY / scalingFactor),      (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "greenland", false, this::updateSelected));

        addButton(new ButtonMap(halfWidth - 30.8F, y - 101.2F, (int) (euwestX / scalingFactor), (int) (euwestY / scalingFactor),              (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-west", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth - 0.5F, y - 95F, (int) (eumiddleeastX / scalingFactor), (int) (eumiddleeastY / scalingFactor),        (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-middle-east", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth + 24F, y - 114F, (int) (russiaX / scalingFactor), (int) (russiaY / scalingFactor),             (int) (2048 / scalingFactor), (int) (2048 / scalingFactor), "russia", false, this::updateSelected));

        addButton(new ButtonMap(halfWidth + 64.5F, y - 69F, (int) (asiaX / scalingFactor), (int) (asiaY / scalingFactor),                  (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "asia", false, this::updateSelected));
        addButton(new ButtonMap(halfWidth + 119.5F, y - 5.5F, (int) (australiaX / scalingFactor), (int) (australiaY / scalingFactor),                 (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "australia", false, this::updateSelected));

        addButton(new ButtonMap(halfWidth - 35.8F, y - 23.2F, (int) (subsaharanafricaX / scalingFactor), (int) (subsaharanafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "sub-saharan-africa", false, this::updateSelected));
        addButton(new ButtonMap( halfWidth - 41.4F, y - 57.7F, (int) (northafricaX / scalingFactor), (int) (northafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "north-africa", false, this::updateSelected));


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
                    buttons.forEach(widget ->
                    {
                        if (widget.getMessage().getString().equalsIgnoreCase(s))
                        {
                            widget.active = true;
                        }
                        if(order.serverLocation != null)
                        {
                            if(widget.getMessage().getString().equalsIgnoreCase(datacentreToRegion(order.serverLocation)) || widget.getMessage().getString().equalsIgnoreCase(order.serverLocation))
                            {
                                updateSelected((Button) widget);
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
                MineTogether.logger.error("Failed to get region map data");
            }
        }
    }

    private void updateSelected(Button button)
    {
        try
        {
            if(currentFocus != null) currentFocus.changeFocus(false);
            this.currentFocus = button;
            button.changeFocus(true);
            if(dataCenters != null && !dataCenters.isEmpty()) distance = dataCenters.get(regionToDataCentre(currentFocus.getMessage().getString()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
    {
        renderDirtBackground(1);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);

        if(currentFocus != null)
        {
            minecraft.getTextureManager().bindTexture(siv);
            int x = font.getStringWidth(ttl(currentFocus.getMessage().getString()));
            int bufferLeft = 20;

            blit(matrixStack, (this.width - bufferLeft) - x - 5, 20, 0, 0, x + bufferLeft, 20);

            drawString(matrixStack, font, ttl(currentFocus.getMessage().getString()), (this.width - x) - bufferLeft, 26, -1);
            Minecraft.getInstance().getTextureManager().bindTexture(GUI_ICONS_LOCATION);

            //16 = 4 bars
            //32 = 3 bars
            //40 = 2 bars
            //48 = 1 bars
            if(distance != null && !distance.isEmpty())
            {
                AbstractGui.blit(matrixStack, this.width - 18, 26, 8, 8, 0, distanceConvert(Integer.parseInt(distance)), 8, 8, 256, 256);
            }
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
            case "bucharest":
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
                return "bucharest";
            default:
                return "";
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        super.mouseClicked(mouseX, mouseY, button);
        buttons.forEach(widget ->
        {
            if(widget == buttonNext)
            {
                if(currentFocus != null) this.order.serverLocation = currentFocus.getMessage().getString().trim();
            }
            widget.mouseClicked(mouseX, mouseY, button);
        });
        return true;
    }

    @Override
    public String getStepName()
    {
        return Util.localize("gui.server_location");
    }
}