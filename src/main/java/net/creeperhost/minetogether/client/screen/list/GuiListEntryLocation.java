package net.creeperhost.minetogether.client.screen.list;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class GuiListEntryLocation extends GuiListEntry
{
    public final String locationName;
    public final String locationDisplay;
    
    public GuiListEntryLocation(GuiList list, String locationName, String locationDisplay)
    {
        super(list);
        this.locationName = locationName;
        this.locationDisplay = locationDisplay;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_)
    {
        List<IReorderingProcessor> s = RenderComponentsUtil.func_238505_a_(new StringTextComponent(this.locationDisplay), listWidth, this.mc.fontRenderer);
        int start = y + 5;
        for(IReorderingProcessor iTextProperties : s)
        {
            this.mc.fontRenderer.func_238407_a_(matrixStack, iTextProperties, x + 5, start+=5, 16777215);
        }
    }
}
