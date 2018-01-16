package net.creeperhost.minetogether.proxy;

import com.mojang.authlib.GameProfile;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiFriendsList;
import net.creeperhost.minetogether.gui.serverlist.gui.GuiInvited;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Session;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.UUID;

public class Client implements IProxy
{
    public KeyBinding openGuiKey;
    private UUID cache;

    @Override
    public void registerKeys()
    {
        openGuiKey = new KeyBinding("minetogether.key.friends", Keyboard.KEY_M, "minetogether.keys");
        Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.add(Minecraft.getMinecraft().gameSettings.keyBindings, openGuiKey);
    }

    @Override
    public void openFriendsGui()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (CreeperHost.instance.handledInvite == null)
        {
            mc.displayGuiScreen(new GuiFriendsList(mc.currentScreen));
        }
        else
        {
            mc.displayGuiScreen(new GuiInvited(CreeperHost.instance.handledInvite, mc.currentScreen));
            CreeperHost.instance.handledInvite = null;
        }
    }

    @Override
    public UUID getUUID()
    {
        if (cache != null)
            return cache;
        Minecraft mc = Minecraft.getMinecraft();
        Session session = mc.getSession();
        boolean online = true;
        if (session.getToken().length() != 32 || session.getPlayerID().length() != 32)
        {
            online = false;
        }

        UUID uuid;

        if (online)
        {
            PlayerProfileCache playerprofilecache = new PlayerProfileCache(MinecraftServer.getServer(), new File(mc.mcDataDir, MinecraftServer.field_152367_a.getName()));
            uuid = playerprofilecache.func_152655_a(Minecraft.getMinecraft().getSession().getUsername()).getId();
        }
        else
        {
            uuid = EntityPlayer.func_146094_a(new GameProfile(null, session.getUsername().toLowerCase()));
        }
        cache = uuid;
        return uuid;
    }
}
