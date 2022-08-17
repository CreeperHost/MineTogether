package net.creeperhost.minetogether.lib.chat.message;

import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.lib.util.AbstractWeakNotifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * An incredibly simple linked list of Strings.
 * <p>
 * Created by covers1624 on 8/7/22.
 */
public abstract class MessageComponent extends AbstractWeakNotifiable<MessageComponent> {

    @Nullable
    protected MessageComponent prev;
    @Nullable
    protected MessageComponent next;

    public abstract String getMessage();

    public static MessageComponent of() {
        return new EmptyMessageComponent();
    }

    public static MessageComponent of(String text) {
        return new StringMessageComponent(text);
    }

    public static MessageComponent of(Profile profile) {
        return new ProfileMessageComponent(profile);
    }

    public MessageComponent append(String text) {
        return append(of(text));
    }

    public MessageComponent append(Profile profile) {
        return append(of(profile));
    }

    public MessageComponent append(MessageComponent other) {
        assert other.prev == null : "Cannot move components between trees";
        assert prev == null : "Must call append on the root message component.";
        MessageComponent last = this;
        while (last.next != null) {
            assert last != other : "Already added";
            last = last.next;
        }
        other.prev = last;
        last.next = other;
        // Make listeners fire up the list
        other.addListener(other.prev, (i, e) -> i.fire(i));
        return this;
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns an Iterable that iterates through this component and all of its children
     * skipping any empty components.
     *
     * @return An {@link Iterable} returning this component followed by its children.
     */
    public Iterable<MessageComponent> iterate() {
        return () -> new Iterator<MessageComponent>() {
            @Nullable
            private MessageComponent pointer = MessageComponent.this;

            @Override
            public boolean hasNext() {
                return pointer != null;
            }

            @Override
            public MessageComponent next() {
                assert pointer != null;
                MessageComponent ret = pointer;
                do {
                    pointer = pointer.next;
                }
                while (pointer != null && pointer.isEmpty());
                return ret;
            }
        };
    }

    @Override
    public String toString() {
        String result = getMessage();
        if (next != null) {
            return result + next;
        }
        return result;
    }
}
