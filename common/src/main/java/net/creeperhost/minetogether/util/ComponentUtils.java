package net.creeperhost.minetogether.util;

import net.creeperhost.minetogether.MineTogetherClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtils
{
    public static final HoverEvent.Action<Component> RENDER_GIF = new HoverEvent.Action("show_gif_tooltip", true, null, null, null);

    static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR        namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
            Pattern.CASE_INSENSITIVE);

    //Copied from forge
    public static Component newChatWithLinks(String string, boolean allowMissingHeader)
    {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        MutableComponent ichat = null;
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all urls
        while (matcher.find())
        {
            int start = matcher.start();
            int end = matcher.end();

            // Append the previous left overs.
            String part = string.substring(lastEnd, start);
            if (part.length() > 0)
            {
                if (ichat == null)
                    ichat = new TranslatableComponent(part);
                else
                    ichat.append(part);
            }
            lastEnd = end;
            String url = string.substring(start, end);
            MutableComponent link = new TranslatableComponent(url);

            try
            {
                // Add schema so client doesn't crash.
                if ((new URI(url)).getScheme() == null)
                {
                    if (!allowMissingHeader)
                    {
                        if (ichat == null)
                            ichat = new TranslatableComponent(url);
                        else
                            ichat.append(url);
                        continue;
                    }
                    url = "http://" + url;
                }
            }
            catch (URISyntaxException e)
            {
                // Bad syntax bail out!
                if (ichat == null) ichat = new TranslatableComponent(url);
                else ichat.append(url);
                continue;
            }

            // Set the click event and append the link.
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            HoverEvent hoverEvent = new HoverEvent(ComponentUtils.RENDER_GIF, new TranslatableComponent(url));
            link.setStyle(link.getStyle().withClickEvent(click).withHoverEvent(hoverEvent).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            if (ichat == null)
                ichat = new TranslatableComponent("");
            ichat.append(link);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null)
            ichat = new TranslatableComponent(end);
        else if (end.length() > 0)
            ichat.append(new TranslatableComponent(string.substring(lastEnd)));
        return ichat;
    }
}
