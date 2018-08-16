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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.input.Mouse;

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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
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
        if (((GuiNewChatOurs)Minecraft.getMinecraft().ingameGUI.getChatGUI()).base)
        {
            super.sendChatMessage(msg, addToChat);
            return;
        }
        if (msg.startsWith("/"))
        {
            super.sendChatMessage(msg, addToChat);
        } else {
            msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
            if (msg.isEmpty()) return;
            if (addToChat)
            {
                this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
            }
            if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0) return;
            if (ChatHandler.connectionStatus == ChatHandler.ConnectionStatus.CONNECTED)
            {
                String text = msg;
                String[] split = text.split(" ");
                for(int i = 0; i < split.length; i++)
                {
                    String word = split[i].toLowerCase();
                    final String subst = "User$2";

                    final Matcher matcher = pattern.matcher(word);

                    final String result = matcher.replaceAll(subst);

                    String justNick = result.replaceAll("[^A-Za-z0-9]", "");

                    String tempWord = ChatHandler.anonUsersReverse.get(justNick);
                    if(tempWord != null)
                        split[i] = result.replaceAll(justNick, tempWord);
                }

                text = String.join(" ", split);
                ChatHandler.sendMessage(ChatHandler.CHANNEL, text);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        strings.add("Add friend");
        buttonList.add(switchButton = new GuiButtonPair(808, 0, height - 40, 326, 15, "Minecraft Chat" ,"MineTogether Chat", !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).base));
        buttonList.add(menuDropdownButton = new DropdownButton<>(-1337, -1000, -1000, 100, 20, "Menu", new GuiMTChat.Menu(strings), true));
        menuDropdownButton.flipped = true;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == menuDropdownButton) {
            if (menuDropdownButton.getSelected().option.equals("Mute")) {
                CreeperHost.instance.muteUser(activeDropdown);
                ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).setChatLine(new TextComponentString("User has been muted. You will no longer receive messages from this person."), 0, Minecraft.getMinecraft().ingameGUI.getUpdateCounter(), false);
            } else if (menuDropdownButton.getSelected().option.equals("Add friend")) {
                mc.displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), activeDropdown, Callbacks.getFriendCode(), "", false));
            }
        } else if (button == switchButton) {
            if (CreeperHost.instance.gdpr.hasAcceptedGDPR()) {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.base = switchButton.firstActiveButton;
                switchButton.displayString = ourChat.base ? "MineTogether Chat" : "Minecraft Chat";
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiGDPR(null, () ->
                {
                    ((GuiNewChatOurs)Minecraft.getMinecraft().ingameGUI.getChatGUI()).base = false;
                    return new GuiChatOurs();
                }));
            }
        }
        super.actionPerformed(button);
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

                for(int i = 3; i < split.length; i++)
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
