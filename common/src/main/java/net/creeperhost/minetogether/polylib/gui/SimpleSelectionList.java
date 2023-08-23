package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;

/**
 * Created by covers1624 on 24/8/22.
 */
public class SimpleSelectionList<E extends SimpleSelectionList.SimpleEntry<E>> extends AbstractSelectionList<E> {

    private final int rowWidth;
    protected int scrollBarPosition = -1;

    public SimpleSelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int entryHeight) {
        this(minecraft, width, height, y0, y1, entryHeight, 220);
    }

    public SimpleSelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int entryHeight, int rowWidth) {
        super(minecraft, width, height, y0, y1, entryHeight);
        this.rowWidth = rowWidth;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getRowTop(E entry) {
        return getRowTop(children().indexOf(entry));
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public void setScrollBarPosition(int scrollBarPosition) {
        this.scrollBarPosition = scrollBarPosition;
    }

    @Override
    protected int getScrollbarPosition() {
        return scrollBarPosition == -1 ? super.getScrollbarPosition() : scrollBarPosition;
    }

    public static class SimpleEntry<E extends SimpleEntry<E>> extends Entry<E> {

        protected final SimpleSelectionList<E> list;

        public SimpleEntry(SimpleSelectionList<E> list) {
            this.list = list;
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean hovered, float partialTicks) {
        }


        @Override
        @SuppressWarnings ("unchecked")
        public boolean mouseClicked(double mouseX, double mouseY, int i) {
            // Set element as selected on click.
            list.setSelected((E) this);
            return false;
        }
    }
}
