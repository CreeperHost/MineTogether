package net.creeperhost.minetogether.client.screen.order;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.list.GuiList;
import net.creeperhost.minetogether.client.screen.list.GuiListEntry;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryLocation;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;

import java.util.Map;

@Deprecated
public class GuiServerLocation extends GuiGetServer
{
    private GuiList list;
    
    public GuiServerLocation(int stepId, Order order)
    {
        super(stepId, order);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void init()
    {
        super.init();
        
        this.list = new GuiList(this, this.minecraft, this.width, this.height, 56, this.height - 36, 36);
        
        Map<String, String> locations = Callbacks.getAllServerLocations();
        
        String topEntryName = "";
        
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            
            if (this.order.serverLocation.equals(listEntry.locationName))
            {
                topEntryName = entry.getKey();
                this.list.add(listEntry);
                this.list.setSelected(listEntry);
                break;
            }
        }
        
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            if (topEntryName.equals(entry.getKey()))
            {
                continue;
            }
            GuiListEntryLocation listEntry = new GuiListEntryLocation(this.list, entry.getKey(), entry.getValue());
            this.list.add(listEntry);
        }
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.server_location");
    }
    
    @Override
    public void tick()
    {
        super.tick();
        this.buttonNext.active = this.list.getSelected() != null;
    }
    
    @Override
    public void onClose()
    {
        super.onClose();
        
        GuiListEntry entry = (GuiListEntry) this.list.getSelected();
        if (entry instanceof GuiListEntryLocation)
        {
            this.order.serverLocation = ((GuiListEntryLocation) entry).locationName;
        }
    }
    
    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        this.list.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return true;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state)
    {
        this.list.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
        return true;
    }
}
