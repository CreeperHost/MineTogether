package net.creeperhost.minetogether.orderform.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.ServerOrderCallbacks;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrderDetailsScreen extends OrderServerScreen {

    private static final Logger LOGGER = LogManager.getLogger();

    private boolean placingOrder = false;
    private boolean placedOrder = false;
    private boolean creatingAccount = false;
    private boolean createdAccount = false;
    private String createdAccountError = "";
    private int orderNumber;
    private String invoiceID;
    private String placedOrderError = "";
    private Button buttonInvoice;
    private boolean serverAdded;
    private int ticks = 0;

    public OrderDetailsScreen(int stepId, Order order) {
        super(stepId, order);
        if (order.clientID != null && !order.clientID.isEmpty()) {
            creatingAccount = false;
            createdAccount = true;
        }
    }

    @SuppressWarnings ("Duplicates")
    @Override
    public void init() {
        clearWidgets();

        super.init();
        this.buttonNext.setMessage(new TranslatableComponent("minetogether.button.finish"));
        this.buttonNext.visible = false;
        buttonCancel.setMessage(new TranslatableComponent("minetogether.order.ordercancel"));
        buttonCancel.active = false;
        buttonPrev.active = false;
        buttonPrev.visible = false;
        buttonInvoice = addRenderableWidget(new Button(this.width / 2 - 40, (this.height / 2) + 30, 80, 20, new TranslatableComponent("minetogether.button.invoice"), p ->
        {
            try {
                Util.getPlatform().openUri(new URI(ServerOrderCallbacks.getPaymentLink(invoiceID)));
            } catch (Throwable throwable) {
                LOGGER.error("Couldn't open link", throwable);
            }
        }));
        buttonNext.visible = true;
        buttonNext.active = true;
        buttonInvoice.visible = false;
    }

    @SuppressWarnings ("Duplicates")
    public void tick() {
        ticks++;
        super.tick();
        if (!createdAccount && !creatingAccount) {
            if (!createdAccountError.isEmpty()) {
                buttonCancel.active = true;
                return;
            }
            creatingAccount = true;
            CompletableFuture.runAsync(() ->
            {
                String result = ServerOrderCallbacks.createAccount(order, "0");
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success")) {
                    order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                    order.clientID = resultSplit[2] != null ? resultSplit[2] : "0"; // random test account fallback

                } else {
                    createdAccountError = result;
                    createdAccount = true;
                }
                creatingAccount = false;
                createdAccount = true;
            });
        } else if (creatingAccount) {
            return;
        } else if (!createdAccountError.isEmpty()) {
            buttonCancel.active = true;
            return;
        } else if (!placingOrder && !placedOrder) {
            placingOrder = true;
            buttonNext.active = false;
            Runnable runnable = () ->
            {
                String result = ServerOrderCallbacks.createOrder(order, "0", String.valueOf(Config.instance().pregenDiameter));
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success")) {
                    invoiceID = resultSplit[1] != null ? resultSplit[1] : "0";
                    orderNumber = Integer.parseInt(resultSplit[2]);
                } else {
                    placedOrderError = result;
                }
                placedOrder = true;
                placingOrder = false;
            };
            Thread thread = new Thread(runnable);
            thread.start();
            buttonCancel.active = false;
        } else if (placingOrder) {
            return;
        } else if (placedOrderError.isEmpty()) {
            if (!serverAdded) serverAdded = addServerEntry();

            buttonInvoice.visible = true;
            buttonNext.visible = true;
            buttonCancel.active = false;
            buttonNext.active = true;
        } else {
            buttonNext.active = true;
        }

        buttonCancel.active = placedOrder;
    }

    public boolean addServerEntry() {
        ServerList savedServerList = new ServerList(this.minecraft);
        savedServerList.load();
        savedServerList.add(getServerEntry(order));
        savedServerList.save();
        return true;
    }

    public ServerData getServerEntry(Order order) {
        return new ServerData(order.name + ".PlayAt.CH", order.name + ".playat.ch", false);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);

        if (creatingAccount) {
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.accountcreating"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!createdAccountError.isEmpty()) {
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.accounterror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(new TranslatableComponent(createdAccountError), width - 30, font);
            int offset = 10;
            for (FormattedCharSequence str : list) {
                drawCenteredString(matrixStack, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.accounterrorgoback"), this.width / 2, this.height / 2 + offset, 0xFFFFFF);
        } else if (placingOrder) {
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.orderplacing"), this.width / 2, this.height / 2, 0xFFFFFF);
            //TODO replace with LoadingSpinner.class
            ScreenHelper.loadingSpin(matrixStack, partialTicks, ticks, width / 2, height / 2 + 20, new ItemStack(Items.BEEF));
        } else if (!placedOrderError.isEmpty()) {
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.ordererror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(new TranslatableComponent(placedOrderError), width - 30, font);
            int offset = 10;
            for (FormattedCharSequence str : list) {
                drawCenteredString(matrixStack, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.ordererrorsupport"), this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
        } else {
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.ordersuccess"), this.width / 2, this.height / 2, 0xFFFFFF);
            drawCenteredString(matrixStack, font, I18n.get("minetogether.order.ordermodpack"), (this.width / 2) + 10, (this.height / 2) + 10, 0xFFFFFF);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    //Copy paste but allow FormattedCharSequence to be used, Might be better in ScreenUtils
    public void drawCenteredString(PoseStack matrixStack, FormattedCharSequence text, int x, int y, int color) {
        Minecraft.getInstance().font.drawShadow(matrixStack, text, (float) (x - Minecraft.getInstance().font.width(text) / 2), (float) y, color);
    }

    @Override
    public void cancelOrder() {
        ServerOrderCallbacks.cancelOrder(orderNumber);
        super.cancelOrder();
    }

    @Override
    public String getStepName() {
        return I18n.get("minetogether.screen.order");
    }
}
