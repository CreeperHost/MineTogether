package net.creeperhost.minetogether.serverlist.gui;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.serverlist.MineTogetherServerList;
import net.creeperhost.minetogether.serverlist.data.ListType;
import net.creeperhost.minetogether.serverlist.data.SortType;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.TextState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.creeperhost.minetogether.serverlist.gui.ServerListGui.ServerType.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 20/10/2023
 */
public class ServerListGui implements GuiProvider {
    private final ServerStatusPinger pinger = new ServerStatusPinger();

    private SortType sorting = SortType.RANDOM;
    private ServerType serverType = NONE;
    private List<ServerDataPublic> servers = new ArrayList<>();
    private GuiList<ServerDataPublic> serverList;
    private GuiTextField searchField;
    private ModularGui gui;
    private int tick = 0;
    public ServerDataPublic selected;
    public volatile boolean sortDirty = false;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    //TODO re-implement most of the logic from PublicServerEntry inside a server element

    @Override
    public void buildGui(ModularGui gui) {
        this.gui = gui;
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.translatable("minetogether:gui.server_list.title"));

        GuiElement<?> root = gui.getRoot();

        GuiText title = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 5))
                .constrain(HEIGHT, Constraint.literal(8))
                .constrain(LEFT, match(root.get(LEFT)))
                .constrain(RIGHT, match(root.get(RIGHT)));

//        GuiButton oldGui = MTStyle.Flat.button(root, Component.literal("Old Gui"))
//                .onPress(() -> gui.mc().setScreen(new ServerTypeScreen(gui.getScreen())))
//                .constrain(TOP, match(root.get(TOP)))
//                .constrain(RIGHT, match(root.get(RIGHT)))
//                .constrain(WIDTH, literal(40))
//                .constrain(HEIGHT, literal(10));

        GuiButton communityBtn = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.server_list.type.community"))
                .setTooltip(Component.translatable("minetogether:gui.server_list.type.community.info"))
                .setToggleMode(() -> serverType == COMMUNITY)
                .onPress(() -> setServerType(serverType == COMMUNITY ? NONE : COMMUNITY))
                .constrain(WIDTH, dynamic(() -> ((root.getValue(WIDTH) - 20) / 3) - 8))
                .constrain(TOP, relative(title.get(BOTTOM), 6))
                .constrain(HEIGHT, literal(14));
        communityBtn.constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), () -> communityBtn.xSize() / -2));

        GuiButton publicBtn = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.server_list.type.public"))
                .setTooltip(Component.translatable("minetogether:gui.server_list.type.public.info"))
                .setToggleMode(() -> serverType == PUBLIC)
                .onPress(() -> setServerType(serverType == PUBLIC ? NONE : PUBLIC))
                .constrain(TOP, match(communityBtn.get(TOP)))
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(communityBtn.get(LEFT), -4))
                .constrain(HEIGHT, literal(14));

        GuiButton closedBtn = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.server_list.type.closed"))
                .setTooltip(Component.translatable("minetogether:gui.server_list.type.closed.info"))
                .setToggleMode(() -> serverType == CLOSED)
                .onPress(() -> setServerType(serverType == CLOSED ? NONE : CLOSED))
                .constrain(TOP, match(communityBtn.get(TOP)))
                .constrain(LEFT, relative(communityBtn.get(RIGHT), 4))
                .constrain(RIGHT, relative(root.get(RIGHT), -10))
                .constrain(HEIGHT, literal(14));

        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back_arrow"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(BOTTOM, relative(publicBtn.get(TOP), -4))
                .constrain(LEFT, match(publicBtn.get(LEFT)))
                .constrain(WIDTH, literal(50))
                .constrain(HEIGHT, literal(12));

        GuiElement<?> listBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(root.get(RIGHT), -10))
                .constrain(TOP, relative(publicBtn.get(BOTTOM), 3))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -18));

