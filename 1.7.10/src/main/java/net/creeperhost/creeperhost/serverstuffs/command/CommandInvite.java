package net.creeperhost.creeperhost.serverstuffs.command;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.creeperhost.creeperhost.serverstuffs.CreeperHostServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CommandInvite extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "invite";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "creeperhostserver.commands.invite.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (args.length < 1)
        {
            throw new WrongUsageException("creeperhostserver.commands.invite.usage", new Object[0]);
        }
        else
        {
            if ("list".equals(args[0]))
            {
                sender.addChatMessage(new ChatComponentTranslation("creeperhostserver.commands.invite.list", new Object[] {MinecraftServer.getServer().getConfigurationManager().func_152598_l().length, server.func_152358_ax().func_152654_a().length}));
                String[] astring = server.getConfigurationManager().func_152598_l();
                sender.addChatMessage(new ChatComponentText(joinNiceString(astring)));
            }
            else if ("add".equals(args[0]))
            {
                if (args.length < 2)
                {
                    throw new WrongUsageException("creeperhostserver.commands.invite.add.usage", new Object[0]);
                }

                GameProfile gameprofile = server.func_152358_ax().func_152655_a(args[1]);

                if (gameprofile == null)
                {
                    throw new CommandException("creeperhostserver.commands.invite.add.failed", new Object[] {args[1]});
                }

                server.getConfigurationManager().func_152601_d(gameprofile);
                inviteUser(gameprofile);
                func_152373_a(sender, this, "creeperhostserver.commands.invite.add.success", new Object[] {args[1]});
            }
            else if ("remove".equals(args[0]))
            {
                if (args.length < 2)
                {
                    throw new WrongUsageException("creeperhostserver.commands.invite.remove.usage", new Object[0]);
                }

                GameProfile gameprofile1 = server.getConfigurationManager().func_152599_k().func_152706_a(args[1]);

                if (gameprofile1 == null)
                {
                    throw new CommandException("creeperhostserver.commands.invite.remove.failed", new Object[] {args[1]});
                }

                server.getConfigurationManager().func_152597_c(gameprofile1);
                removeUser(gameprofile1);
                func_152373_a(sender, this, "creeperhostserver.commands.invite.remove.success", new Object[] {args[1]});
            }
            else if ("reload".equals(args[0]))
            {
                String[] prevNames = MinecraftServer.getServer().getConfigurationManager().func_152598_l();
                server.getConfigurationManager().loadWhiteList();
                reloadInvites(prevNames);
                func_152373_a(sender, this, "creeperhostserver.commands.invite.reloaded", new Object[0]);
            } else {
                throw new WrongUsageException("creeperhostserver.commands.invite.usage", new Object[0]);
            }
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
    {
        if (p_71516_2_.length == 1)
        {
            /**
             * Returns a List of strings (chosen from the given strings) which the last word in the given string array
             * is a beginning-match for. (Tab completion).
             */
            return getListOfStringsMatchingLastWord(p_71516_2_, new String[] {"on", "off", "list", "add", "remove", "reload"});
        }
        else
        {
            if (p_71516_2_.length == 2)
            {
                if (p_71516_2_[0].equals("remove"))
                {
                    /**
                     * Returns a List of strings (chosen from the given strings) which the last word in the given string
                     * array is a beginning-match for. (Tab completion).
                     */
                    return getListOfStringsMatchingLastWord(p_71516_2_, MinecraftServer.getServer().getConfigurationManager().func_152598_l());
                }

                if (p_71516_2_[0].equals("add"))
                {
                    /**
                     * Returns a List of strings (chosen from the given strings) which the last word in the given string
                     * array is a beginning-match for. (Tab completion).
                     */
                    return getListOfStringsMatchingLastWord(p_71516_2_, MinecraftServer.getServer().func_152358_ax().func_152654_a());
                }
            }

            return null;
        }
    }

    private void inviteUser(GameProfile profile)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Gson gson = new Gson();
        UserListWhitelist whitelistedPlayers = server.getConfigurationManager().func_152599_k();
        final ArrayList<String> tempHash = new ArrayList<String>();
        String name = profile.getName().toLowerCase();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(whitelistedPlayers.func_152706_a(name).getId().toString().getBytes(Charset.forName("UTF-8")));

            tempHash.add((new HexBinaryAdapter()).marshal(hash));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        CreeperHostServer.InviteClass invite = new CreeperHostServer.InviteClass();
        invite.hash = tempHash;
        invite.id = CreeperHostServer.updateID;
        Util.putWebResponse("https://api.creeper.host/serverlist/invite", gson.toJson(invite), true, true);
    }

    private void removeUser(GameProfile profile)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Gson gson = new Gson();
        final ArrayList<String> tempHash = new ArrayList<String>();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(profile.getId().toString().getBytes(Charset.forName("UTF-8")));

            tempHash.add((new HexBinaryAdapter()).marshal(hash));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        CreeperHostServer.InviteClass invite = new CreeperHostServer.InviteClass();
        invite.hash = tempHash;
        invite.id = CreeperHostServer.updateID;
        CreeperHostServer.logger.debug("Sending " + gson.toJson(invite) + " to revoke endpoint");
        String resp = Util.putWebResponse("https://api.creeper.host/serverlist/revokeinvite", gson.toJson(invite), true, true);
        CreeperHostServer.logger.debug("Response from revoke endpoint " + resp);
    }

    public static void reloadInvites(String[] prevNames)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Gson gson = new Gson();
        UserListWhitelist whitelistedPlayers = server.getConfigurationManager().func_152599_k();
        final ArrayList<String> tempHash = new ArrayList<String>();
        final ArrayList<String> removeHash = new ArrayList<String>();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String name : whitelistedPlayers.func_152685_a())
            {
                byte[] hash = digest.digest(whitelistedPlayers.func_152706_a(name).getId().toString().getBytes(Charset.forName("UTF-8")));

                tempHash.add((new HexBinaryAdapter()).marshal(hash));
            }

            for (String name : prevNames)
            {
                if (whitelistedPlayers.func_152706_a(name) == null)
                {
                    byte[] hash = digest.digest(whitelistedPlayers.func_152706_a(name).getId().toString().getBytes(Charset.forName("UTF-8")));

                    removeHash.add((new HexBinaryAdapter()).marshal(hash));
                }
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        CreeperHostServer.InviteClass invite = new CreeperHostServer.InviteClass();
        invite.hash = tempHash;
        invite.id = CreeperHostServer.updateID;

        Util.putWebResponse("https://api.creeper.host/serverlist/invite", gson.toJson(invite), true, true);
        if (!removeHash.isEmpty())
        {
            invite = new CreeperHostServer.InviteClass();
            invite.hash = tempHash;
            invite.id = CreeperHostServer.updateID;
            Util.putWebResponse("https://api.creeper.host/serverlist/revokeinvite", gson.toJson(invite), true, true);
        }
    }
}