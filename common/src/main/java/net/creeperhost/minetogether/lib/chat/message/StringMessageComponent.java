package net.creeperhost.minetogether.lib.chat.message;

/**
 * Created by covers1624 on 13/7/22.
 */
public class StringMessageComponent extends MessageComponent {

    private final String text;

    public StringMessageComponent(String text) {
        this.text = text;
    }

    @Override
    protected String getMessage() {
        return text;
    }
}
