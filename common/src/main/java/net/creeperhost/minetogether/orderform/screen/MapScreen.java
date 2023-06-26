package net.creeperhost.minetogether.orderform.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.orderform.ServerOrderCallbacks;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.widget.ButtonMap;
import net.creeperhost.minetogether.util.Countries;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

public class MapScreen extends OrderServerScreen {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private final ResourceLocation siv = new ResourceLocation(MineTogether.MOD_ID, "textures/guisiv.png");
    private Button currentFocus;
    private Map<String, String> regions;
    private Map<String, String> dataCenters;
    private String distance = "";

    public MapScreen(int stepId, Order order) {
        super(stepId, order);
    }

    @Override
    public void init() {
        clearWidgets();

        super.init();
        int y = (this.height / 2) + 20;
        regions = ServerOrderCallbacks.getRegionMap();
        try {
            dataCenters = ServerOrderCallbacks.getDataCentres();
        } catch (IOException | URISyntaxException ex) {
            LOGGER.error("Failed to poll Data Centers.", ex);
        }
        if (order.country == null || order.country.isEmpty()) order.country = Countries.getOurCountry();
        if (order.serverLocation == null || order.serverLocation.isEmpty()) {
            order.serverLocation = ServerOrderCallbacks.getRecommendedLocation();
        }

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

        float halfWidth = (float) width / 2;

        addRenderableWidget(new ButtonMap(halfWidth - 228F, y - 94F, (int) (nawestX / scalingFactor), (int) (nawestY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-west", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth - 207F, y - 44.8F, (int) (nasouthX / scalingFactor), (int) (nasouthY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-south", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth - 176.5F, y - 96.5F, (int) (naeastX / scalingFactor), (int) (naeastY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "na-east", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth - 171.8F, y - 19.5F, (int) (southamericaX / scalingFactor), (int) (southamericaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "south-america", false, this::updateSelected));

        addRenderableWidget(new ButtonMap(halfWidth - 115F, y - 122F, (int) (greenlandX / scalingFactor), (int) (greenlandY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "greenland", false, this::updateSelected));

        addRenderableWidget(new ButtonMap(halfWidth - 30.8F, y - 101.2F, (int) (euwestX / scalingFactor), (int) (euwestY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-west", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth - 0.5F, y - 95F, (int) (eumiddleeastX / scalingFactor), (int) (eumiddleeastY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "eu-middle-east", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth + 24F, y - 114F, (int) (russiaX / scalingFactor), (int) (russiaY / scalingFactor), (int) (2048 / scalingFactor), (int) (2048 / scalingFactor), "russia", false, this::updateSelected));

        addRenderableWidget(new ButtonMap(halfWidth + 64.5F, y - 69F, (int) (asiaX / scalingFactor), (int) (asiaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "asia", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth + 119.5F, y - 5.5F, (int) (australiaX / scalingFactor), (int) (australiaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "australia", false, this::updateSelected));

        addRenderableWidget(new ButtonMap(halfWidth - 35.8F, y - 23.2F, (int) (subsaharanafricaX / scalingFactor), (int) (subsaharanafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "sub-saharan-africa", false, this::updateSelected));
        addRenderableWidget(new ButtonMap(halfWidth - 41.4F, y - 57.7F, (int) (northafricaX / scalingFactor), (int) (northafricaY / scalingFactor), (int) (1024 / scalingFactor), (int) (1024 / scalingFactor), "north-africa", false, this::updateSelected));

        //Should never be null but /shrug its Minecraft
        if (regions != null && !regions.isEmpty()) {
            try {
                regions.forEach((s, s2) ->
                {
                    children().forEach(child ->
                    {
                        if (child instanceof Button) {
                            Button widget = (Button) child;
                            if (widget.getMessage().getString().equalsIgnoreCase(s)) {
                                widget.active = true;
                            }
                            if (order.serverLocation != null) {
                                if (widget.getMessage().getString().equalsIgnoreCase(datacentreToRegion(order.serverLocation)) || widget.getMessage().getString().equalsIgnoreCase(order.serverLocation)) {
                                    updateSelected((Button) widget);
                                    if (dataCenters != null && distance.isEmpty()) {
                                        distance = dataCenters.get(order.serverLocation);
                                    }
                                }
                            }
                        }
                    });
                });
            } catch (Exception ignored) {
                LOGGER.error("Failed to get region map data");
            }
        }
    }

    private void updateSelected(Button button) {
        try {
            if (currentFocus != null) currentFocus.setFocused(false);
            this.currentFocus = button;
            button.setFocused(true);
            if (dataCenters != null && !dataCenters.isEmpty()) {
                distance = dataCenters.get(regionToDataCentre(currentFocus.getMessage().getString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderDirtBackground(graphics);
        graphics.fill(0, this.height - 20, width, 20, 0x99000000);
        super.render(graphics, p_render_1_, p_render_2_, p_render_3_);

        if (currentFocus != null) {
            RenderSystem.setShaderTexture(0, siv);
            int x = font.width(ttl(currentFocus.getMessage().getString()));
            int bufferLeft = 20;

            graphics.blit(siv, (this.width - bufferLeft) - x - 5, 20, 0, 0, x + bufferLeft, 20);

            graphics.drawString(font, ttl(currentFocus.getMessage().getString()), (this.width - x) - bufferLeft, 26, -1);

            //16 = 4 bars
            //32 = 3 bars
            //40 = 2 bars
            //48 = 1 bars
            if (distance != null && !distance.isEmpty()) {
                graphics.blit(GUI_ICONS_LOCATION, this.width - 18, 26, 8, 8, 0, distanceConvert(Integer.parseInt(distance)), 8, 8, 256, 256);
            }
        }
    }

    public static String ttl(String input) {
        String key = "minetogether.region." + input.toLowerCase(Locale.ROOT);
        if (I18n.exists(key)) return I18n.get(key);
        return input;
    }

    public int distanceConvert(int distance) {
        if (distance < 1000) return 16;
        if (distance > 1000 && distance < 5000) return 32;
        if (distance > 5000 && distance < 6000) return 40;

        return 48;
    }

    public String datacentreToRegion(String centre) {
        switch (centre) {
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
    public String regionToDataCentre(String region) {
        switch (region) {
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
    public String getStepName() {
        return I18n.get("minetogether.screen.server_location");
    }
}
