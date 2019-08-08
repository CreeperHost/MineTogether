package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public class TimestampComponentString extends TextComponentString {
    public String text;
    public static TimestampComponentString currentActive = null;
    public static TimestampComponentString currentFakeActive = null;
    public boolean pretendActive = false;
    private static boolean fakeActive = false;
    private static boolean changed;

    public TimestampComponentString(String msg) {
        super(msg);
        text = msg;
    }

    public static void clearActive() {
        currentActive = null;
    }


    @Override
    public String getUnformattedComponentText() {
        return getText();
    }

    @Override
    public TextComponentString createCopy() {
        TimestampComponentString textcomponentstring = new TimestampComponentString(text);
        if (isActive())
            textcomponentstring.pretendActive = true;
        textcomponentstring.setStyle(this.getStyle().createShallowCopy());

        for (ITextComponent itextcomponent : this.getSiblings())
        {
            textcomponentstring.appendSibling(itextcomponent.createCopy());
        }

        return textcomponentstring;
    }

    public boolean isActive() {
        return currentActive == this || (fakeActive && currentFakeActive == this);
    }

    @Override
    public String getText()
    {
        return isActive() || pretendActive ? getRawText() : "";
    }

    public void setActive()
    {
        changed = true;
        if (fakeActive)
            currentFakeActive = this;
        else
            currentActive = this;
    }

    public static boolean getChanged()
    {
        boolean oldChanged = changed;
        changed = false;
        return oldChanged;
    }

    public static void setFakeActive(boolean active)
    {
        fakeActive = active;
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
