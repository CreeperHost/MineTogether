package net.creeperhost.minetogether.gui.dialogs;

import com.mojang.blaze3d.platform.InputConstants;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * TODO, Turn this into a more general purpose context menu and move it to Polylib
 * Created by brandon3055 on 23/09/2023
 */
public class TextInputDialog extends GuiElement<TextInputDialog> implements BackgroundRender {

    private final GuiTextField textField;
    @Nullable
    private Consumer<String> resultCallback;

    public TextInputDialog(@NotNull GuiParent<?> parent, Component title) {
        this(parent, title, "");
    }

    public TextInputDialog(@NotNull GuiParent<?> parent, Component title, String defaultText) {
        super(parent);
        this.setOpaque(true);

        GuiText titleText = new GuiText(this, title)
                .setWrap(true)
                .constrain(TOP, relative(get(TOP), 5))
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(HEIGHT, literal(font().wordWrapHeight(title, 200 - 10)));

        GuiRectangle textBg = new GuiRectangle(this)
                .fill(0xA0202020)
                .constrain(TOP, relative(titleText.get(BOTTOM), 3))
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(HEIGHT, literal(14));

        this.textField = new GuiTextField(textBg)
                .setEnterPressed(this::accept);
        Constraints.bind(textField, textBg, 0, 3, 0, 3);

        GuiButton accept = GuiButton.flatColourButton(this, () -> Component.translatable("minetogether:gui.button.ok"), hovered -> hovered ? 0xFF44AA44 : 0xFF118811)
                .onPress(this::accept)
                .constrain(TOP, relative(textBg.get(BOTTOM), 3))
                .constrain(LEFT, match(textBg.get(LEFT)))
                .constrain(RIGHT, midPoint(textBg.get(LEFT), textBg.get(RIGHT), -1))
                .constrain(HEIGHT, literal(14));

        GuiButton cancel = GuiButton.flatColourButton(this, () -> Component.translatable("minetogether:gui.button.cancel"), hovered -> hovered ? 0xFFAA4444 : 0xFF881111)
                .onPress(this::close)
                .constrain(TOP, relative(textBg.get(BOTTOM), 3))
                .constrain(LEFT, midPoint(textBg.get(LEFT), textBg.get(RIGHT), 1))
                .constrain(RIGHT, match(textBg.get(RIGHT)))
                .constrain(HEIGHT, literal(14));

        ModularGui gui = getModularGui();
        constrain(TOP, midPoint(gui.get(TOP), gui.get(BOTTOM), -20));
        constrain(LEFT, midPoint(gui.get(LEFT), gui.get(RIGHT), -100));
        constrain(BOTTOM, relative(cancel.get(BOTTOM), 5));
        constrain(WIDTH, literal(200));
        textField.setValue(defaultText);
        textField.setFocus(true);
    }

    public TextInputDialog setResultCallback(Consumer<String> resultCallback) {
        this.resultCallback = resultCallback;
        return this;
    }

    public GuiTextField getTextField() {
        return textField;
    }

    public void accept() {
        if (resultCallback != null) {
            resultCallback.accept(textField.getValue());
        }
        close();
    }

    public void close(){
        getParent().removeChild(this);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == InputConstants.KEY_ESCAPE) {
            close();
        }
        return true;
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        render.toolTipBackground(xMin(), yMin(), xSize(), ySize(), 0xFF100010, 0xFF5000FF, 0xFF28007f);
    }
}
