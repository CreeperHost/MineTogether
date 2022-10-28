package net.creeperhost.minetogether.polylib.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 21/9/22.
 */
public class DropdownButton<E extends DropdownButton.DropdownEntry> extends Button {

    private static final OnPress NONE = e -> { };

    private final List<E> entries = new LinkedList<>();
    private final List<Runnable> dynamicCallbacks = new LinkedList<>();
    private final boolean isAnchored;
    private final boolean hasHeader;
    private final Consumer<E> onPressed;
    private boolean isFlipped;

    private boolean dropdownOpen;

    private E selected;

    public boolean wasJustClosed;

    public DropdownButton(int x, int y, boolean hasHeader, Consumer<E> callback) {
        this(x, y, 200, 20, true, hasHeader, callback);
    }

    /**
     * For movable dropdown buttons.
     */
    public DropdownButton(int width, int height, Consumer<E> callback) {
        this(-1000, -1000, width, height, false, false, callback);
    }

    public DropdownButton(int x, int y, int width, int height, boolean isAnchored, boolean hasHeader, Consumer<E> callback) {
        super(x, y, width, height, Component.empty(), NONE);
        this.isAnchored = isAnchored;
        this.hasHeader = hasHeader;
        onPressed = callback;
    }

    public DropdownButton<E> setFlipped(boolean isFlipped) {
        this.isFlipped = isFlipped;
        return this;
    }

    public DropdownButton<E> setSelected(E entry) {
        this.selected = entry;
        setMessage(selected.getTitle(false));
        return this;
    }

    @SafeVarargs
    public final DropdownButton<E> setEntries(E... entries) {
        return setEntries(Arrays.asList(entries));
    }

    public DropdownButton<E> setEntries(Collection<? extends E> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        return this;
    }

    public DropdownButton<E> withDynamicCallback(Runnable callback) {
        dynamicCallbacks.add(callback);
        return this;
    }

    @Override
    public void renderButton(PoseStack pStack, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        int drawY = y;
        Font font = Minecraft.getInstance().font;
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        isHovered = mouseX >= x && mouseY >= drawY && mouseX < x + width && mouseY < drawY + height;
        int i = getHoverState(isHovered);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if (hasHeader) {
            blit(pStack, x, drawY, 0, 46 + i * 20, width / 2, height);
            blit(pStack, x + width / 2, drawY, 200 - width / 2, 46 + i * 20, width / 2, height);
            int j = 14737632;

            if (!active) {
                j = 10526880;
            } else if (isHovered) {
                j = 16777120;
            }

            drawCenteredString(pStack, font, getMessage(), x + width / 2, y + (height - 8) / 2, j);
        }

        if (dropdownOpen) {
            drawY += 1;
            int yOffset = height - 2;
            if (isFlipped) {
                yOffset = -yOffset;
                drawY -= 1;
            }

            for (E e : entries) {
                drawY += yOffset;
                boolean ourHovered = mouseX >= x && mouseY >= drawY && mouseX < x + width && mouseY < drawY + height - 2;

                int subHovered = ourHovered ? 2 : 0;

                RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                // TODO: Fix rendering being dodgy, but it is "good enough" to avoid spending too much time on right now
                blit(pStack, x, drawY, 0, 46 + subHovered * 20 + 1, width / 2, height - 1);
                blit(pStack, x + width / 2, drawY, 200 - width / 2, 46 + subHovered * 20 + 1, width / 2, height - 1);

                int textColour = 14737632;
                drawCenteredString(pStack, font, e.getTitle(true), x + width / 2, drawY + (height - 10) / 2, textColour);
            }
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, button);
        if (dropdownOpen) {
            if (hasHeader) {
                if (pressed) {
                    close();
                    return false; // Clicked on button header. Nothing to do just close.
                }
            }
            E clicked = getClickedElement(mouseX, mouseY);
            if (clicked != null) {
                setSelected(clicked);
                onPressed.accept(clicked);
                playDownSound(Minecraft.getInstance().getSoundManager());
                close();
                return true; // We did a thing! \o/
            }
            close();
            return false; // Nope, just close it.
        }
        if (pressed) {
            if (hasHeader) {
                dropdownOpen = true;
                // Run dynamic callbacks to re-populate the button entries.
                for (Runnable dynamicCallback : dynamicCallbacks) {
                    dynamicCallback.run();
                }
            }
        }

        return false;
    }

    public void close() {
        dropdownOpen = false;
        wasJustClosed = true;
        if (!isAnchored) {
            x = -1000;
            y = -1000;
        }
    }

    public void openAt(double mouseX, double mouseY) {
        if (isAnchored) throw new UnsupportedOperationException("Cannot move anchored dropdown.");
        x = (int) mouseX;
        y = (int) mouseY;
        isFlipped = mouseY > 150; // TODO constant? should this be based off the number of entries?
        if (!isFlipped) {
            y -= getHeight() - 1;
            x++;
        }
        dropdownOpen = true;
    }

    private int getHoverState(boolean mouseOver) {
        if (mouseOver) return 2;
        if (!active) return 0;

        return dropdownOpen ? 2 : 1;
    }

    private E getClickedElement(double mouseX, double mouseY) {
        E clickedElement = null;
        int y = this.y + 1;

        int yOffset = height - 2;
        if (isFlipped) {
            yOffset = -yOffset;
            y -= 1;
        }
        for (E e : entries) {
            y += yOffset;
            if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height - 2) {
                clickedElement = e;
                break;
            }
        }
        return clickedElement;
    }

    public interface DropdownEntry {

        Component getTitle(boolean isOpen);
    }
}