//        Think i will just mimic the vanilla server list, but in the MT style. Keep it familiar to the user.

        GuiElement<?> searchBg = MTStyle.Flat.contentArea(root)
                .constrain(TOP, relative(listBg.get(BOTTOM), 2))
                .constrain(LEFT, match(listBg.get(LEFT)))
                .constrain(WIDTH, literal(100))
                .constrain(HEIGHT, literal(14));

        searchField = new GuiTextField(searchBg)
                .setTextState(TextState.simpleState("", s -> updateServerList()))
                .setSuggestion(Component.translatable("minetogether:gui.server_list.search"));
        Constraints.bind(searchField, searchBg, 0, 3, 0, 3);

        GuiButton refreash = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.refresh"))
                .onPress(() -> setServerType(serverType))
                .constrain(TOP, relative(listBg.get(BOTTOM), 2))
                .constrain(LEFT, midPoint(listBg.get(LEFT), listBg.get(RIGHT), -50))
                .constrain(WIDTH, literal(100))
                .constrain(HEIGHT, literal(14));

        GuiButton join = MTStyle.Flat.buttonPrimary(root, Component.translatable("minetogether:gui.server_list.join"))
                .setEnabled(() -> selected != null)
                .onPress(() -> join(selected))
                .constrain(TOP, relative(listBg.get(BOTTOM), 2))
                .constrain(RIGHT, match(listBg.get(RIGHT)))
                .constrain(WIDTH, literal(100))
                .constrain(HEIGHT, literal(14));

        GuiButton sort = MTStyle.Flat.button(root, () -> Component.translatable("minetogether:gui.server_list.sort_by").append(" ").append(sorting.translate()))
                .onPress(() -> cycleSorting(1), GuiButton.LEFT_CLICK)
                .onPress(() -> cycleSorting(-1), GuiButton.RIGHT_CLICK)
                .constrain(BOTTOM, relative(publicBtn.get(TOP), -4))
                .constrain(RIGHT, match(closedBtn.get(RIGHT)))
                .constrain(WIDTH, literal(100))
                .constrain(HEIGHT, literal(12));

        addTypeInfo(listBg);
        setupServerList(listBg);


        gui.onTick(this::tick);
        gui.onClose(pinger::removeAll);
    }

    private void tick() {
        pinger.tick();

        for (GuiElement<?> value : serverList.getElementMap().values()) {
            if (value instanceof ServerEntry entry) {
                entry.update();
            }
        }

        if (sortDirty && tick++ % 20 == 0) {
            sortDirty = false;
            cycleSorting(0);
        }
    }

    private void addTypeInfo(GuiElement<?> background) {
        GuiText last = new GuiText(background, Component.translatable("minetogether:gui.server_list.type.select").withStyle(ChatFormatting.UNDERLINE))
                .setEnabled(() -> serverType == NONE)
                .setWrap(true)
                .constrain(LEFT, relative(background.get(LEFT), 5))
                .constrain(RIGHT, relative(background.get(RIGHT), -5))
                .constrain(TOP, relative(background.get(TOP), 5))
                .autoHeight();

        last = new GuiText(background, Component.translatable("minetogether:gui.server_list.type.public.info"))
                .setEnabled(() -> serverType == NONE)
                .setWrap(true)
                .constrain(LEFT, relative(background.get(LEFT), 5))
                .constrain(RIGHT, relative(background.get(RIGHT), -5))
                .constrain(TOP, relative(last.get(BOTTOM), 10))
                .autoHeight();

        last = new GuiText(background, Component.translatable("minetogether:gui.server_list.type.community.info"))
                .setEnabled(() -> serverType == NONE)
                .setWrap(true)
                .constrain(LEFT, relative(background.get(LEFT), 5))
                .constrain(RIGHT, relative(background.get(RIGHT), -5))
                .constrain(TOP, relative(last.get(BOTTOM), 10))
                .autoHeight();

        last = new GuiText(background, Component.translatable("minetogether:gui.server_list.type.closed.info"))
                .setEnabled(() -> serverType == NONE)
                .setWrap(true)
                .constrain(LEFT, relative(background.get(LEFT), 5))
                .constrain(RIGHT, relative(background.get(RIGHT), -5))
                .constrain(TOP, relative(last.get(BOTTOM), 10))
                .autoHeight();
    }

    private void setupServerList(GuiElement<?> background) {
        serverList = new GuiList<ServerDataPublic>(background)
                .setDisplayBuilder((parent, data) -> new ServerEntry(parent, data, this, servers.indexOf(data)))
                .setItemSpacing(1)
                .setEnabled(() -> serverType != NONE);
        Constraints.bind(serverList, background, 1, 1, 1, 1);

        var scrollBar = MTStyle.Flat.scrollBar(background, Axis.Y);
        scrollBar.container
                .setEnabled(() -> serverList.hiddenSize() > 0)
                .constrain(TOP, match(background.get(TOP)))
                .constrain(BOTTOM, match(background.get(BOTTOM)))
                .constrain(LEFT, relative(background.get(RIGHT), 2))
                .constrain(WIDTH, literal(6));
        scrollBar.primary
                .setScrollableElement(serverList)
                .setSliderState(serverList.scrollState());
    }

    private void setServerType(ServerType serverType) {
        this.serverType = serverType;
        this.selected = null;
        if (serverType != NONE) {
            //TODO, Move this off thread
            servers = MineTogetherServerList.updateServers(serverType.listType).stream().map(ServerDataPublic::new).collect(Collectors.toList());
            updateServerList();
        }
    }

    public void cycleSorting(int dir) {
        this.sorting = SortType.values()[Math.floorMod(sorting.ordinal() + dir, SortType.values().length)];
        if (sorting == SortType.RANDOM) {
            Collections.shuffle(servers);
        } else {
            try {
                servers.sort(sorting);
            } catch (Throwable ignored) {
                //Pretty sure there is an edge case crash when server ping gets updated in the middle of a sort. This fixes that.
                sortDirty = true;
            }
        }
        updateServerList();
    }

    private void updateServerList() {
        serverList.getList().clear();
        this.selected = null;

        String search = searchField.getValue().toLowerCase(Locale.ROOT);
        if (search.isEmpty()) {
            serverList.getList().addAll(servers);
        } else {
            for (ServerDataPublic server : servers) {
                String motd = server.motd == null ? "" : server.motd.getString().toLowerCase(Locale.ROOT);
                if (server.name.toLowerCase(Locale.ROOT).contains(search) || motd.contains(search)) {
                    serverList.add(server);
                }
            }
        }
        serverList.markDirty();
    }

    public void join(ServerData serverData) {
        ConnectScreen.startConnecting(gui.getScreen(), Minecraft.getInstance(), ServerAddress.parseString(serverData.ip), serverData);
    }

    public ServerStatusPinger getPinger() {
        return pinger;
    }

    enum ServerType {
        NONE(null),
        PUBLIC(ListType.PUBLIC),
        COMMUNITY(ListType.INVITE),
        CLOSED(ListType.APPLICATION);

        private final ListType listType;

        ServerType(ListType listType) {
            this.listType = listType;
        }
    }
}
