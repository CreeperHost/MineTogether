package net.creeperhost.minetogether.gui.serverlist.gui;

import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.serverlist.gui.elements.GuiButtonLarge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.Color;

import java.io.IOException;

public class GuiServerType extends GuiScreen
{
    private GuiButton PUBLIC;
    private GuiButton COMMUNITY;
    private GuiButton APPLICATION;
    private GuiButton EXIT;

    private final int PUBLIC_ID = 1;
    private final int COMMUNITY_ID = 2;
    private final int APPLICATION_ID = 3;
    private final int EXIT_ID = 4;
    
    private GuiGDPR.IScreenGetter getter = null;
    private GuiScreen parent = null;
    
    
    public GuiServerType()
    {

    }
    
    public GuiServerType(GuiScreen parent)
    {
        this.parent = parent;
    }
    
    public GuiServerType(GuiScreen parent, GuiGDPR.IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5f, 1.5f, 1.5f);
        drawCenteredString(fontRendererObj, TextFormatting.BOLD  + I18n.format("minetogether.listing.title"), (width/3), 12, -1);
        GlStateManager.popMatrix();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        buttonList.clear();
        buttonList.add(PUBLIC = new GuiButtonLarge(1, (width / 2) - 180, (height / 8)+20, 120, 165, "PUBLIC", I18n.format("minetogether.listing.public"), new ItemStack(Items.BANNER)));
        buttonList.add(COMMUNITY = new GuiButtonLarge(2, (width / 2) - 60, (height / 8)+20, 120, 165, "COMMUNITY", I18n.format("minetogether.listing.community"), new ItemStack(Items.OAK_DOOR)));
        buttonList.add(APPLICATION = new GuiButtonLarge(3, (width / 2) + 60, (height / 8)+20, 120, 165, "CLOSED", I18n.format("minetogether.listing.closed"), new ItemStack(Items.IRON_DOOR)));
        buttonList.add(EXIT = new GuiButton(4, (width / 2)-110, height - 22, 220, 20, I18n.format("gui.cancel")));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch(button.id)
        {
            case PUBLIC_ID:
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.PUBLIC, GuiMultiplayerPublic.SortOrder.NAME, true));
                break;
            case COMMUNITY_ID:
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.INVITE, GuiMultiplayerPublic.SortOrder.NAME, true));
                break;
            case APPLICATION_ID:
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayerPublic(parent, GuiMultiplayerPublic.ListType.APPLICATION, GuiMultiplayerPublic.SortOrder.NAME, true));
                break;
            default:
                Minecraft.getMinecraft().displayGuiScreen(new GuiMockMultiplayer());
        }
        super.actionPerformed(button);
    }
    
    @FunctionalInterface
    public interface IScreenGetter
    {
        GuiScreen method();
    }
    
}
