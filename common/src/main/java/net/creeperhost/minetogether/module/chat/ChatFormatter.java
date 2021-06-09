package net.creeperhost.minetogether.module.chat;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.util.ComponentUtils;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.knownUsers;

public class ChatFormatter
{
    private static final Pattern nameRegex = Pattern.compile("^(\\w+?):");

    public static Component formatLine(Message message)
    {
        try
        {
            String inputNick = message.sender;
            String outputNick = inputNick;

            if (inputNick.contains(":")) {
                String[] split = inputNick.split(":");
                switch (split[0]) {
                    case "FR": { // new scope because Java is stupid
                        if (split.length < 2)
                            return null;
                        String nick = split[1];
                        String nickDisplay = ChatHandler.getNameForUser(nick);

                        String cmdStr = message.messageStr;
                        String[] cmdSplit = cmdStr.split(" ");

                        if (cmdSplit.length < 2)
                            return null;

                        String friendCode = cmdSplit[0];

                        StringBuilder nameBuilder = new StringBuilder();

                        for (int i = 1; i < cmdSplit.length; i++)
                            nameBuilder.append(cmdSplit[i]);

                        String friendName = nameBuilder.toString();

                        Component userComp = new TranslatableComponent("(" + nickDisplay + ") would like to add you as a friend. Click to ");

                        Component accept = new TranslatableComponent("<Accept>");
                        accept = accept.copy().withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "AC:" + nick + ":" + friendCode + ":" + friendName)).withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)));
                        userComp.getSiblings().add(accept);

