package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public class TimestampComponentString extends TextComponentString {
    public boolean active = false;
    public String text;

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

    @Override
    public String getText()
    {
        return active  ? getRawText() : "";
    }

    public void setActive(boolean active)
    {
        this.active = active;

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
