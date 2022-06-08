package net.creeperhost.minetogether.module.chat;

import net.creeperhost.minetogether.lib.chat.ChatHandler;
import net.creeperhost.polylib.client.screen.widget.buttons.DropdownButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Target implements DropdownButton.IDropdownOption
{
    private final String targetName;
    private static ArrayList<Target> possibleValsCache = new ArrayList<>();
    private static boolean updating = false;
    private final String internalTarget;
    private static Map<String, String> oldFriends;
    private static int oldMessagesSize;
    private final boolean isChannel;
    public static Target privateChannel;

    private Target(String targetName, String internalTarget, boolean isChannel)
    {
        this.internalTarget = internalTarget;
        this.targetName = targetName;
        this.isChannel = isChannel;
    }

    private Target(String targetName, String internalTarget)
    {
        this(targetName, internalTarget, false);
    }

    @Override
    public String getTranslate(DropdownButton.IDropdownOption currentDO, boolean dropdownOpen)
    {
        Target current = (Target) currentDO;
        boolean newMessages = false;
        if (current == this)
        {
            for (DropdownButton.IDropdownOption targetObj : getPossibleVals())
            {
                Target target = (Target) targetObj;
                if (ChatHandler.hasNewMessages(target.getInternalTarget()))
                {
                    if (!dropdownOpen)
                    {
                        newMessages = true;
                        break;
                    }
                }
            }
        }
        else
        {
            newMessages = ChatHandler.hasNewMessages(getInternalTarget());
        }
        if (newMessages)
        {
            Component str = Component.translatable(targetName);
            return str.copy().append(Component.literal(" \u2022")).withStyle(style -> style.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))).getString();
        }

        return targetName;
    }

    @Override
    public void updateDynamic()
    {
        updateCache(this);
    }

    public static void updateCache()
    {
        updateCache(null);
    }

    public static void updateCache(Target current)
    {
        if (ChatHandler.messages == null) return;

        int chatSize = ChatHandler.messages.size();
        /*if (updating || oldFriends == ChatHandler.friends && chatSize == oldMessagesSize)
            return;*/

        updating = true;

        ArrayList<Target> oldVals = possibleValsCache;

        LinkedHashSet<Target> tempSet = new LinkedHashSet<>();

        // lets check if main has changed its internal target
        if (current != null && current.targetName.equals("Main"))
        {
            if (!current.internalTarget.equals(ChatHandler.CHANNEL))
            {
                current = new Target("Main", ChatHandler.CHANNEL, true);
            }
        }

        possibleValsCache = new ArrayList<>();
        tempSet.add(new Target("Main", ChatHandler.CHANNEL, true));

        if (current != null && !tempSet.contains(current)) tempSet.add(current);

        for (String chat : ChatHandler.messages.keySet())
        {
            if (chat.equals(ChatHandler.CHANNEL)) continue;
            for (Target target : oldVals)
            {
                if (target.getInternalTarget().equals(chat))
                {
                    if (target.targetName.equals("Group Chat") && (ChatHandler.currentParty.isEmpty() || !ChatHandler.currentParty.equals(target.internalTarget)))
                        continue;
                    tempSet.add(target);
                    break;
                }
            }
        }

        if (ChatHandler.hasParty)
        {
            Target target = new Target("Group Chat", ChatHandler.currentParty, true);
            privateChannel = target;
            tempSet.add(target);
        }

        possibleValsCache = new ArrayList<>(tempSet);

        updating = false;
        oldFriends = ChatHandler.friends;
        oldMessagesSize = chatSize;
    }

    public static Target getMainTarget()
    {
        updateCache();
        for (Target defTar : possibleValsCache)
        {
            if (defTar.getInternalTarget().equals(ChatHandler.CHANNEL))
            {
                return defTar;
            }
        }
        return possibleValsCache.size() > 0 ? possibleValsCache.get(0) : new Target("Main", ChatHandler.CHANNEL, true);
    }

    public static Target getTargetFromString(String targetName)
    {
        updateCache();
        for (Target defTar : possibleValsCache)
        {
            if (defTar.getInternalTarget().equals(targetName))
            {
                return defTar;
            }
        }
        return null;
    }

    @Override
    public List<DropdownButton.IDropdownOption> getPossibleVals()
    {
        return (ArrayList) possibleValsCache;
    }

    public String getInternalTarget()
    {
        return internalTarget;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Target && (((Target) obj).internalTarget.equals(internalTarget)) && ((Target) obj).targetName.equals(targetName);
    }

    public static Target getPrivateChannel()
    {
        return privateChannel;
    }

    @Override
    public int hashCode()
    {
        return internalTarget.hashCode() + targetName.hashCode();
    }

    public boolean isChannel()
    {
        return isChannel;
    }
}
