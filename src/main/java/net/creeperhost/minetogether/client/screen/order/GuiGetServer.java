package net.creeperhost.minetogether.client.gui.order;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class GuiGetServer extends Screen
{
    private static final int STEP_AMOUNT = 5;
    protected final int stepId;
    protected final Order order;
    protected Button buttonPrev;
    protected Button buttonNext;
    protected Button buttonCancel;
    
    public GuiGetServer(int stepId, Order order)
    {
        super(new TranslationTextComponent(""));
        MineTogether.instance.updateCurse();
        this.stepId = stepId;
        this.order = order;
    }
    
    @SuppressWarnings("Duplicates")
    public static Screen getByStep(int step, Order order)
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
    public void init()
    {
        super.init();
        
        int y = this.height - 30;
        
        this.buttonPrev = new Button(10, y, 80, 20, Util.localize("button.prev"), (button) ->
        {
            this.minecraft.displayGuiScreen(getByStep(this.stepId - 1, this.order));
        });
        
        this.buttonPrev.active = this.stepId > 0;
        this.addButton(this.buttonPrev);
        
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
        
        this.buttonNext = new Button(this.width - 90, y, 80, 20, nextStr, (button) ->
        {
            if ((this.stepId + 1) == STEP_AMOUNT)
            {
                this.minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
            } else
            {
                this.minecraft.displayGuiScreen(getByStep(this.stepId + 1, this.order));
            }
        });
        this.addButton(buttonNext);
        
        this.buttonCancel = new Button(this.width / 2 - 40, y, 80, 20, Util.localize("button.cancel"), (button) ->
        {
            this.minecraft.displayGuiScreen(null);
        });
        this.addButton(this.buttonCancel);
    }
    
    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_)
    {
        this.drawCenteredString(minecraft.fontRenderer, Util.localize("gui.get_server"), this.width / 2, 10, -1);
        
        this.drawCenteredString(minecraft.fontRenderer, Util.localize("info.step", this.stepId + 1, STEP_AMOUNT), this.width / 2, 20, -1);
        this.drawCenteredString(minecraft.fontRenderer, this.getStepName(), this.width / 2, 30, -1);
        
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }
    
    public abstract String getStepName();
}
