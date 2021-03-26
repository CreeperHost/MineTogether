package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public abstract class GuiGetServer extends GuiScreen
{
    private static final int STEP_AMOUNT = 5;
    protected final int stepId;
    protected final Order order;
    protected GuiButton buttonPrev;
    protected GuiButton buttonNext;
    protected GuiButton buttonCancel;
    
    public GuiGetServer(int stepId, Order order)
    {
        CreeperHost.instance.updateCurse();
        this.stepId = stepId;
        this.order = order;
    }
    
    @SuppressWarnings("Duplicates")
    public static GuiScreen getByStep(int step, Order order)
    {
        switch (step)
        {
            case 0:
            default:
                return new GuiGeneralServerInfo(0, order);
            case 1:
                return new GuiQuote(1, order);
            case 2:
                return new GuiServerLocation(2, order);
            case 3:
                return new GuiPersonalDetails(3, order);
            case 4:
                return new GuiOrderDetails(4, order);
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void initGui()
    {
        super.initGui();
        
        int y = this.height - 30;
        
        this.buttonPrev = new GuiButton(-1, 10, y, 80, 20, Util.localize("button.prev"));
        this.buttonPrev.enabled = this.stepId > 0;
        this.buttonList.add(this.buttonPrev);
        
        String nextStr = "";
        
        if ((this.stepId + 1) == STEP_AMOUNT)
        {
            nextStr = Util.localize("button.done");
        } else if (this.stepId == 3)
        {
            nextStr = Util.localize("button.order");
        } else
        {
            nextStr = Util.localize("button.next");
        }
        
        this.buttonNext = new GuiButton(-2, this.width - 90, y, 80, 20, nextStr);
        this.buttonList.add(buttonNext);
        
        this.buttonCancel = new GuiButton(-3, this.width / 2 - 40, y, 80, 20, Util.localize("button.cancel"));
        this.buttonList.add(this.buttonCancel);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawCenteredString(fontRendererObj, "Step " + (this.stepId + 1 + " / ") + STEP_AMOUNT, this.width - 30, 10, -1);
        this.drawCenteredString(fontRendererObj, this.getStepName(), this.width / 2, 10, -1);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button == this.buttonPrev)
        {
            this.mc.displayGuiScreen(getByStep(this.stepId - 1, this.order));
        } else if (button == this.buttonNext)
        {
            if ((this.stepId + 1) == STEP_AMOUNT)
            {
                this.mc.displayGuiScreen(new GuiMultiplayer(null));
            } else
            {
                this.mc.displayGuiScreen(getByStep(this.stepId + 1, this.order));
            }
        } else if (button == this.buttonCancel)
        {
            this.mc.displayGuiScreen(null);
        } else
        {
            super.actionPerformed(button);
        }
    }
    
    public abstract String getStepName();
}
