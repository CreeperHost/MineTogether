package net.creeperhost.minetogether.lib.chat.message;

import net.covers1624.quack.collection.StreamableIterable;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.chat.profile.ProfileManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by covers1624 on 13/7/22.
 */
public class MessageUtils {

    private static final Pattern MT_USER_REGEX = Pattern.compile("(MT[\\dA-Fa-f]{15,28})");

    private static final String FALLBACK_SYSTEM_USER = "System";

    private static final Pattern SYSTEM_USER_REGEX = Pattern.compile("^(\\w+?):");

    /**
     * Parses a message, extracting out each User mention
     * building a {@link MessageComponent} tree with
     * {@link ProfileMessageComponent}s representing user mentions.
     *
     * @param profileManager The ProfileManager to lookup Profiles with.
     * @param message        The message.
     * @return The constructed component.
     */
    public static MessageComponent parseMessage(ProfileManager profileManager, String message) {
        Matcher matcher = MT_USER_REGEX.matcher(message);

        MessageComponent result = MessageComponent.of();
        int lastEnd = -1;
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            String hash = message.substring(start, end);
            result = result.append(message.substring(lastEnd != -1 ? lastEnd : 0, start));
            lastEnd = end;
            result = result.append(profileManager.lookupProfile(hash));
        }

        if (lastEnd != -1 && lastEnd < message.length()) {
            result = result.append(message.substring(lastEnd));
        }

        if (result.isEmpty()) {
            return MessageComponent.of(message);
        }
        return result;
    }

    /**
     * Parses a System user message.
     * <p>
     * The left-hand side of the returned Pair will be either, the user parsed out of the message, or
     * the fallback of {@link #FALLBACK_SYSTEM_USER}.
     * The right-hand side of the pair will be the remaining Message as parsed by {@link #parseMessage}.
     *
     * @param profileManager The ProfileManager to lookup Profiles with.
     * @param message        The Message.
     * @return The Pair of parsed Components.
     */
    public static Pair<MessageComponent, MessageComponent> parseSystemMessage(ProfileManager profileManager, String message) {
        Matcher matcher = SYSTEM_USER_REGEX.matcher(message);
        String user = FALLBACK_SYSTEM_USER;
        if (matcher.find()) {
            user = matcher.group(1);
            message = message.substring(matcher.end(0) + 1);
        }
        return Pair.of(MessageComponent.of(user), parseMessage(profileManager, message));
    }

    /**
     * Replaces all known user display names in the message with their MT hash reference.
     *
     * @param profileManager The profile manager to iterate Profiles.
     * @param message        The message to parse and replace.
     * @return The replaced message.
     */
    public static String substituteKnownUsers(ProfileManager profileManager, String message) {
        Map<String, String> knownUsers = StreamableIterable.of(profileManager.getKnownProfiles())
                .filter(e -> e.getIrcName() != null)
                .toMap(Profile::getDisplayName, Profile::getIrcName);

        // Not sure if there is a more performant version of this..
        for (Map.Entry<String, String> entry : knownUsers.entrySet()) {
            message = message.replaceAll(entry.getKey(), "MT" + entry.getValue());
        }
        return message;
    }
}
