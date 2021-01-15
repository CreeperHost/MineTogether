package net.creeperhost.minetogether.trade.commands;

import net.creeperhost.minetogether.serverstuffs.CreeperHostServer;
import net.creeperhost.minetogether.serverstuffs.MineTogetherPropertyManager;
import net.creeperhost.minetogether.trade.CloudUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CommandTrade extends CommandBase
{
    @Override
    public String getName() {
        return "trade";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "creeperhostserver.commands.trade.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length > 0)
        {
            if (sender.getCommandSenderEntity() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
                ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
                switch (args[0].trim())
                {
                    case "send":
                        if (!itemStack.isEmpty()) {
                            boolean flag = CloudUtils.sendStack(player, itemStack);
                            player.sendMessage(new TextComponentString("Send " + flag));
                            break;
                        }
                    case "remove":
                        if (!itemStack.isEmpty()) {
                            boolean flag = CloudUtils.removeStack(player, itemStack);
                            player.sendMessage(new TextComponentString("Remove " + flag));
                            break;
                        }
                    case "list":
                        List<ItemStack> itemStacks = CloudUtils.listStacks(player);
                        if(!itemStacks.isEmpty()) {
                            player.sendMessage(new TextComponentString(TextFormatting.RED + "Tradable Items"));
                            for (ItemStack i : itemStacks) {
                                player.sendMessage(new TextComponentString("Item Type: " + i.getDisplayName() + " Quantity: " + i.getCount()));
                            }
                        } else {
                            player.sendMessage(new TextComponentString("EMPTY"));
                        }
                        break;
                    case "serverid":
                        player.sendMessage(new TextComponentString(CreeperHostServer.INSTANCE.getNick()));
                }
//            player.openGui(CreeperHost.INSTANCE, GuiHandler.trade, player.world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
            }
        }
    }
}
