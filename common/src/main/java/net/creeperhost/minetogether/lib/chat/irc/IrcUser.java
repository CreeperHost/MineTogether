package net.creeperhost.minetogether.lib.chat.irc;

import net.creeperhost.minetogether.lib.chat.profile.Profile;

/**
 * Represents a user on IRC.
 * <p>
 * Created by covers1624 on 9/9/22.
 */
public interface IrcUser {

    /**
     * Gets the profile for the user.
     *
     * @return The profile.
     */
    Profile getProfile();

    /**
     * Gets the online status of this user.
     *
     * @return If the user is online.
     */
    boolean isOnline();

    /**
     * Sends a raw CTCP message to this user.
     *
     * @param command The command.
     * @throws IllegalStateException If the user is not online.
     */
    void sendRawCTCP(String command);

    /**
     * Sends a Friend Request to this user.
     *
     * @param friendCode  The friend code of the sender.
     * @param desiredName The name the sender wishes to use for the recipient.
     */
    default void sendFriendRequest(String friendCode, String desiredName) {
        sendRawCTCP("FRIENDREQ " + friendCode + " " + desiredName);
    }

    /**
     * Sends a Friend Request accept to this user.
     *
     * @param friendCode  The friend code of the sender.
     * @param desiredName The name the recipient wants to use for the sender.
     */
    default void acceptFriendRequest(String friendCode, String desiredName) {
        sendRawCTCP("FRIENDACC " + friendCode + " " + desiredName);
    }
}
