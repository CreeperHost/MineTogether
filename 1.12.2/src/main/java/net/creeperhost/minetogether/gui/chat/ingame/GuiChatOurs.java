package net.creeperhost.minetogether.gui.chat.ingame;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.chat.GuiChatFriend;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.chat.GuiTextFieldLockable;
import net.creeperhost.minetogether.gui.chat.TimestampComponentString;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.element.GuiButtonPair;
import net.creeperhost.minetogether.paul.Callbacks;
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
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiChatOurs extends GuiChat
{
    private DropdownButton<GuiMTChat.Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButtonPair switchButton;
    private String presetString;
    private boolean sleep;
    private boolean disabledDueToBadwords;

    @Override
    protected void handleComponentHover(ITextComponent component, int x, int y) {
        if (component != null && component.getStyle().getHoverEvent() != null)
        {
            HoverEvent event = component.getStyle().getHoverEvent();
            if (event.getAction() == CreeperHost.instance.TIMESTAMP)
            {
                List<ITextComponent> siblings = ((GuiNewChatOurs)mc.ingameGUI.getChatGUI()).getBaseChatComponent(Mouse.getX(), Mouse.getY()).getSiblings();
                for(ITextComponent sibling: siblings) {
                    if (sibling instanceof TimestampComponentString)
                    {
                        ((TimestampComponentString)sibling).setActive(true);
                    }
                }
            }
        }
        super.handleComponentHover(component, x, y);
    }

    public void processBadwords()
    {
        if (ChatHandler.badwordsFormat == null)
            return;
        String text = inputField.getText().replaceAll(ChatHandler.badwordsFormat, "");
        boolean veryNaughty = false;
        if (ChatHandler.badwords != null)
        {
            for (String bad : ChatHandler.badwords)
            {
                if (bad.startsWith("(") && bad.endsWith(")"))
                {
                    if (text.matches(bad))
                    {
                        veryNaughty = true;
                        break;
                    }
                }
                if (text.toLowerCase().contains(bad.toLowerCase()))
                {
                    veryNaughty = true;
                    break;
                }
            }
        }

        if ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && ((GuiNewChatOurs)mc.ingameGUI.getChatGUI()).base)
            veryNaughty = false;

        if (veryNaughty)
        {
            ((GuiTextFieldLockable)inputField).setDisabled("Cannot send message as contains content which may not be suitable for all audiences");
            disabledDueToBadwords = true;
            return;
        }                                                                                                                                             

        if (disabledDueToBadwords)
        {
            ((GuiTextFieldLockable)inputField).setDisabled("");
            disabledDueToBadwords = false;
            inputField.setEnabled(true);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            if (sleep)
                this.wakeFromSleep();
            else
            {
                boolean ourEnabled = ((GuiTextFieldLockable)inputField).getOurEnabled();

                if (!ourEnabled)
                {
                    inputField.setEnabled(true);
                }

                super.keyTyped(typedChar, keyCode);

                if (!ourEnabled)
                {
                    inputField.setEnabled(false);
                }

                if ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs)mc.ingameGUI.getChatGUI()).base) processBadwords();

            }
        } else
        {
            boolean ourEnabled = ((GuiTextFieldLockable)inputField).getOurEnabled();

            if (!ourEnabled)
            {
                if ((keyCode == 28 || keyCode == 156) && ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs)mc.ingameGUI.getChatGUI()).base))
                    return;
                inputField.setEnabled(true);
            }

            super.keyTyped(typedChar, keyCode);

            if (!ourEnabled)
            {
                inputField.setEnabled(false);

            }
            if ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs)mc.ingameGUI.getChatGUI()).base) processBadwords();
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.xPosition = menuDropdownButton.yPosition = -10000;
            menuDropdownButton.wasJustClosed = false;
        }
    }
    
    final Pattern pattern = Pattern.compile("((?:user)?(\\d+))", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    
    @Override
    public void sendChatMessage(String msg, boolean addToChat)
    {
        if (!(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base)
        {
            super.sendChatMessage(msg, addToChat);
            return;
        }
        if (msg.startsWith("/"))
        {
            super.sendChatMessage(msg, addToChat);
            ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base = true;
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
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base = true;
                return;
            }
            if (ChatHandler.isOnline())
            {
                String text = msg;
                String[] split = text.split(" ");
                for (int i = 0; i < split.length; i++)
                {
                    String word = split[i].toLowerCase();
                    final String subst = "User$2";
                    
                    final Matcher matcher = pattern.matcher(word);
                    
                    final String result = matcher.replaceAll(subst);
                    
                    String justNick = result.replaceAll("[^A-Za-z0-9]", "");
                    
                    String tempWord = ChatHandler.anonUsersReverse.get(justNick);
                    if (tempWord != null)
                        split[i] = result.replaceAll(justNick, tempWord);
                }
                
                text = String.join(" ", split);
                ChatHandler.sendMessage(ChatHandler.CHANNEL, text);
            }
        }
    }
    
    @Override
    public void initGui()
    {
        if (!presetString.isEmpty())
        {
            if (Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
            {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.base = true;
            }
        }
        super.initGui();
        GuiTextField oldInputField = this.inputField;
        this.inputField = new GuiTextFieldLockable(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(oldInputField.getText());
        this.inputField.setCanLoseFocus(false);
        List<String> strings = new ArrayList<>();

        strings.add(I18n.format("minetogether.chat.button.mute"));
        strings.add(I18n.format("minetogether.chat.button.addfriend"));
        
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
            buttonList.add(switchButton = new GuiButtonPair(808, x, height - 215, 234, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base ? 0 : ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).chatTarget.equals(ChatHandler.CHANNEL) ? 1 : 2, false, false, true, defaultStr, I18n.format("minetogether.ingame.chat.global"), I18n.format("minetogether.ingame.chat.group")));
        } else {
            buttonList.add(switchButton = new GuiButtonPair(808, x, height - 156, 156, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base ? 0 : 1, false, false, true, defaultStr, I18n.format("minetogether.ingame.chat.global")));
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
        if (button == menuDropdownButton)
        {
            if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mute")))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setChatLine(new TextComponentString(I18n.format("minetogether.chat.muted")), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.addfriend")))
            {
                mc.displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), activeDropdown, Callbacks.getFriendCode(), "", false));
            }
            return;
        }
        else if (button == switchButton)
        {
            if (CreeperHost.instance.gdpr.hasAcceptedGDPR())
            {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.base = switchButton.activeButton == 0;
                if (!ourChat.base) {
                    ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.privateChatList.getChannelname());
                    processBadwords();
                }
                switchButton.displayString = ourChat.base ? "MineTogether Chat" : "Minecraft Chat";
            }
            else {
                try {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiGDPR(null, () ->
                    {
                        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                        ourChat.base = false;
                        ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.privateChatList.getChannelname());
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
        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            this.buttonList.get(i).func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
        
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
        if ((!chatGui.chatTarget.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase()))&&(!chatGui.chatTarget.toLowerCase().contains(ChatHandler.CHANNEL.toLowerCase()))&&(chatGui.chatTarget.length() > 0)&&(!chatGui.chatTarget.toLowerCase().equals("#minetogether")))
        {
            //System.out.println("\nChatTarget"+chatGui.chatTarget.toLowerCase()+"\nChatHandler"+ChatHandler.CHANNEL.toLowerCase());
            String str = chatGui.closeComponent.getFormattedText();
            int x = mc.ingameGUI.getChatGUI().getChatWidth() - 2;
            int y = height - 40 - (mc.fontRendererObj.FONT_HEIGHT * Math.min(chatGui.drawnChatLines.size(), chatGui.getLineCount()));
            //System.out.println(x + " " + y);
            mc.fontRendererObj.drawString(str, x, y, 0xFFFFFF);
        }


        for (ChatLine chatline : ((GuiNewChatOurs)this.mc.ingameGUI.getChatGUI()).drawnChatLines) {
            List<ITextComponent> siblings = chatline.getChatComponent().getSiblings();
            for (ITextComponent sibling : siblings) {
                if (sibling instanceof TimestampComponentString) {
                    ((TimestampComponentString) sibling).setActive(false);
                }
            }
        }

        if (!((GuiTextFieldLockable)inputField).getOurEnabled() && ((GuiTextFieldLockable)inputField).isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(((GuiTextFieldLockable)inputField).getDisabledMessage()), mouseX, mouseY);
        }

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }
    }
    
    @Deprecated
    public void drawLogo()
    {
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/creeperhost25.png");
        ResourceLocation resourceLocationMinetogetherLogo = new ResourceLocation("creeperhost", "textures/minetogether25.png");
        
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
        if (!(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base)
        {
            return super.handleComponentClick(component);
        }

        if(component == ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).closeComponent)
            System.out.println("Close meh");
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
                
                Minecraft.getMinecraft().displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), chatInternalName, friendCode, friendName, true));
                
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
}
