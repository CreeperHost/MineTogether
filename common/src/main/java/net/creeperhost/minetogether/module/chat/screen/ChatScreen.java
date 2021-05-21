package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogethergui.gif.AnimatedGif;
import net.creeperhost.minetogether.util.ComponentUtils;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogethergui.widgets.Target;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatConnectionStatus;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Friend;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.ircLock;
import static net.creeperhost.minetogetherlib.chat.ChatHandler.knownUsers;

public class ChatScreen extends Screen
{
    private final Screen parent;
    private ScrollingChat chat;
    private EditBox send;
    private String currentTarget = ChatHandler.CHANNEL;
    private ButtonString connectionStatus;
    public DropdownButton<Target> targetDropdownButton;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private AnimatedGif gifImage;
    private AnimatedGif.GifPlayer gifPlayer;

    public ChatScreen(Screen parent)
    {
        super(new TranslatableComponent("MineTogether Chat"));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        chat = new ScrollingChat(width, height);
        chat.setLeftPos(10);
        send = new EditBox(minecraft.font, 11, this.height - 48, width - 22, 20, new TranslatableComponent(""));
        send.setFocus(true);
        send.setMaxLength(256);

        try {
//            gifImage = AnimatedGif.fromPath(Paths.get("C:\\Users\\TravisN\\Documents\\dev\\Architectury\\MineTogether\\common\\src\\main\\resources\\assets\\minetogether\\textures\\giphy.gif"));
//            gifImage = AnimatedGif.fromURL(new URL("https://i.pinimg.com/originals/61/65/cb/6165cba36c16381449d0be75d2d6a85c.gif"));

        }catch (Exception e)
        {
            e.printStackTrace();;
        }

        addButtons();
        super.init();
    }

    public void addButtons()
    {
        addButton(targetDropdownButton = new DropdownButton<>(width - 5 - 100, 5, 100, 20, new TranslatableComponent("Chat: %s"), Target.getMainTarget(), true, p ->
        {
            if (targetDropdownButton.getMessage().getString().contains("new channel"))
            {
//                PrivateChat privateChat = new PrivateChat("#" + MineTogether.instance.ourNick, MineTogether.instance.ourNick);
//                ChatHandler.privateChatList = privateChat;
//                IrcHandler.sendString("JOIN " + privateChat.getChannelname(), true);
            }
        }));
        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        strings.add("Add friend");
        strings.add("Mention");
        addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new TranslatableComponent("Menu"), new Menu(strings), false, p ->
        {
            if(!menuDropdownButton.dropdownOpen) return;

            if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Mute"))
            {
//                MineTogether.instance.muteUser(activeDropdown);
                chat.updateLines(currentTarget);
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Add friend"))
            {
                minecraft.setScreen(new FriendRequestScreen(new ChatScreen(parent), Minecraft.getInstance().getUser().getName(), knownUsers.findByDisplay(activeDropdown), ChatCallbacks.getFriendCode(MineTogetherClient.getUUID()), "", false));
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase("Mention"))
            {
                this.send.setFocus(true);
                this.send.setValue(this.send.getValue() + " " + activeDropdown + " ");
            }
            else if (ChatHandler.privateChatInvite != null)
            {
//                confirmInvite();
            }
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            menuDropdownButton.dropdownOpen = false;
        }));

