package net.creeperhost.minetogether.module.multiplayer.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TranslatableComponent;

public class JoinMultiplayerScreenPublic extends JoinMultiplayerScreen
{
    private final Screen parent;
    private ServerListType serverListType;
    public ServerSortOrder sortOrder = ServerSortOrder.RANDOM;
    private DropdownButton<ServerSortOrder> sortOrderButton;
    private ServerSortOrder serverSortOrder;
//    private ServerSelectionListPublic serverListSelector;

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
        String buttonName = "minetogether.multiplayer.title.prefix." + serverListType.name().toLowerCase();
        //Clear the list before we start adding our own entries
        serverSelectionList.children().clear();
        addButton(new Button(width - 85, 5, 80, 20, new TranslatableComponent(buttonName), p ->
        {
//            if (changeSort)
//            {
//                changeSort = false;
//            }
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
    }

//    public void sort()
//    {
//        switch (this.sortOrder)
//        {
//            default:
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
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.NameComparator.INSTANCE);
//                break;
//            case LOCATION:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.LocationComparator.INSTANCE);
//                break;
//            case PING:
//                Collections.sort(serverListSelector.serverListInternetOurs, Server.PingComparator.INSTANCE);
//                break;
//        }
//    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);


        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
