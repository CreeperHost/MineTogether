package net.creeperhost.minetogether.trade.client;

import net.creeperhost.minetogether.trade.ContainerTrade;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiTrade extends GuiContainer
{
    private EntityPlayer player;
    private final ResourceLocation texture = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
    private GuiButton trade;
    private ContainerTrade containerTrade;

    public GuiTrade(EntityPlayer player)
    {
        super(new ContainerTrade(player));
        this.containerTrade = (ContainerTrade) this.inventorySlots;
        this.player = player;
        this.xSize = 176;
        this.ySize = 167;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.buttonList.add(trade = new GuiButton(8008, width - 80 - 50, 50, 20, 20, "Trade"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button == trade)
        {
            if(!this.containerTrade.tradeInventory.getStackInSlot(0).isEmpty())
            {
                this.containerTrade.tradeInventory.getStackInSlot(0);
                this.containerTrade.tradeInventory.setInventorySlotContents(0, new ItemStack(Items.APPLE));
            }
        }
        super.actionPerformed(button);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
    }
}
