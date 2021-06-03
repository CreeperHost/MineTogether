package net.creeperhost.minetogether.module.multiplayer.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.mixin.MixinJoinMultiplayerScreen;
import net.creeperhost.minetogether.module.multiplayer.MultiPlayerModule;
import net.creeperhost.minetogether.module.multiplayer.data.ServerDataPublic;
import net.creeperhost.minetogether.module.multiplayer.data.ServerListType;
import net.creeperhost.minetogether.module.multiplayer.data.ServerSortOrder;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogetherlib.serverlists.Server;
import net.creeperhost.minetogetherlib.serverlists.ServerListCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private DropdownButton dropdownButton;
    private AbstractWidget editButton;
    private AbstractWidget deleteButton;


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
        List<Server> list = ServerListCallbacks.getServerList(serverListType, MineTogetherClient.getUUID(), MineTogetherClient.base64, Config.getInstance().getCurseProjectID());
        for(Server server : list)
        {
            serverList.add(new ServerDataPublic(server));
        }

        serverSelectionList.updateOnlineServers(serverList);
        loadingSevers = false;
    }

    @SuppressWarnings("unchecked")
    public void addButtons()
    {
        String buttonName = "minetogether.multiplayer.title.prefix." + serverListType.name().toLowerCase();

        addButton(new Button(width - 85, 5, 80, 20, new TranslatableComponent(buttonName), p ->
        {
            minecraft.setScreen(new ServerTypeScreen(this));
        }));

        addButton(dropdownButton = new DropdownButton<>(width - 165, 5, 80, 20, new TranslatableComponent("minetogether.multiplayer.sort"), sortOrder, false, p ->
        {
//            if (sortOrder != sortOrderButton.getSelected())
//            {
////                changeSort = true;
//                sortOrder = sortOrderButton.getSelected();
//                sort();
//                minecraft.setScreen(new JoinMultiplayerScreenPublic(parent, serverListType, sortOrder));
//            }
        }));
        //Set the button to the correct state
        try {
            dropdownButton.setSelected(sortOrder);
        } catch (Exception ignored) {}

        editButton = ScreenHelpers.findButton("selectServer.edit", buttons);
        deleteButton = ScreenHelpers.findButton("selectServer.delete", buttons);
        addButton(new ButtonMultiple(width / 2 + 134, height - 52, 2,
                p -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(new TitleScreen(), serverListType, serverSortOrder))));
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
    public void onSelectedChange()
    {
        super.onSelectedChange();
        if(editButton != null) editButton.active = false;
        if(deleteButton != null) deleteButton.active = false;
    }

    @Override
    public void tick()
    {
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
