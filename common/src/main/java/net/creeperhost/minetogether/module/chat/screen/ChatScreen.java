package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.ScrollingChat;
import net.creeperhost.minetogether.module.chat.Target;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonNoBlend;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatConnectionStatus;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.chat.irc.IrcHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.ircLock;

public class ChatScreen extends MineTogetherScreen
{
    private final Screen parent;
    private ScrollingChat chat;
    private EditBox send;
    private String currentTarget = ChatHandler.CHANNEL;
    private ButtonString connectionStatus;
    public DropdownButton<Target> targetDropdownButton;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private Button newUserButton;
    private Button disableButton;
    private Button friendsList;

    public ChatScreen(Screen parent)
    {
        super(new TranslatableComponent("MineTogether Chat"));
        this.parent = parent;
    }

    public ChatScreen(Screen parent, String currentTarget)
    {
        super(new TranslatableComponent("MineTogether Chat"));
        this.parent = parent;
        this.currentTarget = currentTarget;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        chat = new ScrollingChat(this, width, height, 13);
        chat.setLeftPos(10);
        chat.updateLines(currentTarget);
        send = new EditBox(minecraft.font, 11, this.height - 48, width - 22, 20, new TranslatableComponent(""));
        send.setFocus(true);
        send.setMaxLength(256);

        addButtons();
        super.init();
    }

