package net.creeperhost.minetogether.module.multiplayer.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.creeperhost.minetogether.module.multiplayer.data.ServerDataPublic;
import net.creeperhost.minetogether.module.multiplayer.data.ServerListType;
import net.creeperhost.minetogether.module.multiplayer.data.ServerSortOrder;
import net.creeperhost.minetogether.module.multiplayer.sort.*;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogetherlib.serverlists.Server;
import net.creeperhost.minetogetherlib.serverlists.ServerListCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
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
    private DropdownButton<ServerSortOrder> dropdownButton;
    private boolean loadingSevers = false;
    private int ticks;
    private AbstractWidget editButton;
    private AbstractWidget deleteButton;
    private AbstractWidget cancelButton;

    public JoinMultiplayerScreenPublic(Screen parent, ServerListType serverListType, ServerSortOrder serverSortOrder)
    {
        super(parent);
        this.parent = parent;
        this.serverListType = serverListType;
        this.sortOrder = serverSortOrder;
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
        sort();
        addButtons();
    }

    public void updateServerList()
    {
        loadingSevers = true;
        ServerList serverList = new ServerList(Minecraft.getInstance());
        List<Server> list = ServerListCallbacks.getServerList(serverListType, MineTogetherClient.getUUID(), MineTogether.base64, Config.getInstance().getCurseProjectID());
        for(Server server : list)
        {
            serverList.add(new ServerDataPublic(server));
        }

        updateServers(serverList);

//        serverSelectionList.updateOnlineServers(serverList);
        loadingSevers = false;
    }

    public void updateServers(ServerList serverList)
    {
        serverSelectionList.children().clear();
        for(int i = 0; i < serverList.size(); ++i)
        {
            this.serverSelectionList.children().add(new PublicServerEntry(this, serverSelectionList, serverList.get(i)));
        }
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
            if (sortOrder != dropdownButton.getSelected())
            {
//                changeSort = true;
                sortOrder = dropdownButton.getSelected();
                sort();
                minecraft.setScreen(new JoinMultiplayerScreenPublic(parent, serverListType, sortOrder));
            }
        }));
        //Set the button to the correct state
        try {
            dropdownButton.setSelected(sortOrder);
        } catch (Exception ignored) {}

        editButton = ScreenHelpers.findButton("selectServer.edit", buttons);
        deleteButton = ScreenHelpers.findButton("selectServer.delete", buttons);
        cancelButton = ScreenHelpers.removeButton("selectServer.cancel", buttons);
        if(cancelButton != null)
        {
            addButton(new Button(cancelButton.x, cancelButton.y, cancelButton.getWidth(), cancelButton.getHeight(), cancelButton.getMessage(),
                    button -> minecraft.setScreen(new TitleScreen())));
        }

        //TODO make cancelbutton return to main menu

        ScreenHelpers.findButton("selectServer.add", buttons).active = false;


        addButton(new ButtonMultiple(width / 2 + 134, height - 52, 2, Constants.WIDGETS_LOCATION,
                p -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(new TitleScreen(), serverListType, sortOrder))));
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

    //TODO, Maybe move this to a Mixin?
    @Override
    public void joinSelectedServer()
    {
        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        if(entry instanceof PublicServerEntry)
        {
            join(((PublicServerEntry) entry).getServerData());
            return;
        }
        super.joinSelectedServer();
    }

    public void join(ServerData serverData)
    {
        this.minecraft.setScreen(new ConnectScreen(this, this.minecraft, serverData));
    }

    public void sort()
    {
        switch (this.sortOrder)
        {
            default:
            case RANDOM:
                Collections.shuffle(serverSelectionList.children());
                break;
            case PLAYER:
                Collections.sort(serverSelectionList.children(), PlayerComparator.INSTANCE);
                break;
            case UPTIME:
                Collections.sort(serverSelectionList.children(), UptimeComparator.INSTANCE);
                break;
            case NAME:
                Collections.sort(serverSelectionList.children(), ServerNameComparator.INSTANCE);
                break;
            case LOCATION:
                Collections.sort(serverSelectionList.children(), LocationComparator.INSTANCE);
                break;
            case PING:
                Collections.sort(serverSelectionList.children(), PingComparator.INSTANCE);
                break;
        }
    }
}
