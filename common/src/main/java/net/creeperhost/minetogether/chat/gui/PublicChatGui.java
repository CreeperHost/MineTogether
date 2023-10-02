package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.chat.ChatConstants;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.irc.IrcState;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.sprite.PolyTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 22/09/2023
 */
public class PublicChatGui implements GuiProvider {

    public final ChatMonitor chatMonitor = new ChatMonitor();
    private GuiTextField textField;

    private PublicChatGui() {
    }

    public static GuiProvider createGui() {
        if (MineTogetherChat.isNewUser()) {
            return new NewUserGui();
        } else {
            return new PublicChatGui();
        }
    }

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.translatable("minetogether:gui.chat.title"));

        GuiElement<?> root = gui.getRoot();

        GuiText title = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 5))
                .constrain(HEIGHT, Constraint.literal(8))
                .constrain(LEFT, match(root.get(LEFT)))
                .constrain(RIGHT, match(root.get(RIGHT)));

        GuiText connectionStatus = new GuiText(root)
                .setTooltipSingle(() -> {
                    IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
                    return Component.literal("(" + ChatConstants.STATE_DESC_LOOKUP.get(state) + ")").withStyle(ChatConstants.STATE_FORMAT_LOOKUP.get(state));
                })
                .setTooltipDelay(0)
                .constrain(TOP, match(title.get(TOP)))
                .constrain(LEFT, dynamic(() -> title.xCenter() + (title.font().width(title.getText()) / 2) + 4))
                .constrain(WIDTH, literal(10))
                .constrain(HEIGHT, literal(8))
                .setTextSupplier(() -> {
                    IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
                    return Component.literal(connectIndicator(state)).withStyle(ChatConstants.STATE_FORMAT_LOOKUP.get(state));
                });

        GuiElement<?> textBoxBg = MTStyle.Flat.contentArea(root)
                .setEnabled(() -> MineTogetherChat.CHAT_STATE.ircClient.getState() != IrcState.BANNED)
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(root.get(RIGHT), -10))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -10))
                .constrain(HEIGHT, literal(16));

        //If they are banned, then we can just replace the text box with the appeal button.
        GuiButton banned = MTStyle.Flat.buttonCaution(root, Component.translatable("minetogether:gui.button.banned").withStyle(ChatFormatting.UNDERLINE))
                .setEnabled(() -> MineTogetherChat.CHAT_STATE.ircClient.getState() == IrcState.BANNED)
                .setTooltip(Component.translatable("minetogether:gui.button.banned.info"))
                .setTooltipDelay(0)
                .onPress(() -> Util.getPlatform().openUri("https://minetogether.io/profile/standing"));

        Constraints.bind(banned, textBoxBg);

        GuiElement<?> chatBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, match(textBoxBg.get(LEFT)))
                .constrain(RIGHT, match(textBoxBg.get(RIGHT)))
                .constrain(TOP, relative(root.get(TOP), 22))
                .constrain(BOTTOM, relative(textBoxBg.get(TOP), -4));

        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back_arrow"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(BOTTOM, relative(chatBg.get(TOP), -4))
                .constrain(LEFT, match(chatBg.get(LEFT)))
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

        GuiButton oldGui = MTStyle.Flat.button(root, Component.literal("Old Gui"))
                .onPress(() -> gui.mc().setScreen(new ChatScreen(gui.getScreen())))
                .constrain(BOTTOM, relative(root.get(BOTTOM), 0))
                .constrain(RIGHT, relative(root.get(RIGHT), 0))
                .constrain(WIDTH, literal(50))
                .constrain(HEIGHT, literal(10));

        GuiButton friends = MTStyle.Flat.button(root, (Supplier<Component>) null)
                //Keep the same parent screen, So the back button always takes us to the 'first' screen (Main Menu / Pause Menu)
                .setTooltip(Component.translatable("minetogether:gui.button.friends.info"))
                .setTooltipDelay(0)
                .onPress(() -> gui.mc().setScreen(new ModularGuiScreen(new FriendChatGui(), gui.getParentScreen())))
                .constrain(BOTTOM, match(back.get(BOTTOM)))
                .constrain(RIGHT, relative(settings.get(LEFT), -2))
                .constrain(WIDTH, literal(16))
                .constrain(HEIGHT, literal(14));

        GuiTexture publicIcon = new GuiTexture(friends, MTTextures.get("buttons/friend_chat_light"));
        Constraints.bind(publicIcon, friends, 0, 1, 0, 1);

        // Setup Text Field
        textField = new GuiTextField(textBoxBg)
                .setMaxLength(256)
                .setCanLoseFocus(false)
                .setOnEditComplete(() -> {
                    String message = textField.getValue().trim();
                    if (!message.isEmpty()) {
                        textField.setValue("");
                        if (chatMonitor.getChannel() != null) {
                            chatMonitor.getChannel().sendMessage(message);
                        }
                    }
                });
        Constraints.bind(textField, textBoxBg, 0, 3, 0, 3);

        // Setup Chat List Window
        GuiList<Message> chatList = new GuiList<>(chatBg);
        chatList.setDisplayBuilder((parent, message) -> new MessageElement(parent, message, textField));
        Constraints.bind(chatList, chatBg, 2);

        var scrollBar = MTStyle.Flat.scrollBar(root, Axis.Y);
        scrollBar.container
                .setEnabled(() -> chatList.hiddenSize() > 0)
                .constrain(TOP, match(chatBg.get(TOP)))
                .constrain(BOTTOM, match(chatBg.get(BOTTOM)))
                .constrain(LEFT, relative(chatBg.get(RIGHT), 2))
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

        IrcChannel channel = MineTogetherChat.CHAT_STATE.ircClient.getPrimaryChannel();
        if (channel != null) {
            chatMonitor.attach(channel);
        }

        chatList.scrollState().setPos(1);
        gui.onTick(this::tick);
        gui.onClose(chatMonitor::onGuiClose);
    }

    private void tick() {
        chatMonitor.tick();

        IrcState state = MineTogetherChat.CHAT_STATE.ircClient.getState();
        if (state != IrcState.CONNECTED) {
            textField.setFocus(false);
            textField.setEditable(false);
            textField.setValue("");
            textField.setSuggestion(Component.translatable(ChatConstants.STATE_SUGGESTION_LOOKUP.get(state)));
            return;
        }

        textField.setEditable(true);
        textField.setFocus(true);
        textField.setSuggestion((Supplier<Component>) null);
    }

    private String connectIndicator(IrcState state) {
        if (state == IrcState.CONNECTED) {
            return "✔";
        } else if (state == IrcState.CONNECTING || state == IrcState.RECONNECTING || state == IrcState.VERIFYING) {
            return new String[]{"|", "/", "-", "\\"}[(int) ((System.currentTimeMillis() / 100) % 4)];
        } else {
            return "❌";
        }
    }
}
