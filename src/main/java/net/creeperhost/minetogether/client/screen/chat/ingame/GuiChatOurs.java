package net.creeperhost.minetogether.client.screen.chat.ingame;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.chat.ChatConnectionHandler;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.chat.ChatFriendScreen;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.chat.ScreenTextFieldLockable;
import net.creeperhost.minetogether.client.screen.element.DropdownButton;
import net.creeperhost.minetogether.client.screen.element.GuiButtonPair;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.proxy.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

import static net.creeperhost.minetogether.chat.ChatHandler.addStatusMessage;

public class GuiChatOurs extends ChatScreen
{
    private DropdownButton<MTChatScreen.Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButtonPair switchButton;
    private String presetString;
    private boolean sleep;
    private boolean disabledDueToBadwords;
    Minecraft mc = Minecraft.getInstance();
    private boolean first = true;

    public GuiChatOurs(String presetString, boolean sleep)
    {
        super(presetString);
        this.presetString = presetString;
        this.sleep = sleep;
    }
    
    public static boolean isBase()
    {
        NewChatGui chat = Minecraft.getInstance().ingameGUI.getChatGUI();
        return !MineTogether.instance.gdpr.hasAcceptedGDPR() || !(chat instanceof GuiNewChatOurs) || ((GuiNewChatOurs) chat).isBase();
    }

