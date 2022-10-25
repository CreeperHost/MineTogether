package net.creeperhost.minetogether.serverlist.data;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.serverlist.gui.PublicServerEntry;
import net.creeperhost.minetogether.util.EnumFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Comparator;
import java.util.Locale;

import static net.creeperhost.minetogether.util.Countries.getOurCountry;

/**
 * Created by covers1624 on 25/10/22.
 */
public enum SortType implements DropdownButton.DropdownEntry, Comparator<PublicServerEntry> {
    RANDOM() {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) { return 0; }
    },
    PLAYER {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) {
            int o1Players = o1.getServerData().playerList != null ? o1.getServerData().playerList.size() : 0;
            int o2Players = o2.getServerData().playerList != null ? o2.getServerData().playerList.size() : 0;
            return o1Players > o2.getServerData().server.expectedPlayers ? -1 : o1Players < o2Players ? 1 : 0;
        }
    },
    NAME {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) {
            int ret = o1.getServerData().name.compareToIgnoreCase(o2.getServerData().name);
            return ret == 0 ? o1.getServerData().name.compareTo(o2.getServerData().name) : ret;
        }
    },
    UPTIME {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) {
            return Long.compare(o1.getServerData().server.uptime, o2.getServerData().server.uptime);
        }
    },
    LOCATION {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) {
            EnumFlag f1 = o1.getServerData().server.getFlag();
            EnumFlag f2 = o2.getServerData().server.getFlag();

            if (f1 == f2) return 1;
            if (f1.name().equals(getOurCountry())) {
                return f2.name().equals(getOurCountry()) ? 1 : -1;
            }
            if (f2.name().equals(getOurCountry())) {
                return f1.name().equals(getOurCountry()) ? -1 : 1;
            }

            int ret = f1.name().compareToIgnoreCase(f2.name());
            return ret == 0 ? f1.name().compareTo(f2.name()) : ret;
        }
    },
    PING {
        @Override
        public int compare(PublicServerEntry o1, PublicServerEntry o2) {
            if (o1.getServerData().ping == o2.getServerData().ping) return 0;
            if (o1.getServerData().ping <= 0) return 1;
            if (o2.getServerData().ping <= 0) return -1;
            return Long.compare(o1.getServerData().ping, o2.getServerData().ping);
        }
    };

    @Override
    public Component getTitle(boolean isOpen) {
        return new TranslatableComponent("minetogether:screen.multiplayer.sort." + name().toLowerCase(Locale.ROOT));
    }
}
