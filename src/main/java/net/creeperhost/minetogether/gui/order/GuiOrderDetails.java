package net.creeperhost.minetogether.gui.order;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.gui.order.GuiGetServer;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.ServerList;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Created by Aaron on 02/05/2017.
 */
public class GuiOrderDetails extends GuiGetServer
{
    private boolean placingOrder = false;
    private boolean placedOrder = false;
    private boolean creatingAccount = false;
    private boolean createdAccount = false;
    private String createdAccountError = "";
    private int orderNumber;
    private String invoiceID;
    private String placedOrderError = "";
    private GuiButton buttonInvoice;
    private boolean serverAdded;
    
    
    public GuiOrderDetails(int stepId, Order order)
    {
        super(stepId, order);
        if (order.clientID != null && !order.clientID.isEmpty())
        {
            creatingAccount = false;
            createdAccount = true;
        }
    }
    
    @Override
    public String getStepName()
    {
        return Util.localize("gui.order");
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonNext.visible = false;
        buttonCancel.displayString = Util.localize("order.ordercancel");
        buttonCancel.enabled = false;
        buttonInvoice = new GuiButton(80000085, this.width / 2 - 40, (this.height / 2) + 30, 80, 20, Util.localize("button.invoice"));
        this.buttonList.add(buttonInvoice);
        buttonInvoice.visible = false;
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == buttonCancel.id)
        {
            CreeperHost.instance.getImplementation().cancelOrder(orderNumber);
        }
        super.actionPerformed(button);
        if (button.id == 80000085)
        {
            try
            {
                Class<?> oclass = Class.forName("java.awt.Desktop");
                Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
                oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new Object[]{new URI(CreeperHost.instance.getImplementation().getPaymentLink(invoiceID))});
                this.buttonNext.visible = true;
                this.buttonNext.enabled = true;
            } catch (Throwable throwable)
            {
                CreeperHost.logger.error("Couldn\'t open link", throwable);
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    public void updateScreen()
    {
        super.updateScreen();
        if (!createdAccount && !creatingAccount)
        {
            if (!createdAccountError.isEmpty())
            {
                buttonCancel.enabled = true;
                return;
            }
            creatingAccount = true;
            Runnable runnable = () -> {
                String result = Callbacks.createAccount(order);
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success"))
                {
                    order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                    order.clientID = resultSplit[2] != null ? resultSplit[2] : "0"; // random test account fallback

                } else
                {
                    createdAccountError = result;
                    createdAccount = true;
                }
                creatingAccount = false;
                createdAccount = true;
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } else if (creatingAccount)
        {
            return;
        } else if (!createdAccountError.isEmpty())
        {
            buttonCancel.enabled = true;
            return;
        } else if (!placingOrder && !placedOrder)
        {
            placingOrder = true;
            buttonNext.enabled = false;
            Runnable runnable = () -> {
                String result = Callbacks.createOrder(order);
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success"))
                {
                    invoiceID = resultSplit[1] != null ? resultSplit[1] : "0";
                    orderNumber = Integer.valueOf(resultSplit[2]);
                } else
                {
                    placedOrderError = result;
                }
                placedOrder = true;
                placingOrder = false;
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } else if (placingOrder)
        {
            return;
        } else if (placedOrderError.isEmpty())
        {
            if (!serverAdded)
            {
                ServerList savedServerList = new ServerList(this.mc);
                savedServerList.loadServerList();
                savedServerList.addServerData(CreeperHost.instance.getImplementation().getServerEntry(order));
                savedServerList.saveServerList();
                serverAdded = true;
            }
            buttonInvoice.visible = true;
            buttonNext.visible = true;
            buttonCancel.enabled = true;
            return;
        } else {
            buttonNext.enabled = true;
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        drawGradientRect(0, this.height - 20, width, 20, 0x99000000, 0x99000000);
        if (creatingAccount)
        {
            drawCenteredString(fontRendererObj, Util.localize("order.accountcreating"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!createdAccountError.isEmpty())
        {
            drawCenteredString(fontRendererObj, Util.localize("order.accounterror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<String> list = fontRendererObj.listFormattedStringToWidth(createdAccountError, width - 30);
            int offset = 10;
            for (String str : list)
            {
                drawCenteredString(fontRendererObj, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(fontRendererObj, Util.localize("order.accounterrorgoback"), this.width / 2, this.height / 2 + offset, 0xFFFFFF);
        } else if (placingOrder)
        {
            drawCenteredString(fontRendererObj, Util.localize("order.orderplacing"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!placedOrderError.isEmpty())
        {
            drawCenteredString(fontRendererObj, Util.localize("order.ordererror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<String> list = fontRendererObj.listFormattedStringToWidth(placedOrderError, width - 30);
            int offset = 10;
            for (String str : list)
            {
                drawCenteredString(fontRendererObj, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(fontRendererObj, Util.localize("order.ordererrorsupport"), this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
        } else
        {
            drawCenteredString(fontRendererObj, Util.localize("order.ordersuccess"), this.width / 2, this.height / 2, 0xFFFFFF);
            drawCenteredString(fontRendererObj, Util.localize("order.ordermodpack"), (this.width / 2) + 10, (this.height / 2) + 10, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
