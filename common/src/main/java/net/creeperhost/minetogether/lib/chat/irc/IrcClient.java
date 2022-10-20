package net.creeperhost.minetogether.lib.chat.irc;

import net.creeperhost.minetogether.lib.chat.profile.Profile;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by covers1624 on 21/6/22.
 */
public interface IrcClient {

    /**
     * Starts the {@link IrcClient}.
     */
    void start();

    /**
     * Stops the {@link IrcClient}.
     */
    void stop();

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
     * Get the {@link IrcUser} associated with the profile.
     *
     * @param profile The user profile.
     * @return The {@link IrcUser} or {@code null} if it does not exist.
     */
    @Nullable
    IrcUser getUser(Profile profile);

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