        addButton(new Button(5, 5, 100, 20, new TranslatableComponent("Friends list"), p ->
        {
            this.minecraft.setScreen(new FriendsListScreen(this));
        }));
        addButton(new ButtonMultiple(width - 124, 5, 3, p ->
        {
            this.minecraft.setScreen(new SettingsScreen(this));
        }));
        addButton(new Button(width - 100 - 5, height - 5 - 20, 100, 20, new TranslatableComponent("Cancel"), p ->
        {
            this.minecraft.setScreen(parent);
        }));
        addButton(connectionStatus = new ButtonString(5, height - 26, 70, 20, new TranslatableComponent(""), p ->
        {

        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);

        renderConnectionStatus();
        chat.render(poseStack, mouseX, mouseY, partialTicks);
        menuDropdownButton.render(poseStack, mouseX, mouseY, partialTicks);
        send.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 5, 0xFFFFFF);

        if(gifPlayer != null) gifPlayer.render(poseStack, mouseX + 5, mouseY + 5, 60, 40, partialTicks);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void renderConnectionStatus()
    {
        ChatConnectionStatus chatConnectionStatus = ChatHandler.connectionStatus;
        Component comp = new TranslatableComponent(ChatFormatting.getByName(chatConnectionStatus.colour) + "\u2022" + " " + ChatFormatting.WHITE + chatConnectionStatus.display);
        connectionStatus.setMessage(comp);
    }

    @Override
    public void tick()
    {
        if(gifPlayer != null) gifPlayer.tick();
        String buttonTarget = targetDropdownButton.getSelected().getInternalTarget();
        if (!buttonTarget.equals(currentTarget)) currentTarget = buttonTarget;

        synchronized (ircLock)
        {
            if (ChatHandler.hasNewMessages(currentTarget))
            {
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;

        if (send.mouseClicked(mouseX, mouseY, mouseButton)) {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            menuDropdownButton.dropdownOpen = false;
            return true;
        }
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            return true;
        }
        chat.mouseClicked(mouseX, mouseY, mouseButton);
        return false;//super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g)
    {
        chat.mouseDragged(d, e, i, f, g);
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f)
    {
        chat.mouseScrolled(d, e, f);
        return super.mouseScrolled(d, e, f);
    }

    @Deprecated
    public void rebuildChat()
    {
        double scroll = chat.getScrollAmount();
        chat.updateLines(currentTarget);
        chat.setScrollAmount(scroll);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if ((p_keyPressed_1_ == GLFW.GLFW_KEY_ENTER || p_keyPressed_1_ == GLFW.GLFW_KEY_KP_ENTER) && !send.getValue().trim().isEmpty())
        {
            ChatHandler.sendMessage(currentTarget, getStringForSending(send.getValue()));
            send.setValue("");
        }
        send.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        send.charTyped(c, i);
        return super.charTyped(c, i);
    }

    public boolean handleComponentClick(Style style, double mouseX, double mouseY)
    {
        if(style == null) return false;
        if(style.getClickEvent() == null) return false;
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

                //TODO
//                Minecraft.getInstance().displayGuiScreen(new ChatFriendScreen(this, MineTogether.instance.playerName, targetProfile, friendCode, friendName, true));

                return true;
            }
            boolean friends = false;

            List<Friend> friends1 = ChatCallbacks.getFriendsList(false, MineTogetherClient.getUUID());

            //Added null check as friends list can take some time to come back when its huge...
            if(friends1 != null) {
                for (Friend f : friends1) {
                    if (f.getProfile() != null) {
                        if (eventValue.startsWith(f.getProfile().getShortHash())) {
                            friends = true;
                            break;
                        }
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
                Profile profile = knownUsers.findByDisplay(justNick);
                if(profile == null)
                {
                    continue;
                }
                String tempWord = profile.getShortHash();
                if (tempWord != null)
                {
                    split[i] = result.replaceAll(justNick, tempWord);
                    replaced = true;
                }
                else if (justNick.toLowerCase().equals(Minecraft.getInstance().getUser().getName()))
                {
                    split[i] = result.replaceAll(justNick, MineTogetherChat.INSTANCE.ourNick);
                    replaced = true;
                }
            }
        }
        if(replaced)
        {
            text = String.join(" ", split);
        }
        return text;
    }

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

    private class ScrollingChat extends ObjectSelectionList
    {
        private ArrayList<FormattedCharSequence> lines;
        private int height;
        private int Width;
        private int top;
        private int bottom;
        private int itemHeight;

        public ScrollingChat(int width, int height)
        {
            super(Minecraft.getInstance(), width - 20, height - 50, 30, height - 50, 10);
            this.height = height - 50;
            this.width = width - 20;
            this.top = 30;
            this.bottom = height - 50;
            this.itemHeight = 10;
            lines = new ArrayList<>();
            updateLines(currentTarget);
        }

        public void renderEntry(PoseStack poseStack, int index, int mouseX, int mouseY, float partialTicks)
        {
            try
            {
                FormattedCharSequence component = lines.get(index);
                int totalWidth = 5;

                int oldTotal = totalWidth;
                totalWidth += minecraft.font.width(component);

                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;

                Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);

                if(hovering)
                {
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1, 1, 1, 0.90F);
                    minecraft.font.draw(poseStack, component, 10 + oldTotal, getRowTop(index), 0xBBFFFFFF);
                    renderComponentHoverEffect(poseStack, style , mouseX, mouseY);
                    if(style.getHoverEvent() != null && style.getHoverEvent().getAction() == ComponentUtils.RENDER_GIF)
                    {
                        Component urlComponent = (Component)style.getHoverEvent().getValue(ComponentUtils.RENDER_GIF);
                        String url = urlComponent.getString();
                        gifImage = AnimatedGif.fromURL(new URL(url));
                        if(gifImage != null && gifPlayer == null)
                        {
                            gifPlayer = gifImage.makeGifPlayer();
                            gifPlayer.setAutoplay(true);
                            gifPlayer.setLooping(true);
                        }
                    }
                    else
                    {
                        gifPlayer = null;
                    }
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                else
                {
                    minecraft.font.draw(poseStack, component, 10 + oldTotal, getRowTop(index), 0xFFFFFF);
                }
            } catch (Exception e)
            {
//                e.printStackTrace();
            }
        }

        @Override
        protected int getItemCount()
        {
            return lines.size();
        }

        protected void updateLines(String key)
        {
            LimitedSizeQueue<Message> tempMessages;
            int oldMaxScroll = this.getMaxScroll();
            synchronized (ircLock)
            {
                if (ChatHandler.messages == null || ChatHandler.messages.size() == 0) return;
                tempMessages = ChatHandler.messages.get(key);
            }

            ArrayList<FormattedCharSequence> oldLines = lines;
            int listHeight = this.height - (this.bottom - this.top - 4);
            lines = new ArrayList<>();
            if (tempMessages == null)
                return;
            try {
                for (Message message : tempMessages) {
                    Component display = formatLine(message);
                    if (display == null)
                        continue;
                    lines.addAll(ComponentRenderUtils.wrapComponents(display, width - 10, font));
                }
            } catch (Exception ignored) {}
            if (lines.size() > oldLines.size() && this.getScrollAmount() == oldMaxScroll) ;
            {
                this.setScrollAmount(this.getMaxScroll());
            }
        }

        private int getRowBottom(int p_getRowBottom_1_)
        {
            return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
        }

        @Override
        protected boolean isSelectedItem(int i)
        {
            return false;
        }

        @Override
        protected int getScrollbarPosition()
        {
            return width + 4;
        }

        @Override
        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            this.renderBackground(matrixStack);
            int i = this.getScrollbarPosition();
            int j = i + 6;
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tessellator.end();
            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int)this.getScrollAmount();

            ScreenHelpers.drawLogo(matrixStack, font, width - 20, height + 18, 20, 30, 0.75F);
            this.renderList(matrixStack, k, l, mouseX, mouseY, partialTicks);
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int i1 = -100;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tessellator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            int j1 = 4;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tessellator.end();
            int k1 = this.getMaxScroll();
            if (k1 > 0) {
                int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
                int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
                if (i2 < this.y0) {
                    i2 = this.y0;
                }

                bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.end();
            }

            this.renderDecorations(matrixStack, mouseX, mouseY);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        }

        @Override
        protected void renderList(PoseStack poseStack, int p_renderList_1_, int p_renderList_2_, int mouseX, int mouseY, float p_renderList_5_)
        {
            int i = lines.size();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();

            if(!lines.isEmpty())
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
                        bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                        bufferbuilder.vertex((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                        bufferbuilder.vertex((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                        tessellator.end();
                        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                        bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                        bufferbuilder.vertex((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                        bufferbuilder.vertex((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
                        tessellator.end();
                        RenderSystem.enableTexture();
                    }
                    renderEntry(poseStack, j, mouseX, mouseY, p_renderList_5_);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
        {
            for (int i = 0; i < lines.size(); i++)
            {
                FormattedCharSequence component = lines.get(i);
                int totalWidth = 5;
                int oldTotal = totalWidth;
                totalWidth += minecraft.font.width(component);
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(i) && mouseY < getRowTop(i) + itemHeight;

                if (hovering)
                {
                    Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);
                    handleComponentClick(style, mouseX, mouseY);
                    return true;
                }
            }
            return false;
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
}