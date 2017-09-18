package net.creeperhost.creeperhost;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.GuiProgressDisconnected;
import net.creeperhost.creeperhost.gui.GuiServerInfo;
import net.creeperhost.creeperhost.gui.element.ButtonCreeper;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostEntry;
import net.creeperhost.creeperhost.api.Order;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostServerSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler{

    private static final int BUTTON_ID = 30051988;

    private static Field parentScreenField;
    private static GuiServerInfo guiServerInfo = new GuiServerInfo();

    private GuiMultiplayer lastInitialized = null;
    private static Field reasonField = null;
    private static Field messageField = null;
    private static Field parentField = null;
    private static Field networkManagerField = null;

    private static NetworkManager lastNetworkManager = null;

    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event)
    {
        GuiScreen gui = event.gui;
        GuiScreen curGui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiDisconnected && !(gui instanceof GuiProgressDisconnected))
        {
            GuiDisconnected dc = (GuiDisconnected) gui;
            if (reasonField == null)
            {
                reasonField = ReflectionHelper.findField(gui.getClass(), "field_146306_a", "reason");
                reasonField.setAccessible(true);
            }

            if (messageField == null)
            {
                messageField = ReflectionHelper.findField(gui.getClass(), "field_146304_f", "message");
                messageField.setAccessible(true);
            }

            if (parentField == null)
            {
                parentField = ReflectionHelper.findField(gui.getClass(), "field_146307_h", "parentScreen");
                parentField.setAccessible(true);
            }

            try
            {
                String reason = (String) reasonField.get(dc);
                IChatComponent message = (IChatComponent) messageField.get(dc);

                if(curGui instanceof GuiProgressDisconnected)
                {
                    if (message.getFormattedText().contains("Server is still pre-generating!"))
                    {
                        GuiProgressDisconnected curDiscon = (GuiProgressDisconnected) curGui;
                        curDiscon.update(reason, message);
                        event.setCanceled(true);
                    }
                } else if (message.getFormattedText().contains("Server is still pre-generating!"))
                {
                    /*if (lastNetworkManager == null)
                        return;*/
                    event.gui = new GuiProgressDisconnected((GuiScreen) parentField.get(dc), reason, message, null);
                    lastNetworkManager = null;
                }
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        } else if(gui instanceof GuiConnecting) {
            //lastNetworkManager = getNetworkManager((GuiConnecting) gui);
        }
    }

    public static NetworkManager getNetworkManager(GuiConnecting con)
    {
        long time = System.currentTimeMillis() + 3000;
        try
        {
            if (networkManagerField == null)
            {
                networkManagerField = ReflectionHelper.findField(GuiConnecting.class, "field_146371_g", "networkManager");
                networkManagerField.setAccessible(true);
            }

            NetworkManager manager = null;
            while (manager == null) // loop to wait until networkManager is set.
            {
                if (System.currentTimeMillis() > time)
                    break;
                manager = (NetworkManager) networkManagerField.get(con);
            }

            return manager;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event){
        GuiScreen gui = event.gui;
        if (Config.getInstance().isMainMenuEnabled() && gui instanceof GuiMainMenu) {
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = event.buttonList;
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12));
            }
        } else if(Config.getInstance().isMpMenuEnabled() && CreeperHost.instance.getImplementation() != null && gui instanceof GuiMultiplayer && lastInitialized != gui) {
            GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
            try
            {
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
        if (event.world.isRemote && !hasJoinedWorld && Minecraft.getMinecraft().thePlayer != null) {
            hasJoinedWorld = true;
            CreeperHost.instance.makeQueryGetter();
            if(CreeperHost.instance.getQueryGetter() != null) {
                CreeperHost.instance.getQueryGetter().run();
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event){
        if (!Config.getInstance().isMainMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            GuiButton button = Util.getButton(event);
            if(button != null && button.id == BUTTON_ID){
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
        if (event.type != RenderGameOverlayEvent.ElementType.PLAYER_LIST)
        {
            return;
        }
        if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
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
        if (!((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.getIsKeyPressed()) || Minecraft.getMinecraft().isIntegratedServerRunning() || !guiServerInfo.getIsPlayerOpped()) {
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
        if (!CreeperHost.MOD_ID.equals(eventArgs.modID)) {
            return;
        }

        CreeperHost.instance.saveConfig();
    }
}
