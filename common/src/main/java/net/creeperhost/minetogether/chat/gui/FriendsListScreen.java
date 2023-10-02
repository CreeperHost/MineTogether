package net.creeperhost.minetogether.chat.gui;

import net.covers1624.quack.collection.FastStream;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.chat.ChatConstants;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.polylib.gui.IconButton;
import net.creeperhost.minetogether.polylib.gui.SimpleSelectionList;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

/**
 * Created by covers1624 on 1/9/22.
 */
// TODO Display smol spinner somewhere when friend update is running: ProfileManager.isFriendUpdateRunning
public class FriendsListScreen extends Screen {

    private final Screen parent;

    private SimpleSelectionList<FriendEntry> friendList;
    private EditBox searchBox;

    private ChatScrollList chatList;
    private EditBox chatBox;

    @Nullable
    private Profile targetProfile;

    private Button removeFriend;
    private Button blockButton;
    private Button partyButton;
    private Button editButton;

    private Button acceptRequest;
    private Button denyRequest;

    private int ticks;
    private int lastFriendUpdateCookie = -1;

    public FriendsListScreen(Screen parent) {
        super(Component.translatable("minetogether:screen.friends.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        lastFriendUpdateCookie = -1;
        friendList = new SimpleSelectionList<>(minecraft, 100, height - 90, 32, height - 55, 28, 100);
        friendList.setLeftPos(18);
        addRenderableWidget(friendList);

        chatList = new ChatScrollList(Minecraft.getInstance(), width - friendList.getRowWidth() - 40, height - 90, 32, height - 55);
        chatList.setLeftPos(friendList.getRowRight());
        chatList.setScrollAmount(chatList.getMaxScroll());
        addRenderableWidget(chatList);

        chatBox = new EditBox(font, friendList.getRowRight() + 1, height - 50, chatList.width - 2, 20, Component.empty());
        chatBox.setMaxLength(256);
        addRenderableWidget(chatBox);

        searchBox = new EditBox(font, 19, height - 50, friendList.getWidth() - 2, 20, Component.empty());
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("minetogether:button.cancel"), e -> minecraft.setScreen(parent))
                .bounds(5, height - 26, 100, 20)
                .build()
        );

        acceptRequest = addRenderableWidget(Button.builder(Component.translatable("minetogether:screen.friends.button.accept"), e -> {
                            FriendEntry entry = friendList.getSelected();
                            assert entry != null;
                            assert entry.request != null;
                            Minecraft.getInstance().setScreen(new FriendRequestScreen(this, entry.request));
                        })
                        .bounds(width - 215, height - 26, 100, 20)
                        .build()
        );
        acceptRequest.visible = false;

        denyRequest = addRenderableWidget(Button.builder(Component.translatable("minetogether:screen.friends.button.deny"), e -> {
                            FriendEntry entry = friendList.getSelected();
                            assert entry != null;
                            assert entry.request != null;
                            MineTogetherChat.CHAT_STATE.profileManager.denyFriendRequest(entry.request);
                            updateList(true);
                        })
                        .bounds(width - 110, height - 26, 100, 20)
                        .build()
        );
        denyRequest.visible = false;

        removeFriend = addRenderableWidget(new IconButton(width - 20, 32, 5, Constants.WIDGETS_SHEET, e -> {
            assert targetProfile != null;
            chatList.attach(null);
            MineTogetherChat.CHAT_STATE.profileManager.removeFriend(targetProfile);
        }));
        blockButton = addRenderableWidget(new IconButton(width - 20, 52, 6, Constants.WIDGETS_SHEET, e -> {
            assert targetProfile != null;
            targetProfile.mute();
        }));
        partyButton = addRenderableWidget(new IconButton(width - 20, 72, 7, Constants.WIDGETS_SHEET, e -> {
        }));
        partyButton.active = false;
        editButton = addRenderableWidget(new IconButton(width - 20, 92, 8, Constants.WIDGETS_SHEET, e -> {
            assert targetProfile != null;
            Minecraft.getInstance().setScreen(new FriendRequestScreen(this, targetProfile, FriendRequestScreen.Type.UPDATE));
        }));

        if (!(parent instanceof MutedUsersScreen)) {
            addRenderableWidget(Button.builder(Component.translatable("minetogether:screen.friends.button.muted"), e -> minecraft.setScreen(new MutedUsersScreen(this)))
                    .bounds(5, 5, 100, 20)
                    .build()
            );
        }

        //Yay! No tooltip containers required!
        removeFriend.setTooltip(Tooltip.create(Component.translatable("minetogether:screen.friends.tooltip.remove")));
        blockButton.setTooltip(Tooltip.create(Component.translatable("minetogether:screen.friends.tooltip.block")));
        partyButton.setTooltip(Tooltip.create(Component.translatable("minetogether:screen.friends.tooltip.party")));
        editButton.setTooltip(Tooltip.create(Component.translatable("minetogether:screen.friends.tooltip.edit")));

        updateList(false);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        renderDirtBackground(graphics);
        super.render(graphics, i, j, f);
    }

