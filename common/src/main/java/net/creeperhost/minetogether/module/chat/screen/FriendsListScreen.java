package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.lib.chat.ChatCallbacks;
import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.ScrollingChat;
import net.creeperhost.minetogether.module.chat.screen.listentries.ListEntryFriend;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.creeperhost.minetogether.threads.FriendUpdateThread;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FriendsListScreen extends MineTogetherScreen
{
    private final Screen parent;
    private ScreenList<ListEntryFriend> list;
    private ScrollingChat chat;
    private EditBox chatBox;
    private EditBox searchEntry;
    private int ticks;
    private Profile targetProfile = null;
    private Button removeFriend;
    private Button blockButton;
    private Button partyButton;
    private Button friendCodeButton;
    private Button editButton;
    private Button mutedList;

    public FriendsListScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.friendscreen.title"));
        this.parent = parent;
    }

    @SuppressWarnings("unchecked, rawtypes")
    @Override
    public void init()
    {
        super.init();
        CompletableFuture.runAsync(FriendUpdateThread::updateFriendsList);

        list = new ScreenList(this, minecraft, 100, height - 90, 32, this.height - 55, 28, 100);
        list.setLeftPos(18);
//        list.setScrollBarPosition(12);

        chat = new ScrollingChat(this, width - list.getRowWidth() - 40, this.height - 90, 32, this.height - 55, 125, true);
        chat.setLeftPos(list.getRowRight());

        chatBox = new EditBox(this.font, list.getRowRight() + 1, this.height - 50, chat.getWidth() - 2, 20, new TranslatableComponent(""));
        chatBox.setMaxLength(256);

        searchEntry = new EditBox(this.font, 19, this.height - 50, list.width - 2, 20, new TranslatableComponent(""));
        searchEntry.setSuggestion(I18n.get("minetogether.search"));

        addWidget(list);
        addWidget(searchEntry);
        addWidget(chatBox);
        addWidget(chat);
        addButtons();
        refreshFriendsList();
    }

    public void addButtons()
    {
        addRenderableWidget(new Button(5, height - 26, 100, 20, new TranslatableComponent("Cancel"), p -> minecraft.setScreen(parent)));

        addRenderableWidget(friendCodeButton = new ButtonString(width - 105, 5, 120, 20, new TranslatableComponent(MineTogetherChat.profile.get().getFriendCode()), p ->
        {
            minecraft.keyboardHandler.setClipboard(MineTogetherChat.profile.get().getFriendCode());
            MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("Copied to clipboard."), width - 160, 0, 5000, ToastHandler.EnumToastType.DEFAULT, null);
        }));

        addRenderableWidget(removeFriend = new ButtonMultiple(width - 20, 32, 5, Constants.WIDGETS_LOCATION, new TranslatableComponent("minetogether.friendscreen.tooltip.removebutton"), (button) ->
        {
            removeFriend(targetProfile);
        }));

        addRenderableWidget(blockButton = new ButtonMultiple(width - 20, 52, 6, Constants.WIDGETS_LOCATION, new TranslatableComponent("minetogether.friendscreen.tooltip.block"), (button) ->
        {
            ChatModule.muteUser(targetProfile.getLongHash());
        }));

        addRenderableWidget(partyButton = new ButtonMultiple(width - 20, 72, 7, Constants.WIDGETS_LOCATION, new TranslatableComponent("minetogether.friendscreen.tooltip.partytime"), (button) ->
        {
            ChatHandler.sendPartyInvite(targetProfile.getMediumHash(), MineTogetherChat.profile.get().getMediumHash());
        }));

        addRenderableWidget(editButton = new ButtonMultiple(width - 20, 92, 8, Constants.WIDGETS_LOCATION, new TranslatableComponent("minetogether.friendscreen.tooltip.editbutton"), (button) ->
        {
            minecraft.setScreen(new FriendRequestScreen(this, minecraft.getUser().getName(), targetProfile, ChatCallbacks.getFriendCode(MineTogetherClient.getPlayerHash()), targetProfile.getFriendName(), false, true));
        }));

        addRenderableWidget(mutedList = new Button(5, 5, 100, 20, new TranslatableComponent("Muted List"), p ->
        {
            this.minecraft.setScreen(new MutedListScreen(this));
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        list.render(poseStack, i, j, f);
        searchEntry.render(poseStack, i, j, f);
        chatBox.render(poseStack, i, j, f);
        chat.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 12, 0xFFFFFF);
        if (list.children().isEmpty())
            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.friendslist.empty"), width / 2, (this.height / 2) - 20, -1);
        if (friendCodeButton != null && friendCodeButton.isHoveredOrFocused())
            renderTooltip(poseStack, new TranslatableComponent("minetogether.friendslist.copytoclipboard"), i, j);

        renderTooltips(poseStack, i, j, f);
    }

    @Override
    public void tick()
    {
        ticks++;
        if (ticks % 600 == 0)
        {
            refreshFriendsList();
        }
        if (targetProfile != null)
        {
            partyButton.active = targetProfile.isOnline();
        }
        if (list.getCurrSelected() != null && targetProfile != null && !targetProfile.equals(list.getCurrSelected().getProfile()))
        {
            targetProfile = list.getCurrSelected().getProfile();
            chat.updateLines(targetProfile.getMediumHash());
        }

        if (targetProfile != null)
        {
            chatBox.setSuggestion(targetProfile.isOnline() ? "" : "Friend is offline");
            chatBox.setEditable(targetProfile.isOnline());
            if (ChatHandler.hasNewMessages(targetProfile.getMediumHash()))
            {
                chat.updateLines(targetProfile.getMediumHash());
                ChatHandler.setMessagesRead(targetProfile.getMediumHash());
            }
        }
        toggleInteractionButtons(list.getCurrSelected() != null);
    }

    public void toggleInteractionButtons(boolean value)
    {
        removeFriend.active = value;
        blockButton.active = value;
        editButton.active = value;
        if (targetProfile == null) partyButton.active = value;
    }

    public static ArrayList<Profile> removedFriends = new ArrayList<>();

    protected boolean refreshFriendsList()
    {
        List<Profile> friendsRet = KnownUsers.getFriends();
        if (friendsRet == null) return false;
        List<Profile> friends = new ArrayList<Profile>();
        List<Profile> onlineFriends = friendsRet.stream().filter(Profile::isOnline).collect(Collectors.toList());
        onlineFriends.sort(NameComparator.INSTANCE);
        List<Profile> offlineFriends = friendsRet.stream().filter(profile -> !profile.isOnline()).collect(Collectors.toList());
        offlineFriends.sort(NameComparator.INSTANCE);

        friends.addAll(onlineFriends);
        friends.addAll(offlineFriends);

        list.clearList();
        if (friends != null)
        {
            for (Profile friendProfile : friends)
            {
                ListEntryFriend friendEntry = new ListEntryFriend(this, list, friendProfile);
                if (searchEntry != null && !searchEntry.getValue().isEmpty())
                {
                    String s = searchEntry.getValue();
                    if (friendProfile.friendName.toLowerCase().contains(s.toLowerCase()))
                    {
                        if (!removedFriends.contains(friendProfile)) list.add(friendEntry);
                    }
                }
                else
                {
                    if (!removedFriends.contains(friendProfile)) list.add(friendEntry);
                }
                if (targetProfile != null && friendProfile.getFriendName().equals(targetProfile.getFriendName()))
                    list.setSelected(friendEntry);
            }
            List<Profile> removedCopy = new ArrayList<Profile>(removedFriends);
            for (Profile removed : removedCopy)
            {
                boolean isInList = false;
                for (Profile friend : friends)
                {
                    if (friend.friendCode.equalsIgnoreCase(removed.friendCode))
                    {
                        isInList = true;
                        break;
                    }
                }
                if (!isInList)
                {
                    removedFriends.remove(removed);
                }
            }
        }
        return true;
    }

    @SuppressWarnings("all")
    public void removeFriend(Profile profile)
    {
        ConfirmScreen confirmScreen = new ConfirmScreen(t ->
        {
            if (t)
            {
                CompletableFuture.runAsync(() ->
                {
                    removedFriends.add(profile);
                    refreshFriendsList();
                    if (!ChatCallbacks.removeFriend(profile.getFriendCode(), MineTogetherClient.getPlayerHash()))
                    {
                        profile.setFriend(false);
                        refreshFriendsList();
                    }
                });
            }
            minecraft.setScreen(new FriendsListScreen(parent));
        }, new TranslatableComponent("minetogether.removefriend.sure1"), new TranslatableComponent("minetogether.removefriend.sure2"));
        minecraft.setScreen(confirmScreen);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if (searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(c, i);
            refreshFriendsList();
            return flag;
        }
        if (chatBox.isFocused())
        {
            return chatBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if (searchEntry.isFocused())
        {
            searchEntry.setSuggestion("");
            boolean flag = searchEntry.keyPressed(i, j, k);
            refreshFriendsList();
            return flag;
        }
        if (targetProfile != null && chatBox.isFocused())
        {
            if ((i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && !chatBox.getValue().trim().isEmpty())
            {
                ChatHandler.sendMessage(targetProfile.getMediumHash(), ChatFormatter.getStringForSending(chatBox.getValue()));
                chatBox.setValue("");
            }
            return chatBox.keyPressed(i, j, k);
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i)
    {
        if (list.isMouseOver(d, e) && list.getCurrSelected() != null)
        {
            if(list.getCurrSelected() == null) return super.mouseReleased(d, e, i);

            boolean flag = targetProfile == null || !targetProfile.equals(list.getCurrSelected().getProfile());
            if (flag)
            {
                Profile profile = list.getCurrSelected().getProfile();
                if (profile != null && profile.isFriend())
                {
                    targetProfile = profile;
                    chat.updateLines(profile.getMediumHash());
                }
                return flag;
            }
        }
        return super.mouseReleased(d, e, i);
    }

    public static class NameComparator implements Comparator<Profile>
    {
        public static final NameComparator INSTANCE = new NameComparator();

        @Override
        public int compare(Profile profile1, Profile profile2)
        {
            String str1 = profile1.friendName;
            String str2 = profile2.friendName;

            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
}
