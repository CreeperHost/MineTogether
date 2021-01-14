package net.creeperhost.minetogether.client.screen.order;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

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
    private Button buttonInvoice;
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
    public void init()
    {
        super.init();
        this.buttonNext.visible = false;
        buttonCancel.setMessage(new StringTextComponent(Util.localize("order.ordercancel")));
        buttonCancel.active = false;
        buttonPrev.active = false;
        buttonPrev.visible = false;
        buttonInvoice = addButton(new Button(this.width / 2 - 40, (this.height / 2) + 30, 80, 20, new StringTextComponent(Util.localize("button.invoice")), p ->
        {
            try
            {
                net.minecraft.util.Util.getOSType().openURI(new URI(MineTogether.instance.getImplementation().getPaymentLink(invoiceID)));
            } catch (Throwable throwable)
            {
                MineTogether.logger.error("Couldn't open link", throwable);
            }
        }));
        buttonNext.visible = true;
        buttonNext.active = true;
        buttonInvoice.visible = false;
    }
    
    @SuppressWarnings("Duplicates")
    public void tick()
    {
        super.tick();
        if (!createdAccount && !creatingAccount)
        {
            if (!createdAccountError.isEmpty())
            {
                buttonCancel.active = true;
                return;
            }
            creatingAccount = true;
            Runnable runnable = () ->
            {
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
            buttonCancel.active = true;
            return;
        } else if (!placingOrder && !placedOrder)
        {
            placingOrder = true;
            buttonNext.active = false;
            Runnable runnable = () ->
            {
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
            buttonCancel.active = false;
        } else if (placingOrder)
        {
            return;
        } else if (placedOrderError.isEmpty())
        {
            if (!serverAdded)
            {
                ServerList savedServerList = new ServerList(this.minecraft);
                savedServerList.loadServerList();
                savedServerList.addServerData(MineTogether.instance.getImplementation().getServerEntry(order));
                savedServerList.saveServerList();
                serverAdded = true;
            }
            buttonInvoice.visible = true;
            buttonNext.visible = true;
            buttonCancel.active = false;
            buttonNext.active = true;
        } else
        {
            buttonNext.active = true;
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);

        if (creatingAccount)
        {
            drawCenteredString(matrixStack, font, Util.localize("order.accountcreating"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!createdAccountError.isEmpty())
        {
            drawCenteredString(matrixStack, font, Util.localize("order.accounterror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(new StringTextComponent(createdAccountError), width - 30, font);
            int offset = 10;
            for (IReorderingProcessor str : list)
            {
                drawCenteredString(matrixStack, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(matrixStack, font, Util.localize("order.accounterrorgoback"), this.width / 2, this.height / 2 + offset, 0xFFFFFF);
        } else if (placingOrder)
        {
            drawCenteredString(matrixStack, font, Util.localize("order.orderplacing"), this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!placedOrderError.isEmpty())
        {
            drawCenteredString(matrixStack, font, Util.localize("order.ordererror"), this.width / 2, this.height / 2, 0xFFFFFF);
            List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(new StringTextComponent(placedOrderError), width - 30, font);
            int offset = 10;
            for (IReorderingProcessor str : list)
            {
                drawCenteredString(matrixStack, str, this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
                offset += 10;
            }
            drawCenteredString(matrixStack, font, Util.localize("order.ordererrorsupport"), this.width / 2, (this.height / 2) + offset, 0xFFFFFF);
        } else
        {
            drawCenteredString(matrixStack, font, Util.localize("order.ordersuccess"), this.width / 2, this.height / 2, 0xFFFFFF);
            drawCenteredString(matrixStack, font, Util.localize("order.ordermodpack"), (this.width / 2) + 10, (this.height / 2) + 10, 0xFFFFFF);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void drawCenteredString(MatrixStack matrixStack, IReorderingProcessor text, int x, int y, int color)
    {
        Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, text, (float)(x - Minecraft.getInstance().fontRenderer.func_243245_a(text) / 2), (float)y, color);
    }
}
