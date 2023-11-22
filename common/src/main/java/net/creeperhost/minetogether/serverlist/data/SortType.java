package net.creeperhost.minetogether.serverlist.data;

import net.creeperhost.minetogether.polylib.gui.DropdownButton;
import net.creeperhost.minetogether.serverlist.gui.ServerDataPublic;
import net.creeperhost.minetogether.util.EnumFlag;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Locale;

import static net.creeperhost.minetogether.util.Countries.getOurCountry;

/**
 * Created by covers1624 on 25/10/22.
 */
public enum SortType implements DropdownButton.DropdownEntry, Comparator<ServerDataPublic> {
    RANDOM() {
        @Override
        public int compare(ServerDataPublic o1, ServerDataPublic o2) { return 0; }
    },
    PLAYER {
        @Override
        public int compare(ServerDataPublic o1, ServerDataPublic o2) {
            int o1Players = o1.playerList != null ? o1.playerList.size() : 0;
            int o2Players = o2.playerList != null ? o2.playerList.size() : 0;
            return o1Players > o2.server.expectedPlayers ? -1 : o1Players < o2Players ? 1 : 0;
        }
    },
    NAME {
        @Override
        public int compare(ServerDataPublic o1, ServerDataPublic o2) {
            int ret = o1.name.compareToIgnoreCase(o2.name);
            return ret == 0 ? o1.name.compareTo(o2.name) : ret;
        }
    },
    UPTIME {
        @Override
        public int compare(ServerDataPublic o1, ServerDataPublic o2) {
            return Long.compare(o1.server.uptime, o2.server.uptime);
        }
    },
    LOCATION {
        @Override
        public int compare(ServerDataPublic o1, ServerDataPublic o2) {
            EnumFlag f1 = o1.server.getFlag();
            EnumFlag f2 = o2.server.getFlag();

            if (f1 == f2) return 0;
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
        public int compare(ServerDataPublic o1, ServerDataPublic o2) {
            if (o1.ping == o2.ping) return 0;
            if (o1.ping <= 0) return 1;
            if (o2.ping <= 0) return -1;
            return Long.compare(o1.ping, o2.ping);
        }
    };

    @Override
    public Component getTitle(boolean isOpen) {
        return Component.translatable("minetogether:screen.multiplayer.sort." + name().toLowerCase(Locale.ROOT));
    }

    public Component translate() {
        return Component.translatable("minetogether:gui.server_list.sort." + name().toLowerCase(Locale.ROOT));
    }
}
