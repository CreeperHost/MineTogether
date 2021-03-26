package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.api.OrderSummary;
import net.creeperhost.minetogether.gui.element.GuiWell;
import net.creeperhost.minetogether.gui.list.GuiList;
import net.creeperhost.minetogether.gui.list.GuiListEntryCountry;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GuiQuote extends GuiGetServer
{
    public OrderSummary summary;
    private GuiList list;
    private boolean countryEnabled = false;
    private GuiWell wellLeft;
    private GuiWell wellRight;
    private GuiWell wellBottom;
    private GuiButton countryButton;
    private boolean refreshing;
    private int oldButtonxPrev = 0;
    private int oldButtonxNext = 0;
    private GuiListEntryCountry countryPrev;
    private boolean changed;
    private boolean firstTime = true;
    private boolean countryOnRelease;
    
    public GuiQuote(int stepId, Order order)
    {
        super(stepId, order);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void initGui()
    {
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height - 36, 36);
        
        this.wellLeft = new GuiWell(this.mc, this.width / 2 - 10, 67, this.height - 88, Util.localize("quote.vpsfeatures"), new ArrayList<String>(), true, 0, zLevel);
        this.wellRight = new GuiWell(this.mc, this.width, 67, this.height - 88, Util.localize("quote.vpsincluded"), new ArrayList<String>(), true, (this.width / 2) + 10, zLevel);
        this.wellBottom = new GuiWell(this.mc, this.width, this.height - 83, this.height - 36, "", new ArrayList<String>(), true, 0, zLevel);

        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;
        
        String name = Callbacks.getCountries().get(order.country);
        
        countryButton = new GuiButton(8008135, start + middle - 100, this.height - 70, 200, 20, name);
        
        this.buttonList.add(countryButton);
        
        if (summary == null)
        {
            if (!refreshing)
                updateSummary();
            countryButton.visible = false;
        } else
        {
            
            this.wellLeft.lines = summary.serverFeatures;
            this.wellRight.lines = summary.serverIncluded;
            
            Map<String, String> locations = Callbacks.getCountries();
            for (Map.Entry<String, String> entry : locations.entrySet())
            {
                GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                list.addEntry(listEntry);
                
                if (order.country.equals(listEntry.countryID))
                {
                    list.setCurrSelected(listEntry);
                }
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    private void updateSummary()
    {
        countryButton.visible = false;
        refreshing = true;
        summary = null;
        
        final Order order = this.order;
        
        Runnable runnable = () -> {
            summary = Callbacks.getSummary(order);

            order.productID = summary.productID;
            order.currency = summary.currency;

            if (firstTime)
            {
                firstTime = false;
                Map<String, String> locations = Callbacks.getCountries();
                for (Map.Entry<String, String> entry : locations.entrySet())
                {
                    GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                    list.addEntry(listEntry);

                    if (order.country.equals(listEntry.countryID))
                    {
                        list.setCurrSelected(listEntry);
                    }
                }
            }

            wellLeft.lines = summary.serverFeatures;
            wellRight.lines = summary.serverIncluded;
            countryButton.displayString = Callbacks.getCountries().get(order.country);
            countryButton.visible = true;
            refreshing = false;
        };
        
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.quote");
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        
        this.buttonNext.enabled = this.list.getCurrSelected() != null && !countryEnabled && !refreshing;
        this.buttonPrev.enabled = !refreshing;
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        if (this.countryEnabled)
        {
            this.list.handleMouseInput();
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 8008135)
        {
            countryOnRelease = true;
            this.oldButtonxPrev = this.buttonPrev.xPosition;
            this.countryPrev =  (GuiListEntryCountry) this.list.getCurrSelected();
            this.oldButtonxNext = this.buttonNext.xPosition;
            this.buttonPrev.xPosition = this.buttonNext.xPosition;
            this.buttonNext.yPosition = -50;
        }
        
        if (countryEnabled && button.id == buttonPrev.id||countryEnabled && button.id == -3)
        {
            this.countryEnabled = false;
            this.buttonPrev.displayString = Util.localize("button.prev");
            this.buttonPrev.xPosition = this.oldButtonxPrev;
            this.buttonNext.xPosition = this.oldButtonxNext;
            this.buttonNext.yPosition = this.buttonPrev.yPosition;
            if (changed && button.id != -3)
            {
                changed = false;
                updateSummary();
            } else
            {
                countryButton.visible = true;
                list.setCurrSelected(this.countryPrev);
            }
            return;
        }
        super.actionPerformed(button);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        drawGradientRect(0, this.height - 20, width, 20, 0x99000000, 0x99000000);

        if (countryEnabled)
        {
            this.list.drawScreen(mouseX, mouseY, partialTicks);
        } else
        {
            if (!refreshing)
            {
                if (!summary.summaryError.isEmpty())
                {
                    super.drawScreen(mouseX, mouseY, partialTicks);
                    this.drawCenteredString(this.fontRendererObj, Util.localize("quote.error"), this.width / 2, 50, -1);
                    this.drawCenteredString(this.fontRendererObj, Util.localize(summary.summaryError), this.width / 2, 60, -1);
                    countryButton.visible = false;
                    buttonNext.visible = false;
                    buttonPrev.visible = false;
                    return;
                }
                this.wellBottom.drawScreen();
                this.wellLeft.drawScreen();
                this.wellRight.drawScreen();
                
                
                this.drawCenteredString(this.fontRendererObj, Util.localize("quote.requirements") + " " + summary.serverHostName.toLowerCase() + " package", this.width / 2, 50, -1);
                
                String formatString = summary.prefix + "%1$.2f " + summary.suffix;
                
                String subTotalString = Util.localize("quote.subtotal") + ":  ";
                int subTotalWidth = fontRendererObj.getStringWidth(subTotalString);
                String discountString = Util.localize("quote.discount") + ":  ";
                int discountWidth = fontRendererObj.getStringWidth(discountString);
                String taxString = Util.localize("quote.tax") + ":  ";
                int taxWidth = fontRendererObj.getStringWidth(taxString);
                String totalString = Util.localize("quote.total") + ":  ";
                int totalWidth = fontRendererObj.getStringWidth(totalString);
                
                int headerSize = Math.max(subTotalWidth, Math.max(taxWidth, Math.max(totalWidth, discountWidth)));
                
                int subTotalValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.subTotal));
                int discountValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.discount));
                int taxValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.tax));
                int totalValueWidth = fontRendererObj.getStringWidth(String.format(formatString, summary.tax));
                
                int maxStringSize = headerSize + Math.max(subTotalValueWidth, Math.max(discountValueWidth, Math.max(taxValueWidth, totalValueWidth)));
                
                int offset = maxStringSize / 2;
                int otherOffset = ((this.width / 2 - 10) / 2) - offset;
                
                this.drawString(this.fontRendererObj, subTotalString, otherOffset, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, discountString, otherOffset, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, taxString, otherOffset, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, totalString, otherOffset, this.height - 50, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);
                
                int start = (this.width / 2) + 10;
                int end = this.width;
                int middle = (end - start) / 2;
                int stringStart = this.fontRendererObj.getStringWidth(Util.localize("quote.figures")) / 2;
                
                this.drawString(this.fontRendererObj, Util.localize("quote.figures"), start + middle - stringStart, this.height - 80, 0xFFFFFF);
            } else
            {
                this.drawCenteredString(this.fontRendererObj, Util.localize("quote.refreshing"), this.width / 2, 50, -1);
            }
            
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.countryEnabled)
        {
            if (this.list.mouseClicked(mouseX, mouseY, mouseButton))
            {
                GuiListEntryCountry country = (GuiListEntryCountry) this.list.getCurrSelected();
                order.country = country.countryID;
                changed = true;
            }
        }
        
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if (this.countryEnabled)
        {
            this.list.mouseReleased(mouseX, mouseY, state);
        }
        if (countryOnRelease)
        {
            countryOnRelease = false;
            this.countryEnabled = !this.countryEnabled;
            this.buttonPrev.displayString = Util.localize("button.quoteback");
            countryButton.visible = false;
            return;
        }
    }
}
