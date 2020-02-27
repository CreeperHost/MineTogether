package net.creeperhost.minetogether.server.command;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.WhiteList;
import net.minecraft.server.management.WhitelistEntry;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandInvite
{
    public static void reloadInvites(String[] prevNames)
    {
        MinecraftServer server = MineTogether.server;
        if (server == null)
        {
            return;
        }
        Gson gson = new Gson();
        WhiteList whitelistedPlayers = server.getPlayerList().getWhitelistedPlayers();
        final ArrayList<String> tempHash = new ArrayList<String>();
        final ArrayList<String> removeHash = new ArrayList<String>();

//        try
//        {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            for (String name : whitelistedPlayers.getKeys())
//            {
//                byte[] hash = digest.digest(whitelistedPlayers.getByName(name).getId().toString().getBytes(Charset.forName("UTF-8")));
//
//                tempHash.add((new HexBinaryAdapter()).marshal(hash));
//            }
//
//            for (String name : prevNames)
//            {
//                if (whitelistedPlayers.getByName(name) == null)
//                {
//                    byte[] hash = digest.digest(whitelistedPlayers.getByName(name).getId().toString().getBytes(Charset.forName("UTF-8")));
//
//                    removeHash.add((new HexBinaryAdapter()).marshal(hash));
//                }
//            }
//        } catch (NoSuchAlgorithmException e)
//        {
//            e.printStackTrace();
//        }

        MineTogether.InviteClass invite = new MineTogether.InviteClass();
        invite.hash = tempHash;
        invite.id = MineTogether.updateID;

        MineTogether.logger.debug("Sending " + gson.toJson(invite) + " to add endpoint");
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invite", gson.toJson(invite), true, true);
        MineTogether.logger.debug("Response from add endpoint " + resp);
        if (!removeHash.isEmpty())
        {
            invite = new MineTogether.InviteClass();
            invite.id = MineTogether.updateID;
            invite.hash = tempHash;
            MineTogether.logger.debug("Sending " + gson.toJson(invite) + " to revoke endpoint");
            resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/revokeinvite", gson.toJson(invite), true, true);
            MineTogether.logger.debug("Response from revoke endpoint " + resp);
        }
    }

//    public GameProfile getByName(String profileName, WhiteList whiteList)
//    {
//        for (WhitelistEntry userlistwhitelistentry : whiteList.getEntries())
//        {
//            if (profileName.equalsIgnoreCase((userlistwhitelistentry.getValue()).getName()))
//            {
//                return userlistwhitelistentry.getValue();
//            }
//        }
//        return null;
//    }

//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
//    {
//        if (args.length < 1)
//        {
//            throw new WrongUsageException("creeperhostserver.commands.invite.usage", new Object[0]);
//        } else
//        {
//            if ("list".equals(args[0]))
//            {
//                sender.sendMessage(new TextComponentTranslation("creeperhostserver.commands.invite.list", new Object[]{server.getPlayerList().getWhitelistedPlayerNames().length, server.getPlayerList().getAvailablePlayerDat().length}));
//                String[] astring = server.getPlayerList().getWhitelistedPlayerNames();
//                sender.sendMessage(new TextComponentString(joinNiceString(astring)));
//            } else if ("add".equals(args[0]))
//            {
//                if (args.length < 2)
//                {
//                    throw new WrongUsageException("creeperhostserver.commands.invite.add.usage", new Object[0]);
//                }
//
//                GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
//
//                if (gameprofile == null)
//                {
//                    throw new CommandException("creeperhostserver.commands.invite.add.failed", new Object[]{args[1]});
//                }
//
//                server.getPlayerList().addWhitelistedPlayer(gameprofile);
//                inviteUser(gameprofile);
//                notifyCommandListener(sender, this, "creeperhostserver.commands.invite.add.success", new Object[]{args[1]});
//            } else if ("remove".equals(args[0]))
//            {
//                if (args.length < 2)
//                {
//                    throw new WrongUsageException("creeperhostserver.commands.invite.remove.usage", new Object[0]);
//                }
//
//                GameProfile gameprofile1 = server.getPlayerList().getWhitelistedPlayers().getByName(args[1]);
//
//                if (gameprofile1 == null)
//                {
//                    throw new CommandException("creeperhostserver.commands.invite.remove.failed", new Object[]{args[1]});
//                }
//
//                server.getPlayerList().removePlayerFromWhitelist(gameprofile1);
//                removeUser(gameprofile1);
//                notifyCommandListener(sender, this, "creeperhostserver.commands.invite.remove.success", new Object[]{args[1]});
//            } else if ("reload".equals(args[0]))
//            {
//                String[] prevNames = server.getPlayerList().getWhitelistedPlayerNames();
//                server.getPlayerList().reloadWhitelist();
//                reloadInvites(prevNames);
//                notifyCommandListener(sender, this, "creeperhostserver.commands.invite.reloaded", new Object[0]);
//            } else
//            {
//                throw new WrongUsageException("creeperhostserver.commands.invite.usage", new Object[0]);
//            }
//        }
//    }
//
//    @Override
//    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
//    {
//        if (args.length == 1)
//        {
//            return getListOfStringsMatchingLastWord(args, new String[]{"list", "add", "remove", "reload"});
//        } else
//        {
//            if (args.length == 2)
//            {
//                if ("remove".equals(args[0]))
//                {
//                    return getListOfStringsMatchingLastWord(args, server.getPlayerList().getWhitelistedPlayerNames());
//                }
//                if ("add".equals(args[0]))
//                {
//                    return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
//                }
//            }
//            return Collections.emptyList();
//        }
//    }

    private void inviteUser(GameProfile profile)
    {
        MinecraftServer server = MineTogether.server;
        Gson gson = new Gson();
        WhiteList whitelistedPlayers = server.getPlayerList().getWhitelistedPlayers();

        final ArrayList<String> tempHash = new ArrayList<String>();
        String name = profile.getName().toLowerCase();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

//            byte[] hash = digest.digest(whitelistedPlayers.getByName(name).getId().toString().getBytes(Charset.forName("UTF-8")));

//            tempHash.add((new HexBinaryAdapter()).marshal(hash));
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        MineTogether.InviteClass invite = new MineTogether.InviteClass();
        invite.hash = tempHash;
        invite.id = MineTogether.updateID;
        MineTogether.logger.debug("Sending " + gson.toJson(invite) + " to add endpoint");
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invite", gson.toJson(invite), true, true);
        MineTogether.logger.debug("Response from add endpoint " + resp);
    }

    private void removeUser(GameProfile profile)
    {
        MinecraftServer server = MineTogether.server;
        Gson gson = new Gson();
        final ArrayList<String> tempHash = new ArrayList<String>();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(profile.getId().toString().getBytes(Charset.forName("UTF-8")));

            String hashString = (new HexBinaryAdapter()).marshal(hash);

            MineTogether.logger.info("Removing player with hash " + hashString);

            tempHash.add(hashString);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        MineTogether.InviteClass invite = new MineTogether.InviteClass();
        invite.hash = tempHash;
        invite.id = MineTogether.updateID;
        MineTogether.logger.debug("Sending " + gson.toJson(invite) + " to revoke endpoint");
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/revokeinvite", gson.toJson(invite), true, true);
        MineTogether.logger.debug("Response from revoke endpoint " + resp);
    }
}