package net.creeperhost.minetogether.orderform.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.orderform.ServerOrderCallbacks;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.polylib.client.screen.ScreenHelper;
import net.creeperhost.polylib.client.screen.widget.LoadingSpinner;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
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

    @SuppressWarnings("Duplicates")
    @Override
    public void init() {
        clearWidgets();

        super.init();
        this.buttonNext.setMessage(Component.translatable("minetogether.button.finish"));
        this.buttonNext.visible = false;
        buttonCancel.setMessage(Component.translatable("minetogether.order.ordercancel"));
        buttonCancel.active = false;
        buttonPrev.active = false;
        buttonPrev.visible = false;
        buttonInvoice = addRenderableWidget(Button.builder(Component.translatable("minetogether.button.invoice"), p ->
                        {
                            try {
                                Util.getPlatform().openUri(new URI(ServerOrderCallbacks.getPaymentLink(invoiceID)));
                            } catch (Throwable throwable) {
                                LOGGER.error("Couldn't open link", throwable);
                            }
                        })
                        .bounds(this.width / 2 - 40, (this.height / 2) + 30, 80, 20)
                        .build()
        );
        buttonNext.visible = true;
        buttonNext.active = true;
        buttonInvoice.visible = false;
    }

    @SuppressWarnings("Duplicates")
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
                String result = ServerOrderCallbacks.createAccount(order);
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
                String result = ServerOrderCallbacks.createOrder(order, String.valueOf(Config.instance().pregenDiameter));
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
        savedServerList.add(getServerEntry(order), false);
        savedServerList.save();
        return true;
    }

    public ServerData getServerEntry(Order order) {
        return new ServerData(order.name + ".PlayAt.CH", order.name + ".playat.ch", false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(graphics);
        graphics.fill(0, this.height - 20, width, 20, 0x99000000);

        if (creatingAccount) {
            graphics.drawCenteredString(font, I18n.get("minetogether.order.accountcreating"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!createdAccountError.isEmpty()) {
            graphics.drawCenteredString(font, I18n.get("minetogether.order.accounterror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(Component.translatable(createdAccountError), width - 30, font);
            int offset = 10;
            for (FormattedCharSequence str : list) {
                graphics.drawCenteredString(font, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            graphics.drawCenteredString(font, I18n.get("minetogether.order.accounterrorgoback"), this.width / 2, this.height / 2 + offset, 0xFFFFFF);
        } else if (placingOrder) {
            graphics.drawCenteredString(font, I18n.get("minetogether.order.orderplacing"), this.width / 2, this.height / 2, 0xFFFFFF);
            LoadingSpinner.render(graphics.pose(), partialTicks, ticks, width / 2, height / 2 + 20, new ItemStack(Items.BEEF));
        } else if (!placedOrderError.isEmpty()) {
            graphics.drawCenteredString(font, I18n.get("minetogether.order.ordererror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(Component.translatable(placedOrderError), width - 30, font);
            int offset = 10;
            for (FormattedCharSequence str : list) {
                graphics.drawCenteredString(font, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            graphics.drawCenteredString(font, I18n.get("minetogether.order.ordererrorsupport"), this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(font, I18n.get("minetogether.order.ordersuccess"), this.width / 2, this.height / 2, 0xFFFFFF);
            graphics.drawCenteredString(font, I18n.get("minetogether.order.ordermodpack"), (this.width / 2) + 10, (this.height / 2) + 10, 0xFFFFFF);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
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
