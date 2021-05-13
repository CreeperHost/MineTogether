package net.creeperhost.minetogether.module.serverorder.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.helpers.ScreenHelpers;
import net.creeperhost.minetogether.module.serverorder.screen.listentries.ListEntryCountry;
import net.creeperhost.minetogether.screen.prefab.ScreenList;
import net.creeperhost.minetogether.screen.widgets.ScreenWell;
import net.creeperhost.minetogetherlib.Order;
import net.creeperhost.minetogetherlib.minigames.MinigamesCallbacks;
import net.creeperhost.minetogetherlib.serverorder.OrderSummary;
import net.creeperhost.minetogetherlib.serverorder.ServerOrderCallbacks;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class QuoteScreen extends OrderServerScreen
{
    public OrderSummary summary;
    private ScreenList list;
    private ScreenWell wellLeft;
    private ScreenWell wellRight;
    private ScreenWell wellBottom;
    private Button countryButton;
    private boolean refreshing;
    private int ticks;
    private boolean first = false;
    private boolean showList = false;

    public QuoteScreen(int stepId, Order order)
    {
        super(stepId, order);
    }

    @Override
    public void init()
    {
        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;

        super.init();
        addWidget(this.list = new ScreenList(this, this.minecraft, this.width, this.height, 56, this.height - 36, 36));
        this.wellLeft = new ScreenWell(this.minecraft, this.width / 2 - 10, 67, this.height - 88, I18n.get("minetogether.quote.vpsfeatures"), new ArrayList<String>(), true, 0);
        this.wellRight = new ScreenWell(this.minecraft, this.width, 67, this.height - 88, I18n.get("minetogether.quote.vpsincluded"), new ArrayList<String>(), true, (this.width / 2) + 10);
        this.wellBottom = new ScreenWell(this.minecraft, this.width, this.height - 83, this.height - 36, "", new ArrayList<String>(), true, 0);

        String name = ServerOrderCallbacks.getCountries().get(order.country);
        if(name == null || name.isEmpty()) name = "Failed to load";
        countryButton = addButton(new Button(start + middle - 100, this.height - 70, 200, 20, new TranslatableComponent(name), p ->
        {
            showList = true;
//            countryOnRelease = true;
//            this.oldButtonxPrev = this.buttonPrev.x;
//            this.countryPrev = (GuiListEntryCountry) this.list.getSelected();
//            this.oldButtonxNext = this.buttonNext.x;
//            this.buttonPrev.x = this.buttonNext.x;
//            this.buttonNext.y = -50;
        }));

        if(summary == null)
        {
            if(!refreshing) updateSummary();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        fill(poseStack, 0, this.height - 20, width, 20, 0x99000000);

        if(!refreshing)
        {
            if(showList) list.render(poseStack, mouseX, mouseY, partialTicks);

            if (!summary.summaryError.isEmpty())
            {
                super.render(poseStack, mouseX, mouseY, partialTicks);
                drawCenteredString(poseStack, this.font, I18n.get("quote.error"), this.width / 2, 50, -1);
                drawCenteredString(poseStack, this.font, I18n.get(summary.summaryError), this.width / 2, 60, -1);
                countryButton.visible = false;
                return;
            }
            wellBottom.render();
            wellLeft.render();
            wellRight.render();

            drawCenteredString(poseStack, this.font, I18n.get("minetogether.quote.requirements") + " " + summary.serverHostName.toLowerCase() + " package", this.width / 2, 50, -1);

            String formatString = summary.prefix + "%1$.2f " + summary.suffix;

            String subTotalString = I18n.get("minetogether.quote.subtotal") + ":  ";
            int subTotalWidth = font.width(subTotalString);
            String discountString = I18n.get("minetogether.quote.discount") + ":  ";
            int discountWidth = font.width(discountString);
            String taxString = I18n.get("minetogether.quote.tax") + ":  ";
            int taxWidth = font.width(taxString);
            String totalString = I18n.get("minetogether.quote.total") + ":  ";
            int totalWidth = font.width(totalString);

            int headerSize = Math.max(subTotalWidth, Math.max(taxWidth, Math.max(totalWidth, discountWidth)));

            int subTotalValueWidth = font.width(String.format(formatString, summary.subTotal));
            int discountValueWidth = font.width(String.format(formatString, summary.discount));
            int taxValueWidth = font.width(String.format(formatString, summary.tax));
            int totalValueWidth = font.width(String.format(formatString, summary.tax));

            int maxStringSize = headerSize + Math.max(subTotalValueWidth, Math.max(discountValueWidth, Math.max(taxValueWidth, totalValueWidth)));

            int offset = maxStringSize / 2;
            int otherOffset = ((this.width / 2 - 10) / 2) - offset;

            drawString(poseStack, this.font, subTotalString, otherOffset, this.height - 80, 0xFFFFFF);
            drawString(poseStack,this.font, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
            drawString(poseStack,this.font, discountString, otherOffset, this.height - 70, 0xFFFFFF);
            drawString(poseStack,this.font, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
            drawString(poseStack,this.font, taxString, otherOffset, this.height - 60, 0xFFFFFF);
            drawString(poseStack,this.font, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
            drawString(poseStack,this.font, totalString, otherOffset, this.height - 50, 0xFFFFFF);
            drawString(poseStack,this.font, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);

            int start = (this.width / 2) + 10;
            int end = this.width;
            int middle = (end - start) / 2;
            int stringStart = this.font.width(I18n.get("minetogether.quote.figures")) / 2;

            drawString(poseStack,this.font, I18n.get("minetogether.quote.figures"), start + middle - stringStart, this.height - 80, 0xFFFFFF);
        } else
        {
            drawCenteredString(poseStack,this.font, I18n.get("minetogether.quote.refreshing"), this.width / 2, 50, -1);
            ScreenHelpers.loadingSpin(partialTicks, ticks, width / 2, height / 2, new ItemStack(Items.BEEF));
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick()
    {
        ticks++;
        super.tick();
    }

    @Override
    public String getStepName()
    {
        return I18n.get("minetogether.order.screen.generalinfo.quote");
    }

    @SuppressWarnings("Duplicates")
    private void updateSummary()
    {
        countryButton.visible = false;
        refreshing = true;
        summary = null;

        final Order order = this.order;

        CompletableFuture.runAsync(() ->
        {
            summary = ServerOrderCallbacks.getSummary(order);

            order.productID = summary.productID;
            order.currency = summary.currency;

            if (first) updateList();

            wellLeft.lines = summary.serverFeatures;
            wellRight.lines = summary.serverIncluded;
            countryButton.setMessage(new TranslatableComponent(ServerOrderCallbacks.getCountries().get(order.country)));
            countryButton.visible = true;
            refreshing = false;
        });
    }

    @SuppressWarnings("unchecked")
    public void updateList()
    {
        first = false;
        Map<String, String> locations = ServerOrderCallbacks.getCountries();
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            ListEntryCountry listEntry = new ListEntryCountry(list, entry.getKey(), entry.getValue());
            list.add(listEntry);

            if (order.country.equals(listEntry.countryID))
            {
                list.setSelected(listEntry);
            }
        }
    }
}
