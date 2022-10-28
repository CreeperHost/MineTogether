package net.creeperhost.minetogether.chat;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.minecraft.network.chat.Component;

/**
 * Created by covers1624 on 5/10/22.
 */
public enum MessageDropdownOption implements DropdownButton.DropdownEntry {
    MUTE(Component.translatable("minetogether:button.mute")),
    ADD_FRIEND(Component.translatable("minetogether:button.add_friend")),
    MENTION(Component.translatable("minetogether:button.mention"));

    public static final MessageDropdownOption[] VALUES = values();

    private final Component title;

    MessageDropdownOption(Component title) {
        this.title = title;
    }

    @Override
    public Component getTitle(boolean isOpen) {
        return title;
    }
}
