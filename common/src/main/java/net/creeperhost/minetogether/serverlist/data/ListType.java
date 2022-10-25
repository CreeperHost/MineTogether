package net.creeperhost.minetogether.serverlist.data;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Created by covers1624 on 25/10/22.
 */
public enum ListType implements DropdownButton.DropdownEntry {
    PUBLIC(new TranslatableComponent("minetogether:screen.servertype.title.public"), new TranslatableComponent("minetogether:screen.servertype.listing.public")),
    INVITE(new TranslatableComponent("minetogether:screen.servertype.title.community"), new TranslatableComponent("minetogether:screen.servertype.listing.community")),
    APPLICATION(new TranslatableComponent("minetogether:screen.servertype.title.closed"), new TranslatableComponent("minetogether:screen.servertype.listing.closed")),
    ;

    public final Component title;
    public final Component description;

    ListType(Component title, Component description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public Component getTitle(boolean isOpen) {
        return title;
    }
}
