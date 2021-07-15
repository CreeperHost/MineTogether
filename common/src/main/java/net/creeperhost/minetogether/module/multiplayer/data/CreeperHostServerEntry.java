package net.creeperhost.minetogether.module.multiplayer.data;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.mixin.MixinSelectionList;
import net.creeperhost.minetogether.module.serverorder.screen.OrderServerScreen;
import net.creeperhost.minetogetherlib.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class CreeperHostServerEntry extends ServerSelectionList.NetworkServerEntry
{
    private final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation serverIcon = new ResourceLocation(Constants.MOD_ID, "textures/creeperhost.png");
    private float transparency = 0.5F;
    private final String cross = new String(Character.toChars(10006));
    private final int stringWidth;
    protected final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/hidebtn.png");
    private ServerSelectionList serverSelectionList;

    public CreeperHostServerEntry(ServerSelectionList serverSelectionList)
    {
        super(null, null);
        stringWidth = this.mc.font.width(cross);
        this.serverSelectionList = serverSelectionList;
    }

    @Override
    public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_)
    {
        if (isHovering)
        {
            if (transparency <= 1.0F) transparency += 0.04;
        }
        else
        {
            if (transparency >= 0.5F) transparency -= 0.04;
        }

        this.mc.getTextureManager().bind(serverIcon);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, transparency);
        Screen.blit(matrixStack, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        int transparentString = (int) (transparency * 254) << 24;
        this.mc.font.draw(matrixStack, I18n.get("minetogether.multiplayerscreen.partner"), x + 35, y, 16777215 + transparentString);
        RenderSystem.enableBlend();
        this.mc.font.draw(matrixStack, I18n.get("minetogether.multiplayerscreen.getserver"), x + 32 + 3, y + this.mc.font.lineHeight + 1, 16777215 + transparentString);
        String s = I18n.get("minetogether.multiplayerscreen.clickherebrand");
        this.mc.font.draw(matrixStack, s, x + 32 + 3, y + (this.mc.font.lineHeight * 2) + 3, 8421504 + transparentString);
        this.mc.font.drawShadow(matrixStack, cross, listWidth + x - stringWidth - 4, y, 0xFF0000 + transparentString);
        if (mouseX >= listWidth + x - stringWidth - 4 && mouseX <= listWidth - 5 + x && mouseY >= y && mouseY <= y + 7)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            final int tooltipY = mouseY + ((mc.screen.width / 2 >= mouseY) ? 11 : -11);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, transparency);
            mc.getTextureManager().bind(BUTTON_TEXTURES);
            Screen.blit(matrixStack, mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60, 10);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int listWidth = ((((MixinSelectionList) serverSelectionList).getWidth() - serverSelectionList.getRowWidth()) / 2) + serverSelectionList.getRowWidth();

        int y = ((MixinSelectionList) serverSelectionList).invokeRowTop(serverSelectionList.children().indexOf(this));

        if (mouseX >= listWidth - stringWidth - 4 && mouseX <= listWidth - 5 && mouseY >= y && mouseY >= y - 7)
        {
            Config.getInstance().setMpMenuEnabled(false);
            Config.saveConfig();
            this.mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
            return true;
        }
        Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), new JoinMultiplayerScreen(new TitleScreen())));
        return true;
    }
}
