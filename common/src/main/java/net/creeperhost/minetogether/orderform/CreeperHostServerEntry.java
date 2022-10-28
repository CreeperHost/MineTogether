package net.creeperhost.minetogether.orderform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.screen.OrderServerScreen;
import net.creeperhost.polylib.client.screen.widget.buttons.ButtonString;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CreeperHostServerEntry extends ServerSelectionList.NetworkServerEntry {

    private final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation serverIcon = new ResourceLocation(MineTogether.MOD_ID, "textures/creeperhost.png");
    private float transparency = 0.5F;
    protected final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(MineTogether.MOD_ID, "textures/hidebtn.png");
    private ServerSelectionList serverSelectionList;
    private Button removeButton;

    public CreeperHostServerEntry(ServerSelectionList serverSelectionList) {
        super(null, new LanServer("", ""));
        this.serverSelectionList = serverSelectionList;

        removeButton = new ButtonString(0, 0, 10, 10, Component.translatable(ChatFormatting.RED + new String(Character.toChars(10006))), button ->
        {
            Config.instance().mpMenuEnabled = false;
            Config.save();
            this.mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        });
    }

    @Override
    public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_) {
        if (isHovering) {
            if (transparency <= 1.0F) transparency += 0.04;
        } else {
            if (transparency >= 0.5F) transparency -= 0.04;
        }

        RenderSystem.setShaderTexture(0, serverIcon);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, transparency);
        Screen.blit(matrixStack, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        int transparentString = (int) (transparency * 254) << 24;
        this.mc.font.draw(matrixStack, I18n.get("minetogether.multiplayerscreen.partner"), x + 35, y, 16777215 + transparentString);
        RenderSystem.enableBlend();
        this.mc.font.draw(matrixStack, I18n.get("minetogether.multiplayerscreen.getserver"), x + 32 + 3, y + this.mc.font.lineHeight + 1, 16777215 + transparentString);
        String s = I18n.get("minetogether.multiplayerscreen.clickherebrand");
        this.mc.font.draw(matrixStack, s, x + 32 + 3, y + (this.mc.font.lineHeight * 2) + 3, 8421504 + transparentString);

        if (removeButton != null) {
            removeButton.render(matrixStack, x, y, p_render_9_);
            removeButton.x = listWidth + x - Minecraft.getInstance().font.width(new String(Character.toChars(10006))) - 4;
            removeButton.y = y;

            if (removeButton.isMouseOver(mouseX, mouseY)) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                final int tooltipY = mouseY + ((mc.screen.width / 2 >= mouseY) ? 11 : -11);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, transparency);
                RenderSystem.setShaderTexture(0, BUTTON_TEXTURES);
                Screen.blit(matrixStack, mouseX - 74, tooltipY - 1, 0.0F, 0.0F, 60, 10, 60, 10);
            }
        }
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        if (removeButton.isMouseOver(d, e)) return true;
        return super.isMouseOver(d, e);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (removeButton != null && removeButton.mouseClicked(mouseX, mouseY, button)) return true;
        Minecraft.getInstance().setScreen(OrderServerScreen.getByStep(0, new Order(), new JoinMultiplayerScreen(new TitleScreen())));
        return true;
    }

}
