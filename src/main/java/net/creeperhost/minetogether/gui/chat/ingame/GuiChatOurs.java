package net.creeperhost.minetogether.gui.chat.ingame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.data.Profile;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.chat.GuiChatFriend;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.chat.GuiTextFieldLockable;
import net.creeperhost.minetogether.gui.chat.TimestampComponentString;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.element.FancyButton;
import net.creeperhost.minetogether.gui.element.GuiButtonPair;
import net.creeperhost.minetogether.irc.IrcHandler;
import net.creeperhost.minetogether.misc.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GuiChatOurs extends GuiChat
{
    private DropdownButton<GuiMTChat.Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButtonPair switchButton;
    private String presetString;
    private boolean sleep;
    private boolean disabledDueToBadwords;

    public static boolean isBase()
    {
        GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        return !CreeperHost.instance.gdpr.hasAcceptedGDPR() || !(chat instanceof GuiNewChatOurs) || ((GuiNewChatOurs) chat).isBase();
    }

    @Override
    protected void handleComponentHover(ITextComponent component, int x, int y)
    {
        if (component != null && component.getStyle().getHoverEvent() != null)
        {
            HoverEvent event = component.getStyle().getHoverEvent();
            if (event.getAction() == CreeperHost.instance.TIMESTAMP && !menuDropdownButton.dropdownOpen)
            {
                List<ITextComponent> siblings = ((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).getBaseChatComponent(Mouse.getX(), Mouse.getY()).getSiblings();
                for(ITextComponent sibling: siblings)
                {
                    if (sibling instanceof TimestampComponentString)
                    {
                        ((TimestampComponentString)sibling).setActive();
                    }
                }
            }
        }
        super.handleComponentHover(component, x, y);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1 && sleep)
        {
            wakeFromSleep();
            super.keyTyped(typedChar, keyCode);
            return;
        }

        if ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && ((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()) {
            inputField.setEnabled(true);
            super.keyTyped(typedChar, keyCode);
            return;
        }

        boolean ourEnabled = ((GuiTextFieldLockable)inputField).getOurEnabled();

        if (!ourEnabled)
        {
            if ((keyCode == 28 || keyCode == 156) && ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()))
                return;
            inputField.setEnabled(true);
        }

        super.keyTyped(typedChar, keyCode);

        if (!ourEnabled)
        {
            inputField.setEnabled(false);
        }
    }
    
    public GuiChatOurs(String presetString, boolean sleep)
    {
        super(presetString);
        this.presetString = presetString;
        this.sleep = sleep;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        TimestampComponentString.setFakeActive(true);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        TimestampComponentString.setFakeActive(false);
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.xPosition = menuDropdownButton.yPosition = -10000;
            menuDropdownButton.wasJustClosed = false;
        }
    }

    @Override
    public void sendChatMessage(String msg, boolean addToChat)
    {
        if (!(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).isBase())
        {
            super.sendChatMessage(msg, addToChat);
            return;
        }
        if (msg.startsWith("/"))
        {
            super.sendChatMessage(msg, addToChat);
            ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setBase(true);
            return;
        }
        else
        {
            msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
            if (msg.isEmpty()) return;
            if (addToChat)
            {
                this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
            }
            if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0)
            {
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setBase(true);
                return;
            }
            if (ChatHandler.isOnline()) {
                String text = GuiMTChat.getStringForSending(msg);
                //ChatHandler.sendMessage(ChatHandler.CHANNEL, text);
                String currentTarget = ChatHandler.CHANNEL;
                switch (switchButton.activeButton)
                {
                    case 2:
                        if (ChatHandler.hasGroup) {
                            currentTarget = ChatHandler.currentGroup;
                        }
                    break;

                }
                ChatHandler.sendMessage(currentTarget, text);
            } else if (ChatHandler.isBanned()) {
                ChatHandler.onNotice("System", "Unable to send message as you are banned. Please open the main MineTogether chat from the pause menu to appeal.");
            }
        }
    }

    private Field tabCompleterField = null;
    public void replaceTabCompleter()
    {
        if (tabCompleterField == null) {
            tabCompleterField = ReflectionHelper.findField(GuiChat.class, "tabCompleter", "field_184096_i", "");
            tabCompleterField.setAccessible(true);
        }

        try {
            tabCompleterField.set(this, new OurChatTabCompleter(inputField));
        } catch (IllegalAccessException e) {
        }

    }

    private GuiButton newUserButton;
    private GuiButton disableButton;

    private String userCount = "over 2 million";
    private String onlineCount = "thousands of";
    
    @Override
    public void initGui()
    {
        GuiNewChatOurs ourChat = null;

        if (!presetString.isEmpty())
        {
            if (Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
            {
                ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.setBase(true);
            }
        }
        super.initGui();

        if(Config.getInstance().getFirstConnect())
        {
            CompletableFuture.runAsync(() -> {
                if(onlineCount.equals("thousands of")) {
                    String statistics = WebUtils.getWebResponse("https://minetogether.io/api/stats/all");
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    HashMap<String, String> stats = gson.fromJson(statistics, HashMap.class);
                    String users = stats.get("users");
                    if (users != null && users.length() > 4) {
                        userCount = users;
                    }
                    String online = stats.get("online");
                    if (online != null && !online.equalsIgnoreCase("null")) {
                        onlineCount = online;
                    }
                }
                }, CreeperHost.otherExecutor);

                addButton(newUserButton = new FancyButton(776, 6, height - ((mc.ingameGUI.getChatGUI().getChatHeight()+80)/2)+45, mc.ingameGUI.getChatGUI().getChatWidth(), 20, "Join " + onlineCount + " online users now!", p ->
                {
                    IrcHandler.sendCTCPMessage("Freddy", "ACTIVE", "");
                    Config.getInstance().setFirstConnect(false);
                    newUserButton.visible = false;
                    disableButton.visible = false;
                    this.mc.displayGuiScreen(null);
                }));
                addButton(disableButton = new FancyButton(777, 6, height - ((mc.ingameGUI.getChatGUI().getChatHeight()+80)/2)+70, mc.ingameGUI.getChatGUI().getChatWidth(), 20, "Don't ask me again", p ->
                {
                    Config.getInstance().setChatEnabled(false);
                    CreeperHost.proxy.disableIngameChat();
                    disableButton.visible = false;
                    newUserButton.visible = false;
                    IrcHandler.stop(true);
                    buttonList.clear();
                }));
                if(isBase()) {
                    newUserButton.visible = false;
                    disableButton.visible = false;
                }
        }
        GuiTextField oldInputField = this.inputField;
        this.inputField = new GuiTextFieldLockable(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(oldInputField.getText());
        this.inputField.setCanLoseFocus(false);
        replaceTabCompleter();
        List<String> strings = new ArrayList<>();

        strings.add(I18n.format("minetogether.chat.button.mute"));
        strings.add(I18n.format("minetogether.chat.button.addfriend"));
        strings.add(I18n.format("minetogether.chat.button.mention"));

        int x = MathHelper.ceil(((float) mc.ingameGUI.getChatGUI().getChatWidth())) + 16 + 2;
        String defaultStr = "Default";
        defaultStr = I18n.format("minetogether.ingame.chat.local");
        try {
            if (mc.getCurrentServerData().isOnLAN() || (!mc.getCurrentServerData().serverIP.equals("127.0.0.1"))) {
                defaultStr = I18n.format("minetogether.ingame.chat.server");
            }
        } catch(NullPointerException err){}//Who actually cares? If getCurrentServerData() is a NPE then we've got our answer anyway.
        if(ChatHandler.hasGroup) {
        //if(true) {
            buttonList.add(switchButton = new GuiButtonPair(808, Config.getInstance().isLeft() ? x : 12, height - 215, 234, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).isBase() ? 0 : ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).chatTarget.equals(ChatHandler.CHANNEL) ? 1 : 2, false, false, true, !Config.getInstance().isLeft(), defaultStr, I18n.format("minetogether.ingame.chat.global"), I18n.format("minetogether.ingame.chat.group")));
        } else {
            buttonList.add(switchButton = new GuiButtonPair(808, Config.getInstance().isLeft() ? x : 12, height - 156, 156, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).isBase() ? 0 : 1, false, false, true, !Config.getInstance().isLeft(), defaultStr, I18n.format("minetogether.ingame.chat.global")));
        }
        buttonList.add(menuDropdownButton = new DropdownButton<>(-1337, -1000, -1000, 100, 20, "Menu", new GuiMTChat.Menu(strings), true));
        menuDropdownButton.flipped = true;
        if (sleep)
        {
            buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 40, I18n.format("multiplayer.stopSleeping")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button instanceof FancyButton)
        {
            ((FancyButton) button).onPress();
        }
        if (button == menuDropdownButton)
        {
            if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mute")))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.rebuildChat(ourChat.chatTarget);
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setChatLine(null, new TextComponentString(I18n.format("minetogether.chat.muted")), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.addfriend")))
            {
                Profile p = ChatHandler.knownUsers.findByDisplay(activeDropdown);
                mc.displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), p, Callbacks.getFriendCode(), "", false));
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mention")))
            {
                inputField.setFocused(true);
                inputField.setText(inputField.getText() + " " + activeDropdown + " ");
            }
            return;
        }
        else if (button == switchButton)
        {
            if (CreeperHost.instance.gdpr.hasAcceptedGDPR())
            {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.setBase(switchButton.activeButton == 0);
                if (!ourChat.isBase()) {
                    ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.currentGroup);//ChatHandler.privateChatList.getChannelname());
//                    ChatHandler.sendActive();
                }
                switchButton.displayString = ourChat.isBase() ? "MineTogether Chat" : "Minecraft Chat";
            }
            else {
                try {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiGDPR(null, () ->
                    {
                        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                        if(switchButton.activeButton == 1) {
                            ourChat.setBase(false);
                            ourChat.rebuildChat(ChatHandler.CHANNEL);
                        }
                        return new GuiChatOurs(presetString, sleep);
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        else if (sleep && button.id == 1)
        {
            wakeFromSleep();
            return;
        }
        super.actionPerformed(button);
    }
    
    @Override
    public void updateScreen()
    {
        if (sleep && !mc.player.isPlayerSleeping())
        {
            mc.displayGuiScreen(null);
        }
        super.updateScreen();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        for (int j = 0; j < this.labelList.size(); ++j)
        {
            this.labelList.get(j).drawLabel(this.mc, mouseX, mouseY);
        }

        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();

        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (!(this.mc.ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;

        GuiNewChatOurs chatGui = (GuiNewChatOurs) mc.ingameGUI.getChatGUI();
        if ((!chatGui.isBase()) && (!chatGui.chatTarget.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase())) && (!chatGui.chatTarget.toLowerCase().contains(ChatHandler.CHANNEL.toLowerCase())) && (chatGui.chatTarget.length() > 0)&&(!chatGui.chatTarget.toLowerCase().equals("#minetogether")))
        {
            String str = chatGui.closeComponent.getFormattedText();
            int x = mc.ingameGUI.getChatGUI().getChatWidth() - 2;
            int y = height - 40 - (mc.fontRendererObj.FONT_HEIGHT * Math.max(Math.min(chatGui.drawnChatLines.size(), chatGui.getLineCount()), 20));
            mc.fontRendererObj.drawString(str, x, y, 0xFFFFFF);
        }

        if (!((GuiTextFieldLockable)inputField).getOurEnabled() && ((GuiTextFieldLockable)inputField).isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(((GuiTextFieldLockable)inputField).getDisabledMessage()), mouseX, mouseY);
        }

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
            if (!TimestampComponentString.getChanged() && !menuDropdownButton.dropdownOpen)
                TimestampComponentString.clearActive();
        } else {
            if(!menuDropdownButton.dropdownOpen)
            {
                TimestampComponentString.clearActive();
            }
        }

        if(Config.getInstance().getFirstConnect())
        {
            if(newUserButton != null)
            {
                newUserButton.visible = true;
                disableButton.visible = true;
            }

            int y = height - 40 - (fontRendererObj.FONT_HEIGHT * Math.max(Math.min(chatGui.drawnChatLines.size(), chatGui.getLineCount()), 20));

            drawGradientRect( 0, y, chatGui.getChatWidth() + 6, chatGui.getChatHeight() + y, 0x99000000, 0x99000000);

            drawCenteredString(fontRendererObj, "Welcome to MineTogether", (chatGui.getChatWidth()/2)+3, height - ((chatGui.getChatHeight()+80)/2), 0xFFFFFF);
            drawCenteredString(fontRendererObj, "MineTogether is a multiplayer enhancement mod that provides", (chatGui.getChatWidth()/2)+3, height - ((chatGui.getChatHeight()+80)/2)+10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "a multitude of features like chat, friends list, server listing", (chatGui.getChatWidth()/2)+3, height - ((chatGui.getChatHeight()+80)/2)+20, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "and more. Join "+userCount+" unique users.", (chatGui.getChatWidth() / 2)+3, height - ((chatGui.getChatHeight()+80)/2)+30, 0xFFFFFF);
        }

        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            this.buttonList.get(i).func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
    }

    @Deprecated
    public void drawLogo()
    {
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation(CreeperHost.MOD_ID, "textures/creeperhost25.png");
        ResourceLocation resourceLocationMinetogetherLogo = new ResourceLocation(CreeperHost.MOD_ID, "textures/minetogether25.png");

        GL11.glPushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        GL11.glEnable(GL11.GL_BLEND);
        Gui.drawModalRectWithCustomSizedTexture(-8, this.height - 80, 0.0F, 0.0F, 40, 40, 40F, 40);

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationMinetogetherLogo);
        Gui.drawModalRectWithCustomSizedTexture(this.width / 2 - 160, this.height - 155, 0.0F, 0.0F, 160, 120, 160F, 120F);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    
    private void wakeFromSleep()
    {
        NetHandlerPlayClient nethandlerplayclient = this.mc.player.connection;
        nethandlerplayclient.sendPacket(new CPacketEntityAction(this.mc.player, CPacketEntityAction.Action.STOP_SLEEPING));
    }
    
    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        if (!(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).isBase())
        {
            return super.handleComponentClick(component);
        }

        if(menuDropdownButton != null && menuDropdownButton.dropdownOpen)
        {
            return false;
        }

        if(component == ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).closeComponent)
        {
            CreeperHost.instance.closeGroupChat();
            return true;
        }
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

                Profile targetProfile = ChatHandler.knownUsers.findByNick(chatInternalName);
                if(targetProfile == null) targetProfile = ChatHandler.knownUsers.add(chatInternalName);
                
                Minecraft.getMinecraft().displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), targetProfile, friendCode, friendName, true));
                
                return true;
            }
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            menuDropdownButton.xPosition = mouseX;
            menuDropdownButton.yPosition = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        return super.handleComponentClick(component);
    }

    public static class OurChatTabCompleter extends ChatTabCompleter
    {
        private String[] ourCompletions = new String[0];
        private boolean replace = false;

        @Override
        public void setCompletions(String... newCompl) {
            String[] ret;
            if (!replace)
                ret = ArrayUtils.addAll(newCompl, ourCompletions);
            else
                ret = ourCompletions;
            super.setCompletions(ret);
        }

        @Override
        public void complete() {
            GuiNewChatOurs.tabCompletion = true;
            if (!didComplete)
                prepareCompletions();
            super.complete();
            GuiNewChatOurs.tabCompletion = false;
        }

        private void prepareCompletions() {
            String text = textField.getText();
            String[] words = text.split(" ");
            int length = words.length;
            String lastWord = length == 0 ? "" : words[words.length - 1];
            ourCompletions = new String[0];

            if (text.startsWith("/")) {
                replace = false;
            } else {
                if (!isBase()) {
                    //TODO fix this
//                    ourCompletions = ChatHandler.getOnlineUsers().stream().filter(name -> ChatHandler.knownUsers.findByDisplay(name) != null || ChatHandler.friends.containsKey(name)).map(s -> CreeperHost.instance.getNameForUser(s)).filter(nick -> nick.toLowerCase().startsWith(lastWord.toLowerCase())).toArray(String[]::new);
                    replace = true;
                }
            }
        }

        public OurChatTabCompleter(GuiTextField p_i46749_1_) {
            super(p_i46749_1_);
        }
    }
}
