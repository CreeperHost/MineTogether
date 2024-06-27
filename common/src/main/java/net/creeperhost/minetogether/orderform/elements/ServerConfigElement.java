package net.creeperhost.minetogether.orderform.elements;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.TextState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 28/06/2024
 */
public class ServerConfigElement extends GuiElement<ServerConfigElement> {

    private GuiTextField nameField;

    public ServerConfigElement(@NotNull GuiParent<?> parent, OrderGui gui) {
        super(parent);

        Constraint midPos = relative(get(LEFT), 90);

        GuiElement<?> lastElement = new GuiText(this, Component.translatable("minetogether:gui.order.configure").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, match(get(TOP)))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        //Server Name
        lastElement = new GuiText(this, Component.translatable("minetogether:gui.order.server_name"))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 5))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, midPos)
                .constrain(HEIGHT, literal(14));

        GuiButton randomise = MTStyle.Flat.button(this, Component.translatable("minetogether:gui.order.button.randomize"))
                .onPress(() -> nameField.setValue(OrderGui.getDefaultName()))
                .constrain(TOP, match(lastElement.get(TOP)))
                .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(WIDTH, literal(70));

        GuiElement<?> nameBackground = MTStyle.Flat.contentArea(this)
                .constrain(TOP, match(lastElement.get(TOP)))
                .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                .constrain(LEFT, midPos)
                .constrain(RIGHT, relative(randomise.get(LEFT), -2));

        GuiElement<?> highlight = new GuiRectangle(nameBackground)
                .border(0x50FFFFFF)
                .fill(0x30FFFFFF);
        Constraints.bind(highlight, nameBackground);

        Pattern namePattern = Pattern.compile("([A-Za-z0-9]*)");
        nameField = new GuiTextField(nameBackground)
                .setTextState(TextState.create(() -> gui.order.name, s -> {
                    gui.order.name = s;
                    gui.nameDirty();
                }))
                .setMaxLength(16)
                .setFilter(s -> s.isEmpty() || namePattern.matcher(s).matches());
        Constraints.bind(nameField, nameBackground, 0, 3, 0, 3);
        highlight.setEnabled(nameField::isFocused);

        //Player Count
        lastElement = new GuiText(this, Component.translatable("minetogether:gui.order.player_count"))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 5))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, midPos)
                .constrain(HEIGHT, literal(14));

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            int count = 5 + (i * 5);
            MTStyle.Flat.button(this, Component.literal(String.valueOf(count)))
                    .setToggleMode(() -> gui.order.playerAmount == count)
                    .onPress(() -> {
                        gui.order.playerAmount = count;
                        gui.locations.updateLocations();
                        gui.summaryDirty();
                    })
                    .constrain(TOP, match(lastElement.get(TOP)))
                    .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                    .constrain(LEFT, dynamic(() -> midPos.get() + ((((xMax() + 1) - midPos.get()) / 5) * finalI)).precise())
                    .constrain(WIDTH, dynamic(() -> (((xMax() + 1) - midPos.get()) / 5) - 1).precise());
        }

        Component playerCountInfo = Component.translatable("minetogether:gui.order.player_count.info").withStyle(ChatFormatting.GRAY);
        lastElement = new GuiText(this, playerCountInfo)
                .setWrap(true)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)));
        lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(playerCountInfo, (int) xMax() - (int) xMin())));

        constrain(BOTTOM, match(lastElement.get(BOTTOM)));
//        Constraints.bind(new GuiRectangle(this).border(0xFFFF0000), this);
    }
}
