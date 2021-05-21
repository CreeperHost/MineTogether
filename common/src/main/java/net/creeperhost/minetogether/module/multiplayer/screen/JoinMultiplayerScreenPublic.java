package net.creeperhost.minetogether.module.multiplayer.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.multiplayer.data.ServerDataPublic;
import net.creeperhost.minetogether.module.multiplayer.data.ServerListType;
import net.creeperhost.minetogether.module.multiplayer.data.ServerSortOrder;
import net.creeperhost.minetogether.module.multiplayer.sort.ServerNameComparator;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogetherlib.serverlists.Server;
import net.creeperhost.minetogetherlib.serverlists.ServerListCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

public class JoinMultiplayerScreenPublic extends JoinMultiplayerScreen
{
    private final Screen parent;
    private ServerListType serverListType;
    public ServerSortOrder sortOrder = ServerSortOrder.RANDOM;
    private DropdownButton<ServerSortOrder> sortOrderButton;
    private ServerSortOrder serverSortOrder;
    private boolean loadingSevers = false;
    private int ticks;

    public JoinMultiplayerScreenPublic(Screen parent, ServerListType serverListType, ServerSortOrder serverSortOrder)
    {
        super(parent);
        this.parent = parent;
        this.serverListType = serverListType;
        this.serverSortOrder = serverSortOrder;
    }

    @Override
    public void init()
    {
        super.init();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        //Clear the list before we start adding our own entries
        serverSelectionList.children().clear();
        //Update the server list
        updateServerList();

        addButtons();
    }

    public void updateServerList()
    {
        loadingSevers = true;
        ServerList serverList = new ServerList(Minecraft.getInstance());
        List<Server> list = ServerListCallbacks.getServerList(serverListType);
        for(Server server : list)
        {
            serverList.add(new ServerDataPublic(server));
        }

        serverSelectionList.updateOnlineServers(serverList);
        loadingSevers = false;
    }

    public void addButtons()
    {
        String buttonName = "minetogether.multiplayer.title.prefix." + serverListType.name().toLowerCase();

        addButton(new Button(width - 85, 5, 80, 20, new TranslatableComponent(buttonName), p ->
        {
            minecraft.setScreen(new ServerTypeScreen(this));
        }));

        addButton(new DropdownButton<>(width - 165, 5, 80, 20, new TranslatableComponent("minetogether.multiplayer.sort"), sortOrder, false, p ->
        {
//            if (sortOrder != sortOrderButton.getSelected())
//            {
//                changeSort = true;
//                sortOrder = sortOrderButton.getSelected();
//                sort();
//                minecraft.setScreen(new JoinMultiplayerScreenPublic(parent, serverListType, sortOrder));
//            }
        }));
        //TODO replace the refresh button with to just refresh the server list
        //TODO replace the cancel button to return and not send to main menu
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(loadingSevers)
        {
            ScreenHelpers.loadingSpin(partialTicks, ticks, width / 2, height / 2, new ItemStack(Items.BEEF));
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        ticks++;
    }


    public void sort()
    {
        switch (this.sortOrder)
        {
            default:
//            case RANDOM:
//                Collections.shuffle(serverListSelector.serverListInternetOurs);
//                break;
//            case PLAYER:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.PlayerComparator.INSTANCE);
//                break;
//            case UPTIME:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.UptimeComparator.INSTANCE);
//                break;
//            case NAME:
//                Collections.sort(serverSelectionList.children(), ServerNameComparator.INSTANCE);
//                break;
//            case LOCATION:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.LocationComparator.INSTANCE);
//                break;
//            case PING:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.PingComparator.INSTANCE);
//                break;
        }
    }
}
