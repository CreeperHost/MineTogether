package net.creeperhost.minetogether.client.screen.chat;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.chat.PrivateChat;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.SettingsScreen;
import net.creeperhost.minetogether.client.screen.element.ButtonString;
import net.creeperhost.minetogether.client.screen.element.DropdownButton;
import net.creeperhost.minetogether.client.screen.element.GuiButtonMultiple;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.data.Friend;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.oauth.KeycloakOAuth;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogether.chat.ChatHandler.*;

public class MTChatScreen extends Screen
{
    private final Screen parent;
    private GuiScrollingChat chat;
    private ScreenTextFieldLockable send;
    public DropdownButton<Target> targetDropdownButton;
    private Button friendsButton;
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
    private ButtonString connectionStatus;

    public MTChatScreen(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
    }

    public MTChatScreen(Screen parent, boolean invite)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
        inviteTemp = invite;
    }

    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
        try {
            chat.updateLines(currentTarget);
        } catch (Exception ignored) {}
    }

    @Override
    public void init()
    {
        //Get this data early
        CompletableFuture.runAsync(Callbacks::getBanMessage, MineTogether.profileExecutor);

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
            minecraft.displayGuiScreen(new GDPRScreen(parent, () -> new MTChatScreen(parent)));
            return;
        }

        chat = new GuiScrollingChat(10);
        chat.setLeftPos(10);
        send = new ScreenTextFieldLockable(minecraft.fontRenderer, 11, this.height - 48, width - 22, 20, "");
        if (targetDropdownButton == null)
        {
            targetDropdownButton = new DropdownButton<>(width - 5 - 100, 5, 100, 20, new StringTextComponent("Chat: %s"), Target.getMainTarget(), true, p ->
            {
                if (targetDropdownButton.getMessage().getString().contains("new channel"))
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
        strings.add("Mention");
        if (menuDropdownButton == null)
        {
            addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new StringTextComponent("Menu"), new Menu(strings), true, p ->
            {
                if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Mute"))
                {
                    MineTogether.instance.muteUser(activeDropdown);
                    chat.updateLines(currentTarget);
                }
                else if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Add friend"))
                {
                    minecraft.displayGuiScreen(new ChatFriendScreen(this, MineTogether.instance.playerName, knownUsers.findByDisplay(activeDropdown), Callbacks.getFriendCode(), "", false));
                }
                else if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Mention"))
                {
                    this.send.setFocused2(true);
                    this.send.setText(this.send.getText() + " " + activeDropdown + " ");
                }
                else if (ChatHandler.privateChatInvite != null)
                {
                    confirmInvite();
                }
                menuDropdownButton.x = menuDropdownButton.y = -10000;
                menuDropdownButton.wasJustClosed = false;
            }));
        }
        addButton(friendsButton = new Button(5, 5, 100, 20, new StringTextComponent("Friends list"), p ->
        {
            MineTogether.proxy.openFriendsGui();
        }));
        //Settings menu
        addButton(new GuiButtonMultiple(width - 124, 5, 3, p ->
        {
            this.minecraft.displayGuiScreen(new SettingsScreen(this));
        }));
        addButton(cancelButton = new Button(width - 100 - 5, height - 5 - 20, 100, 20, new StringTextComponent("Cancel"), p ->
        {
            this.minecraft.displayGuiScreen(parent);
        }));
        addButton(reconnectionButton = new Button(5 + 80, height - 5 - 20, 100, 20, new StringTextComponent("Reconnect"), p ->
        {
            ChatHandler.reInit();
        }));
        reconnectionButton.visible = reconnectionButton.active = !(ChatHandler.tries.get() < 5);

        addButton(invited = new Button(5 + 70, height - 5 - 20, 60, 20, new StringTextComponent("Invites"), p ->
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

        addButton(connectionStatus = new ButtonString(5, height - 26, 70, 20, "", p ->
        {
            if(ChatHandler.connectionStatus == ConnectionStatus.BANNED)
            {
                minecraft.displayGuiScreen(new ConfirmScreen(e ->
                {
                    if(e)
                    {
                        try {
                            KeycloakOAuth.openURL(new URL("https://minetogether.io/profile/standing"));
                        } catch (MalformedURLException malformedURLException) { malformedURLException.printStackTrace(); }
                    }
                    this.minecraft.displayGuiScreen(this);

                }, new StringTextComponent(I18n.format("minetogether.banned1" + Callbacks.banMessage)),
                        new StringTextComponent(I18n.format("minetogether.banned2"))));
            }
        }));
    }

    public void refresh()
    {
        Minecraft.getInstance().displayGuiScreen(new MTChatScreen(parent));
    }

    private long tickCounter = 0;

    @Override
    public void tick()
    {
        super.tick();

        if(tickCounter % 10 == 0) rebuildChat();

        if ((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && tickCounter % 1200 == 0)
        {
            if (!ChatHandler.isInitting.get())
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
            reconnectionButton.visible = reconnectionButton.active = !(ChatHandler.tries.get() < 5);
            if (changed || ChatHandler.hasNewMessages(currentTarget))
            {
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
        }
    }

    public void rebuildChat()
    {
        double scroll = chat.getScrollAmount();
        chat.updateLines(currentTarget);
        chat.setScrollAmount(scroll);
    }

    boolean disabledDueToConnection = false;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        ChatHandler.ConnectionStatus status = ChatHandler.connectionStatus;
        renderDirtBackground(1);
        targetDropdownButton.updateDisplayString();
        chat.render(matrixStack, mouseX, mouseY, partialTicks);
        send.render(matrixStack, mouseX, mouseY, partialTicks);
        if (!ChatHandler.isOnline())
        {
            send.setDisabled("Cannot send messages as not connected");
            disabledDueToConnection = true;
        } else if (!targetDropdownButton.getSelected().isChannel()) {
            Profile profile = knownUsers.findByNick(currentTarget);
            if (profile == null || !profile.isOnline()) {
                send.setDisabled("Cannot send messages as friend is not online");
                disabledDueToConnection = true;
            }
        }
        else if (disabledDueToConnection)
        {
            disabledDueToConnection = false;
            send.setEnabled(true);
            Target.updateCache();
            if (!targetDropdownButton.getSelected().getPossibleVals().contains(targetDropdownButton.getSelected()))
                targetDropdownButton.setSelected(Target.getMainTarget());
        }
        drawCenteredString(matrixStack, font, "MineTogether Chat", width / 2, 5, 0xFFFFFF);
        ITextComponent comp = new StringTextComponent(TextFormatting.getValueByName(status.colour) + "\u2022" + " " + TextFormatting.WHITE + status.display);

        if (ChatHandler.isInChannel.get()) {
            drawString(matrixStack, font, "Please Contact Support at with your nick " + ChatHandler.nick + " " + new StringTextComponent("here").setStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https:creeperhost.net/contact"))), 10, height - 20, 0xFFFFFF);
        }

        connectionStatus.setMessage(comp);

        drawLogo(matrixStack, font, width - 20, height - 30, 20, 30, 0.75F);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (!send.getOurEnabled() && send.isHovered(mouseX, mouseY))
        {
            renderTooltip(matrixStack, new StringTextComponent(send.getDisabledMessage()), mouseX, mouseY);
        }

        if (connectionStatus != null && connectionStatus.isHovered() && ChatHandler.connectionStatus == ConnectionStatus.BANNED)
        {
            renderTooltip(matrixStack, new StringTextComponent("Click here to appeal your ban"), mouseX, mouseY);
        }
    }

    public static void drawLogo(MatrixStack matrixStack, FontRenderer fontRendererObj, int containerWidth, int containerHeight, int containerX, int containerY, float scale)
    {
        RenderSystem.color4f(1F, 1F, 1F, 1F); // reset alpha
        float adjust = (1 / scale);
        int width = (int) (containerWidth * adjust);
        int height = (int) (containerHeight * adjust);
        int x = (int) (containerX * adjust);
        int y = (int) (containerY * adjust);
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(Constants.MOD_ID, "textures/creeperhost_logo_1-25.png");
        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Constants.MOD_ID, "textures/minetogether25.png");

        RenderSystem.pushMatrix();
        RenderSystem.scaled(scale, scale, scale);

        int mtHeight = (int) (318 / 2.5);
        int mtWidth = (int) (348 / 2.5);

        int creeperHeight = 22;
        int creeperWidth = 80;

        int totalHeight = mtHeight + creeperHeight;
        int totalWidth = mtWidth + creeperWidth;

        totalHeight *= adjust;
        totalWidth *= adjust;

        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationMineTogetherLogo);
        RenderSystem.enableBlend();
        blit(matrixStack, x + (width / 2 - (mtWidth / 2)), y + (height / 2 - (totalHeight / 2)), 0.0F, 0.0F, mtWidth, mtHeight, mtWidth, mtHeight);

        String created = "Created by";
        int stringWidth = fontRendererObj.getStringWidth(created);

        int creeperTotalWidth = creeperWidth + stringWidth;
        fontRendererObj.drawStringWithShadow(matrixStack, created, x + (width / 2 - (creeperTotalWidth / 2)), y + (height / 2 - (totalHeight / 2) + mtHeight + 7), 0x40FFFFFF);
        RenderSystem.color4f(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that

        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        RenderSystem.enableBlend();
        blit(matrixStack, x + (width / 2 - (creeperTotalWidth / 2) + stringWidth), y + (height / 2 - (totalHeight / 2) + mtHeight), 0.0F, 0.0F, creeperWidth, creeperHeight, creeperWidth, creeperHeight);

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    BooleanConsumer booleanConsumer = result ->
    {
        if (result)
        {
            if (ChatHandler.privateChatInvite != null)
            {
                ChatHandler.acceptPrivateChatInvite(ChatHandler.privateChatInvite);
                MineTogether.instance.toastHandler.clearToast(false);
            }
        }
        minecraft.displayGuiScreen(new MTChatScreen(new MainMenuScreen()));
    };

    public void confirmInvite()
    {
        minecraft.displayGuiScreen(new ConfirmScreen(booleanConsumer, new StringTextComponent(I18n.format("You have been invited to join a private channel by %s", MineTogether.instance.getNameForUser(ChatHandler.privateChatInvite.getOwner()))),
                new StringTextComponent("Do you wish to accept this invite?" + (ChatHandler.hasGroup ? " You are already in a group chat - if you continue, you will swap groups - or disband the group if you are the host." : ""))));
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        if(chat != null) chat.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_)
    {
        if(chat != null) chat.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (send.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            return true;
        }
        if (chat.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        if ((p_keyPressed_1_ == GLFW.GLFW_KEY_ENTER || p_keyPressed_1_ == GLFW.GLFW_KEY_KP_ENTER) && send.getOurEnabled() && !send.getText().trim().isEmpty())
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

        return false;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        send.charTyped(p_charTyped_1_, p_charTyped_2_);
        return false;
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
                else if (justNick.toLowerCase().equals(MineTogether.instance.playerName.toLowerCase())) {
                    split[i] = result.replaceAll(justNick, MineTogether.instance.ourNick);
                    replaced = true;
                }
            }
        }
        if(replaced) {
            text = String.join(" ", split);
        }

        return text;
    }

    public boolean handleComponentClick(Style style, double mouseX, double mouseY)
    {
        ClickEvent event = style.getClickEvent();
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

                Profile targetProfile = knownUsers.findByNick(chatInternalName);
                if(targetProfile == null) targetProfile = knownUsers.add(chatInternalName);

                Minecraft.getInstance().displayGuiScreen(new ChatFriendScreen(this, MineTogether.instance.playerName, targetProfile, friendCode, friendName, true));

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
                menuDropdownButton.x = (int) mouseX;
                menuDropdownButton.y = (int) mouseY;
                menuDropdownButton.flipped = mouseY > 150;
                menuDropdownButton.dropdownOpen = true;
                activeDropdown = event.getValue();
                return true;
            }
        }
        if (event.getAction() == ClickEvent.Action.OPEN_URL)
        {
            this.handleComponentClicked(style);
        }
        return false;
    }

    private static final Pattern nameRegex = Pattern.compile("^(\\w+?):");

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

                        ITextComponent userComp = new StringTextComponent("(" + nickDisplay + ") would like to add you as a friend. Click to ");

                        ITextComponent accept = new StringTextComponent("<Accept>");
                        accept.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "AC:" + nick + ":" + friendCode + ":" + friendName)).setColor(Color.func_240744_a_(TextFormatting.GREEN));
                        userComp.getSiblings().add(accept);

                        return userComp;
                    }
                    case "FA":
                        if (split.length < 2)
                            return null;
                        String nick = split[1];
                        String nickDisplay = ChatHandler.getNameForUser(nick);

                        String friendName = message.messageStr;

                        ITextComponent userComp = new StringTextComponent(" (" + nickDisplay + ") accepted your friend request.");

                        return userComp;
                }
            }
            AtomicBoolean premium = new AtomicBoolean(false);

            Profile profile = null;

            if (inputNick.startsWith("MT") && inputNick.length() >= 16) {
                profile = ChatHandler.knownUsers.findByNick(inputNick);
                if (profile == null) profile = knownUsers.add(inputNick);
                if (profile != null) {
                    premium.set(profile.isPremium());
                    outputNick = profile.getUserDisplay();
                }
                if (inputNick.equals(MineTogether.profile.get().getShortHash()) || inputNick.equals(MineTogether.profile.get().getMediumHash())) {
                    outputNick = MineTogether.instance.playerName;
                } else {
                    //Should probably check mutedUsers against their shortHash...
                    if (MineTogether.instance.mutedUsers.contains(inputNick))
                        return null;
                }
            } else if (!inputNick.equals("System")) {
                return null;
            }

            ITextComponent base = new StringTextComponent("");

            TextFormatting nickColour = TextFormatting.WHITE;
            TextFormatting arrowColour = TextFormatting.WHITE;
            TextFormatting messageColour = TextFormatting.WHITE;

            if (profile != null && profile.isFriend()) {
                nickColour = TextFormatting.YELLOW;
                outputNick = profile.friendName;
                if (!ChatHandler.autocompleteNames.contains(outputNick)) {
                    ChatHandler.autocompleteNames.add(outputNick);
                }
            }

            ITextComponent userComp = new StringTextComponent(outputNick);

            String messageStr = message.messageStr;

            String[] split = messageStr.split(" ");

            boolean highlight = false;

            for (int i = 0; i < split.length; i++) {
                String splitStr = split[i];
                String justNick = splitStr.replaceAll("[^A-Za-z0-9#]", "");
                if (justNick.startsWith("MT")) {
                    if ((MineTogether.profile.get() != null && (justNick.equals(MineTogether.profile.get().getShortHash()) || justNick.equals(MineTogether.profile.get().getMediumHash()))) || justNick.equals(MineTogether.instance.ourNick)) {
                        splitStr = splitStr.replaceAll(justNick, TextFormatting.RED + MineTogether.instance.playerName + messageColour);
                        split[i] = splitStr;
                        highlight = true;
                    } else if(justNick.length() >= 16)
                    {
                        String userName = "User#" + justNick.substring(2, 5);
                        Profile mentionProfile = ChatHandler.knownUsers.findByNick(justNick);
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

            ITextComponent messageComp = newChatWithLinksOurs(messageStr);

            if(profile != null && profile.isBanned())
                messageComp = new StringTextComponent("message deleted").setStyle(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.DARK_GRAY)).setItalic(true));

            messageComp.getStyle().setColor(Color.func_240744_a_(TextFormatting.WHITE));

            if (ChatHandler.curseSync.containsKey(inputNick)) {
                String realname = ChatHandler.curseSync.get(inputNick).trim();
                String[] splitString = realname.split(":");

                if (splitString.length >= 2) {
                    String name2 = splitString[1];

                    if (name2.contains(Config.getInstance().curseProjectID) || name2.contains(MineTogether.instance.ftbPackID) && !MineTogether.instance.ftbPackID.isEmpty()) {
                        nickColour = TextFormatting.DARK_PURPLE;
                    }
                }
            }

            if (inputNick.equals(MineTogether.instance.ourNick) || inputNick.equals(MineTogether.instance.ourNick + "`")) {
                nickColour = TextFormatting.GRAY;
                arrowColour = premium.get() ? TextFormatting.GREEN : TextFormatting.GRAY;
                messageColour = TextFormatting.GRAY;
                outputNick = Minecraft.getInstance().getSession().getUsername();
                userComp = new StringTextComponent(outputNick);
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
                    userComp = new StringTextComponent(outputNick);
                }
                nickColour = TextFormatting.AQUA;
            }

            //Resetting the colour back to default as this causes an issue for the message
            userComp = new StringTextComponent(arrowColour + "<" + nickColour + userComp.getString() + arrowColour + "> " + TextFormatting.WHITE);

            if (!inputNick.equals(MineTogether.instance.ourNick) && !inputNick.equals(MineTogether.instance.ourNick + "`") && inputNick.startsWith("MT")) {
                String finalOutputNick = outputNick;
                userComp = userComp.deepCopy().modifyStyle(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, finalOutputNick)));
            }

            TextFormatting finalMessageColour = messageColour;
            messageComp = messageComp.deepCopy().modifyStyle(style -> style.setColor(Color.func_240744_a_(finalMessageColour)));

            base.getSiblings().add(userComp);
            base.getSiblings().add(messageComp);

            return base;

        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return new StringTextComponent("Error formatting line, Please report this to the issue tracker");
    }

    public static String rainbow(String s)
    {
        char[] strings = s.toCharArray();
        StringBuilder out = new StringBuilder();

        for (char sss : strings)
        {
            TextFormatting random = formattingList.get(Minecraft.getInstance().fontRenderer.random.nextInt(formattingList.size()));
            out.append(random).append(sss);
        }
        return out.toString();
    }

    private class GuiScrollingChat extends ExtendedList
    {
        private ArrayList<IReorderingProcessor> lines;

        GuiScrollingChat(int entryHeight)
        {
            super(Minecraft.getInstance(), MTChatScreen.this.width - 20, MTChatScreen.this.height - 50, 30, MTChatScreen.this.height - 50, 10);
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
            int oldMaxScroll = this.getMaxScroll();
            synchronized (ircLock)
            {
                if (ChatHandler.client == null)
                    return;
                if (ChatHandler.messages == null || ChatHandler.messages.size() == 0)
                    return;
                tempMessages = ChatHandler.messages.get(key);
            }

            ArrayList<IReorderingProcessor> oldLines = lines;
            int listHeight = this.getHeight() - (this.getBottom() - this.getTop() - 4);
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
                    lines.addAll(RenderComponentsUtil.func_238505_a_(display, getWidth() - 10, font));
                }
            } catch (Exception ignored) {}
            if (lines.size() > oldLines.size() && this.getScrollAmount() == oldMaxScroll);
            {
                this.setScrollAmount(this.getMaxScroll());
            }
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
                IReorderingProcessor component = lines.get(i);
                int totalWidth = 5;
                int oldTotal = totalWidth;
                totalWidth += minecraft.fontRenderer.func_243245_a(component);
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(i) && mouseY < getRowTop(i) + itemHeight;

                if (hovering)
                {
                    Style style = minecraft.fontRenderer.func_238420_b_().func_243239_a(component, (int) mouseX);
                    handleComponentClick(style, mouseX, mouseY);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected boolean isSelectedItem(int index)
        {
            return false;
        }

        public void renderEntry(MatrixStack matrixStack, int index, int mouseX, int mouseY, float p_renderList_5_)
        {
            try
            {
                IReorderingProcessor component = lines.get(index);
                int totalWidth = 5;

                int oldTotal = totalWidth;
                totalWidth += minecraft.fontRenderer.func_243245_a(component);

                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;

                if(hovering)
                {
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1, 1, 1, 0.90F);
                    minecraft.fontRenderer.func_238407_a_(matrixStack, component, 10 + oldTotal, getRowTop(index), 0xBBFFFFFF);
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                else
                {
                    minecraft.fontRenderer.func_238407_a_(matrixStack, component, 10 + oldTotal, getRowTop(index), 0xFFFFFF);
                }
            } catch (Exception e)
            {
//                e.printStackTrace();
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
        protected void renderList(MatrixStack matrixStack, int p_renderList_1_, int p_renderList_2_, int mouseX, int mouseY, float p_renderList_5_)
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
                    //this.renderSelection &&
                    if (this.isSelectedItem(j))
                    {
                        int l1 = this.x0 + this.width / 2 - k1 / 2;
                        int i2 = this.x0 + this.width / 2 + k1 / 2;
                        RenderSystem.disableTexture();
                        float f = this.isFocused() ? 1.0F : 0.5F;
                        RenderSystem.color4f(f, f, f, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                        bufferbuilder.pos((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                        bufferbuilder.pos((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                        tessellator.draw();
                        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                        bufferbuilder.pos((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                        bufferbuilder.pos((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
                        tessellator.draw();
                        RenderSystem.enableTexture();
                    }
                    renderEntry(matrixStack, j, mouseX, mouseY, p_renderList_5_);
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

    public static ITextComponent newChatWithLinksOurs(String string)
    {
        ITextComponent component = ForgeHooks.newChatWithLinks(string);
        if (component.getStyle().getClickEvent() != null)
        {
            ITextComponent oldcomponent = component;
            List<ITextComponent> siblings = oldcomponent.getSiblings();
            component = new StringTextComponent("");
            component.getSiblings().add(oldcomponent);
            for (ITextComponent sibling : siblings)
            {
                component.getSiblings().add(sibling);
            }
            siblings.clear();
        }
        return component;
    }
}
