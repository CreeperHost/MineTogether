package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.GuiSettings;
import net.creeperhost.minetogether.gui.element.ButtonString;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.element.GuiButtonMultiple;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.data.Friend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;

import static net.creeperhost.minetogether.chat.ChatHandler.ircLock;

public class GuiMTChat extends GuiScreen
{
    private final GuiScreen parent;
    private GuiScrollingChat chat;
    private GuiTextFieldLockable send;
    public DropdownButton<Target> targetDropdownButton;
    private GuiButton friendsButton;
    private String currentTarget = ChatHandler.CHANNEL;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButton reconnectionButton;
    private GuiButton cancelButton;
    private GuiButton invited;
    private boolean inviteTemp = false;
    private String banMessage = "";
    private ButtonString banButton;
    private GuiButton testButton;
    private GuiButton settingsButton;

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
        try
        {
            chat.updateLines(currentTarget);
        } catch (Exception ignored) {}

//        TimestampComponentString.clearActive();
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

        buttonList.add(settingsButton = new GuiButtonMultiple(-80801, width - 124, 5, 3));

        reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries.get() < 5);

        buttonList.add(invited = new GuiButton(777, 5 + 70, height - 5 - 20, 60, 20, "Invites"));
        invited.visible = ChatHandler.privateChatInvite != null;

        send.setMaxStringLength(120);
        send.setFocused(true);

        if (inviteTemp) {
            confirmInvite();
            inviteTemp = false;
        }

        if(Callbacks.isBanned())
        {
            banMessage = "";
            CompletableFuture.runAsync(() -> Callbacks.getBanMessage(), CreeperHost.instance.profileExecutor);
            if(!banMessage.isEmpty())
                buttonList.add(banButton = new ButtonString(8888, 46, height - 26, TextFormatting.RED + "Ban Reason: " + TextFormatting.WHITE + banMessage));
        }
    }

    long tickCounter = 0;
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if(tickCounter % 10 == 0) rebuildChat();

        if((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && tickCounter % 1200 == 0)
        {
            if(!ChatHandler.isInitting.get()) {
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
            reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries.get() < 5);
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
        }
        drawCenteredString(fontRendererObj, "MineTogether Chat", width / 2, 5, 0xFFFFFF);
        ITextComponent comp = new TextComponentString("\u2022").setStyle(new Style().setColor(TextFormatting.getValueByName(status.colour)));
        comp.appendSibling(new TextComponentString(" " + status.display).setStyle(new Style().setColor(TextFormatting.WHITE)));
        if(ChatHandler.isInChannel.get())
        {
            drawString(fontRendererObj, "Please Contact Support at with your nick " + ChatHandler.nick + " " + new TextComponentString("here").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https:creeperhost.net/contact"))), 10, height - 20, 0xFFFFFF);
        }
        else if(banMessage.isEmpty())
        {
            drawString(fontRendererObj, comp.getFormattedText(), 10, height - 20, 0xFFFFFF);
        }

        drawLogo(fontRendererObj, width - 20, height - 30, 20, 30, 0.75F);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!send.getOurEnabled() && send.isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(send.getDisabledMessage()), mouseX, mouseY);
        }

        if(banButton != null && banButton.isMouseOver())
        {
            drawHoveringText(Arrays.asList("Click here copy Ban-ID to clipboard"), mouseX, mouseY);
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
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(CreeperHost.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(CreeperHost.MOD_ID, "textures/minetogether25.png");
        
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
        if(button != null)
        {
            if (button == targetDropdownButton && targetDropdownButton.displayString.contains("new channel"))
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
                    mc.displayGuiScreen(new GuiChatFriend(this, CreeperHost.instance.playerName, activeDropdown, Callbacks.getFriendCode(), "", false));
                }
            } else if (button == friendsButton)
            {
                CreeperHost.proxy.openFriendsGui();
            } else if (button == reconnectionButton)
            {
                ChatHandler.reInit();
//                ChatConnectionHandler.INSTANCE.connect();
            } else if (button == cancelButton)
            {
//                TimestampComponentString.clearActive();
                chat.updateLines(currentTarget);
                this.mc.displayGuiScreen(parent);
            } else if (button == invited && ChatHandler.privateChatInvite != null)
            {
                confirmInvite();
            }
            else if (button == banButton)
            {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Callbacks.banID), null);
                KeycloakOAuth.openURL(new URL("https://minetogether.io/profile"));
            }
            if(button == settingsButton)
            {
                this.mc.displayGuiScreen(new GuiSettings(this));
            }
            chat.actionPerformed(button);
            super.actionPerformed(button);
        }
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
    }

    //Fuck java regex, |(OR) operator doesn't work for shit, regex checked out on regex101, regexr etc.
    final static Pattern patternA = Pattern.compile("((?:user)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternB = Pattern.compile("((?:@)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternC = Pattern.compile("((?:@user)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternD = Pattern.compile("((?:@user)#([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternE = Pattern.compile("((?:user)#([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternF = Pattern.compile("([a-zA-Z0-9]+)#([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternG = Pattern.compile("(@[a-zA-Z0-9]+)#([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static String getStringForSending(String text)
    {
        String[] split = text.split(" ");
        boolean replaced = false;
        for (int i = 0; i < split.length; i++)
        {
            String word = split[i].toLowerCase();
            final String subst = "User#$2";
            final String substr2 = "$1#$2";

            final Matcher matcher  = patternA.matcher(word);
            final Matcher matcherb = patternB.matcher(word);
            final Matcher matcherc = patternC.matcher(word);
            final Matcher matcherd = patternD.matcher(word);
            final Matcher matchere = patternE.matcher(word);
            final Matcher matcherf = patternF.matcher(word);
            final Matcher matcherg = patternG.matcher(word);

            String justNick = word;
            String result = word;
            String result2 = "";
            if(matcher.matches())
            {
                result = matcher.replaceAll(subst);
            } else if(matcherb.matches())
            {
                result = matcherb.replaceAll(subst);
            } else if(matcherc.matches())
            {
                result = matcherc.replaceAll(subst);
            }
            else if(matcherd.matches())
            {
                result = matcherd.replaceAll(subst);
            }
            else if(matchere.matches())
            {
                result = matchere.replaceAll(subst);
            }
            else if(matcherg.matches())
            {
                result2 = matcherg.replaceAll(substr2);
            } else if(matcherf.matches())
            {
                result2 = matcherf.replaceAll(substr2);
            }
            if(result.startsWith("User") || result2.length() > 0)
            {
                if(result2.length() > 0)
                {
                    justNick = result2.replaceAll("[^A-Za-z0-9#]", "");
                } else {
                    justNick = result.replaceAll("[^A-Za-z0-9#]", "");
                }
                Profile profile = ChatHandler.knownUsers.findByDisplay(justNick);
                if(profile == null)
                {
                    continue;
                }
                String tempWord = profile.getShortHash();
                if (tempWord != null) {
                    split[i] = result.replaceAll(justNick, tempWord);
                    replaced = true;
                }
                else if (justNick.toLowerCase().equals(CreeperHost.instance.playerName.toLowerCase())) {
                    split[i] = result.replaceAll(justNick, CreeperHost.instance.ourNick);
                    replaced = true;
                }
            }
        }
        if(replaced) {
            text = String.join(" ", split);
        }

        return text;
    }
    
    private static Field field;
    
    static
    {
        try
        {
            field = GuiScrollingList.class.getDeclaredField("scrollDistance");
            field.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
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
                
                Minecraft.getMinecraft().displayGuiScreen(new GuiChatFriend(this, CreeperHost.instance.playerName, chatInternalName, friendCode, friendName, true));
                
                return true;
            }
            boolean friends = false;

            for(Friend f : Callbacks.getFriendsList(false))
            {
                if(f.getProfile() != null)
                {
                    if (eventValue.startsWith(f.getProfile().getShortHash()))
                    {
                        friends = true;
                        break;
                    }
                }
            }

            if(!friends)
            {
                int mouseX = Mouse.getX() * GuiMTChat.this.width / GuiMTChat.this.mc.displayWidth;
                menuDropdownButton.xPosition = mouseX;
                menuDropdownButton.yPosition = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
                menuDropdownButton.dropdownOpen = true;
                activeDropdown = event.getValue();
                return true;
            }
        }
        return super.handleComponentClick(component);
    }

    private static final Pattern nameRegex = Pattern.compile("^(\\w+?):");

//    static SimpleDateFormat timestampFormat = new SimpleDateFormat("[HH:mm:ss] ");

    public static ITextComponent formatLine(Message message)
    {
        try {

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

            AtomicBoolean friend = new AtomicBoolean(false);
            AtomicBoolean premium = new AtomicBoolean(false);

            Profile profile = null;

            if (inputNick.startsWith("MT")) {
                profile = ChatHandler.knownUsers.findByNick(inputNick);
                if (profile != null)
                    premium.set(profile.isPremium());

                if (inputNick.equals(CreeperHost.profile.get().getShortHash()) || inputNick.equals(CreeperHost.profile.get().getMediumHash())) {
                    outputNick = CreeperHost.instance.playerName;
                } else {
                    //Should probably check mutedUsers against their shortHash...
                    if (CreeperHost.instance.mutedUsers.contains(inputNick))
                        return null;

                    String newNick = ChatHandler.getNameForUser(inputNick);
                    if (newNick == null)
                        return null;

                    List<Friend> friendList = Callbacks.getFriendsList(false);
                    if (friendList != null) {
                        for (Friend friend1 : friendList) {
                            if (friend1 != null) {
                                Profile profileFriend = friend1.getProfile();
                                if (profileFriend != null) {
                                    if (!profileFriend.getShortHash().isEmpty() && profileFriend.getShortHash().equalsIgnoreCase(inputNick)) {
                                        friend.set(true);
                                    } else if (!profileFriend.getMediumHash().isEmpty() && profileFriend.getMediumHash().equalsIgnoreCase(inputNick)) {
                                        friend.set(true);
                                    }
                                }
                            }
                        }
                    }
                    outputNick = newNick;
                    if (!ChatHandler.autocompleteNames.contains(outputNick))
                        ChatHandler.autocompleteNames.add(outputNick);
                }
            } else if (!inputNick.equals("System")) {
                return null;
            }

            ITextComponent base = new TextComponentString("");

            TextFormatting nickColour = TextFormatting.WHITE;
            TextFormatting arrowColour = TextFormatting.WHITE;
            TextFormatting messageColour = TextFormatting.WHITE;

            ITextComponent userComp = new TextComponentString(outputNick);

            String messageStr = message.messageStr;

            String[] split = messageStr.split(" ");

            boolean highlight = false;

            for (int i = 0; i < split.length; i++) {
                String splitStr = split[i];
                String justNick = splitStr.replaceAll("[^A-Za-z0-9#]", "");
                if (justNick.startsWith("MT")) {
                    if ((CreeperHost.profile.get() != null && (justNick.equals(CreeperHost.profile.get().getShortHash()) || justNick.equals(CreeperHost.profile.get().getMediumHash()))) || justNick.equals(CreeperHost.instance.ourNick)) {
                        splitStr = splitStr.replaceAll(justNick, TextFormatting.RED + CreeperHost.instance.playerName + messageColour);
                        split[i] = splitStr;
                        highlight = true;
                    } else {
                        String userName = "User#" + justNick.substring(2, 5);
                        Profile mentionProfile = ChatHandler.knownUsers.findByNick(justNick);
                        if (mentionProfile != null) {
                            userName = mentionProfile.getDisplay();
                        }
                        if (userName != null) {
                            splitStr = splitStr.replaceAll(justNick, userName);
                            split[i] = splitStr;
                        }
                    }
                }
            }

            messageStr = String.join(" ", split);

            ITextComponent messageComp = newChatWithLinksOurs(messageStr);


            if (CreeperHost.bannedUsers.contains(inputNick))
                messageComp = new TextComponentString("message deleted").setStyle(new Style().setColor(TextFormatting.DARK_GRAY).setItalic(true));

            messageComp.getStyle().setColor(TextFormatting.WHITE);

            if (ChatHandler.curseSync.containsKey(inputNick)) {
                String realname = ChatHandler.curseSync.get(inputNick).trim();
                String[] splitString = realname.split(":");

                if (splitString.length >= 2) {
                    String name2 = splitString[1];

                    if (name2.contains(Config.getInstance().curseProjectID) || name2.contains(CreeperHost.instance.ftbPackID) && !CreeperHost.instance.ftbPackID.isEmpty()) {
                        nickColour = TextFormatting.DARK_PURPLE;
//                    userComp.getStyle().setColor(TextFormatting.DARK_PURPLE);
                    }
                }
            }

            if (inputNick.equals(CreeperHost.instance.ourNick) || inputNick.equals(CreeperHost.instance.ourNick + "`")) {
                nickColour = TextFormatting.GRAY;
                arrowColour = premium.get() ? TextFormatting.GREEN : TextFormatting.GRAY;
                messageColour = TextFormatting.GRAY;

                messageComp.getStyle().setColor(TextFormatting.GRAY);//Make own messages 'obvious' but not in your face as they're your own...
            }

            if (friend.get()) {
                nickColour = TextFormatting.YELLOW;
                inputNick = outputNick;
            }
            if (premium.get()) {
                arrowColour = TextFormatting.GREEN;
            } else if (outputNick.equals("System")) {
                Matcher matcher = nameRegex.matcher(messageStr);
                if (matcher.find()) {
                    outputNick = matcher.group();
                    messageStr = messageStr.substring(outputNick.length() + 1);
                    outputNick = outputNick.substring(0, outputNick.length() - 1);
                    messageComp = newChatWithLinksOurs(messageStr);
                    userComp = new TextComponentString(outputNick);
                }
                nickColour = TextFormatting.AQUA;
                userComp.getStyle().setColor(TextFormatting.AQUA);
            }

            userComp = new TextComponentString(arrowColour + "<" + nickColour + userComp.getFormattedText() + arrowColour + "> ");

            if (!inputNick.equals(CreeperHost.instance.ourNick) && !inputNick.equals(CreeperHost.instance.ourNick + "`") && inputNick.startsWith("MT")) {
                userComp.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, inputNick)));
            }

            base.appendSibling(userComp);

            return base.appendSibling(messageComp.setStyle(new Style().setColor(messageColour)));
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return new TextComponentString("Error formatting line, Please report this to the issue tracker");
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
            return Math.max(super.getContentHeight(), viewHeight);
        }
        
        protected void updateLines(String key)
        {
            LimitedSizeQueue<Message> tempMessages;
            synchronized (ircLock)
            {
                if(ChatHandler.client == null)
                    return;
                if (ChatHandler.messages == null || ChatHandler.messages.size() == 0)
                    return;
                tempMessages = ChatHandler.messages.get(key);
            }

            ArrayList<ITextComponent> oldLines = lines;
            int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
            lines = new ArrayList<>();
            if (tempMessages == null)
                return;
            try
            {
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
            } catch (Exception e)
            {
//                e.printStackTrace();
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
            try
            {
                ITextComponent component = lines.get(slotIdx);
                int mouseX = Mouse.getX() * GuiMTChat.this.width / GuiMTChat.this.mc.displayWidth;
                mouseX -= this.left;
                int totalWidth = 5;
                for (ITextComponent sibling : component.getSiblings()) {
                    int oldTotal = totalWidth;
                    totalWidth += fontRendererObj.getStringWidth(sibling.getFormattedText());
                    boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > slotTop && mouseY < slotTop + slotHeight;
                    if (sibling.getStyle().getClickEvent() != null) {
                        if (hovering) {
                            mc.fontRendererObj.drawString(TextFormatting.getTextWithoutFormattingCodes(sibling.getUnformattedComponentText()), left + oldTotal, slotTop, 0xFF000000);
                            GlStateManager.enableBlend();
                            GlStateManager.color(1, 1, 1, 0.90F);
                            mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xBBFFFFFF);
                            GlStateManager.color(1, 1, 1, 1);

                        } else {
                            mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xFFFFFFFF);
                        }

                    } else {
                        mc.fontRendererObj.drawString(sibling.getFormattedText(), left + oldTotal, slotTop, 0xFFFFFF);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
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
