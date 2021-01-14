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

        buttons.add(new ButtonMap((width / 2) - 230, y - 94, 60, 60, new StringTextComponent("na-west"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) - 204, y - 35, 52, 42, new StringTextComponent("na-south"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) - 170, y - 97, 60, 62, new StringTextComponent("na-east"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) - 170, y - 1, 84, 88, new StringTextComponent("south-america"), false, this::updateSelected));

        buttons.add(new ButtonMap((width / 2) - 115, y - 122, 54, 28, new StringTextComponent("greenland"), false, this::updateSelected));

        buttons.add(new ButtonMap((width / 2) - 36, y - 104, 55, 55, new StringTextComponent("eu-west"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2 - 4), y - 94, 80, 80, new StringTextComponent("eu-middle-east"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) + 24, y - 114, 156, 76, new StringTextComponent("russia"), false, this::updateSelected));

        buttons.add(new ButtonMap((width / 2) + 70, y - 62, 96, 80, new StringTextComponent("asia"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) + 130, y, 98, 67, new StringTextComponent("australia"), false, this::updateSelected));

        buttons.add(new ButtonMap((width / 2) - 39, y - 5, 84, 74, new StringTextComponent("sub-saharan-africa"), false, this::updateSelected));
        buttons.add(new ButtonMap((width / 2) - 45, y - 47, 84, 44, new StringTextComponent("north-africa"), false, this::updateSelected));

        //TODO remove this
        buttons.add(new Button(this.width - 180, height - 30, 80, 20, new StringTextComponent("REFRESH"), (button) ->
        {
            Minecraft.getInstance().displayGuiScreen(new GuiLocationMap(stepId, order));
        }));

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
                            if(widget.getMessage().getString().equalsIgnoreCase(datacentreToRegion(order.serverLocation)))
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
//            fill(matrixStack, this.width - 140, 40, width, 20, 0x99000000);
            blit(matrixStack, this.width - 140, 20, 0, 0, 140, 20);
            drawString(matrixStack, minecraft.fontRenderer, ttl(currentFocus.getMessage().getString()), this.width - 138, 26, -1);
            Minecraft.getInstance().getTextureManager().bindTexture(GUI_ICONS_LOCATION);

            //16 = 4 bars
            //32 = 3 bars
            //40 = 2 bars
            //48 = 1 bars
            if(!distance.isEmpty()) AbstractGui.blit(matrixStack, this.width - 18, 22, 16, 16, 0, distanceConvert(Integer.parseInt(distance)), 8, 8, 256, 256);
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
