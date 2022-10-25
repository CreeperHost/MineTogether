package net.creeperhost.minetogether.serverlist.gui;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.creeperhost.minetogether.serverlist.data.ListType;
import net.creeperhost.minetogether.serverlist.data.Server;
import net.creeperhost.minetogether.serverlist.data.SortType;
import net.creeperhost.polylib.client.screen.ButtonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 25/10/22.
 */
public class JoinMultiplayerScreenPublic extends JoinMultiplayerScreen {

    private final SortType sorting;
    private final Screen parent;
    private final ListType listType;

    private DropdownButton<SortType> sortDropdown;

    public JoinMultiplayerScreenPublic(Screen screen, ListType listType, SortType sorting) {
        super(screen);
        parent = screen;
        this.listType = listType;
        this.sorting = sorting;
    }

    @Override
    protected void init() {
        super.init();

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        serverSelectionList.children().clear();
        updateServerList();
        if (sorting == SortType.RANDOM) {
            Collections.shuffle(serverSelectionList.children());
        } else {
            serverSelectionList.children().sort(unsafeCast(sorting));
        }

        addRenderableWidget(new Button(width - 85, 5, 80, 20, listType.title, p -> {
            minecraft.setScreen(new ServerTypeScreen(this));
        }));

        sortDropdown = addRenderableWidget(new DropdownButton<>(width - 165, 5, 80, 20, true, true, e -> {
            minecraft.setScreen(new JoinMultiplayerScreenPublic(parent, listType, e));
        }));
        sortDropdown.setEntries(SortType.values());
        sortDropdown.setSelected(sorting);

        // No adding.
        ButtonHelper.removeButton("selectServer.add", this);

        addRenderableWidget(new Button(width / 2 + 80, height - 52, 75, 20, new TranslatableComponent("selectServer.refresh"), p -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, listType, sorting))));
    }

    private void updateServerList() {
        // TODO purge use of ServerList here? seems redundant? We are effectively using it as a List.
        ServerList serverList = new ServerList(Minecraft.getInstance());

        int num = serverList.size();
        for (int i = 0; i < num; i++) {
            serverList.remove(serverList.get(i));
        }

        List<Server> servers = MineTogetherServerList.updateServers(listType);
        for (Server server : servers) {
            serverList.add(new ServerDataPublic(server));
        }
        updateServers(serverList);
    }

    private void updateServers(ServerList serverList) {
        serverSelectionList.children().clear();
        for (int i = 0; i < serverList.size(); ++i) {
            this.serverSelectionList.children().add(new PublicServerEntry(this, serverSelectionList, serverList.get(i)));
        }
    }
}