    @Override
    protected void renderComponentHoverEffect(MatrixStack matrixStack, Style style, int mouseX, int mouseY)
    {
        super.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        if (isBase())
        {
            return super.charTyped(typedChar, keyCode);
        }
        
        if (keyCode == 1 && sleep)
        {
            wakeFromSleep();
            return super.charTyped(typedChar, keyCode);
        }
        
        assert inputField instanceof ScreenTextFieldLockable;
        boolean ourEnabled = inputField instanceof ScreenTextFieldLockable && ((ScreenTextFieldLockable) inputField).getOurEnabled();
        
        if (!ourEnabled)
        {
            if ((keyCode == 28 || keyCode == 156) && ((Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()))
                inputField.setEnabled(true);
            return ourEnabled;
        }
        
        super.charTyped(typedChar, keyCode);
        
        if (!ourEnabled)
        {
            inputField.setEnabled(false);
        }
        
        return ourEnabled;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        boolean flag = super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (isBase())
        {
            return flag;
        }
        
        if (menuDropdownButton != null && menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            return true;
        }
        return false;
    }
    
    @Override
    public void sendMessage(String msg, boolean addToChat)
    {
        if (!(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).isBase())
        {
            super.sendMessage(msg, addToChat);
            return;
        }
        if (msg.startsWith("/"))
        {
            super.sendMessage(msg, addToChat);
            ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(true);
            return;
        } else
        {
            msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
            if (msg.isEmpty()) return;
            if (addToChat)
            {
                this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
            }
            if (ChatHandler.isOnline())
            {
                String text = MTChatScreen.getStringForSending(msg);
                String currentTarget = ChatHandler.CHANNEL;
                switch (switchButton.activeButton)
                {
                    case 2:
                        if (ChatHandler.hasGroup)
                        {
                            currentTarget = ChatHandler.currentGroup;
                        }
                        break;
                }
                ChatHandler.sendMessage(currentTarget, text);
            }
        }
    }
    
    @Override
    public void init()
    {
        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
        
        if (MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            ourChat.setBase(Client.chatType == 0);
            if (!ourChat.isBase())
            {
                ourChat.rebuildChat(ChatHandler.CHANNEL);
            }
        } else
        {
            try
            {
                if (Client.chatType == 1)
                {
                    ourChat.setBase(false);
                    ourChat.rebuildChat(ChatHandler.CHANNEL);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        String defaultStr;
        if (mc.getCurrentServerData() != null && (mc.getCurrentServerData().isOnLAN() || !mc.getCurrentServerData().serverIP.equals("127.0.0.1"))) {
            defaultStr = I18n.format("minetogether.ingame.chat.server");
        } else {
            defaultStr = I18n.format("minetogether.ingame.chat.local");
        }
        addToggleButtons(defaultStr);
        
        if (isBase())
        {
            super.init();

            if (sleep)
            {
                addButton(new Button(this.width / 2 - 100, this.height - 40, 60, 20, new StringTextComponent(I18n.format("multiplayer.stopSleeping")), p ->
                {
                    wakeFromSleep();
                }));
            }
            return;
        }
        
        if (!presetString.isEmpty())
        {
            if (Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
            {
                ourChat.setBase(true);
            }
        }
        super.init();
        children.remove(inputField);
        this.inputField = new ScreenTextFieldLockable(mc.fontRenderer, 4, this.height - 12, this.width - 4, 12, "");
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused2(true);
        this.inputField.setCanLoseFocus(false);
        this.inputField.setResponder(this::func_212997_a);
        children.add(inputField);
        this.commandSuggestionHelper = new CommandSuggestionHelperMT(this.minecraft, this, this.inputField, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestionHelper.init();
        setFocusedDefault(inputField);
        List<String> strings = new ArrayList<>();
        
        strings.add(I18n.format("minetogether.chat.button.mute"));
        strings.add(I18n.format("minetogether.chat.button.addfriend"));
        strings.add(I18n.format("minetogether.chat.button.mention"));
        if(MineTogether.instance.isBanned.get() && Client.first)
        {
            addStatusMessage(TextFormatting.RED + "You have been banned from Minetogether chat");
            addStatusMessage(TextFormatting.RED + "Ban Reason: " + TextFormatting.WHITE + Callbacks.getBanMessage());
            addStatusMessage(TextFormatting.RED + "Ban ID: " + TextFormatting.WHITE + Callbacks.banID);
            addStatusMessage("If you feel like this was a mistake please open a ban appeal on our github with your ban ID " + "https://github.com/CreeperHost/CreeperHostGui/issues");
            Client.first = false;
            ChatConnectionHandler.INSTANCE.disconnect();
        }
        
        addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new StringTextComponent("Menu"), new MTChatScreen.Menu(strings), true, p ->
        {
            if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mute")))
            {
                MineTogether.instance.muteUser(activeDropdown);
                ourChat.rebuildChat(ourChat.chatTarget);
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setChatLine(null, new StringTextComponent(I18n.format("minetogether.chat.muted")), 0, 5, false);
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.addfriend")))
            {
                Profile profile = ChatHandler.knownUsers.findByDisplay(activeDropdown);
                mc.displayGuiScreen(new ChatFriendScreen(this, mc.getSession().getUsername(), profile, Callbacks.getFriendCode(), "", false));
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mention")))
            {
                inputField.setFocused2(true);
                inputField.setText(inputField.getText() + " " + activeDropdown + " ");
            }
        }));
        menuDropdownButton.flipped = false;
        if (sleep)
        {
            addButton(new Button(this.width / 2 - 100, this.height - 40, 60, 20, new StringTextComponent(I18n.format("multiplayer.stopSleeping")), p -> wakeFromSleep()));
        }
    }
    
    public void addToggleButtons(String defaultString)
    {
        int x = MathHelper.ceil(((float) mc.ingameGUI.getChatGUI().getChatWidth())) + 16 + 2;
        
        if (ChatHandler.hasGroup)
        {
            addButton(switchButton = new GuiButtonPair(x, height - 215, 234, 16, !MineTogether.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).isBase() ? 0 : ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).chatTarget.equals(ChatHandler.CHANNEL) ? 1 : 2, false, false, true, p ->
            {
                if (MineTogether.instance.gdpr.hasAcceptedGDPR())
                {
                    GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                    ourChat.setBase(switchButton.activeButton == 0);
                    if (!ourChat.isBase())
                    {
                        Client.chatType = 0;
                        ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.currentGroup);
                    }
                    switchButton.setMessage(ourChat.isBase() ? new StringTextComponent("MineTogether Chat") : new StringTextComponent("Minecraft Chat"));
                } else
                {
                    try
                    {
                        Minecraft.getInstance().displayGuiScreen(new GDPRScreen(null, () ->
                        {
                            GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                            if (switchButton.activeButton == 1)
                            {
                                Client.chatType = 1;
                                ourChat.setBase(false);
                                ourChat.rebuildChat(ChatHandler.CHANNEL);
                            }
                            return new GuiChatOurs(presetString, sleep);
                        }));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, defaultString, I18n.format("minetogether.ingame.chat.global"), I18n.format("minetogether.ingame.chat.group")));
        } else
        {
            addButton(switchButton = new GuiButtonPair(x, height - 156, 156, 16, Client.chatType, false, false, true, p ->
            {
                if (MineTogether.instance.gdpr.hasAcceptedGDPR())
                {
                    GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                    ourChat.setBase(switchButton.activeButton == 0);
                    if (!ourChat.isBase())
                    {
                        ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.currentGroup);
                    }
                    switchButton.setMessage(ourChat.isBase() ? new StringTextComponent("MineTogether Chat") : new StringTextComponent("Minecraft Chat"));
                } else
                {
                    try
                    {
                        Minecraft.getInstance().displayGuiScreen(new GDPRScreen(null, () ->
                        {
                            GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                            if (switchButton.activeButton == 1)
                            {
                                ourChat.setBase(false);
                                ourChat.rebuildChat(ChatHandler.CHANNEL);
                            }
                            return new GuiChatOurs(presetString, sleep);
                        }));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, defaultString, I18n.format("minetogether.ingame.chat.global")));
        }
    }

    @Override
    public void tick()
    {
        if (sleep && !mc.player.isSleeping())
        {
            mc.displayGuiScreen(null);
        }
        super.tick();
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (isBase())
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            return;
        }

        this.buttons.forEach(p -> p.render(matrixStack, mouseX, mouseY, partialTicks));
        this.setFocusedDefault(this.inputField);

        this.inputField.setFocused2(true);
        fill(matrixStack, 2, this.height - 14, this.width - 2, this.height - 2, mc.gameSettings.getChatBackgroundColor(-2147483648));
        this.inputField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.inputField.canWrite();
        //render
        this.commandSuggestionHelper.func_238500_a_(matrixStack, mouseX, mouseY);
        
        if (!(this.mc.ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;
        
        GuiNewChatOurs chatGui = (GuiNewChatOurs) mc.ingameGUI.getChatGUI();
        if ((!chatGui.isBase()) && (!chatGui.chatTarget.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase())) && (!chatGui.chatTarget.toLowerCase().contains(ChatHandler.CHANNEL.toLowerCase())) && (chatGui.chatTarget.length() > 0) && (!chatGui.chatTarget.toLowerCase().equals("#minetogether")))
        {
            String str = chatGui.closeComponent.getString();
            int x = mc.ingameGUI.getChatGUI().getChatWidth() - 2;
            int y = height - 40 - (mc.fontRenderer.FONT_HEIGHT * Math.max(Math.min(chatGui.drawnChatLines.size(), chatGui.getLineCount()), 20));
            mc.fontRenderer.drawString(matrixStack, str, x, y, 0xFFFFFF);
        }
    }
    
    private void wakeFromSleep()
    {
        ClientPlayNetHandler nethandlerplayclient = this.mc.player.connection;
        nethandlerplayclient.sendPacket(new CEntityActionPacket(this.mc.player, CEntityActionPacket.Action.STOP_SLEEPING));
    }

    @Override
    public boolean handleComponentClicked(Style style)
    {
        if (isBase())
        {
            return super.handleComponentClicked(style);
        }

        if (style == ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).closeComponent)
        {
            MineTogether.instance.closeGroupChat();
            return true;
        }
        ClickEvent event = style.getClickEvent();
        if (event == null) return false;
        if (menuDropdownButton.dropdownOpen) return false;
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
                
                Minecraft.getInstance().displayGuiScreen(new ChatFriendScreen(this, mc.getSession().getUsername(), targetProfile, friendCode, friendName, true));
                
                return true;
            }

            if(menuDropdownButton == null) return false;

            menuDropdownButton.x = (int) (mc.mouseHelper.getMouseX() * (this.height / this.mc.getMainWindow().getWidth())) +28;
            menuDropdownButton.y = (int) mc.mouseHelper.getMouseY() * this.height / this.mc.getMainWindow().getHeight();
            menuDropdownButton.dropdownOpen = true;
            menuDropdownButton.flipped = true;
            activeDropdown = event.getValue();
            return true;
        }
        return super.handleComponentClicked(style);
    }
}
