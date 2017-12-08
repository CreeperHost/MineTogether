package net.creeperhost.creeperhost.proxy;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.creeperhost.creeperhost.gui.GuiFriendsList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Session;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.UUID;

public class Client implements IProxy
{
    private KeyBinding openGuiKey;

    @Override
    public void registerKeys()
    {
        openGuiKey = new KeyBinding("minetogether.opengui", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_F, "key.categories.general");
        ClientRegistry.registerKeyBinding(openGuiKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (openGuiKey.isPressed())
        {
            openFriendsGui();
        }
    }


    @Override
    public void openFriendsGui()
    {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiFriendsList(mc.currentScreen));
    }

    private UUID cache;
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
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(mc.getProxy(), UUID.randomUUID().toString());
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(mc.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
            uuid = playerprofilecache.getGameProfileForUsername(Minecraft.getMinecraft().getSession().getUsername()).getId();
        } else {
            uuid = EntityPlayer.getOfflineUUID(session.getUsername().toLowerCase());
        }
        cache = uuid;
        return uuid;
    }
}
