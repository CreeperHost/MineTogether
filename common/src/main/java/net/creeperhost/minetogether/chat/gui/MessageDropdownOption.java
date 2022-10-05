package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Created by covers1624 on 5/10/22.
 */
public enum MessageDropdownOption implements DropdownButton.DropdownEntry {
    MUTE(new TranslatableComponent("minetogether:button.mute")),
    ADD_FRIEND(new TranslatableComponent("minetogether:button.add_friend")),
    MENTION(new TranslatableComponent("minetogether:button.mention"));

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
