package net.creeperhost.minetogether.client.screen.order;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.element.GuiButtonRefresh;
import net.creeperhost.minetogether.client.screen.list.GuiList;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryModpack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.ModPack;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GuiModPackList extends Screen
{
    private final Screen parent;
    private GuiList<GuiListEntryModpack> list;
    private boolean first = true;
    private TextFieldWidget displayEntry;
    
    public GuiModPackList(Screen currentScreen)
    {
        super(new StringTextComponent(""));
        this.parent = currentScreen;
    }

    @Override
    public void init()
    {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        if (list == null)
        {
            list = new GuiList<>(this, minecraft, width, height, 40, this.height - 40, 36);
        }
        {
            list.updateSize(width + 11, height, 32, this.height - 40);
        }
        
        this.children.add(list);
        
        if (first)
        {
            first = false;
            refreshList();
        }
        
        int y = this.height - 32;

        int buttonWidth = 80;
        
        displayEntry = new TextFieldWidget(this.font, this.width / 2 - 80, y, 160, 20, new StringTextComponent(""));
        displayEntry.setVisible(true);
        
        this.addButton(new Button(10, y, buttonWidth, 20, new StringTextComponent(Util.localize("button.cancel")), (button) -> minecraft.displayGuiScreen(new MainMenuScreen())));
        
        this.addButton(new Button(this.width - 90, y, buttonWidth, 20, new StringTextComponent("Select"), (button) ->
        {
            if (list.getSelected() != null)
            {
                String ID = ((GuiListEntryModpack) list.getSelected()).getModpack().getId();
                Config.getInstance().setVersion(ID);
                minecraft.displayGuiScreen(parent);
            }
        }));
    }

    public static List<ModPack> modpacks;

    private void refreshList()
    {
        try
        {
            String s = "";
            if (displayEntry != null)
            {
                s = displayEntry.getText();
            }
            list.clearList();

            modpacks = Callbacks.getModpackFromCurse(s, 10);
            if (modpacks != null && !modpacks.isEmpty())
            {
                modpacks.forEach(mp ->
                {
                    GuiListEntryModpack entry = new GuiListEntryModpack(this, list, mp);
                    list.add(entry);
                });
            }
        } catch (Exception ignored) {}
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (list != null)
        {
            this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        
        if (displayEntry != null) this.displayEntry.render(matrixStack, mouseX, mouseX, partialTicks);

        if(displayEntry != null && displayEntry.getText().trim().isEmpty() && !displayEntry.isFocused())
        {
            font.drawStringWithShadow(matrixStack, TextFormatting.ITALIC + "Search", displayEntry.x + 3, displayEntry.y + 5, 14737632);
        }
        
        this.drawCenteredString(matrixStack, this.font, "Minecraft Modpack Selector", this.width / 2, 10, -1);
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    int delay = 10;
    int i;
    boolean hasChanged;

    @Override
    public void tick() {
        super.tick();

        if(displayEntry != null && displayEntry.isFocused() && hasChanged)
        {
            i++;
            if(i >= delay)
            {
                refreshList();
                i = 0;
                hasChanged = false;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        if(list != null) this.list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return true;
    }
    
    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        if(list != null) this.list.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if(displayEntry != null) this.displayEntry.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return true;
    }
    
    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.charTyped(p_charTyped_1_, p_charTyped_2_);
            i = 0;
            hasChanged = true;
            if (p_charTyped_1_ == GLFW.GLFW_KEY_ENTER || p_charTyped_1_ == GLFW.GLFW_KEY_KP_ENTER) {
                refreshList();
                displayEntry.changeFocus(false);
            }
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (displayEntry != null && displayEntry.isFocused())
        {
            displayEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
}
