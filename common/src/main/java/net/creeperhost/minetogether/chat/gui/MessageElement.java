package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.chat.MessageDropdownOption;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.creeperhost.polylib.client.modulargui.elements.GuiButton;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiList;
import net.creeperhost.polylib.client.modulargui.elements.GuiTextField;
import net.creeperhost.polylib.client.modulargui.lib.ForegroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.HEIGHT;

/**
 * Created by brandon3055 on 23/09/2023
 */
public class MessageElement extends GuiElement<MessageElement> implements ForegroundRender {
    private final Message message;
    private final GuiTextField textField;
    private final boolean friendUI;
    private int lastWidth;
    private int height = 9;
    private int inset = 0;

    private List<FormattedCharSequence> wrappedLines = new ArrayList<>();

    public MessageElement(GuiList<?> parent, Message message, GuiTextField textField) {
        this(parent, message, textField, false);
    }

    public MessageElement(GuiList<?> parent, Message message, GuiTextField textField, boolean friendUI) {
        super(parent);
        this.message = message;
        this.lastWidth = (int) parent.xSize();
        this.textField = textField;
        this.friendUI = friendUI;
        message.addListener(this, (i, m) -> i.updateMessage(false));
        this.constrain(HEIGHT, Constraint.dynamic(() -> (double) height));
        updateMessage(true);
    }

    private void updateMessage(boolean init) {
        wrappedLines.clear();
        Component formatted = MessageFormatter.formatMessage(message);
        if (formatted.getString().startsWith("§f<§bSystem§f>")) {
            inset = font().width("<System>");
        }
        if (lastWidth - inset < 20) return;
        wrappedLines.addAll(ComponentRenderUtils.wrapComponents(formatted, lastWidth - inset, font()));
        int lines = Math.max(wrappedLines.size(), 1);
        height = (lines * font().lineHeight) + (lines - 1);
        if (!init) ((GuiList<?>) getParent()).markDirty();
    }

    @Override
    public void tick(double mouseX, double mouseY) {
        super.tick(mouseX, mouseY);
        if ((int) xSize() != lastWidth) {
            lastWidth = (int) xSize();
            updateMessage(false);
        }
    }

    @Override
    public void renderInFront(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        double y = yMin();
        for (FormattedCharSequence line : wrappedLines) {
            render.drawString(line, xMin() + (y == yMin() ? 0 : inset), y, 0xFFFFFF);
            y += font().lineHeight + 1;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (friendUI || !isMouseOver(mouseX, mouseY) || (button != GuiButton.LEFT_CLICK && button != GuiButton.RIGHT_CLICK)) return false;
        Style style = getStyleAtPos(mouseX, mouseY);
        if (style == null) return false;

        ClickEvent event = style.getClickEvent();
        if (event == null) return false;

        if (MessageFormatter.CLICK_NAME.equals(event.getValue()) && message.sender != null && message.sender != MineTogetherChat.getOurProfile()) {
            ContextMenu menu = new ContextMenu(getModularGui().getRoot());
            menu.addTitle(Component.literal(message.senderName.getMessage()).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));
            for (MessageDropdownOption value : MessageDropdownOption.VALUES) {
                switch (value) {
                    case MUTE -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.RED), message.sender::mute);
                    case ADD_FRIEND -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.AQUA), () -> {
                        ProfileManager profileManager = MineTogetherChat.CHAT_STATE.profileManager;
                        new TextInputDialog(getModularGui().getRoot(), Component.translatable("minetogether:screen.friendreq.desc.request"), displayName(message.sender))
                                .setResultCallback(friendName -> profileManager.sendFriendRequest(message.sender, friendName.trim()));
                    });
                    case MENTION -> menu.addOption(value.getTitle(true).copy().withStyle(ChatFormatting.AQUA), () -> {
                        String val = textField.getValue();
                        if (!val.isEmpty() && val.charAt(val.length() - 1) != ' ') {
                            val = val + " ";
                        }
                        textField.setValue(val + message.sender.getDisplayName());
                    });
                }
            }
            menu.setPosition(mouseX, mouseY);
        } else {
            getModularGui().getScreen().handleComponentClicked(style);
        }
        return false;
    }

    @Override
    public boolean renderOverlay(GuiRender render, double mouseX, double mouseY, float partialTicks, boolean consumed) {
        if (consumed || super.renderOverlay(render, mouseX, mouseY, partialTicks, consumed)) return true;
        if (!isMouseOver(mouseX, mouseY)) return false;

        Style style = getStyleAtPos(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            render.renderComponentHoverEffect(style, (int) mouseX, (int) mouseY);
            return true;
        }
        return false;
    }

    @Nullable
    public Style getStyleAtPos(double x, double y) {
        x -= xMin();
        y -= yMin();
        int index = (int) (y / (font().lineHeight + 1));
        if (index < 0 || index >= wrappedLines.size()) return null;
        FormattedCharSequence line = wrappedLines.get(index);
        return font().getSplitter().componentStyleAtWidth(line, (int) Math.floor(x));
    }

    private String displayName(@Nullable Profile profile) {
        return profile == null ? "" : profile.isFriend() && profile.hasFriendName() ? profile.getFriendName() : profile.getDisplayName();
    }
}
