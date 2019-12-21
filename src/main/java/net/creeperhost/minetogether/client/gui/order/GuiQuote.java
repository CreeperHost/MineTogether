package net.creeperhost.minetogether.client.gui.order;

import net.creeperhost.minetogether.util.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.api.OrderSummary;
import net.creeperhost.minetogether.client.gui.element.GuiWell;
import net.creeperhost.minetogether.client.gui.list.GuiList;
import net.creeperhost.minetogether.client.gui.list.GuiListEntryCountry;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.widget.button.Button;

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
    private Button countryButton;
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
    public void init()
    {
        super.init();
        
        this.list = new GuiList(this, this.minecraft, this.width, this.height, 56, this.height - 36, 36);
        
        this.wellLeft = new GuiWell(this.minecraft, this.width / 2 - 10, 67, this.height - 88, Util.localize("quote.vpsfeatures"), new ArrayList<String>(), true, 0);
        this.wellRight = new GuiWell(this.minecraft, this.width, 67, this.height - 88, Util.localize("quote.vpsincluded"), new ArrayList<String>(), true, (this.width / 2) + 10);
        this.wellBottom = new GuiWell(this.minecraft, this.width, this.height - 83, this.height - 36, "", new ArrayList<String>(), true, 0);
        
        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;
        
        String name = Callbacks.getCountries().get(order.country);
        
        countryButton = addButton(new Button(start + middle - 100, this.height - 70, 200, 20, name, p ->
        {
            countryOnRelease = true;
            this.oldButtonxPrev = this.buttonPrev.x;
            this.countryPrev =  (GuiListEntryCountry) this.list.getSelected();
            this.oldButtonxNext = this.buttonNext.x;
            this.buttonPrev.x = this.buttonNext.x;
            this.buttonNext.y = -50;
        }));

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
                list.add(listEntry);
                
                if (order.country.equals(listEntry.countryID))
                {
                    list.setSelected(listEntry);
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
                    list.add(listEntry);

                    if (order.country.equals(listEntry.countryID))
                    {
                        list.setSelected(listEntry);
                    }
                }
            }

            wellLeft.lines = summary.serverFeatures;
            wellRight.lines = summary.serverIncluded;
            countryButton.setMessage(Callbacks.getCountries().get(order.country));
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
    public void tick()
    {
        super.tick();
        
        this.buttonNext.active = this.list.getSelected() != null && !countryEnabled && !refreshing;
        this.buttonPrev.active = !refreshing;
    }
    
//    @Override
//    public void handleMouseInput() throws IOException
//    {
//        super.handleMouseInput();
//        if (this.countryEnabled)
//        {
//            this.list.handleMouseInput();
//        }
//    }
    
    @SuppressWarnings("Duplicates")
//    @Override
//    protected void actionPerformed(GuiButton button) throws IOException
//    {
//        if (button.id == 8008135)
//        {
//            countryOnRelease = true;
//            this.oldButtonxPrev = this.buttonPrev.xPosition;
//            this.countryPrev =  (GuiListEntryCountry) this.list.getCurrSelected();
//            this.oldButtonxNext = this.buttonNext.xPosition;
//            this.buttonPrev.xPosition = this.buttonNext.xPosition;
//            this.buttonNext.yPosition = -50;
//        }
//
//        if (countryEnabled && button.id == buttonPrev.id||countryEnabled && button.id == -3)
//        {
//            this.countryEnabled = false;
//            this.buttonPrev.displayString = Util.localize("button.prev");
//            this.buttonPrev.xPosition = this.oldButtonxPrev;
//            this.buttonNext.xPosition = this.oldButtonxNext;
//            this.buttonNext.yPosition = this.buttonPrev.yPosition;
//            if (changed && button.id != -3)
//            {
//                changed = false;
//                updateSummary();
//            } else
//            {
//                countryButton.visible = true;
//                list.setCurrSelected(this.countryPrev);
//            }
//            return;
//        }
//        super.actionPerformed(button);
//    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        
        if (countryEnabled)
        {
            this.list.render(mouseX, mouseY, partialTicks);
        } else
        {
            if (!refreshing)
            {
                if (!summary.summaryError.isEmpty())
                {
                    super.render(mouseX, mouseY, partialTicks);
                    this.drawCenteredString(this.font, Util.localize("quote.error"), this.width / 2, 50, -1);
                    this.drawCenteredString(this.font, Util.localize(summary.summaryError), this.width / 2, 60, -1);
                    countryButton.visible = false;
                    buttonNext.visible = false;
                    buttonPrev.visible = false;
                    return;
                }
                this.wellBottom.drawScreen();
                this.wellLeft.drawScreen();
                this.wellRight.drawScreen();
                
                
                this.drawCenteredString(this.font, Util.localize("quote.requirements") + " " + summary.serverHostName.toLowerCase() + " package", this.width / 2, 50, -1);
                
                String formatString = summary.prefix + "%1$.2f " + summary.suffix;
                
                String subTotalString = Util.localize("quote.subtotal") + ":  ";
                int subTotalWidth = font.getStringWidth(subTotalString);
                String discountString = Util.localize("quote.discount") + ":  ";
                int discountWidth = font.getStringWidth(discountString);
                String taxString = Util.localize("quote.tax") + ":  ";
                int taxWidth = font.getStringWidth(taxString);
                String totalString = Util.localize("quote.total") + ":  ";
                int totalWidth = font.getStringWidth(totalString);
                
                int headerSize = Math.max(subTotalWidth, Math.max(taxWidth, Math.max(totalWidth, discountWidth)));
                
                int subTotalValueWidth = font.getStringWidth(String.format(formatString, summary.subTotal));
                int discountValueWidth = font.getStringWidth(String.format(formatString, summary.discount));
                int taxValueWidth = font.getStringWidth(String.format(formatString, summary.tax));
                int totalValueWidth = font.getStringWidth(String.format(formatString, summary.tax));
                
                int maxStringSize = headerSize + Math.max(subTotalValueWidth, Math.max(discountValueWidth, Math.max(taxValueWidth, totalValueWidth)));
                
                int offset = maxStringSize / 2;
                int otherOffset = ((this.width / 2 - 10) / 2) - offset;
                
                this.drawString(this.font, subTotalString, otherOffset, this.height - 80, 0xFFFFFF);
                this.drawString(this.font, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
                this.drawString(this.font, discountString, otherOffset, this.height - 70, 0xFFFFFF);
                this.drawString(this.font, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
                this.drawString(this.font, taxString, otherOffset, this.height - 60, 0xFFFFFF);
                this.drawString(this.font, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
                this.drawString(this.font, totalString, otherOffset, this.height - 50, 0xFFFFFF);
                this.drawString(this.font, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);
                
                int start = (this.width / 2) + 10;
                int end = this.width;
                int middle = (end - start) / 2;
                int stringStart = this.font.getStringWidth(Util.localize("quote.figures")) / 2;
                
                this.drawString(this.font, Util.localize("quote.figures"), start + middle - stringStart, this.height - 80, 0xFFFFFF);
            } else
            {
                this.drawCenteredString(this.font, Util.localize("quote.refreshing"), this.width / 2, 50, -1);
            }
            
        }
        
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.countryEnabled)
        {
            if (this.list.mouseClicked(mouseX, mouseY, mouseButton))
            {
                GuiListEntryCountry country = (GuiListEntryCountry) this.list.getSelected();
                order.country = country.countryID;
                changed = true;
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state)
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
            this.buttonPrev.setMessage(Util.localize("button.quoteback"));
            countryButton.visible = false;
            return true;
        }
        return false;
    }
}
