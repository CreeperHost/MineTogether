package net.creeperhost.minetogether.gui.dialogs;

import com.mojang.blaze3d.platform.InputConstants;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.TextState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 09/10/2023
 */
public class ItemSelectDialog<E> extends GuiElement<ItemSelectDialog<E>> implements BackgroundRender {

    private final GuiTextField searchField;
    private final GuiList<E> itemList;
    @Nullable
    private Consumer<E> onItemSelected;
    private final List<E> items;
    private E selected;

    public ItemSelectDialog(@NotNull GuiParent<?> parent, Component title, List<E> items) {
        this(parent, title, items, null);
    }

    public ItemSelectDialog(@NotNull GuiParent<?> parent, Component title, List<E> items, E defaultItem) {
        super(parent);
        this.items = items;
        this.selected = defaultItem;
        this.setOpaque(true);

        //Title
        GuiText titleText = new GuiText(this, title)
                .setWrap(true)
                .constrain(TOP, relative(get(TOP), 5))
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(HEIGHT, dynamic(() -> (double) font().wordWrapHeight(title.getString(), (int) xSize() - 10)));

        //Buttons
        GuiButton select = button(this, new TranslatableComponent("minetogether:gui.select_dialog.select"))
                .setDisabled(() -> selected == null)
                .onPress(this::accept)
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, midPoint(get(LEFT), get(RIGHT), -1))
                .constrain(BOTTOM, relative(get(BOTTOM), -5))
                .constrain(HEIGHT, literal(12));

        GuiButton cancel = button(this, new TranslatableComponent("minetogether:gui.select_dialog.cancel"))
                .onPress(this::close)
                .constrain(LEFT, midPoint(get(LEFT), get(RIGHT), 1))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(BOTTOM, relative(get(BOTTOM), -5))
                .constrain(HEIGHT, literal(12));

        //Search
        GuiRectangle searchBg = new GuiRectangle(this)
                .fill(0xA0202020)
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(BOTTOM, relative(select.get(TOP), -2))
                .constrain(HEIGHT, literal(14));

        this.searchField = new GuiTextField(searchBg)
                .setTextState(TextState.simpleState("", s -> reloadItems()))
                .setSuggestion(new TranslatableComponent("minetogether:gui.select_dialog.search"));
        Constraints.bind(searchField, searchBg, 0, 3, 0, 3);

        //List in the middle
        GuiRectangle listBg = new GuiRectangle(this)
//                .fill(0xA0202020)
                .constrain(LEFT, relative(get(LEFT), 5))
                .constrain(RIGHT, relative(get(RIGHT), -5))
                .constrain(TOP, relative(titleText.get(BOTTOM), 3))
                .constrain(BOTTOM, relative(searchBg.get(TOP), -2));

        this.itemList = new GuiList<E>(this)
                .addHiddenScrollBar()
                .setItemSpacing(1);
        Constraints.bind(itemList, listBg, 0);

        itemList.setDisplayBuilder((list, item) -> {
            Component text = new TextComponent(String.valueOf(item));

            GuiButton button = GuiButton.flatColourButton(list, null, highlight -> highlight ? 0xA0808080 : 0xA0202020)
                    .setToggleMode(() -> item.equals(selected))
                    .onPress(() -> selectItem(item));
            button.constrain(HEIGHT, dynamic(() -> (double)font().wordWrapHeight(text.getString(), (int)button.xSize() - 4) + 2));

            GuiText display = new GuiText(button, text)
                    .setWrap(true);
            Constraints.bind(display, button, 0, 2, 0, 2);

            return button;
        });

        //Position Dialog
        ModularGui gui = getModularGui();
        constrain(TOP, midPoint(gui.get(TOP), gui.get(BOTTOM), -100));
        constrain(LEFT, midPoint(gui.get(LEFT), gui.get(RIGHT), -75));
        constrain(WIDTH, literal(150));
        constrain(HEIGHT, literal(200));
        reloadItems();
    }

    public GuiList<E> getItemList() {
        return itemList;
    }

    public void reloadItems() {
        itemList.getList().clear();
        String search = searchField.getValue();
        for (E item : items) {
            if (search.isEmpty() || String.valueOf(item).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                itemList.add(item);
            }
        }
        itemList.markDirty();
        if (selected != null) {
            itemList.scrollTo(selected);
        }
    }

    public static GuiButton button(GuiElement<?> parent, Component label) {
        GuiButton button = new GuiButton(parent);
        GuiRectangle background = new GuiRectangle(button)
                .fill(() -> button.isDisabled() ? 0x88202020 : (button.isMouseOver() || button.toggleState() || button.isPressed() ? 0xFF909090 : 0xFF505050));
        Constraints.bind(background, button);

        GuiText text = new GuiText(button, label);
        button.setLabel(text);
        Constraints.bind(text, button, 0, 2, 0, 2);
        return button;
    }

    public ItemSelectDialog<E> setOnItemSelected(Consumer<E> onItemSelected) {
        this.onItemSelected = onItemSelected;
        return this;
    }

    public void selectItem(E item) {
        if (item != null && item.equals(selected)) {
            accept();
        }
        this.selected = item;
    }

    public void accept() {
        if (onItemSelected != null && selected != null) {
            onItemSelected.accept(selected);
        }
        close();
    }

    public void close() {
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
