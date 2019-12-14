package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.element.DropdownButton;

import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogether.chat.ChatHandler.ircLock;

public class GuiMTChat extends GuiScreen
{
    private final GuiScreen parent;
    private GuiScrollingChat chat;
    private GuiTextFieldLockable send;
    public DropdownButton<Target> targetDropdownButton;
    private GuiButton friendsButton;
    public static String playerName = Minecraft.getMinecraft().getSession().getUsername();
    private String currentTarget = ChatHandler.CHANNEL;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButton reconnectionButton;
    private GuiButton cancelButton;
    private GuiButton invited;
    private boolean inviteTemp = false;

    public GuiMTChat(GuiScreen parent)
    {
        this.parent = parent;
    }

    public GuiMTChat(GuiScreen parent, boolean invite)
    {
        this.parent = parent;
        inviteTemp = invite;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        chat.updateLines(currentTarget);
        TimestampComponentString.clearActive();
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        if (!CreeperHost.instance.gdpr.hasAcceptedGDPR())
        {
            mc.displayGuiScreen(new GuiGDPR(parent, () -> new GuiMTChat(parent)));
            return;
        }
        
        chat = new GuiScrollingChat(10);
        send = new GuiTextFieldLockable(8008, mc.fontRendererObj, 10, this.height - 50, width - 20, 20);
        if (targetDropdownButton == null)
            targetDropdownButton = new DropdownButton<>(-1337, width - 5 - 100, 5, 100, 20, "Chat: %s", Target.getMainTarget(), true);
        else
            targetDropdownButton.xPosition = width - 5 - 100;
        buttonList.add(targetDropdownButton);
        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        strings.add("Add friend");
        buttonList.add(menuDropdownButton = new DropdownButton<>(-1337, -1000, -1000, 100, 20, "Menu", new Menu(strings), true));
        buttonList.add(friendsButton = new GuiButton(-80088, 5, 5, 100, 20, "Friends list"));
        buttonList.add(cancelButton = new GuiButton(-800885, width - 100 - 5, height - 5 - 20, 100, 20, "Cancel"));
        buttonList.add(reconnectionButton = new GuiButton(-80084, 5 + 80, height - 5 - 20, 100, 20, "Reconnect"));
        reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries < 5);

        buttonList.add(invited = new GuiButton(777, 5 + 70, height - 5 - 20, 60, 20, "Invites"));
        invited.visible = ChatHandler.privateChatInvite != null;

        send.setMaxStringLength(120);
        send.setFocused(true);

