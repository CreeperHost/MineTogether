package net.creeperhost.minetogether.gui.chat.ingame;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.chat.GuiChatFriend;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.element.GuiButtonPair;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
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
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            if (sleep)
                this.wakeFromSleep();
            else
                super.keyTyped(typedChar, keyCode);
        } else
        {
            super.keyTyped(typedChar, keyCode);
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
            if (ChatHandler.connectionStatus == ChatHandler.ConnectionStatus.CONNECTED)
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
        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        strings.add("Add friend");
        
        float f1 = mc.ingameGUI.getChatGUI().getChatScale();
        int x = MathHelper.ceil((float) mc.ingameGUI.getChatGUI().getChatWidth() / f1) + 8;
        
        buttonList.add(switchButton = new GuiButtonPair(808, x, height - 41, 92, 16, "Default", "Global", !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base, false, false, true));
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
            if (menuDropdownButton.getSelected().option.equals("Mute"))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setChatLine(new TextComponentString("User has been muted. You will no longer receive messages from this person."), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
            }
            else if (menuDropdownButton.getSelected().option.equals("Add friend"))
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
                ourChat.base = switchButton.firstActiveButton;
                switchButton.displayString = ourChat.base ? "MineTogether Chat" : "Minecraft Chat";
            }
            else
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiGDPR(null, () ->
                {
                    ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base = false;
                    return new GuiChatOurs(presetString, sleep);
                }));
            }
            return;
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
            ((GuiButton) this.buttonList.get(i)).func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
        
        for (int j = 0; j < this.labelList.size(); ++j)
        {
            ((GuiLabel) this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }
        
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        
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
