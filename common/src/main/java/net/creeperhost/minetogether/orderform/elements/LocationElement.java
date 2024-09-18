package net.creeperhost.minetogether.orderform.elements;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.minetogether.orderform.requests.GetDataCentresRequest;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.helpers.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.minecraft.ChatFormatting.RED;

/**
 * Created by brandon3055 on 28/06/2024
 */
public class LocationElement extends GuiElement<LocationElement> {

    private final OrderGui gui;
    public GuiElement<?> locations;

    public LocationElement(@NotNull GuiParent<?> parent, OrderGui gui) {
        super(parent);
        this.gui = gui;

        GuiElement<?> lastElement = new GuiText(this, Component.translatable("minetogether:gui.order.location").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, match(get(TOP)))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        lastElement = locations = new GuiElement<>(this)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        GuiText locationLoading = new GuiText(locations, Component.translatable("minetogether:gui.order.loading_locations").withStyle(ChatFormatting.YELLOW))
                .setAlignment(Align.LEFT);
        Constraints.bind(locationLoading, locations);

        Component pingInfo = Component.translatable("minetogether:gui.order.region.signal").withStyle(ChatFormatting.GRAY);
        lastElement = new GuiText(this, pingInfo)
                .setWrap(true)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)));
        lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(pingInfo, (int) xMax() - (int) xMin())));

        constrain(BOTTOM, match(lastElement.get(BOTTOM)));
//        Constraints.bind(new GuiRectangle(this).border(0xFF00FF00), this);
    }

    public void updateLocations() {
        locations.getChildren().forEach(locations::removeChild);
        if (gui.dcMap.isEmpty()) {
            locations.constrain(HEIGHT, literal(8));
            GuiText error = new GuiText(locations, Component.translatable("minetogether:gui.order.loading_locations_fail").withStyle(ChatFormatting.RED))
                    .setAlignment(Align.LEFT);
            Constraints.bind(error, locations);
        } else {
            List<GetDataCentresRequest.DC> dcOrder = new ArrayList<>(gui.dcMap.values());
            GuiElement<?> element = null;
            dcOrder.sort(Comparator.comparingDouble(dc -> gui.dcPing.getOrDefault(dc.slug, -1) < 0 ? 5000 : gui.dcPing.getOrDefault(dc.slug, -1) + (dc.available ? 0 : 5000)));
            for (GetDataCentresRequest.DC dc : dcOrder) {
                element = locationButton(locations, dc)
                        .constrain(TOP, element == null ? match(locations.get(TOP)) : relative(element.get(BOTTOM), 1))
                        .constrain(LEFT, match(locations.get(LEFT)))
                        .constrain(RIGHT, match(locations.get(RIGHT)));
            }
            if (element != null) {
                locations.constrain(BOTTOM, match(element.get(BOTTOM)));
            }
        }
    }

    private GuiElement<?> locationButton(GuiElement<?> parent, GetDataCentresRequest.DC dc) {
        dc.available = true; //No longer display the low availability warning, It's all just handled behind the scenes.

        String name = dc.slug == null ? "" : dc.slug;
        GuiButton button = MTStyle.Flat.button(parent, (Supplier<Component>) null)
                .setToggleMode(() -> name.equals(gui.order.serverLocation))
                .onPress(() -> {
                    gui.order.serverLocation = name;
                    gui.summaryDirty();
                })
                .constrain(HEIGHT, literal(dc.available ? 12 : 32));

        double ping = gui.dcPing.getOrDefault(name, -2);
        long distance = gui.dcDistance.getOrDefault(name, -1L);
        Component pingText = Component.literal(((int) Math.ceil(ping)) + " ms");
        GuiText pingLabel = new GuiText(button, pingText)
                .setEnabled(() -> ping > 0)
                .setAlignment(Align.RIGHT)
                .setScroll(false)
                .constrain(TOP, relative(button.get(TOP), 2))
                .constrain(WIDTH, literal(parent.font().width(pingText)))
                .constrain(RIGHT, relative(button.get(RIGHT), -14))
                .constrain(HEIGHT, literal(8));

        GuiTexture flag = new GuiTexture(button, gui.getFlag(dc))
                .constrain(TOP, relative(button.get(TOP), 2))
                .constrain(LEFT, relative(button.get(LEFT), 2))
                .constrain(HEIGHT, literal(8))
                .constrain(WIDTH, dynamic(() -> gui.flagWidth(dc, 8)));

        GuiText label = new GuiText(button, Component.literal(dc.name + ", " + dc.countryName))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(button.get(TOP), 2))
                .constrain(LEFT, dynamic(() -> Math.max(flag.xMax() + 2, button.xMin() + 21)))
                .constrain(RIGHT, relative(pingLabel.get(LEFT), -3))
                .constrain(HEIGHT, literal(8));

        GuiTexture signal = new GuiTexture(button, MTTextures.getter(() -> getSignalIcon(ping, distance)))
                .setTooltipSingle(() -> getSignalTooltip(ping, distance))
                .constrain(TOP, match(button.get(TOP)))
                .constrain(RIGHT, match(button.get(RIGHT)))
                .constrain(HEIGHT, literal(12))
                .constrain(WIDTH, literal(12));

        if (!dc.available) {
            GuiText lowAvail = new GuiText(button, Component.translatable("minetogether:gui.order.low_availability").withStyle(RED))
                    .setAlignment(Align.LEFT)
                    .setWrap(true)
                    .constrain(BOTTOM, relative(button.get(BOTTOM), -2))
                    .constrain(LEFT, relative(button.get(LEFT), 4))
                    .constrain(RIGHT, relative(button.get(RIGHT), -4))
                    .autoHeight();

            button.constrain(HEIGHT, dynamic(() -> 12 + lowAvail.ySize() + 4));
        }

        return button;
    }

    private String getSignalIcon(double ping, long distance) {
        if (ping > 0) {
            int icon = MathUtil.clamp(5 - (int) (ping / 42), 1, 5);
            return "signal/signal_" + icon;
        } else if (distance != -1) {
            if (distance < 1000) return "signal/signal_5";
            if (distance > 1000 && distance < 3000) return "signal/signal_4";
            if (distance > 3000 && distance < 5000) return "signal/signal_3";
            if (distance > 5000 && distance < 6000) return "signal/signal_2";
            return "signal/signal_1";
        } else if (ping == -1) {
            int l = (int) (Util.getMillis() / 100L & 7L);
            if (l > 4) {
                l = 8 - l;
            }
            return "signal/scan_" + l;
        }
        return "signal/signal_0";
    }

    private Component getSignalTooltip(double ping, long distance) {
        if (ping > 0) {
            return Component.translatable("minetogether:gui.order.region.signal");
        } else if (distance > 0) {
            return Component.translatable("minetogether:gui.order.region.from_distance");
        } else if (ping == -1) {
            return Component.translatable("minetogether:gui.order.region.pinging");
        }
        return Component.translatable("minetogether:gui.order.region.pinging_fail");
    }
}