        if (inviteTemp) {
            confirmInvite();
            inviteTemp = false;
        }
    }

    long tickCounter = 0;
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && tickCounter % 1200 == 0)
        {
            if(!ChatHandler.isInitting) {
                ChatHandler.reInit();
            }
        }
        tickCounter++;
        String buttonTarget = targetDropdownButton.getSelected().getInternalTarget();
        boolean changed = false;
        if (!buttonTarget.equals(currentTarget))
        {
            changed = true;
            currentTarget = buttonTarget;
        }
        synchronized (ircLock)
        {
            reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries < 5);
            if (changed || ChatHandler.hasNewMessages(currentTarget))
            {
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
        }
    }

    public void rebuildChat()
    {
        chat.updateLines(currentTarget);
    }
    
    boolean disabledDueToConnection = false;
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        ChatHandler.ConnectionStatus status = ChatHandler.connectionStatus;
        drawDefaultBackground();
        targetDropdownButton.updateDisplayString();
        chat.drawScreen(mouseX, mouseY, partialTicks);
        send.setFocused(true);
        send.drawTextBox();
        if (!ChatHandler.isOnline())
        {
            send.setDisabled("Cannot send messages as not connected");
            disabledDueToConnection = true;
        } else if (!targetDropdownButton.getSelected().isChannel() && !ChatHandler.friends.containsKey(currentTarget))
        {
            send.setDisabled("Cannot send messages as friend is not online");
            disabledDueToConnection = true;
        } else if (disabledDueToConnection)
        {
            disabledDueToConnection = false;
            send.setEnabled(true);
            Target.updateCache();
            if (!targetDropdownButton.getSelected().getPossibleVals().contains(targetDropdownButton.getSelected()))
                targetDropdownButton.setSelected(Target.getMainTarget());
            processBadwords();
        }
        drawCenteredString(fontRendererObj, "MineTogether Chat", width / 2, 5, 0xFFFFFF);
        ITextComponent comp = new TextComponentString("\u2022").setStyle(new Style().setColor(TextFormatting.getValueByName(status.colour)));
        comp.appendSibling(new TextComponentString(" " + status.display).setStyle(new Style().setColor(TextFormatting.WHITE)));
        drawString(fontRendererObj, comp.getFormattedText(), 10, height - 20, 0xFFFFFF);
        drawLogo(fontRendererObj, width - 20, height - 30, 20, 30, 0.75F);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!send.getOurEnabled() && send.isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(send.getDisabledMessage()), mouseX, mouseY);
        }
    }

    public static void drawLogo(FontRenderer fontRendererObj, int containerWidth, int containerHeight, int containerX, int containerY, float scale)
    {
        GlStateManager.color(1F,1F,1F,1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation("creeperhost", "textures/minetogether25.png");
        
        GL11.glPushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GL11.glEnable(GL11.GL_BLEND);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationMineTogetherLogo);
        Gui.drawModalRectWithCustomSizedTexture(x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.getStringWidth(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawStringWithShadow(created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        GlStateManager.color(1F,1F,1F,1F); // reset alpha as font renderer isn't nice like that

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        Gui.drawModalRectWithCustomSizedTexture(x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);
        

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    
    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if(button == targetDropdownButton && targetDropdownButton.displayString.contains("new channel"))
        {
            PrivateChat p = new PrivateChat("#" + CreeperHost.instance.ourNick, CreeperHost.instance.ourNick);
            ChatHandler.privateChatList = p;
            ChatHandler.createChannel(p.getChannelname());
        }
        if (button == menuDropdownButton)
        {
            if (menuDropdownButton.getSelected().option.equals("Mute"))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                chat.updateLines(currentTarget);
            } else if (menuDropdownButton.getSelected().option.equals("Add friend"))
            {
                mc.displayGuiScreen(new GuiChatFriend(this, playerName, activeDropdown, Callbacks.getFriendCode(), "", false));
            }
        } else if (button == friendsButton)
        {
            CreeperHost.proxy.openFriendsGui();
        } else if (button == reconnectionButton)
        {
            ChatHandler.reInit();
        } else if (button == cancelButton)
        {
            TimestampComponentString.clearActive();
            chat.updateLines(currentTarget);
            this.mc.displayGuiScreen(parent);
        }
        else if (button == invited && ChatHandler.privateChatInvite != null)
        {
            confirmInvite();
        }
        chat.actionPerformed(button);
        super.actionPerformed(button);
    }

    public void confirmInvite()
    {
        mc.displayGuiScreen(new GuiYesNo(this, I18n.format("You have been invited to join a private channel by %s", CreeperHost.instance.getNameForUser(ChatHandler.privateChatInvite.getOwner())), "Do you wish to accept this invite?" + (ChatHandler.hasGroup ? " You are already in a group chat - if you continue, you will swap groups - or disband the group if you are the host." : ""), 777));
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        if(result)
        {
            if(id == 777 && ChatHandler.privateChatInvite != null)
            {
                ChatHandler.acceptPrivateChatInvite(ChatHandler.privateChatInvite);
                mc.displayGuiScreen(this);
                CreeperHost.instance.clearToast(false);
                return;
            }
        }
        super.confirmClicked(result, id);
    }

    boolean disabledDueToBadwords = false;

    public void processBadwords()
    {
        String text = send.getText().replaceAll(ChatHandler.badwordsFormat, "");
        boolean veryNaughty = false;
        if (ChatHandler.badwords != null)
        {
            for (String bad : ChatHandler.badwords)
            {
                if (bad.startsWith("(") && bad.endsWith(")"))
                {
                    if (text.matches(bad) || text.matches(bad.toLowerCase()))
                    {
                        veryNaughty = true;
                        break;
                    }
                }
            }
        }
        
        if (veryNaughty)
        {
            send.setDisabled("Cannot send message as contains content which may not be suitable for all audiences");
            disabledDueToBadwords = true;
            return;
        }
        
        if (disabledDueToBadwords)
        {
            disabledDueToBadwords = false;
            send.setEnabled(true);
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        super.handleMouseInput();
        chat.handleElementClicks();
        this.chat.handleMouseInput(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.xPosition = menuDropdownButton.yPosition = -10000;
            menuDropdownButton.wasJustClosed = false;
        }
    }
    //Fuck java regex, |(OR) operator doesn't work for shit, regex checked out on regex101, regexr etc.
    final static Pattern patternA = Pattern.compile("((?:user)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternB = Pattern.compile("((?:@)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternC = Pattern.compile("((?:@user)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    @SuppressWarnings("Duplicates")
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        
        if ((keyCode == 28 || keyCode == 156) && send.getOurEnabled() && !send.getText().trim().isEmpty())
        {
            ChatHandler.sendMessage(currentTarget, getStringForSending(send.getText()));
            send.setText("");
        }
        
        boolean ourEnabled = send.getOurEnabled();
        
        if (!ourEnabled)
        {
            send.setEnabled(true);
        }
        
        send.textboxKeyTyped(typedChar, keyCode);
        
        if (!ourEnabled)
        {
            send.setEnabled(false);
        }
        processBadwords();
    }

    public static String getStringForSending(String text)
    {
        String[] split = text.split(" ");
        for (int i = 0; i < split.length; i++)
        {
            String word = split[i].toLowerCase();
            final String subst = "User$2";

            final Matcher matcher = patternA.matcher(word);
            final Matcher matcherb = patternB.matcher(word);
            final Matcher matcherc = patternC.matcher(word);
            String justNick = word;
            String result = word;
            if(matcher.matches()) {
                result = matcher.replaceAll(subst);
            } else if(matcherb.matches())
            {
                result = matcherb.replaceAll(subst);
            } else if(matcherc.matches())
            {
                result = matcherc.replaceAll(subst);
            }
            justNick = result.replaceAll("[^A-Za-z0-9]", "");


            String tempWord = ChatHandler.anonUsersReverse.get(justNick);
            if (tempWord != null)
                split[i] = result.replaceAll(justNick, tempWord);
            else
            if (justNick.toLowerCase().equals(playerName.toLowerCase()))
                split[i] = result.replaceAll(justNick, CreeperHost.instance.ourNick);
        }
        text = String.join(" ", split);

        return text;
    }
    
    private static Field field;
    
    static
    {
        try
        {
            field = GuiScrollingList.class.getDeclaredField("scrollDistance");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {}
    }
    
    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        ClickEvent event = component.getStyle().getClickEvent();
        if (event == null)
            return false;
        if (event.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
        {
            String eventValue = event.getValue();
            if (eventValue.contains(":"))
            {
                String[] split = eventValue.split(":");
                if (split.length < 3)
                    return false;
                
                String chatInternalName = split[1];
                
                String friendCode = split[2];
                
                StringBuilder builder = new StringBuilder();
                
                for (int i = 3; i < split.length; i++)
                    builder.append(split[i]).append(" ");
                
                String friendName = builder.toString().trim();
                
                Minecraft.getMinecraft().displayGuiScreen(new GuiChatFriend(this, playerName, chatInternalName, friendCode, friendName, true));
                
                return true;
            }
            int mouseX = Mouse.getX() * GuiMTChat.this.width / GuiMTChat.this.mc.displayWidth;
            menuDropdownButton.xPosition = mouseX;
            menuDropdownButton.yPosition = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        return super.handleComponentClick(component);
    }

    private static final Pattern nameRegex = Pattern.compile("^(\\w+?):");

    static SimpleDateFormat timestampFormat = new SimpleDateFormat("[HH:mm:ss] ");
    
    public static ITextComponent formatLine(Message message)
    {
        String inputNick = message.sender;
        String outputNick = inputNick;
        
        if (inputNick.contains(":"))
        {
            String[] split = inputNick.split(":");
            switch (split[0])
            {
                case "FR":
                { // new scope because Java is stupid
                    if (split.length < 2)
                        return null;
                    String nick = split[1];
                    String nickDisplay = ChatHandler.getNameForUser(nick);
                    if (!nickDisplay.startsWith("User"))
                        return null;
                    
                    String cmdStr = message.messageStr;
                    String[] cmdSplit = cmdStr.split(" ");
                    
                    if (cmdSplit.length < 2)
                        return null;
                    
                    String friendCode = cmdSplit[0];
                    
                    StringBuilder nameBuilder = new StringBuilder();
                    
                    for (int i = 1; i < cmdSplit.length; i++)
                        nameBuilder.append(cmdSplit[i]);
                    
                    String friendName = nameBuilder.toString();
                    
                    ITextComponent userComp = new TextComponentString(friendName + " (" + nickDisplay + ") would like to add you as a friend. Click to ");
                    
                    ITextComponent accept = new TextComponentString("<Accept>").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "AC:" + nick + ":" + friendCode + ":" + friendName)).setColor(TextFormatting.GREEN));
                    
                    userComp.appendSibling(accept);
                    
                    return userComp;
                }
                case "FA":
                    if (split.length < 2)
                        return null;
                    String nick = split[1];
                    String nickDisplay = ChatHandler.getNameForUser(nick);
                    
                    String friendName = message.messageStr;
                    
                    ITextComponent userComp = new TextComponentString(friendName + " (" + nickDisplay + ") accepted your friend request.");
                    
                    return userComp;
            }
        }
        
        boolean friend = false;
        if (inputNick.startsWith("MT"))
        {
            if (inputNick.equals(CreeperHost.instance.ourNick) || inputNick.equals(CreeperHost.instance.ourNick + "`"))
            {
                outputNick = playerName;
            } else {
                if (CreeperHost.instance.mutedUsers.contains(inputNick))
                    return null;
                
                String newNick = ChatHandler.getNameForUser(inputNick);
                if (newNick == null)
                    return null;
                if (!inputNick.equals(newNick) && !newNick.startsWith("User"))
                {
                    friend = true;
                }
                outputNick = newNick;
                if (!ChatHandler.autocompleteNames.contains(outputNick))
                    ChatHandler.autocompleteNames.add(outputNick);
            }
        } else if (!inputNick.equals("System"))
        {
            return null;
        }
        
        ITextComponent base = new TextComponentString("");
        
        ITextComponent userComp = new TextComponentString("<" + outputNick + ">");
        userComp.setStyle(new Style().setColor(TextFormatting.GRAY)); // Default colour for people on different modpacks
        
        if (!inputNick.equals(CreeperHost.instance.ourNick) && !inputNick.equals(CreeperHost.instance.ourNick + "`") && inputNick.startsWith("MT"))
        {
            userComp.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, inputNick)));
        }
        
        String messageStr = message.messageStr;
        
        for (String swear : ChatHandler.badwords)
        {
            messageStr = messageStr.replace(swear, StringUtils.repeat("*", swear.length()));
        }

        String[] split = messageStr.split(" ");
        
        boolean highlight = false;
        
        for (int i = 0; i < split.length; i++)
        {
            String splitStr = split[i];
            String justNick = splitStr.replaceAll("[^A-Za-z0-9]", "");
            if (justNick.startsWith("MT"))
            {
                if (justNick.equals(ChatHandler.initedString))
                {
                    splitStr = splitStr.replaceAll(justNick, playerName);
                    split[i] = splitStr;
                    highlight = true;
                } else
                {
                    String userName = ChatHandler.getNameForUser(justNick);
                    if (userName != null)
                    {
                        splitStr = splitStr.replaceAll(justNick, userName);
                        split[i] = splitStr;
                    }
                }
            }
        }
        
        messageStr = String.join(" ", split);

        ITextComponent messageComp = newChatWithLinksOurs(messageStr);

        if (CreeperHost.bannedUsers.contains(inputNick))
            messageComp.getStyle().setColor(TextFormatting.DARK_GRAY).setItalic(true);
//            messageComp = new TextComponentString("message deleted").setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Message deleted as user was banned"))).setColor(TextFormatting.DARK_GRAY).setItalic(true));

        messageComp.getStyle().setColor(TextFormatting.WHITE);

        if(ChatHandler.curseSync.containsKey(inputNick))
        {
            String realname = ChatHandler.curseSync.get(inputNick).trim();
            String[] splitString = realname.split(":");

            String name2 = splitString[1];

            if(name2.contains(Config.getInstance().curseProjectID))
            {
//                userComp.getStyle().setColor(TextFormatting.DARK_PURPLE).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("User on same modpack")));
                userComp.getStyle().setColor(TextFormatting.DARK_PURPLE);
            }
        }

        if(inputNick.equals(CreeperHost.instance.ourNick) || inputNick.equals(CreeperHost.instance.ourNick + "`"))
        {
            messageComp.getStyle().setColor(TextFormatting.GRAY);//Make own messages 'obvious' but not in your face as they're your own...
            userComp.getStyle().setColor(TextFormatting.GRAY); // make sure re:set
        }
        
        if (friend)
        {
//            userComp.getStyle().setColor(TextFormatting.YELLOW).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Friend! MineTogether Friend!")));
            userComp.getStyle().setColor(TextFormatting.YELLOW);
        }
        else if (outputNick.equals("System"))
        {
            Matcher matcher = nameRegex.matcher(messageStr);
            if (matcher.find())
            {
                outputNick = matcher.group();
                messageStr = messageStr.substring(outputNick.length() + 1);
                outputNick = outputNick.substring(0, outputNick.length() - 1);
                messageComp = newChatWithLinksOurs(messageStr);
                userComp = new TextComponentString("<" + outputNick + ">");
//                userComp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Moderator")));
            }
            userComp.getStyle().setColor(TextFormatting.AQUA);
        }

        if (highlight)
        {
            userComp.getStyle().setColor(TextFormatting.RED);
            messageComp.getStyle().setColor(TextFormatting.RED);
            base.getStyle().setColor(TextFormatting.RED);
        }

//        if(Minecraft.getMinecraft().currentScreen instanceof GuiMTChat)
//        {
//            base.appendSibling(new TextComponentString(timestampFormat.format(new Date(message.timeReceived))).setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
//        }
//        else
//        {
            base.getStyle().setHoverEvent(new HoverEvent(CreeperHost.instance.TIMESTAMP, new TextComponentString(timestampFormat.format(new Date(message.timeReceived))).setStyle(new Style().setColor(TextFormatting.DARK_GRAY))));
//        }

        base.appendSibling(new TimestampComponentString("Test"));
        
        base.appendSibling(userComp);
        base.appendSibling(new TextComponentString(" ").setStyle(new Style().setColor(TextFormatting.WHITE)));
        
        return base.appendSibling(messageComp);
    }

    private class GuiScrollingChat extends GuiScrollingList
    {
        private ArrayList<ITextComponent> lines;
        
        GuiScrollingChat(int entryHeight)
        {
            super(Minecraft.getMinecraft(), GuiMTChat.this.width - 20, GuiMTChat.this.height - 50, 30, GuiMTChat.this.height - 50, 10, entryHeight, GuiMTChat.this.width, GuiMTChat.this.height);
            lines = new ArrayList<>();
            updateLines(currentTarget);
        }
        
        @Override
        protected int getContentHeight()
        {
            int viewHeight = this.bottom - this.top - 4;
            return super.getContentHeight() < viewHeight ? viewHeight : super.getContentHeight();
        }
        
        protected void updateLines(String key)
        {
            LimitedSizeQueue<Message> tempMessages;
            synchronized (ircLock)
            {
                if (ChatHandler.messages == null || ChatHandler.messages.size() == 0)
                    return;
                tempMessages = ChatHandler.messages.get(key);
            }

            ArrayList<ITextComponent> oldLines = lines;
            int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
            lines = new ArrayList<>();
            if (tempMessages == null)
                return;
            for (Message message : tempMessages)
            {
                ITextComponent display = formatLine(message);
                if (display == null)
                    continue;
                List<ITextComponent> strings = GuiUtilRenderComponents.splitText(display, listWidth - 6, fontRendererObj, false, true);
                for (ITextComponent string : strings)
                {
                    lines.add(string);
                }
            }
            try
            {
                if (lines.size() > oldLines.size() && ((float) field.get(this) == listHeight) || listHeight < 0)
                {
                    listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
                    field.set(this, listHeight);
                }
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        
        @Override
        protected int getSize()
        {
            return lines.size();
        }
        
        int elementClicked = -1;
        
        protected void handleElementClicks()
        {
            if (elementClicked == -1)
                return;
            
            ITextComponent component = lines.get(elementClicked);
            elementClicked = -1;
            int mouseX = Mouse.getX() * GuiMTChat.this.width / GuiMTChat.this.mc.displayWidth;
            mouseX -= this.left;
            int totalWidth = 0;
            for (ITextComponent sibling : component.getSiblings())
            {
                int oldTotal = totalWidth;
                totalWidth += fontRendererObj.getStringWidth(sibling.getFormattedText());
                if (sibling.getStyle().getClickEvent() != null)
                {
                    if (mouseX > oldTotal && mouseX < totalWidth)
                    {
                        handleComponentClick(sibling);
                        return;
                    }
                }
            }
        }
        
        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            elementClicked = index; // defer until later as this is done in the screen handling which is too soon for us to intercept properly
        }
        
        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }
        
        @Override
        protected void drawBackground() {}
        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            ITextComponent component = lines.get(slotIdx);
            int mouseX = Mouse.getX() * GuiMTChat.this.width / GuiMTChat.this.mc.displayWidth;
            mouseX -= this.left;
            int totalWidth = 5;
            for (ITextComponent sibling : component.getSiblings())
            {
                int oldTotal = totalWidth;
                totalWidth += fontRendererObj.getStringWidth(sibling.getFormattedText());
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > slotTop && mouseY < slotTop + slotHeight;
                if (sibling.getStyle().getClickEvent() != null)
                {
                    if (hovering)
                    {
                        mc.fontRendererObj.drawString(TextFormatting.getTextWithoutFormattingCodes(sibling.getUnformattedComponentText()), left + oldTotal, slotTop, 0xFF000000);
                        GlStateManager.enableBlend();
                        GlStateManager.color(1, 1, 1, 0.90F);
                        mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xBBFFFFFF);
                        GlStateManager.color(1, 1, 1, 1);
                        
                    } else
                    {
                        mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xFFFFFFFF);
                    }
                    
                } else
                {
                    mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xFFFFFF);
                }
            }
        }
    }
    
    public static class Menu implements DropdownButton.IDropdownOption
    {
        List<DropdownButton.IDropdownOption> possibleValsCache;
        public String option;
        
        public Menu(List<String> options)
        {
            possibleValsCache = new ArrayList<>();
            possibleValsCache.add(this);
            option = options.get(0);
            options.remove(0);
            for (String option : options)
            {
                possibleValsCache.add(new Menu(possibleValsCache, option));
            }
        }
        
        public Menu(List<DropdownButton.IDropdownOption> vals, String option)
        {
            possibleValsCache = vals;
            this.option = option;
        }
        
        @Override
        public String getTranslate(DropdownButton.IDropdownOption current, boolean dropdownOpen)
        {
            return option;
        }
        
        @Override
        public List<DropdownButton.IDropdownOption> getPossibleVals()
        {
            return possibleValsCache;
        }
    }

    static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR        namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
            Pattern.CASE_INSENSITIVE);


    public static ITextComponent newChatWithLinksOurs(String string)
    {
        ITextComponent component = ForgeHooks.newChatWithLinks(string);
        if (component.getStyle().getClickEvent() != null)
        {
            ITextComponent oldcomponent = component;
            List<ITextComponent> siblings = oldcomponent.getSiblings();
            component = new TextComponentString("");
            component.appendSibling(oldcomponent);
            for(ITextComponent sibling: siblings)
            {
                component.appendSibling(sibling);
            }
            siblings.clear();
        }
        return component;
    }
    
}