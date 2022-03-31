//package net.creeperhost.minetogethergui.widgets;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiComponent;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.network.chat.Component;
//
//import java.util.function.Supplier;
//
//public class ButtonString extends Button
//{
//    private final Supplier<Component> displayGetter;
//    private final RenderPlace renderPlace;
//
//    public enum RenderPlace
//    {
//        EXACT,
//        CENTRED
//    }
//
//    public ButtonString(int xPos, int yPos, int width, int height, Supplier<Component> displayGetter, RenderPlace renderPlace, OnPress onPress)
//    {
//        super(xPos, yPos, width, height, displayGetter.get(), onPress);
//        this.displayGetter = displayGetter;
//        this.renderPlace = renderPlace;
//    }
//
//    public ButtonString(int xPos, int yPos, int width, int height, Component displayString, OnPress onPress)
//    {
//        this(xPos, yPos, width, height, () -> displayString, RenderPlace.CENTRED, onPress);
//    }
//
//    @Override
//    public boolean mouseClicked(double d, double e, int i)
//    {
//        if(hasText())
//        {
//            return super.mouseClicked(d, e, i);
//        }
//        return false;
//    }
//
//    @Override
//    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial)
//    {
//        this.visible = hasText();
//        if (this.visible)
//        {
//            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
//
//            Component buttonText = displayGetter.get();
//
//            if(renderPlace == RenderPlace.CENTRED) {
//                GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFF);
//            } else {
//                GuiComponent.drawString(poseStack, Minecraft.getInstance().font, buttonText, x, y, 0xFFFFFF);
//            }
//        }
//    }
//
//    public boolean hasText()
//    {
//        return !displayGetter.get().toString().isEmpty();
//    }
//}
