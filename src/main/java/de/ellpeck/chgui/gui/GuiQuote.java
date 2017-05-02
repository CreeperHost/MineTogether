package de.ellpeck.chgui.gui;

import de.ellpeck.chgui.Util;
import de.ellpeck.chgui.gui.element.GuiWell;
import de.ellpeck.chgui.gui.list.GuiList;
import de.ellpeck.chgui.gui.list.GuiListEntryCountry;
import de.ellpeck.chgui.paul.Callbacks;
import de.ellpeck.chgui.paul.Order;
import de.ellpeck.chgui.paul.OrderSummary;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiQuote extends GuiGetServer{

    private GuiList list;
    private boolean countryEnabled = false;
    private GuiWell wellLeft;
    private GuiWell wellRight;
    private GuiWell wellBottom;
    private GuiButton countryButton;
    private boolean refreshing;
    private boolean changed;
    private boolean firstTime = true;
    private boolean countryOnRelease;

    public GuiQuote(int stepId, Order order){
        super(stepId, order);
    }

    public OrderSummary summary;

    @Override
    public void initGui(){
        super.initGui();

        this.list = new GuiList(this, this.mc, this.width, this.height, 56, this.height-36, 36);

        List<String> vpsIncluded = new ArrayList<String>();
        vpsIncluded.add("Free Dedicated IPv4");
        vpsIncluded.add("Unmetered slots");
        vpsIncluded.add("Free website");
        vpsIncluded.add("Free VoIP");
        vpsIncluded.add("Install or switch games for free");
        vpsIncluded.add("Monthly subscription - cancel at any");
        vpsIncluded.add("time with 7 days notice");

        this.wellLeft = new GuiWell(this.mc, this.width / 2 - 10, this.height, 67, this.height - 88, 88, "VPS Features", new ArrayList<String>(), true, 0);
        this.wellRight = new GuiWell(this.mc, this.width, this.height, 67, this.height - 88, 88, "Included Features", vpsIncluded, true, (this.width / 2) + 10);
        this.wellBottom = new GuiWell(this.mc, this.width, this.height, this.height - 83, this.height - 36, 36, "", new ArrayList<String>(), true, 0);

        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;

        String name = Callbacks.getCountries().get(order.country);

        countryButton = new GuiButton(8008135, start + middle - 100, this.height - 70, 200, 20, name);

        this.buttonList.add(countryButton);

        if (summary == null) {
            if (!refreshing)
                updateSummary();
            countryButton.visible = false;
       } else {

            this.wellLeft.lines = summary.vpsFeatures;

            Map<String, String> locations = Callbacks.getCountries();
            for(Map.Entry<String, String> entry : locations.entrySet()){
                GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                list.addEntry(listEntry);

                if(order.country.equals(listEntry.countryID)){
                    list.setCurrSelected(listEntry);
                }
            }

        }

    }

    private void updateSummary() {

        countryButton.visible = false;
        refreshing = true;
        summary = null;

        final Order order = this.order;

        Runnable runnable = new Runnable() {

            @Override
            public void run()
            {
                summary = Callbacks.getSummary(order);

                order.productID = summary.productID;
                order.currency = summary.currency;

                if (firstTime) {
                    firstTime = false;
                    Map<String, String> locations = Callbacks.getCountries();
                    for(Map.Entry<String, String> entry : locations.entrySet()){
                        GuiListEntryCountry listEntry = new GuiListEntryCountry(list, entry.getKey(), entry.getValue());
                        list.addEntry(listEntry);

                        if(order.country.equals(listEntry.countryID)){
                            list.setCurrSelected(listEntry);
                        }
                    }
                }

                wellLeft.lines = summary.vpsFeatures;
                countryButton.displayString = Callbacks.getCountries().get(order.country);
                countryButton.visible = true;
                refreshing = false;
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public String getStepName(){
        return Util.localize("gui.quote");
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        this.buttonNext.enabled = this.list.getCurrSelected() != null && !countryEnabled && !refreshing;
        this.buttonPrev.enabled = !refreshing;
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();
        if (this.countryEnabled) {
            this.list.handleMouseInput();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 8008135) {
            countryOnRelease = true;
        }

        if (countryEnabled && button.id == buttonPrev.id) {
            this.countryEnabled = false;
            this.buttonPrev.displayString = Util.localize("button.prev");
            if (changed) {
                changed = false;
                updateSummary();
            } else {
                countryButton.visible = true;
            }
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        if (countryEnabled) {
            this.list.drawScreen(mouseX, mouseY, partialTicks);
        } else {
            if (!refreshing) {
                this.wellBottom.drawScreen(mouseX, mouseY, partialTicks);
                this.wellLeft.drawScreen(mouseX, mouseY, partialTicks);
                this.wellRight.drawScreen(mouseX, mouseY, partialTicks);


                this.drawCenteredString(this.fontRendererObj, "Based on your requirements, we recommend a " + summary.vpsDisplay, this.width/2, 50, -1); // TODO: Properly use language file

                String formatString = summary.prefix + "%1$.2f " + summary.suffix;

                int headerSize = Math.max(fontRendererObj.getStringWidth("Subtotal:  "), Math.max(fontRendererObj.getStringWidth("Tax:  "), Math.max(fontRendererObj.getStringWidth("Total:  "), fontRendererObj.getStringWidth("Discount:  "))));

                int maxStringSize = fontRendererObj.getStringWidth(String.format("Subtotal:  " + formatString, summary.subTotal));

                int offset = maxStringSize / 2;
                int otherOffset = ((this.width / 2 - 10) / 2) - offset;

                this.drawString(this.fontRendererObj, "SubTotal:  ", otherOffset, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
                this.drawString(this.fontRendererObj, "Discount:  ", otherOffset, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
                this.drawString(this.fontRendererObj, "Tax:  ", otherOffset, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
                this.drawString(this.fontRendererObj, "Total:  ", otherOffset, this.height - 50, 0xFFFFFF);
                this.drawString(this.fontRendererObj, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);


                int start = (this.width / 2) + 10;
                int end = this.width;
                int middle = (end - start) / 2;
                int stringStart = this.fontRendererObj.getStringWidth("Figures based on you being in ") / 2;

                this.drawString(this.fontRendererObj, "Figures based on you being in ", start + middle - stringStart, this.height - 80, 0xFFFFFF);
            } else {
                this.drawCenteredString(this.fontRendererObj, "Please wait - refreshing quote", this.width/2, 50, -1); // TODO: Properly use language file
            }

        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.countryEnabled)
        {
            if (this.list.mouseClicked(mouseX, mouseY, mouseButton)) {
                GuiListEntryCountry country = (GuiListEntryCountry)this.list.getCurrSelected();
                order.country = country.countryID;
                changed = true;
            }
        }

    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state){
        super.mouseReleased(mouseX, mouseY, state);
        if (this.countryEnabled)
        {
            this.list.mouseReleased(mouseX, mouseY, state);
        }
        if (countryOnRelease) {
            countryOnRelease = false;
            this.countryEnabled = !this.countryEnabled;
            this.buttonPrev.displayString = "Back to quote";
            countryButton.visible = false;
            return;
        }
    }
}
