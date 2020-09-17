package net.creeperhost.minetogether.client.screen.mpreplacement;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.order.GuiGetServer;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.ConfigHandler;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class CreeperHostEntry extends ServerData
{
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/hidebtn.png");
    private final Minecraft mc = Minecraft.getInstance();
    private final String cross;
    private final int stringWidth;
    private ResourceLocation serverIcon;

    private float transparency = 0.5F;

    protected CreeperHostEntry(String name, String ip, boolean isLan)
    {
        super(name, ip, isLan);
        serverIcon = Config.getInstance().isServerHostMenuImage() ? MineTogether.instance.getImplementation().getMenuIcon() : new ResourceLocation(Constants.MOD_ID, "textures/nobrandmp.png");
        cross = new String(Character.toChars(10006));
        stringWidth = this.mc.fontRenderer.getStringWidth(cross);
    }
    
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }
    
    @SuppressWarnings("Duplicates")
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int x, int y)
    {
        if (x >= 303 - stringWidth - 2 && x <= 303 - 3 && y >= 0 && y <= 7)
        {
            Config.getInstance().setMpMenuEnabled(false);
            ConfigHandler.saveConfig();
            this.mc.displayGuiScreen(new MultiplayerScreen(null));
            return true;
        }
        Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
        return true;
    }
}
