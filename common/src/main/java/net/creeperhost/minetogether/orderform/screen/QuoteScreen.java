package net.creeperhost.minetogether.orderform.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.ServerOrderCallbacks;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.data.OrderSummary;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.creeperhost.polylib.client.screen.widget.LoadingSpinner;
import net.creeperhost.polylib.client.screen.widget.ScreenWell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class QuoteScreen extends OrderServerScreen {

    public OrderSummary summary;
    private ScreenWell wellLeft;
    private ScreenWell wellRight;
    private ScreenWell wellBottom;
    private boolean refreshing;
    private int ticks;

    public QuoteScreen(int stepId, Order order) {
        super(stepId, order);
    }

    @Override
    public void init() {
        clearWidgets();

        int start = (this.width / 2) + 10;
        int end = this.width;
        int middle = (end - start) / 2;

        super.init();
        this.wellLeft = new ScreenWell(this.minecraft, this.width / 2 - 10, 67, this.height - 88, I18n.get("minetogether.quote.vpsfeatures"), new ArrayList<String>(), true, 0);
        this.wellRight = new ScreenWell(this.minecraft, this.width, 67, this.height - 88, I18n.get("minetogether.quote.vpsincluded"), new ArrayList<String>(), true, (this.width / 2) + 10);
        this.wellBottom = new ScreenWell(this.minecraft, this.width, this.height - 83, this.height - 36, "", new ArrayList<String>(), true, 0);

        String name = Countries.COUNTRIES.get(order.country);
        if (name == null || name.isEmpty()) name = "Failed to load";

        if (summary == null) {
            if (!refreshing) updateSummary();
        }
        this.buttonNext.setMessage(Component.translatable("minetogether.button.order"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(graphics);
        graphics.fill(0, this.height - 20, width, 20, 0x99000000);

        if (!refreshing) {
            if (!summary.summaryError.isEmpty()) {
                super.render(graphics, mouseX, mouseY, partialTicks);
                graphics.drawCenteredString(this.font, I18n.get("quote.error"), this.width / 2, 50, -1);
                graphics.drawCenteredString(this.font, I18n.get(summary.summaryError), this.width / 2, 60, -1);
                return;
            }
            wellBottom.render(graphics);
            wellLeft.render(graphics);
            wellRight.render(graphics);

            graphics.drawCenteredString(this.font, I18n.get("minetogether.quote.requirements") + " " + summary.serverHostName.toLowerCase() + " package", this.width / 2, 50, -1);

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

            graphics.drawString(this.font, subTotalString, otherOffset, this.height - 80, 0xFFFFFF);
            graphics.drawString(this.font, String.format(formatString, summary.preDiscount), otherOffset + headerSize, this.height - 80, 0xFFFFFF);
            graphics.drawString(this.font, discountString, otherOffset, this.height - 70, 0xFFFFFF);
            graphics.drawString(this.font, String.format(formatString, summary.discount), otherOffset + headerSize, this.height - 70, 0xFFFFFF);
            graphics.drawString(this.font, taxString, otherOffset, this.height - 60, 0xFFFFFF);
            graphics.drawString(this.font, String.format(formatString, summary.tax), otherOffset + headerSize, this.height - 60, 0xFFFFFF);
            graphics.drawString(this.font, totalString, otherOffset, this.height - 50, 0xFFFFFF);
            graphics.drawString(this.font, String.format(formatString, summary.total), otherOffset + headerSize, this.height - 50, 0xFFFFFF);

            int start = (this.width / 2) + 10;
            int end = this.width;
            int middle = (end - start) / 2;
            int stringStart = this.font.width(I18n.get("minetogether.quote.figures")) / 2;

            graphics.drawString(this.font, I18n.get("minetogether.quote.figures"), start + middle - stringStart, this.height - 80, 0xFFFFFF);
            graphics.drawCenteredString(this.font, ChatFormatting.BOLD + I18n.get(Countries.COUNTRIES.get(order.country)), start + middle, this.height - 65, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(this.font, I18n.get("minetogether.quote.refreshing"), this.width / 2, 50, -1);
            LoadingSpinner.render(graphics.pose(), partialTicks, ticks, width / 2, height / 2, new ItemStack(Items.BEEF));
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        ticks++;
        super.tick();
    }

    @Override
    public String getStepName() {
        return I18n.get("minetogether.order.screen.generalinfo.quote");
    }

    @SuppressWarnings ("Duplicates")
    private void updateSummary() {
        refreshing = true;
        summary = null;

        final Order order = this.order;

        CompletableFuture.runAsync(() ->
        {
            summary = ServerOrderCallbacks.getSummary(order, Config.instance().promoCode);

            order.productID = summary.productID;
            order.currency = summary.currency;
            wellLeft.lines = summary.serverFeatures;
            wellRight.lines = summary.serverIncluded;
            refreshing = false;
        });
    }
}
