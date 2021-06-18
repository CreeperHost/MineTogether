package net.creeperhost.minetogether.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class MineTogetherScreen extends Screen
{
    public MineTogetherScreen(Component component)
    {
        super(component);
    }

    @Override
    public void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int i, int j)
    {
        super.renderComponentHoverEffect(poseStack, style, i, j);
    }

    public boolean handleComponentClicked(@Nullable Style style, double mouseX, double mouseY)
    {
        return false;
    }
}
