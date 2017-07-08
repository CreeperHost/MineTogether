package net.creeperhost.creeperhost;

import net.creeperhost.creeperhost.api.Order;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.gui.GuiServerInfo;
import net.creeperhost.creeperhost.gui.element.ButtonCreeper;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostServerSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler {
    
    private static final int BUTTON_ID = 30051988;
    
    private static Field parentScreenField;
    private static GuiServerInfo guiServerInfo = new GuiServerInfo();


    private GuiMultiplayer lastInitialized = null;

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (Config.getInstance().isMainMenuEnabled() && gui instanceof GuiMainMenu) {
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = event.getButtonList();
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12));
            }
        } else if(Config.getInstance().isMpMenuEnabled() && CreeperHost.instance.getImplementation() != null && gui instanceof GuiMultiplayer && lastInitialized != gui) {
            GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
            try {
                if (serverListSelectorField == null) {
                    serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
                    serverListSelectorField.setAccessible(true);
                }
    
                if (serverListInternetField == null) {
                    serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "field_148198_l", "serverListInternet");
                    serverListInternetField.setAccessible(true);
                }
    
                ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
                CreeperHostServerSelectionList ourList = new CreeperHostServerSelectionList(mpGUI, Minecraft.getMinecraft(), mpGUI.width, mpGUI.height, 32, mpGUI.height - 64, 36);
                ourList.replaceList(serverListInternet);
                serverListInternetField.set(ourList, serverListInternet);
                serverListSelectorField.set(mpGUI, ourList);
                lastInitialized = mpGUI;
            } catch (Throwable e)
            {
                CreeperHost.logger.warn("Reflection to alter server list failed.", e);
            }
        }
    }

    @SubscribeEvent
    public void serverLoginEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        hasJoinedWorld = false;
    }

    private boolean hasJoinedWorld;

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (!Config.getInstance().isSivIntegration())
            return;
        if (event.getWorld().isRemote && !hasJoinedWorld && Minecraft.getMinecraft().player != null) {
            hasJoinedWorld = true;
            CreeperHost.instance.makeQueryGetter();
            if(CreeperHost.instance.getQueryGetter() != null) {
                CreeperHost.instance.getQueryGetter().run();
            }
        }
    }
    
    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event) {
        if (!Config.getInstance().isMainMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu) {
            GuiButton button = event.getButton();
            if (button != null && button.id == BUTTON_ID) {
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }
    }
    
    private static Field serverListSelectorField;
    private static Field serverListInternetField;
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (!Config.getInstance().isSivIntegration())
            return;
        if (event.getType() != RenderGameOverlayEvent.ElementType.PLAYER_LIST)
        {
            return;
        }
        if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();

        ScaledResolution resolution = new ScaledResolution(mc);
        guiServerInfo.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
        if (guiServerInfo.renderServerInfo()) {
            event.setCanceled(true);
        }
    }
    
    
    private static int ticks = 0;
    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event){
        if (!Config.getInstance().isSivIntegration())
            return;
        guiServerInfo.doTick();
        if (!((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown()) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
            return;
        }

        try
        {

            if (ticks == 0)
            {
                ticks = 40;
                //Update
                if (CreeperHost.instance.getQueryGetter() != null)
                {
                    CreeperHost.instance.getQueryGetter().run();
                }
            }
            ticks--;
        } catch (Throwable t) {
            // Catch _ALL_ errors. We should _NEVER_ crash.
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (!CreeperHost.MOD_ID.equals(eventArgs.getModID())) {
            return;
        }

        CreeperHost.instance.saveConfig();
    }
    
}
