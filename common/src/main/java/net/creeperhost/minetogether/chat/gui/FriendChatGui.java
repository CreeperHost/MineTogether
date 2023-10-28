package net.creeperhost.minetogether.chat.gui;

import net.covers1624.quack.collection.FastStream;
import net.creeperhost.minetogether.chat.ChatConstants;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.SettingGui;
import net.creeperhost.minetogether.gui.dialogs.TextInputDialog;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.irc.IrcUser;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.*;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.sprite.PolyTextures;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 24/09/2023
 */
public class FriendChatGui implements GuiProvider {

    public final ChatMonitor chatMonitor = new ChatMonitor();
    private GuiTextField textField;
    private GuiTextField friendSearch;
    private GuiList<FriendElement> friendList;

    private GuiElement<?> friendListBg;
    private GuiElement<?> textBoxBg;
    private GuiElement<?> chatBg;
    private GuiElement<?> codeBoxBg;

    private GuiButton publicChat;

    private int friendCookie = -1;

    @Nullable //Made profile static so we remember the selected profile.
    protected static Profile selected;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.translatable("minetogether:gui.friends.title"));

        GuiElement<?> root = gui.getRoot();

        friendListBg = MTStyle.Flat.contentArea(root)
                .constrain(TOP, relative(root.get(TOP), 22))
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(WIDTH, literal(150))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -30));

        textBoxBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(friendListBg.get(RIGHT), 4))
                .constrain(RIGHT, relative(root.get(RIGHT), -10))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -10))
                .constrain(HEIGHT, literal(16));

        chatBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, match(textBoxBg.get(LEFT)))
                .constrain(RIGHT, match(textBoxBg.get(RIGHT)))
                .constrain(TOP, match(friendListBg.get(TOP)))
                .constrain(BOTTOM, relative(textBoxBg.get(TOP), -4));

        codeBoxBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(friendListBg.get(LEFT), 34))
                .constrain(RIGHT, relative(friendListBg.get(RIGHT), -30))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -10))
                .constrain(HEIGHT, literal(16));

        setupGuiHeader(gui, root);
        setupFriendList(root, friendListBg);
        setupTextBox(textBoxBg);
        setupChatWindow(chatBg);

        GuiText title = new GuiText(root, () -> selected == null ? gui.getGuiTitle() : gui.getGuiTitle().copy().append(" - ").append(displayName(selected)))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(friendSearch.get(TOP), 3))
                .constrain(LEFT, match(chatBg.get(LEFT)))
                .constrain(HEIGHT, Constraint.literal(8))
                .constrain(RIGHT, relative(publicChat.get(LEFT), -2));

        gui.onTick(this::tickFriendList);
        gui.onTick(this::updateSelected);
        gui.onTick(chatMonitor::tick);

        if (selected != null) {
            ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
            List<Profile> knownUsers = profileManager.getKnownProfiles();
            List<Profile> friends = FastStream.of(knownUsers).filter(Profile::isFriend).toLinkedList();
            if (!friends.contains(selected)) selected = null;
        }
    }

    private void setupGuiHeader(ModularGui gui, GuiElement<?> root) {
        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back_arrow"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(BOTTOM, relative(chatBg.get(TOP), -4))
                .constrain(LEFT, match(friendListBg.get(LEFT)))
                .constrain(WIDTH, literal(50))
                .constrain(HEIGHT, literal(14));

        GuiButton settings = MTStyle.Flat.button(root, (Supplier<Component>) null)
                .setTooltip(Component.translatable("minetogether:gui.button.settings.info"))
                .setTooltipDelay(0)
                .onPress(() -> gui.mc().setScreen(new ModularGuiScreen(new SettingGui(), gui.getScreen())))
                .constrain(BOTTOM, match(back.get(BOTTOM)))
                .constrain(RIGHT, match(chatBg.get(RIGHT)))
                .constrain(WIDTH, literal(14))
                .constrain(HEIGHT, literal(14));

        GuiTexture gear = new GuiTexture(settings, PolyTextures.get("widgets/gear_light"));
        Constraints.bind(gear, settings);

        publicChat = MTStyle.Flat.button(root, (Supplier<Component>) null)
                //Keep the same parent screen, So the back button always takes us to the 'first' screen (Main Menu / Pause Menu)
                .setTooltip(Component.translatable("minetogether:gui.button.global_chat.info"))
                .setTooltipDelay(0)
                .onPress(() -> gui.mc().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), gui.getParentScreen())))
                .constrain(BOTTOM, match(back.get(BOTTOM)))
                .constrain(RIGHT, relative(settings.get(LEFT), -2))
                .constrain(WIDTH, literal(16))
                .constrain(HEIGHT, literal(14));

        GuiTexture publicIcon = new GuiTexture(publicChat, MTTextures.get("buttons/public_chat_light"));
        Constraints.bind(publicIcon, publicChat, 0, 1, 0, 1);
    }

    private void setupFriendList(GuiElement<?> root, GuiElement<?> background) {
        friendList = new GuiList<FriendElement>(background)
                .setDisplayBuilder((list, e) -> e);
        Constraints.bind(friendList, background, 5);

        GuiElement<?> searchBg = MTStyle.Flat.contentArea(root)
                .constrain(BOTTOM, relative(background.get(TOP), -4))
                .constrain(RIGHT, match(background.get(RIGHT)))
                .constrain(WIDTH, literal(90))
                .constrain(HEIGHT, literal(14));

        friendSearch = new GuiTextField(searchBg)
                .setTextState(TextState.simpleState("", s -> scheduleFriendUpdate()))
                .setSuggestion(Component.translatable("minetogether:gui.friends.search_suggestion"));
        Constraints.bind(friendSearch, searchBg, 0, 3, 0, 3);

        var scrollBar = MTStyle.Flat.scrollBar(background, Axis.Y);
        scrollBar.container
                .setEnabled(() -> friendList.hiddenSize() > 0)
                .constrain(TOP, match(background.get(TOP)))
                .constrain(BOTTOM, match(background.get(BOTTOM)))
                .constrain(RIGHT, match(background.get(RIGHT)))
                .constrain(WIDTH, literal(4));
        scrollBar.primary
                .setScrollableElement(friendList)
                .setSliderState(friendList.scrollState());

        GuiButton copyCode = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.friends.copy_code"))
                .onPress(() -> root.mc().keyboardHandler.setClipboard(MineTogetherChat.getOurProfile().getFriendCode()))
                .setTooltip(Component.translatable("minetogether:gui.friends.copy_code.info"))
                .constrain(TOP, match(codeBoxBg.get(TOP)))
                .constrain(BOTTOM, match(codeBoxBg.get(BOTTOM)))
                .constrain(LEFT, match(friendListBg.get(LEFT)))
                .constrain(RIGHT, relative(codeBoxBg.get(LEFT), -2));

        GuiTextField friendCode = new GuiTextField(codeBoxBg)
                .setSuggestion(Component.translatable("minetogether:gui.friends.enter_code"));
        Constraints.bind(friendCode, codeBoxBg, 0, 3, 0, 3);

        //TODO Needs to support friend codes

        GuiButton addFriendViaCode = MTStyle.Flat.buttonPrimary(root, Component.translatable("minetogether:gui.friends.enter_code.add"))
                .setTooltip(Component.translatable("minetogether:gui.friends.enter_code.info"))
                .onPress(() -> {
                    ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;

                    new TextInputDialog(root, Component.translatable("minetogether:screen.friendreq.desc.request"), "")
                            .setResultCallback(friendName -> {
                                if (friendCode.getValue().equals(profileManager.getOwnProfile().getFriendCode())) {
                                    return;
                                }
                                profileManager.sendFriendRequest(friendCode.getValue(), friendName.trim());
                                friendCode.setValue("");
                            });
                })
                .setDisabled(() -> friendCode.getValue().isEmpty())
                .constrain(TOP, match(textBoxBg.get(TOP)))
                .constrain(BOTTOM, match(codeBoxBg.get(BOTTOM)))
                .constrain(LEFT, relative(codeBoxBg.get(RIGHT), 2))
                .constrain(RIGHT, match(friendListBg.get(RIGHT)));
    }

    private void setupTextBox(GuiElement<?> background) {
        textField = new GuiTextField(background)
                .setMaxLength(256)
                .setOnEditComplete(() -> {
                    String message = textField.getValue().trim();
                    if (!message.isEmpty()) {
                        textField.setValue("");
                        if (chatMonitor.getChannel() != null) {
                            chatMonitor.getChannel().sendMessage(message);
                        }
                    }
                });
        Constraints.bind(textField, background, 0, 3, 0, 3);
    }

    private void setupChatWindow(GuiElement<?> background) {
        GuiList<Message> chatList = new GuiList<>(background);
        chatList.setDisplayBuilder((parent, message) -> new MessageElement(parent, message, textField, true));
        Constraints.bind(chatList, background, 2);

        var scrollBar = MTStyle.Flat.scrollBar(background, Axis.Y);
        scrollBar.container
                .setEnabled(() -> chatList.hiddenSize() > 0)
                .constrain(TOP, match(background.get(TOP)))
                .constrain(BOTTOM, match(background.get(BOTTOM)))
                .constrain(LEFT, relative(background.get(RIGHT), 2))
                .constrain(WIDTH, literal(6));
        scrollBar.primary
                .setScrollableElement(chatList)
                .setSliderState(chatList.scrollState());

        chatMonitor.onMessagesUpdated(messages -> {
            double lastHidden = chatList.hiddenSize();
            double pos = chatList.scrollState().getPos();
            chatList.getList().clear();
            chatList.getList().addAll(messages);
            chatList.rebuildElements();
            double newHidden = chatList.hiddenSize();
            //Update scroll pos, so we stay at the same position when a new message comes in.
            if (newHidden != lastHidden && pos != 0 && pos != 1) {
                double pxlPos = lastHidden * pos;
                chatList.scrollState().setPos(pxlPos / newHidden);
            }
        });

        chatList.scrollState().setPos(1);
    }

    private void tickFriendList() {
        ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
        int newCookie = profileManager.getFriendUpdateCookie();
        if (friendCookie == newCookie) return;
        friendCookie = newCookie;

        List<Profile> knownUsers = profileManager.getKnownProfiles();
        List<Profile> friends = FastStream.of(knownUsers).filter(Profile::isFriend).toLinkedList();
        friends.sort(Comparator.comparing(FriendChatGui::displayName, (o1, o2) -> {
            int res = String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
            return res == 0 ? o1.compareTo(o2) : res;
        }));
        friends.sort(Comparator.comparingInt(e -> e.isOnline() ? 0 : 1));

        friendList.getList().clear();
        friendList.markDirty();
        String search = friendSearch.getValue();
        for (Profile friend : friends) {
            if (!search.isEmpty() && !displayName(friend).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                continue;
            }
            friendList.add(new FriendElement(friendList, friend));
        }

        List<ProfileManager.FriendRequest> requests = profileManager.getFriendRequests();
        if (requests.isEmpty()) return;
        friendList.add(new FriendElement(friendList, (Profile) null));

        for (ProfileManager.FriendRequest request : requests) {
            if (!search.isEmpty() && !displayName(request.from).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                continue;
            }
            friendList.add(new FriendElement(friendList, request));
        }
    }

    private void updateSelected() {
        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        boolean textBoxActive = false;
        if (selected != null && state == IrcState.CONNECTED) {
            IrcUser user = MineTogetherChat.CHAT_STATE.ircClient.getUser(selected);
            if (user != null) {
                chatMonitor.attach(user.getChannel());
                textBoxActive = true;
            } else {
                chatMonitor.attach(null);
                textField.setSuggestion(Component.translatable("minetogether:gui.friends.user_offline"));
            }
        } else {
            chatMonitor.attach(null);
            if (state == IrcState.CONNECTED) {
                textField.setSuggestion(Component.translatable("minetogether:gui.friends.select_friend"));
            } else {
                textField.setSuggestion(Component.literal(ChatConstants.STATE_DESC_LOOKUP.get(state)));
            }
        }

        if (textBoxActive) {
            textField.setEditable(true);
            textField.setFocusable(true);
            textField.setFocus(true);
            textField.setSuggestion((Supplier<Component>) null);
        } else {
            textField.setEditable(false);
            textField.setFocusable(false);
            textField.setFocus(false);
        }
    }

    public static String displayName(@Nullable Profile profile) {
        return profile == null ? "" : profile.isFriend() && profile.hasFriendName() ? profile.getFriendName() : profile.getDisplayName();
    }

    private void scheduleFriendUpdate() {
        friendCookie = -1;
    }
}
