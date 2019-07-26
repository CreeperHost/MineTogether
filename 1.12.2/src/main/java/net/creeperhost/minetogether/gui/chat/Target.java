package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

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
        } else
        {
            newMessages = ChatHandler.hasNewMessages(getInternalTarget());
        }
        if (newMessages)
        {
            TextComponentString str = new TextComponentString(targetName);
            str.appendSibling(new TextComponentString(" \u2022").setStyle(new Style().setColor(TextFormatting.RED)));
            return str.getFormattedText();
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
        if (updating || oldFriends == ChatHandler.friends && chatSize == oldMessagesSize)
            return;
        
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
        if (current != null && !tempSet.contains(current))
            tempSet.add(current);
        
        for (Map.Entry<String, String> friend : ChatHandler.friends.entrySet())
        {
            Target tempTarget = new Target(friend.getValue(), friend.getKey());
            if (!tempSet.contains(tempTarget))
            {
                if (oldVals.contains(tempTarget))
                {
                    tempTarget = oldVals.get(oldVals.indexOf(tempTarget));
                }
                tempSet.add(tempTarget);
            }
        }
        
        for (String chat : ChatHandler.messages.keySet())
        {
            if (chat.equals(ChatHandler.CHANNEL))
                continue;
            for (Target target : oldVals)
            {
                if (target.getInternalTarget().equals(chat))
                {
                    if (ChatHandler.privateChatList == null && target.targetName.equals("Group Chat"))
                        continue;
                    tempSet.add(target);
                    break;
                }
            }
        }

        if(ChatHandler.privateChatList == null)
        {
            //Target t = new Target("new channel", ChatHandler.CHANNEL, false);
            //tempSet.add(t);
        }
        else
        {
            Target p = new Target("Group Chat", ChatHandler.privateChatList.getChannelname(), true);
            privateChannel = p;
            tempSet.add(p);
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
