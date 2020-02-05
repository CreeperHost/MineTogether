package net.creeperhost.minetogether.client.gui.chat;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.client.gui.GuiGDPR;
import net.creeperhost.minetogether.client.gui.element.ButtonString;
import net.creeperhost.minetogether.client.gui.element.DropdownButton;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogether.chat.ChatHandler.ircLock;

public class GuiMTChat extends Screen
{
    private final Screen parent;
    private GuiScrollingChat chat;
    private GuiTextFieldLockable send;
    public DropdownButton<Target> targetDropdownButton;
    private Button friendsButton;
    public static String playerName = Minecraft.getInstance().getSession().getUsername();
    private String currentTarget = ChatHandler.CHANNEL;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private Button reconnectionButton;
    private Button cancelButton;
    private Button invited;
    private boolean inviteTemp = false;
    public static List<TextFormatting> formattingList = new ArrayList<>();
    private String banMessage = "";
    private ButtonString banButton;

    public GuiMTChat(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }

    public GuiMTChat(Screen parent, boolean invite)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
        inviteTemp = invite;
    }

    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void init()
    {
        formattingList.add(TextFormatting.RED);
        formattingList.add(TextFormatting.GREEN);
        formattingList.add(TextFormatting.BLUE);
        formattingList.add(TextFormatting.YELLOW);
        formattingList.add(TextFormatting.AQUA);
        formattingList.add(TextFormatting.GOLD);
        formattingList.add(TextFormatting.LIGHT_PURPLE);

        minecraft.keyboardListener.enableRepeatEvents(true);
        if (!MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            minecraft.displayGuiScreen(new GuiGDPR(parent, () -> new GuiMTChat(parent)));
            return;
        }

        chat = new GuiScrollingChat(10);
        chat.setLeftPos(10);
        send = new GuiTextFieldLockable(minecraft.fontRenderer, 11, this.height - 48, width - 22, 20, "");
        if (targetDropdownButton == null)
        {
            targetDropdownButton = new DropdownButton<>(width - 5 - 100, 5, 100, 20, "Chat: %s", Target.getMainTarget(), true, p ->
            {
                if (targetDropdownButton.getMessage().contains("new channel"))
                {
                    PrivateChat privateChat = new PrivateChat("#" + MineTogether.instance.ourNick, MineTogether.instance.ourNick);
                    ChatHandler.privateChatList = privateChat;
                    ChatHandler.createChannel(privateChat.getChannelname());
                }
            });
        } else
        {
            targetDropdownButton.x = width - 5 - 100;
        }
        addButton(targetDropdownButton);

        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        strings.add("Add friend");
        if(menuDropdownButton == null)
        {
            addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, "Menu", new Menu(strings), true, p ->
            {
                
                if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Mute"))
                {
                    MineTogether.instance.muteUser(activeDropdown);
                    chat.updateLines(currentTarget);
                }
                else if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Add friend"))
                {
                    minecraft.displayGuiScreen(new GuiChatFriend(this, playerName, activeDropdown, Callbacks.getFriendCode(), "", false));
                }
                else if (ChatHandler.privateChatInvite != null)
                {
                    confirmInvite();
                }
            }));
        }
        addButton(friendsButton = new Button(5, 5, 100, 20, "Friends list", p ->
        {
            MineTogether.proxy.openFriendsGui();
        }));
        addButton(cancelButton = new Button(width - 100 - 5, height - 5 - 20, 100, 20, "Cancel", p ->
        {
            this.minecraft.displayGuiScreen(parent);
        }));
        addButton(reconnectionButton = new Button(5 + 80, height - 5 - 20, 100, 20, "Reconnect", p ->
        {
            ChatHandler.reInit();
        }));
        reconnectionButton.visible = reconnectionButton.active = !(ChatHandler.tries < 5);

        addButton(invited = new Button(5 + 70, height - 5 - 20, 60, 20, "Invites", p ->
        {
            if (ChatHandler.privateChatInvite != null)
            {
                confirmInvite();
            }
        }));
        invited.visible = ChatHandler.privateChatInvite != null;

        send.setMaxStringLength(120);
        send.setFocused2(true);

        if (inviteTemp)
        {
            confirmInvite();
            inviteTemp = false;
        }

        if(Callbacks.isBanned())
        {
            banMessage = Callbacks.getBanMessage();
            addButton(banButton = new ButtonString(30, height - 26, 60, 20, "Ban Reason: " + banMessage, p ->
            {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Callbacks.banID), null);
            }));
        }
    }

    private long tickCounter = 0;

    @Override
    public void tick()
    {
        super.tick();
        if(tickCounter % 20 == 0) rebuildChat();

        if ((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && tickCounter % 1200 == 0)
        {
            if (!ChatHandler.isInitting)
            {
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
            reconnectionButton.visible = reconnectionButton.active = !(ChatHandler.tries < 5);
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
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        ChatHandler.ConnectionStatus status = ChatHandler.connectionStatus;
        renderDirtBackground(1);
        targetDropdownButton.updateDisplayString();
        chat.render(mouseX, mouseY, partialTicks);
        send.render(mouseX, mouseY, partialTicks);
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
        drawCenteredString(font, "MineTogether Chat", width / 2, 5, 0xFFFFFF);
        ITextComponent comp = new StringTextComponent("\u2022").setStyle(new Style().setColor(Objects.requireNonNull(TextFormatting.getValueByName(status.colour))));
        comp.appendSibling(new StringTextComponent(" " + status.display).setStyle(new Style().setColor(TextFormatting.WHITE)));
        drawString(font, comp.getFormattedText(), 10, height - 20, 0xFFFFFF);
        drawLogo(font, width - 20, height - 30, 20, 30, 0.75F);
        super.render(mouseX, mouseY, partialTicks);
        if (!send.getOurEnabled() && send.isHovered(mouseX, mouseY))
        {
            renderTooltip(Arrays.asList(send.getDisabledMessage()), mouseX, mouseY);
        }
    }

    public static void drawLogo(FontRenderer fontRendererObj, int containerWidth, int containerHeight, int containerX, int containerY, float scale)
    {
        GlStateManager.color4f(1F, 1F, 1F, 1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation("creeperhost", "textures/minetogether25.png");

        GL11.glPushMatrix();
        GlStateManager.scaled(scale, scale, scale);
        GL11.glEnable(GL11.GL_BLEND);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationMineTogetherLogo);
        blit(x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.getStringWidth(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawStringWithShadow(created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        GlStateManager.color4f(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that

        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        blit(x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    
    BooleanConsumer booleanConsumer = result ->
    {
        if(result)
        {
            if(ChatHandler.privateChatInvite != null)
            {
                ChatHandler.acceptPrivateChatInvite(ChatHandler.privateChatInvite);
//                    MineTogether.instance.clearToast(false);
            }
        }
        minecraft.displayGuiScreen(new GuiMTChat(new MainMenuScreen()));
    };

    public void confirmInvite()
    {
        minecraft.displayGuiScreen(new ConfirmScreen(booleanConsumer, new StringTextComponent(I18n.format("You have been invited to join a private channel by %s", MineTogether.instance.getNameForUser(ChatHandler.privateChatInvite.getOwner()))),
                new StringTextComponent("Do you wish to accept this invite?" + (ChatHandler.hasGroup ? " You are already in a group chat - if you continue, you will swap groups - or disband the group if you are the host." : ""))));
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
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        chat.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        send.mouseClicked(mouseX, mouseY, mouseButton);
        chat.mouseClicked(mouseX, mouseY, mouseButton);
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            return true;
        }
        return false;
    }

    //Fuck java regex, |(OR) operator doesn't work for shit, regex checked out on regex101, regexr etc.
    final static Pattern patternA = Pattern.compile("((?:user)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternB = Pattern.compile("((?:@)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternC = Pattern.compile("((?:@user)(\\d+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @SuppressWarnings("Duplicates")
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        if ((p_keyPressed_2_ == 28 || p_keyPressed_2_ == 156) && send.getOurEnabled() && !send.getText().trim().isEmpty())
        {
            ChatHandler.sendMessage(currentTarget, getStringForSending(send.getText()));
            send.setText("");
        }

        boolean ourEnabled = send.getOurEnabled();

        if (!ourEnabled)
        {
            send.setEnabled(true);
        }

        send.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        if (!ourEnabled)
        {
            send.setEnabled(false);
        }
        processBadwords();

        return false;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        send.charTyped(p_charTyped_1_, p_charTyped_2_);
        return false;
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
            if (matcher.matches())
            {
                result = matcher.replaceAll(subst);
            } else if (matcherb.matches())
            {
                result = matcherb.replaceAll(subst);
            } else if (matcherc.matches())
            {
                result = matcherc.replaceAll(subst);
            }
            justNick = result.replaceAll("[^A-Za-z0-9]", "");


            String tempWord = ChatHandler.anonUsersReverse.get(justNick);
            if (tempWord != null)
                split[i] = result.replaceAll(justNick, tempWord);
            else if (justNick.toLowerCase().equals(playerName.toLowerCase()))
                split[i] = result.replaceAll(justNick, MineTogether.instance.ourNick);
        }
        text = String.join(" ", split);

        return text;
    }

    public boolean handleComponentClick(ITextComponent component, double mouseX, double mouseY)
    {
        ClickEvent event = component.getStyle().getClickEvent();
        if (event == null)
        {
            return false;
        }
        
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

                Minecraft.getInstance().displayGuiScreen(new GuiChatFriend(this, playerName, chatInternalName, friendCode, friendName, true));

                return true;
            }
            menuDropdownButton.x = (int) mouseX;
            menuDropdownButton.y = (int) mouseY;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        if(event.getAction() == ClickEvent.Action.OPEN_URL)
        {
            this.handleComponentClicked(component);
        }
        return false;
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

                    ITextComponent userComp = new StringTextComponent(friendName + " (" + nickDisplay + ") would like to add you as a friend. Click to ");

                    ITextComponent accept = new StringTextComponent("<Accept>").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "AC:" + nick + ":" + friendCode + ":" + friendName)).setColor(TextFormatting.GREEN));

                    userComp.appendSibling(accept);

                    return userComp;
                }
                case "FA":
                    if (split.length < 2)
                        return null;
                    String nick = split[1];
                    String nickDisplay = ChatHandler.getNameForUser(nick);

                    String friendName = message.messageStr;

                    ITextComponent userComp = new StringTextComponent(friendName + " (" + nickDisplay + ") accepted your friend request.");

                    return userComp;
            }
        }

        boolean friend = false;
        if (inputNick.startsWith("MT"))
        {
            if (inputNick.equals(MineTogether.instance.ourNick) || inputNick.equals(MineTogether.instance.ourNick + "`"))
            {
                outputNick = playerName;
            } else
            {
                if (MineTogether.instance.mutedUsers.contains(inputNick))
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

        ITextComponent base = new StringTextComponent("");

        ITextComponent userComp = new StringTextComponent("<" + outputNick + ">");
        userComp.setStyle(new Style().setColor(TextFormatting.GRAY)); // Default colour for people on different modpacks

        if (!inputNick.equals(MineTogether.instance.ourNick) && !inputNick.equals(MineTogether.instance.ourNick + "`") && inputNick.startsWith("MT"))
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

        ITextComponent messageComp = GuiMTChat.newChatWithLinksOurs(messageStr);

        if (MineTogether.bannedUsers.contains(inputNick))
            messageComp = new StringTextComponent("message deleted").setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Message deleted as user was banned"))).setColor(TextFormatting.DARK_GRAY).setItalic(true));

        messageComp.getStyle().setColor(TextFormatting.WHITE);

        if (ChatHandler.curseSync.containsKey(inputNick))
        {
            String realname = ChatHandler.curseSync.get(inputNick).trim();
            String[] splitString = realname.split(":");

            String name2 = splitString[1];

            if (name2.contains(Config.getInstance().curseProjectID))
            {
                userComp.getStyle().setColor(TextFormatting.DARK_PURPLE).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("User on same modpack")));
            }
        }

        if (inputNick.equals(MineTogether.instance.ourNick) || inputNick.equals(MineTogether.instance.ourNick + "`"))
        {
            messageComp.getStyle().setColor(TextFormatting.GRAY);//Make own messages 'obvious' but not in your face as they're your own...
            userComp.getStyle().setColor(TextFormatting.GRAY); // make sure re:set
        }

        if (friend)
        {
            userComp.getStyle().setColor(TextFormatting.YELLOW).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Friend! MineTogether Friend!")));
        } else if (outputNick.equals("System"))
        {
            Matcher matcher = nameRegex.matcher(messageStr);
            if (matcher.find())
            {
                outputNick = matcher.group();
                messageStr = messageStr.substring(outputNick.length() + 1);
                outputNick = outputNick.substring(0, outputNick.length() - 1);
                messageComp = newChatWithLinksOurs(messageStr);
                userComp = new StringTextComponent("<" + outputNick + ">");
                userComp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Moderator")));
            }
            userComp.getStyle().setColor(TextFormatting.AQUA);
        }

        if (highlight)
        {
            userComp.getStyle().setColor(TextFormatting.RED);
            messageComp.getStyle().setColor(TextFormatting.RED);
            base.getStyle().setColor(TextFormatting.RED);
        }

        base.appendSibling(userComp);
        base.appendSibling(new StringTextComponent(" ").setStyle(new Style().setColor(TextFormatting.WHITE)));

        return base.appendSibling(messageComp);
    }

    private class GuiScrollingChat extends ExtendedList
    {
        private ArrayList<ITextComponent> lines;

        GuiScrollingChat(int entryHeight)
        {
            super(Minecraft.getInstance(), GuiMTChat.this.width - 20, GuiMTChat.this.height - 50, 30, GuiMTChat.this.height - 50, 10);
            lines = new ArrayList<>();
            updateLines(currentTarget);
        }

        @Override
        public int getHeight()
        {
            int viewHeight = this.getBottom() - this.getTop() - 4;
            return Math.max(super.getHeight(), viewHeight);
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
            int listHeight = this.getHeight() - (this.getBottom() - this.getTop() - 4);
            lines = new ArrayList<>();
            if (tempMessages == null)
                return;
            for (Message message : tempMessages)
            {
                ITextComponent display = formatLine(message);
                if (display == null)
                    continue;
                List<ITextComponent> strings = ScreenUtils.splitText(display, getWidth() - 6, font, false, true);
                for (ITextComponent string : strings)
                {
                    lines.add(string);
                }
            }
            setScrollAmount(Integer.MAX_VALUE);
        }

        @Override
        protected int getItemCount()
        {
            return lines.size();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
        {
            for (int i = 0; i < lines.size(); i++)
            {
                ITextComponent component = lines.get(i);
                int totalWidth = 5;
                for (ITextComponent sibling : component.getSiblings())
                {
                    int oldTotal = totalWidth;
                    totalWidth += minecraft.fontRenderer.getStringWidth(sibling.getFormattedText());
                    boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(i) && mouseY < getRowTop(i) + itemHeight;
                    if(hovering && sibling.getStyle().getClickEvent() != null)
                    {
                        handleComponentClick(sibling, mouseX, mouseY);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected boolean isSelectedItem(int index)
        {
            return false;
        }

        public void renderEntry(int index, int mouseX, int mouseY, float p_renderList_5_)
        {
            ITextComponent component = lines.get(index);
            int totalWidth = 5;
            for (ITextComponent sibling : component.getSiblings())
            {
                int oldTotal = totalWidth;
                totalWidth += minecraft.fontRenderer.getStringWidth(sibling.getFormattedText());
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;
                if (sibling.getStyle().getClickEvent() != null)
                {
                    if (hovering)
                    {
                        minecraft.fontRenderer.drawString(TextFormatting.getTextWithoutFormattingCodes(sibling.getUnformattedComponentText()), 10 + oldTotal, getRowTop(index), 0xFF000000);
                        GlStateManager.enableBlend();
                        GlStateManager.color4f(1, 1, 1, 0.90F);
                        minecraft.fontRenderer.drawString(sibling.getFormattedText(), 10 + oldTotal, getRowTop(index), 0xBBFFFFFF);
                        GlStateManager.color4f(1, 1, 1, 1);

                    } else
                    {
                        minecraft.fontRenderer.drawString(sibling.getFormattedText(), 10 + oldTotal, getRowTop(index), 0xFFFFFFFF);
                    }

                } else
                {
                    minecraft.fontRenderer.drawString(sibling.getFormattedText(), 10 + oldTotal, getRowTop(index), 0xFFFFFF);
                }
            }
        }

        private int getRowBottom(int p_getRowBottom_1_)
        {
            return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
        }

        @Override
        protected int getScrollbarPosition()
        {
            return width + 4;
        }

        @Override
        protected void renderList(int p_renderList_1_, int p_renderList_2_, int mouseX, int mouseY, float p_renderList_5_)
        {
            int i = lines.size();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            for (int j = 0; j < i; ++j)
            {
                int k = this.getRowTop(j);
                int l = this.getRowBottom(j);
                if (l >= this.y0 && k <= this.y1)
                {
                    int i1 = p_renderList_2_ + j * this.itemHeight + this.headerHeight;
                    int j1 = this.itemHeight - 4;
                    int k1 = this.getRowWidth();
                    if (this.renderSelection && this.isSelectedItem(j))
                    {
                        int l1 = this.x0 + this.width / 2 - k1 / 2;
                        int i2 = this.x0 + this.width / 2 + k1 / 2;
                        GlStateManager.disableTexture();
                        float f = this.isFocused() ? 1.0F : 0.5F;
                        GlStateManager.color4f(f, f, f, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                        bufferbuilder.pos((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                        tessellator.draw();
                        GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                        bufferbuilder.pos((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture();
                    }
                    renderEntry(j, mouseX, mouseY, p_renderList_5_);
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

        final Pattern URL_PATTERN = Pattern.compile(
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
            component = new StringTextComponent("");
            component.appendSibling(oldcomponent);
            for (ITextComponent sibling : siblings)
            {
                component.appendSibling(sibling);
            }
            siblings.clear();
        }
        return component;
    }
}