    public void addButtons()
    {
        addButton(targetDropdownButton = new DropdownButton<>(width - 5 - 100, 5, 100, 20, new TranslatableComponent("Chat: %s"), Target.getMainTarget(), true, p ->
        {
            if (!targetDropdownButton.dropdownOpen) return;
            if (!targetDropdownButton.getSelected().getInternalTarget().equals(currentTarget))
                currentTarget = targetDropdownButton.getSelected().getInternalTarget();

            chat.updateLines(currentTarget);

            targetDropdownButton.wasJustClosed = false;
            targetDropdownButton.dropdownOpen = false;
        }));
        targetDropdownButton.setSelected(Target.getMainTarget());
        List<String> strings = new ArrayList<>();
        strings.add(I18n.get("minetogether.chat.button.mute"));
        strings.add(I18n.get("minetogether.chat.button.addfriend"));
        strings.add(I18n.get("minetogether.chat.button.mention"));
        addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new TranslatableComponent("Menu"), new Menu(strings), false, p ->
        {
            if (!menuDropdownButton.dropdownOpen) return;

            if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.mute")))
            {
                Profile profile = KnownUsers.findByDisplay(activeDropdown);
                if (profile != null)
                {
                    ChatModule.muteUser(KnownUsers.findByDisplay(activeDropdown).longHash);
                    KnownUsers.findByDisplay(activeDropdown).setMuted(true);
                    ChatHandler.addStatusMessage("Locally blocked " + currentTarget);
                }
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.addfriend")))
            {
                minecraft.setScreen(new FriendRequestScreen(new ChatScreen(parent), Minecraft.getInstance().getUser().getName(), KnownUsers.findByDisplay(activeDropdown), ChatCallbacks.getFriendCode(MineTogetherClient.getUUID()), "", false));
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.mention")))
            {
                this.send.setFocus(true);
                this.send.setValue(this.send.getValue() + " " + activeDropdown + " ");
            }
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            menuDropdownButton.dropdownOpen = false;
        }));

        addButton(friendsList = new Button(5, 5, 100, 20, new TranslatableComponent("Friends list"), p ->
        {
            this.minecraft.setScreen(new FriendsListScreen(this));
        }));
        addButton(new ButtonMultiple(width - 124, 5, 3, Constants.WIDGETS_LOCATION, p ->
        {
            this.minecraft.setScreen(new SettingsScreen(this));
        }));
        addButton(new Button(width - 100 - 5, height - 5 - 20, 100, 20, new TranslatableComponent("Cancel"), p ->
        {
            this.minecraft.setScreen(parent);
        }));
        addButton(connectionStatus = new ButtonString(8, height - 20, 70, 20, () ->
        {
            ChatConnectionStatus status = ChatHandler.connectionStatus;
            return new TranslatableComponent(ChatFormatting.getByName(status.colour) + "\u2022" + " " + ChatFormatting.WHITE + status.display);
        }, ButtonString.RenderPlace.EXACT, button ->
        {
            if (ChatHandler.connectionStatus == ChatConnectionStatus.BANNED)
            {
                ConfirmScreen confirmScreen = new ConfirmScreen(t ->
                {
                    if (t)
                    {
                        try
                        {
                            Util.getPlatform().openUrl(new URL("https://minetogether.io/profile/standing"));
                        } catch (MalformedURLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    minecraft.setScreen(this);
                }, new TranslatableComponent("minetogether.bannedscreen.line1"), new TranslatableComponent("minetogether.bannedscreen.line2"));

                minecraft.setScreen(confirmScreen);
            }
        }));

        if (Config.getInstance().getFirstConnect())
        {
            ChatCallbacks.updateOnlineCount();

            addButton(newUserButton = new ButtonNoBlend(width / 2 - 150, 75 + (height / 4), 300, 20, new TranslatableComponent("Join " + ChatCallbacks.onlineCount + " online users now!"), p ->
            {
                IrcHandler.sendCTCPMessage("Freddy", "ACTIVE", "");
                Config.getInstance().setFirstConnect(false);
                newUserButton.visible = false;
                disableButton.visible = false;
                minecraft.setScreen(this);
            }));
            addButton(disableButton = new ButtonNoBlend(width / 2 - 150, 95 + (height / 4), 300, 20, new TranslatableComponent("Don't ask me again"), p ->
            {
                Config.getInstance().setChatEnabled(false);
                disableButton.visible = false;
                newUserButton.visible = false;
                IrcHandler.stop(true);
                buttons.clear();
                minecraft.setScreen(parent);
            }));
        }
        IrcHandler.sendCTCPMessage("Freddy", "ACTIVE", "");
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

        if (Config.getInstance().getFirstConnect())
        {
            fill(poseStack, 10, chat.getTop(), width - 10, chat.getHeight(), 0x99000000);
            fill(poseStack, 10, chat.getTop(), width - 10, chat.getHeight(), 0x99000000);

            RenderSystem.blendColor(1F, 1F, 1F, 1F); // reset alpha as font renderer isn't nice like that
            drawCenteredString(poseStack, font, "Welcome to MineTogether", width / 2, (height / 4) + 25, 0xFFFFFF);
            drawCenteredString(poseStack, font, "MineTogether is a multiplayer enhancement mod that provides", width / 2, (height / 4) + 35, 0xFFFFFF);
            drawCenteredString(poseStack, font, "a multitude of features like chat, friends list, server listing", width / 2, (height / 4) + 45, 0xFFFFFF);
            drawCenteredString(poseStack, font, "and more. Join " + ChatCallbacks.userCount + " unique users.", width / 2, (height / 4) + 55, 0xFFFFFF);
        }

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
        chat.tick();
        String buttonTarget = targetDropdownButton.getSelected().getInternalTarget();
        if (!buttonTarget.equals(currentTarget)) currentTarget = buttonTarget;

        friendsList.active = !Config.getInstance().getFirstConnect();
        targetDropdownButton.active = !Config.getInstance().getFirstConnect();

        send.active = ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED;
        send.setEditable(ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED);
        //Remove focus if the client is not verified
        if (send.isFocused() && ChatHandler.connectionStatus != ChatConnectionStatus.VERIFIED)
        {
            send.setFocus(false);
        }

        switch (ChatHandler.connectionStatus)
        {
            case VERIFYING:
                send.setSuggestion(I18n.get("minetogether.chat.message.unverified"));
                break;
            case BANNED:
                send.setSuggestion(I18n.get("minetogether.chat.message.banned"));
                break;
            case DISCONNECTED:
                send.setSuggestion(I18n.get("minetogether.chat.message.disconnect"));
                break;
            case CONNECTING:
                send.setSuggestion(I18n.get("minetogether.chat.message.connecting"));
                break;
            case VERIFIED:
                send.setSuggestion("");
                break;
        }

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
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) return true;

        if (send.mouseClicked(mouseX, mouseY, mouseButton))
        {
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
        return false;
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
            ChatHandler.sendMessage(currentTarget, ChatFormatter.getStringForSending(send.getValue()));
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

    @Override
    public boolean handleComponentClicked(@Nullable Style style, double mouseX, double mouseY)
    {
        //Don't allow component clicks while this button is visible
        if (newUserButton != null && newUserButton.visible) return false;

        if (style == null) return false;
        if (style.getClickEvent() == null) return false;
        ClickEvent event = style.getClickEvent();
        if (event == null) return false;

        if (event.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
        {
            String eventValue = event.getValue();
            if (eventValue.contains(":"))
            {
                String[] split = eventValue.split(":");
                if (split.length < 3) return false;

                String chatInternalName = split[1];

                String friendCode = split[2];

                StringBuilder builder = new StringBuilder();

                for (int i = 3; i < split.length; i++)
                    builder.append(split[i]).append(" ");

                String friendName = builder.toString().trim();

                Profile targetProfile = KnownUsers.findByNick(chatInternalName);
                if (targetProfile == null) targetProfile = KnownUsers.add(chatInternalName);

                Minecraft.getInstance().setScreen(new FriendRequestScreen(this, Minecraft.getInstance().getUser().getName(), targetProfile, friendCode, friendName, true));
                return true;
            }

            menuDropdownButton.x = (int) mouseX;
            menuDropdownButton.y = (int) mouseY;
            menuDropdownButton.flipped = mouseY > 150;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        if (event.getAction() == ClickEvent.Action.OPEN_URL)
        {
            this.handleComponentClicked(style);
        }
        return false;
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
