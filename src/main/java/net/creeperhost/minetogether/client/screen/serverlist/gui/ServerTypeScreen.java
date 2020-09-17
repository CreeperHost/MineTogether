package net.creeperhost.minetogether.client.screen.serverlist.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.element.GuiButtonLarge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ServerTypeScreen extends Screen
{
    private GDPRScreen.IScreenGetter getter = null;
    private Screen parent = null;
    
    public ServerTypeScreen()
    {
        super(new StringTextComponent(""));
    }
    
    public ServerTypeScreen(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }
    
    public ServerTypeScreen(Screen parent, GDPRScreen.IScreenGetter getterIn)
    {
        this(parent);
        getter = getterIn;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5f, 1.5f, 1.5f);
        drawCenteredString(matrixStack, font, TextFormatting.BOLD + I18n.format("minetogether.listing.title"), (width / 3), 12, -1);
        RenderSystem.popMatrix();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void init()
    {
        super.init();
        
        buttons.clear();
        
        addButton(new GuiButtonLarge((width / 2) - 180, (height / 8) + 20, 120, 165, "PUBLIC", I18n.format("minetogether.listing.public"), new ItemStack(Items.GUNPOWDER), p ->
        {
            Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(new MainMenuScreen(), MultiplayerPublicScreen.ListType.PUBLIC, MultiplayerPublicScreen.SortOrder.RANDOM, true));
        }));
        addButton(new GuiButtonLarge((width / 2) - 60, (height / 8) + 20, 120, 165, "COMMUNITY", I18n.format("minetogether.listing.community"), new ItemStack(Items.FISHING_ROD), p ->
        {
            Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(new MainMenuScreen(), MultiplayerPublicScreen.ListType.APPLICATION, MultiplayerPublicScreen.SortOrder.RANDOM, true));
        }));
        addButton(new GuiButtonLarge((width / 2) + 60, (height / 8) + 20, 120, 165, "CLOSED", I18n.format("minetogether.listing.closed"), new ItemStack(Items.CHAINMAIL_CHESTPLATE), p ->
        {
            Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(new MainMenuScreen(), MultiplayerPublicScreen.ListType.INVITE, MultiplayerPublicScreen.SortOrder.RANDOM, true));
        }));
        addButton(new Button((width / 2) - 110, height - 22, 220, 20, new StringTextComponent(I18n.format("gui.cancel")), p ->
        {
            Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(new MainMenuScreen()));
        }));
    }
    
    @FunctionalInterface
    public interface IScreenGetter
    {
        Screen method();
    }
}
