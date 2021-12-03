package net.creeperhost.minetogether.commands;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.creeperhost.minetogether.MineTogetherCommon;
import net.creeperhost.minetogether.MineTogetherServer;
import net.creeperhost.minetogether.lib.util.WebUtils;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandInvite
{
    public static LiteralArgumentBuilder<CommandSourceStack> register()
    {
        return Commands.literal("invite").requires(cs -> cs.hasPermission(3)).then(Commands.argument("username", StringArgumentType.string()).executes(cs -> execute(cs, StringArgumentType.getString(cs, "username"))));
    }

    private static int execute(CommandContext<CommandSourceStack> cs, String username)
    {
        MinecraftServer minecraftServer = cs.getSource().getServer();
        if (username.isEmpty()) throw new CommandRuntimeException(new TranslatableComponent("Invalid username"));

        GameProfile gameProfile = minecraftServer.getProfileCache().get(username).get();
        if (gameProfile == null)
            throw new CommandRuntimeException(new TranslatableComponent("Failed to load GameProfile, Username is not valid"));

        if (minecraftServer.getPlayerList().getWhiteList().isWhiteListed(gameProfile))
            throw new CommandRuntimeException(new TranslatableComponent(username + " Is already whitelisted"));

        UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
        minecraftServer.getPlayerList().getWhiteList().add(userWhiteListEntry);
        minecraftServer.getPlayerList().reloadWhiteList();
        sendUserInvite(gameProfile, minecraftServer);

        cs.getSource().sendSuccess(new TranslatableComponent(username + " Added to whitelist"), false);
        return 0;
    }

    public static void sendUserInvite(GameProfile profile, MinecraftServer server)
    {
        Gson gson = new Gson();
        UserWhiteList whitelistedPlayers = server.getPlayerList().getWhiteList();
        final ArrayList<String> tempHash = new ArrayList<>();

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(whitelistedPlayers.get(profile).toString().getBytes(StandardCharsets.UTF_8));

            tempHash.add(Arrays.toString(digest.digest(hash)));

        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        InviteClass invite = new InviteClass();
        invite.hash = tempHash;
        invite.id = MineTogetherServer.updateID;
        MineTogetherCommon.logger.debug("Sending " + gson.toJson(invite) + " to add endpoint");
        String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/invite", gson.toJson(invite), true, true);
        MineTogetherCommon.logger.debug("Response from add endpoint " + resp);
    }

    public static class InviteClass
    {
        public int id = MineTogetherServer.updateID;
        public ArrayList<String> hash;
    }
}
