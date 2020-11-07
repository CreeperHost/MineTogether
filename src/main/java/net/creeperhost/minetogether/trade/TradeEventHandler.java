package net.creeperhost.minetogether.trade;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.trade.commands.CommandGiveNoCheat;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandGive;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.concurrent.CompletableFuture;

public class TradeEventHandler
{
    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        if(event.crafting.isEmpty()) return;
        if(event.player == null) return;
        //TODO checking that items are not spawned in
        if(!event.player.world.isRemote) CompletableFuture.runAsync(() -> CloudUtils.sendStack(event.player, event.crafting), CreeperHost.profileExecutor);

        CommandGiveNoCheat.addCheatTag(event.crafting, false);
    }

    //    public static void execute(ICommand command, MinecraftServer server, ICommandSender sender, String[] args) throws CommandException

    @SubscribeEvent
    public void onSpawned(CommandEvent event)
    {
        if(event.getCommand() instanceof CommandGive)
        {
            try
            {
                CommandGiveNoCheat.execute(event.getCommand(), event.getSender().getServer(), event.getSender(), event.getParameters());
                event.setCanceled(true);
            } catch (CommandException e) { e.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void itemSpawned(EntityJoinWorldEvent entityJoinWorldEvent)
    {
        if(entityJoinWorldEvent.getEntity() instanceof EntityItem)
        {
           EntityItem entityItem = (EntityItem) entityJoinWorldEvent.getEntity();
           ItemStack stack = entityItem.getEntityItem();
           CommandGiveNoCheat.addCheatTag(stack, true);
        }
    }

    @SubscribeEvent
    public void toolTipEvent(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey(CreeperHost.MOD_ID + ":unsafe"))
        {
            event.getToolTip().add(TextFormatting.BLUE + "Account bound " + stack.getTagCompound().getBoolean(CreeperHost.MOD_ID + ":unsafe"));
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.HarvestDropsEvent event)
    {
        for (ItemStack drop : event.getDrops())
        {
            CommandGiveNoCheat.addCheatTag(drop, false);
        }
    }
}
