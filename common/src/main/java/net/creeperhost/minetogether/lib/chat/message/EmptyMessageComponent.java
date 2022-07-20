package net.creeperhost.minetogether.lib.chat.message;

/**
 * Created by covers1624 on 13/7/22.
 */
public class EmptyMessageComponent extends MessageComponent {

    @Override
    protected String getMessage() {
        return "";
    }

    @Override
    public boolean isEmpty() {
        return next == null;
    }

    @Override
    public String toString() {
        return next != null ? next.toString() : "";
    }
}
