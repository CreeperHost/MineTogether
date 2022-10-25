package net.creeperhost.minetogether.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherServer;
import net.creeperhost.minetogether.lib.web.ApiClientResponse;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.server.web.SendInviteRequest;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandInvite {

    private static final Logger LOGGER = LogManager.getLogger();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("invite").requires(cs -> cs.hasPermission(3)).then(Commands.argument("username", StringArgumentType.string()).executes(cs -> execute(cs, StringArgumentType.getString(cs, "username"))));
    }

    private static int execute(CommandContext<CommandSourceStack> cs, String username) {
        MinecraftServer minecraftServer = cs.getSource().getServer();
        if (username.isEmpty()) throw new CommandRuntimeException(new TranslatableComponent("Invalid username"));

        GameProfile gameProfile = minecraftServer.getProfileCache().get(username).get();
        if (gameProfile == null) {
            throw new CommandRuntimeException(new TranslatableComponent("Failed to load GameProfile, Username is not valid"));
        }

        if (minecraftServer.getPlayerList().getWhiteList().isWhiteListed(gameProfile)) { throw new CommandRuntimeException(new TranslatableComponent(username + " Is already whitelisted")); }

        UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
        minecraftServer.getPlayerList().getWhiteList().add(userWhiteListEntry);
        minecraftServer.getPlayerList().reloadWhiteList();
        sendUserInvite(gameProfile, minecraftServer);

        cs.getSource().sendSuccess(new TranslatableComponent(username + " Added to whitelist"), false);
        return 0;
    }

    public static void sendUserInvite(GameProfile profile, MinecraftServer server) {
        UserWhiteList whitelistedPlayers = server.getPlayerList().getWhiteList();
        ArrayList<String> tempHash = new ArrayList<>();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(whitelistedPlayers.get(profile).toString().getBytes(StandardCharsets.UTF_8));
            tempHash.add(Arrays.toString(digest.digest(hash)));
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.fatal("SHA256 not supported??", ex);
        }

        try {
            ApiClientResponse<ApiResponse> resp = MineTogether.API.execute(new SendInviteRequest(MineTogetherServer.inviteId, tempHash));
            LOGGER.debug("Response from add endpoint " + resp.apiResponse().getStatus() + " " + resp.apiResponse().getMessageOrNull());
        } catch (IOException e) {
            LOGGER.error("Failed to send invite.");
        }

    }
}
