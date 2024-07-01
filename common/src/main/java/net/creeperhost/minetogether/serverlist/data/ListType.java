package net.creeperhost.minetogether.serverlist.data;

import net.minecraft.network.chat.Component;

/**
 * Created by covers1624 on 25/10/22.
 */
public enum ListType {
    PUBLIC(Component.translatable("minetogether:screen.servertype.title.public"), Component.translatable("minetogether:screen.servertype.listing.public")),
    INVITE(Component.translatable("minetogether:screen.servertype.title.community"), Component.translatable("minetogether:screen.servertype.listing.community")),
    APPLICATION(Component.translatable("minetogether:screen.servertype.title.closed"), Component.translatable("minetogether:screen.servertype.listing.closed")),
    ;

    public final Component title;
    public final Component description;

    ListType(Component title, Component description) {
        this.title = title;
        this.description = description;
    }

    public Component getTitle(boolean isOpen) {
        return title;
    }
}
