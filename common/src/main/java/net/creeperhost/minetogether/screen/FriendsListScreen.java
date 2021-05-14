package net.creeperhost.minetogether.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.helpers.ScreenHelpers;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogether.screen.listentries.ListEntryFriend;
import net.creeperhost.minetogether.screen.prefab.ScreenList;
import net.creeperhost.minetogether.screen.widgets.ButtonString;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class FriendsListScreen extends Screen
{
    private Screen parent;
    private ScreenList<ListEntryFriend> list;

    private String friendCode = "";
    private String hoveringText = null;
    private EditBox searchEntry;
    private int ticks;

    private boolean first = true;

    public FriendsListScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.friendscreen.title"));
        this.parent = parent;
        this.friendCode = ChatCallbacks.getFriendCode(MineTogetherClient.getUUID());
    }

    @Override
    public void init()
    {
        super.init();
        if (list == null)
        {
            list = new ScreenList(this, minecraft, width, height, 32, this.height - 64, 36);
        } else
        {
            list.updateSize(width, height, 32, this.height - 64);
        }

        addButtons();
        searchEntry = new EditBox(this.font, this.width / 2 - 80, this.height -32, 160, 20, new TranslatableComponent(""));
        children.add(list);
        children.add(searchEntry);
        refreshFriendsList(true);
    }

    public void addButtons()
    {
        addButton(new Button(5, height - 60, 100, 20, new TranslatableComponent("Cancel"), p ->
        {
//            if (!addFriend)
                this.minecraft.setScreen(parent);
//            else
//            {
//                addFriend = false;
//                buttonInvite.visible = true;
//                codeEntry.setText("");
//            }
        }));

        addButton(new ButtonString( 5, this.height - 26, 60, 20, new TranslatableComponent(MineTogetherChat.profile.get().getFriendCode()), p ->
        {
            this.minecraft.keyboardHandler.setClipboard(MineTogetherChat.profile.get().getFriendCode());
//            showAlert(new StringTextComponent("Copied to clipboard."), 0x00FF00, 5000);
        }));

        addButton(new Button(this.width - 105, this.height - 26, 100, 20,
                 new TranslatableComponent("minetogether.button.refresh"), p -> refreshFriendsList(false)));

        Button inviteButton;
        addButton(inviteButton = new Button(this.width - 105, this.height - 60, 100, 20, new TranslatableComponent("minetogether.button.invite"), p ->
        {
            //TODO
        }));
        inviteButton.active = false;

        addButton(new Button(this.width / 2 - 50, this.height - 60, 100, 20, new TranslatableComponent("multiplayer.button.addfriend"), p ->
        {
            //TODO
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        list.render(poseStack, i, j, f);
        searchEntry.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 5, 0xFFFFFF);
        drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.multiplayer.friendcode"), 40, this.height - 35, -1);

        if(!ChatCallbacks.friendFuture.isDone() && list.children().isEmpty()) ScreenHelpers.loadingSpin(f, ticks, width / 2, height / 2, new ItemStack(Items.BEEF));
        if(ChatCallbacks.friendFuture.isDone() && list.children().isEmpty()) drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.friendslist.empty"), width / 2, (this.height / 2) - 20, -1);
    }

    @Override
    public void tick()
    {
        ticks++;
        if(ChatCallbacks.friendFuture != null && ChatCallbacks.friendFuture.isDone())
        {
            if(first)
            {
                first = false;
                refreshFriendsList(false);
            }
        }
    }

    public static ArrayList<String> removedFriends = new ArrayList<>();
    protected boolean refreshFriendsList(boolean force)
    {
        ArrayList<Friend> friends = ChatCallbacks.getFriendsList(force, MineTogetherClient.getUUID());
        list.clearList();
        if (friends != null)
        {
            for (Friend friend : friends)
            {
                ListEntryFriend friendEntry = new ListEntryFriend(this, list, friend);
                if(searchEntry != null && !searchEntry.getValue().isEmpty())
                {
                    String s = searchEntry.getValue();
                    if(friend.getName().toLowerCase().contains(s.toLowerCase()))
                    {
                        if(!removedFriends.contains(friend.getCode())) list.add(friendEntry);
                    }
                }
                else
                {
                    if(!removedFriends.contains(friend.getCode())) list.add(friendEntry);
                }
            }
            ArrayList<String> removedCopy = new ArrayList<String>(removedFriends);
            for(String removed : removedCopy)
            {
                boolean isInList = false;
                for(Friend friend : friends)
                {
                    if(friend.getCode().equalsIgnoreCase(removed))
                    {
                        isInList=true;
                        break;
                    }
                }
                if(!isInList)
                {
                    removedFriends.remove(removed);
                }
            }
        }
        return true;
    }

    public void setHoveringText(String hoveringText)
    {
        this.hoveringText = hoveringText;
    }

    private Friend removeFriend;
    @SuppressWarnings("all")
    public void removeFriend(Friend friend)
    {
        removeFriend = friend;
        ConfirmScreen confirmScreen = new ConfirmScreen(t ->
        {
            if(t)
            {
                CompletableFuture.runAsync(() ->
                {
                   removedFriends.add(friend.getCode());
                   refreshFriendsList(true);
                   if(!ChatCallbacks.removeFriend(removeFriend.getCode(), MineTogetherClient.getUUID()))
                   {
                       removedFriends.remove(friend.getCode());
                       refreshFriendsList(true);
                   }
                });
            }
            minecraft.setScreen(new FriendsListScreen(parent));
        }, new TranslatableComponent("minetogether.removefriend.sure1"), new TranslatableComponent("minetogether.removefriend.sure2"));
        minecraft.setScreen(confirmScreen);
    }

    //TODO
    public void inviteGroupChat(Friend friend) { }

    @Override
    public boolean charTyped(char c, int i)
    {
        if(searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(c, i);
            refreshFriendsList(false);
            return flag;
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if(searchEntry.isFocused())
        {
            boolean flag = searchEntry.keyPressed(i, j, k);
            refreshFriendsList(false);
            return flag;
        }
        return super.keyPressed(i, j, k);
    }
}
