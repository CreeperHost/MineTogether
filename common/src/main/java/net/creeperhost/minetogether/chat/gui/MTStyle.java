package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.polylib.client.PolyPalette;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Assembly;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * For now everything here points to {@link PolyPalette}
 * But running everything through this class gives us the option to customize MT in the future.
 * <p>
 * Created by brandon3055 on 22/09/2023
 */
public class MTStyle {




    public static class Flat {

        public static GuiElement<?> background(GuiParent<?> parent) {
            return PolyPalette.Flat.background(parent);
        }

        public static GuiElement<?> contentArea(GuiElement<?> parent) {
            return PolyPalette.Flat.contentArea(parent);
        }

        public static GuiButton button(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.button(parent, label);
        }

        public static GuiButton button(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.button(parent, label);
        }

        public static GuiButton buttonCaution(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.buttonCaution(parent, label);
        }

        public static GuiButton buttonCaution(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.buttonCaution(parent, label);
        }

        public static GuiButton buttonPrimary(GuiElement<?> parent, Component label) {
            return PolyPalette.Flat.buttonPrimary(parent, label);
        }

        public static GuiButton buttonPrimary(GuiElement<?> parent, @Nullable Supplier<Component> label) {
            return PolyPalette.Flat.buttonPrimary(parent, label);
        }

        public static Assembly<? extends GuiElement<?>, GuiSlider> scrollBar(GuiElement<?> parent, Axis axis) {
            return PolyPalette.Flat.scrollBar(parent, axis);
        }

        //MT Specific

        public static int friendEntryBackground(boolean hoveredOrSelected) {
            return hoveredOrSelected ? 0x40FFFFFF : 0;
        }
    }
}
