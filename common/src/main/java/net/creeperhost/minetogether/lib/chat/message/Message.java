package net.creeperhost.minetogether.lib.chat.message;

import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Created by covers1624 on 8/7/22.
 */
public class Message extends AbstractWeakNotifiable<Message> {

    public final Instant timestamp;
    @Nullable
    public final Profile sender;
    public final MessageComponent senderName;
    private final MessageComponent message;

    @Nullable
    private MessageComponent messageOverride;
    @Nullable
    private Object messageOverrideListener;

    public Message(Instant timestamp, @Nullable Profile sender, MessageComponent senderName, MessageComponent message) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.senderName = senderName;
        this.message = message;

        senderName.addListener(this, (i, e) -> i.fire(i));
        message.addListener(this, (i, e) -> i.fire(i));
    }

    public MessageComponent getMessage() {
        return messageOverride != null ? messageOverride : message;
    }

    public void setMessageOverride(@Nullable MessageComponent component) {
        if (messageOverrideListener != null) {
            assert messageOverride != null : "THWAT?";

            messageOverride.removeListener(messageOverrideListener);
        }

        if (component == null) {
            messageOverride = null;
            messageOverrideListener = null;
        } else {
            messageOverride = component;
            messageOverrideListener = messageOverride.addListener(this, (i, e) -> i.fire(i));
        }
        fire(this);
    }
}
