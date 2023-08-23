package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * An incredibly simple widget for displaying tooltips for other components.
 * <p>
 * Created by covers1624 on 18/10/22.
 */
public class TooltipContainer implements Widget {

    private final Screen parent;
    private final List<Entry<?>> tooltipList = new LinkedList<>();

    public TooltipContainer(Screen parent) {
        this.parent = parent;
    }

    public <C extends GuiComponent & GuiEventListener> TooltipContainer addTooltip(C component, Component comp) {
        return addTooltip(component, e -> comp);
    }

    public <C extends GuiComponent & GuiEventListener> TooltipContainer addTooltip(C component, Function<? super C, Component> func) {
        tooltipList.add(new Entry<>(component, func));
        return this;
    }

    @Override
    public void render(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        for (Entry<?> entry : tooltipList) {
            if (entry.component.isMouseOver(mouseX, mouseY)) {
                Component component = entry.getTooltip();
                if (component != null) {
                    parent.renderTooltip(pStack, component, mouseX, mouseY);
                }
            }
        }
    }

    private static final class Entry<C extends GuiComponent & GuiEventListener> {

        private final C component;
        private final Function<? super C, @Nullable Component> func;

        private Entry(C component, Function<? super C, @Nullable Component> func) {
            this.component = component;
            this.func = func;
        }

            @Nullable
            public Component getTooltip() {
                return func.apply(component);
            }

        public C component() { return component; }

        public Function<? super C, @Nullable Component> func() { return func; }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Entry that = (Entry) obj;
            return Objects.equals(this.component, that.component) &&
                    Objects.equals(this.func, that.func);
        }

        @Override
        public int hashCode() {
            return Objects.hash(component, func);
        }

        @Override
        public String toString() {
            return "Entry[" +
                    "component=" + component + ", " +
                    "func=" + func + ']';
        }

        }
}