                        return userComp;
                    }
                    case "FA":
                        if (split.length < 2)
                            return null;
                        String nick = split[1];
                        String nickDisplay = ChatHandler.getNameForUser(nick);

                        String friendName = message.messageStr;

                        Component userComp = new TranslatableComponent(" (" + nickDisplay + ") accepted your friend request.");

                        return userComp;
                }
            }
            AtomicBoolean premium = new AtomicBoolean(false);

            Profile profile = null;

            if (inputNick.startsWith("MT") && inputNick.length() >= 16) {
                profile = knownUsers.findByNick(inputNick);
                if (profile == null) profile = knownUsers.add(inputNick);
                if (profile != null) {
                    premium.set(profile.isPremium());
                    outputNick = profile.getUserDisplay();
                }
                if (inputNick.equals(MineTogetherChat.profile.get().getShortHash()) || inputNick.equals(MineTogetherChat.profile.get().getMediumHash())) {
                    outputNick = MineTogetherChat.profile.get().getUserDisplay();
                } else {
                    //Should probably check mutedUsers against their shortHash...
                    //TODO
//                    if (MineTogether.instance.mutedUsers.contains(inputNick))
//                        return null;
                }
            } else if (!inputNick.equals("System")) {
                return null;
            }

            Component base = new TranslatableComponent("");

            ChatFormatting nickColour = ChatFormatting.WHITE;
            ChatFormatting arrowColour = ChatFormatting.WHITE;
            ChatFormatting messageColour = ChatFormatting.WHITE;

            if (profile != null && profile.isFriend()) {
                nickColour = ChatFormatting.YELLOW;
                outputNick = profile.friendName;
                if (!ChatHandler.autocompleteNames.contains(outputNick)) {
                    ChatHandler.autocompleteNames.add(outputNick);
                }
            }

            Component userComp = new TranslatableComponent(outputNick);

            String messageStr = message.messageStr;

            String[] split = messageStr.split(" ");

            boolean highlight = false;

            for (int i = 0; i < split.length; i++) {
                String splitStr = split[i];
                String justNick = splitStr.replaceAll("[^A-Za-z0-9#]", "");
                if (justNick.startsWith("MT") && justNick.length() >= 16) {
                    if ((MineTogetherChat.profile.get() != null && (justNick.equals(MineTogetherChat.profile.get().getShortHash()) || justNick.equals(MineTogetherChat.profile.get().getMediumHash()))) || justNick.equals(MineTogetherChat.INSTANCE.ourNick)) {
                        splitStr = splitStr.replaceAll(justNick, ChatFormatting.RED + Minecraft.getInstance().player.getName().toString() + messageColour);
                        split[i] = splitStr;
                        highlight = true;
                    } else if(justNick.length() >= 16)
                    {
                        String userName = "User#" + justNick.substring(2, 5);
                        Profile mentionProfile = knownUsers.findByNick(justNick);
                        if (mentionProfile != null) {
                            userName = mentionProfile.getUserDisplay();
                        }
                        if (userName != null) {
                            splitStr = splitStr.replaceAll(justNick, userName);
                            split[i] = splitStr;
                        }
                    }
                }
            }

            messageStr = String.join(" ", split);

            Component messageComp = newChatWithLinksOurs(messageStr);

            if((profile != null && profile.isBanned()) || ChatHandler.backupBan.get().contains(inputNick)) {
                messageComp = new TranslatableComponent("<Message Deleted>").copy().withStyle(style -> style.withItalic(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)));
                messageColour = ChatFormatting.DARK_GRAY;
            }

            messageComp.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.WHITE));

            if (ChatHandler.curseSync.containsKey(inputNick)) {
                String realname = ChatHandler.curseSync.get(inputNick).trim();
                String[] splitString = realname.split(":");

                if (splitString.length >= 2) {
                    String name2 = splitString[1];

//                    if ((name2.contains(MineTogether.instance.ftbPackID) && !MineTogether.instance.ftbPackID.isEmpty())
//                            || (name2.contains(Config.getInstance().curseProjectID) && !Config.getInstance().curseProjectID.isEmpty() && !Config.getInstance().curseProjectID.equalsIgnoreCase("Insert curse project ID here")))
//                    {
//                        nickColour = ChatFormatting.DARK_PURPLE;
//                        if(profile != null)
//                        {
//                            if(profile.isFriend())
//                            {
//                                nickColour = ChatFormatting.GOLD;
//                            }
//                        }
//                    }
                }
            }

            if (inputNick.equals(MineTogetherChat.INSTANCE.ourNick))
            {
                nickColour = ChatFormatting.GRAY;
                arrowColour = premium.get() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                messageColour = ChatFormatting.GRAY;
                outputNick = MineTogetherChat.profile.get().getUserDisplay();//Minecraft.getInstance().getUser().getName();//player.getName().toString();
                userComp = new TranslatableComponent(outputNick);
            }

            if (premium.get()) {
                arrowColour = ChatFormatting.GREEN;
            } else if (outputNick.equals("System")) {
                Matcher matcher = nameRegex.matcher(messageStr);
                if (matcher.find()) {
                    outputNick = matcher.group();
                    messageStr = messageStr.substring(outputNick.length() + 1);
                    outputNick = outputNick.substring(0, outputNick.length() - 1);
                    messageComp = newChatWithLinksOurs(messageStr);
                    userComp = new TranslatableComponent(outputNick);
                }
                nickColour = ChatFormatting.AQUA;
            }

            //Resetting the colour back to default as this causes an issue for the message
            userComp = new TranslatableComponent(arrowColour + "<" + nickColour + userComp.getString() + arrowColour + "> ");

            if (!inputNick.equals(MineTogetherChat.INSTANCE.ourNick) && inputNick.startsWith("MT")) {
                String finalOutputNick = outputNick;
                userComp = userComp.copy().withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, finalOutputNick)));
            }

            ChatFormatting finalMessageColour = messageColour;
            messageComp = messageComp.copy().withStyle(style -> style.withColor(TextColor.fromLegacyFormat(finalMessageColour)));

//            if(Config.getInstance().getFirstConnect())
//            {
//                messageComp = new StringTextComponent(rot13(messageComp.getString()));
//                messageComp = messageComp.deepCopy().modifyStyle(style -> style.setFontId(GALACTIC_ALT_FONT));
//            }

            base.getSiblings().add(userComp);
            base.getSiblings().add(messageComp);

            return base;

        } catch (Throwable e)
        {
            MineTogether.logger.error("Failed to format line: Sender " + message.sender + " Message" + message.messageStr);
            e.printStackTrace();
        }
        return new TranslatableComponent("Error formatting line, Please report this to the issue tracker");
    }

    public static Component newChatWithLinksOurs(String string)
    {
        Component component = ComponentUtils.newChatWithLinks(string, true);
        if (component.getStyle().getClickEvent() != null)
        {
            Component oldcomponent = component;
            List<Component> siblings = oldcomponent.getSiblings();
            component = new TranslatableComponent("");
            component.getSiblings().add(oldcomponent);
            for (Component sibling : siblings)
            {
                component.getSiblings().add(sibling);
            }
            siblings.clear();
        }
        return component;
    }
}