    @Override
    public void tick() {
        ticks++;
        updateList(false);

        FriendEntry selected = friendList.getSelected();
        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (selected != null && state == IrcState.CONNECTED) {
            targetProfile = selected.profile;
            acceptRequest.visible = selected.request != null;
            denyRequest.visible = selected.request != null;
            IrcUser user = MineTogetherChat.CHAT_STATE.ircClient.getUser(selected.profile);
            if (user != null) {
                chatList.attach(user.getChannel());
                chatBox.setEditable(true);
                chatBox.setSuggestion("");
            } else {
                chatList.attach(null);
                chatBox.setEditable(false);
                chatBox.setSuggestion("User is offline."); // TODO locale.
            }
        } else {
            targetProfile = null;
            chatList.attach(null);
            chatBox.setEditable(false);
            if (state == IrcState.CONNECTED) {
                chatBox.setSuggestion("Select a friend."); // TODO, perhaps just remove the box?
            } else {
                chatBox.setSuggestion(ChatConstants.STATE_DESC_LOOKUP.get(state));
            }
        }

        removeFriend.active = targetProfile != null;
        blockButton.active = targetProfile != null;
        editButton.active = targetProfile != null;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (chatBox.isFocused()) {
            return chatBox.charTyped(c, i);
        }
        boolean ret = super.charTyped(c, i);
        if (searchBox.isFocused()) {
            updateList(true);
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (targetProfile != null && chatBox.isFocused()) {
            String str = chatBox.getValue().trim();
            if ((i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && !str.isEmpty()) {
                chatList.getChannel().sendMessage(str);
                chatBox.setValue("");
            }
        }
        boolean ret = super.keyPressed(i, j, k);
        if (searchBox.isFocused()) {
            updateList(true);
        }
        return ret;
    }

    private void updateList(boolean force) {
        ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
        int newCookie = profileManager.getFriendUpdateCookie();
        if (lastFriendUpdateCookie == newCookie && !force) return;
        lastFriendUpdateCookie = newCookie;

        List<Profile> knownUsers = profileManager.getKnownProfiles();
        List<Profile> friends = FastStream.of(knownUsers).filter(Profile::isFriend).toLinkedList();
        friends.sort(NameComparator.INSTANCE);
        friends.sort(Comparator.comparingInt(e -> e.isOnline() ? 1 : 0));

        friendList.clearEntries();

        String search = searchBox.getValue();
        for (Profile friend : friends) {
            if (!search.isEmpty() && !friend.getFriendName().toLowerCase().contains(search.toLowerCase())) {
                continue;
            }
            FriendEntry friendEntry = new FriendEntry(friendList, friend);
            friendList.addEntry(friendEntry);
        }

        for (ProfileManager.FriendRequest request : profileManager.getFriendRequests()) {
            if (!search.isEmpty() && !request.from.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
                continue;
            }
            FriendEntry friendEntry = new FriendEntry(friendList, request);
            friendList.addEntry(friendEntry);
        }

        for (FriendEntry entry : friendList.children()) {
            if (entry.profile == targetProfile) {
                friendList.setSelected(entry);
                break;
            }
        }
    }

    private static class FriendEntry extends SimpleSelectionList.SimpleEntry<FriendEntry> {

        @Nullable
        private final ProfileManager.FriendRequest request;
        private final Profile profile;

        public FriendEntry(SimpleSelectionList<FriendEntry> list, ProfileManager.FriendRequest request) {
            this(list, request, request.from);
        }

        public FriendEntry(SimpleSelectionList<FriendEntry> list, Profile profile) {
            this(list, null, profile);
        }

        public FriendEntry(SimpleSelectionList<FriendEntry> list, @Nullable ProfileManager.FriendRequest request, Profile profile) {
            super(list);
            this.request = request;
            this.profile = profile;
        }

        @Override
        public void render(GuiGraphics graphics, int idx, int top, int left, int width, int height, int mx, int my, boolean hovered, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;

            String name = profile.isFriend() ? profile.getFriendName() : profile.getDisplayName();
            boolean ellipsis = false;
            while (font.width(name) >= width - 15) {
                name = name.substring(0, name.length() - 1);
                ellipsis = true;
            }
            if (ellipsis) {
                name += "...";
            }
            graphics.drawString(font, name, left + 5, top + 4, 0xFFFFFF);

            Component component = Component.literal(profile.isFriend() ? profile.isOnline() ? ChatFormatting.DARK_GREEN + "Online" : "Offline" : "Pending");
            graphics.drawString(font, component, left + 5, top + 15, 0xFFFFFF);
        }
    }

    public static class NameComparator implements Comparator<Profile> {

        public static final NameComparator INSTANCE = new NameComparator();

        @Override
        public int compare(Profile profile1, Profile profile2) {
            String str1 = profile1.getFriendName();
            String str2 = profile2.getFriendName();

            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0) {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
}
