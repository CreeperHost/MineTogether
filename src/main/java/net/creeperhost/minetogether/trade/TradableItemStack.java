package net.creeperhost.minetogether.trade;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.item.ItemStack;

public class TradableItemStack
{
    private ItemStack itemStack;

    public TradableItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    public boolean isTradeAble()
    {
        if(itemStack.isEmpty()) return false;
        if(!itemStack.hasTagCompound()) return false;
        if(itemStack.getTagCompound().hasKey(CreeperHost.MOD_ID + ":unsafe")) return false;

        return true;
    }
}
