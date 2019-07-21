package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public class TimestampComponentString extends TextComponentString {
    public boolean active = false;
    public String text;
    public static TimestampComponentString currentActive = null;

    public TimestampComponentString(String msg) {
        super(msg);
        text = msg;
    }

    @Override
    public String getUnformattedComponentText() {
        return getText();
    }

    @Override
    public TextComponentString createCopy() {
        TimestampComponentString textcomponentstring = new TimestampComponentString(text);
        textcomponentstring.setActive(active);
        textcomponentstring.setStyle(this.getStyle().createShallowCopy());

        for (ITextComponent itextcomponent : this.getSiblings())
        {
            textcomponentstring.appendSibling(itextcomponent.createCopy());
        }

        return textcomponentstring;
    }

    public static void clearActive() {
        if (currentActive != null)
            currentActive.setActive(false);
        currentActive = null;
    }

    @Override
    public String getText()
    {
        return active  ? getRawText() : "";
    }

    public void setActive(boolean active)
    {
        this.active = active;
        if (active) {
            clearActive(); // shouldn't ever happen, but just in case
            currentActive = this;
        }
    }

    @Override
    public String toString() {
        return "TimestampTextComponent{text='" + text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    public String getRawText() {
        HoverEvent event = getStyle().getHoverEvent();
        String ret = text;
        if (event != null && event.getAction() == CreeperHost.instance.TIMESTAMP)
        {
            ret = event.getValue().getFormattedText();
        }
        return ret;
    }
}
