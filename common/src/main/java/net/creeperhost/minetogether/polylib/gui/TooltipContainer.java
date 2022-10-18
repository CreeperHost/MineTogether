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

    private record Entry<C extends GuiComponent & GuiEventListener>(C component, Function<? super C, @Nullable Component> func) {

        @Nullable
        public Component getTooltip() {
            return func.apply(component);
        }
    }
}
