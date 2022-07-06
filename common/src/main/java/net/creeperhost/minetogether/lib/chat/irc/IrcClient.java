package net.creeperhost.minetogether.lib.chat.irc;

import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by covers1624 on 21/6/22.
 */
public interface IrcClient {

    /**
     * Non-Blocking method to initialize and connect to IRC.
     *
     * @throws IllegalStateException When the client is not in the
     *                               {@link IrcState#DISCONNECTED} state.
     */
    void connect() throws IllegalStateException;

    /**
     * Gets the current state of the IRC client.
     *
     * @return The state.
     */
    IrcState getState();

    /**
     * Gets the profile for the current user.
     *
     * @return The profile.
     */
    Profile getUserProfile();

    /**
     * Gets the {@link ProfileManager} in charge of
     * updating/querying/managing profiles.
     *
     * @return The {@link ProfileManager}.
     */
    ProfileManager getProfileManager();

    @Nullable
    IrcChannel getPrimaryChannel();

    @Nullable
    IrcChannel getChannel(String name);

    Collection<IrcChannel> getChannels();

    void addChannelListener(ChannelListener listener);

    void removeChannelListener(ChannelListener listener);

    interface ChannelListener {

        void channelJoin(IrcChannel channel);

        void channelLeave(IrcChannel channel);
    }
}
