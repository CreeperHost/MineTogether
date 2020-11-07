package net.creeperhost.minetogether.trade.commands;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.command.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;

public class CommandGiveNoCheat
{
    public static void execute(ICommand command, MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.give.usage", new Object[0]);
        }
        else
        {
            EntityPlayer entityplayer = CommandBase.getPlayer(server, sender, args[0]);
            Item item = CommandBase.getItemByText(sender, args[1]);
            int i = args.length >= 3 ? CommandBase.parseInt(args[2], 1, item.getItemStackLimit()) : 1;
            int j = args.length >= 4 ? CommandBase.parseInt(args[3]) : 0;
            ItemStack itemstack = new ItemStack(item, i, j);

            if (args.length >= 5)
            {
                String s = CommandBase.buildString(args, 4);

                try
                {
                    itemstack.setTagCompound(JsonToNBT.getTagFromJson(s));
                }
                catch (NBTException nbtexception)
                {
                    throw new CommandException("commands.give.tagError", new Object[] {nbtexception.getMessage()});
                }
            }

            addCheatTag(itemstack, true);

            boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);

            if (flag)
            {
                entityplayer.world.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                entityplayer.inventoryContainer.detectAndSendChanges();
            }

            if (flag && itemstack.isEmpty())
            {
                itemstack.setCount(1);
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, i);
                EntityItem entityitem1 = entityplayer.dropItem(itemstack, false);

                if (entityitem1 != null)
                {
                    entityitem1.makeFakeItem();
                }
            }
            else
            {
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, i - itemstack.getCount());
                EntityItem entityitem = entityplayer.dropItem(itemstack, false);

                if (entityitem != null)
                {
                    entityitem.setNoPickupDelay();
                    entityitem.setOwner(entityplayer.getName());
                }
            }

            CommandBase.notifyCommandListener(sender, command, "commands.give.success", new Object[] {itemstack.getTextComponent(), i, entityplayer.getName()});
        }
    }

    public static void addCheatTag(ItemStack stack, boolean value)
    {
        NBTTagCompound ours = new NBTTagCompound();
        ours.setBoolean(CreeperHost.MOD_ID + ":unsafe", value);

        if(stack.hasTagCompound())
        {
            NBTTagCompound compound = stack.getTagCompound();
            if(!compound.hasKey(CreeperHost.MOD_ID + ":unsafe")) compound.merge(ours);
        }
        else
        {
            stack.setTagCompound(ours);
        }
    }
}
