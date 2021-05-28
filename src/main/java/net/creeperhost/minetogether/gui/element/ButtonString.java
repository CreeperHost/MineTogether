package net.creeperhost.minetogether.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.function.Supplier;

public class ButtonString extends GuiButtonExt
{
    private final Supplier<String> displayGetter;
    private final RenderPlace place;

    public enum RenderPlace {
        EXACT,
        CENTRED
    }

    public ButtonString(int id, int xPos, int yPos, Supplier<String> displayGetter, RenderPlace place)
    {
        super(id, xPos, yPos, displayGetter.get());
        this.displayGetter = displayGetter;
        this.place = place;
    }

    public ButtonString(int id, int xPos, int yPos, String displayString)
    {
        this(id, xPos, yPos, () -> displayString, RenderPlace.CENTRED);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if(hasText()) {
            return super.mousePressed(mc, mouseX, mouseY);
        }
        return false;
    }

    @Override
    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        this.visible = hasText();
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            this.mouseDragged(mc, mouseX, mouseY);

            String buttonText = displayGetter.get();

            if(place == RenderPlace.CENTRED) {
                this.drawCenteredString(mc.fontRendererObj, buttonText, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xFFFFFF);
            } else {
                this.drawString(mc.fontRendererObj, buttonText, xPosition, yPosition, 0xFFFFFF);
            }

        }
    }

    public boolean hasText() {
        return !displayGetter.get().isEmpty();
    }
}
