package net.creeperhost.minetogether.server.command;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.WhiteList;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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