package net.creeperhost.minetogether.gui.chat;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.common.LimitedSizeQueue;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiOurChat extends GuiScreen
{

    private final GuiScreen parent;
    private GuiScrollingChat chat;
    private GuiTextFieldLockable send;
    private DropdownButton<Target> targetDropdownButton;
    private GuiButton friendsButton;
    private static String playerName = Minecraft.getMinecraft().getSession().getUsername();
    private String currentTarget = ChatHandler.CHANNEL;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButton reconnectionButton;
    private GuiButton cancelButton;

    public GuiOurChat(GuiScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public void initGui()
    {
        chat = new GuiScrollingChat(10);
        send = new GuiTextFieldLockable(8008, mc.fontRendererObj, 10, this.height - 50, width - 20, 20);
        buttonList.add(targetDropdownButton = new DropdownButton<>(-1337, width - 5 - 100, 5, 100, 20, "Chat: %s", Target.getMainTarget(), true));
        List<String> strings = new ArrayList<>();
        strings.add("Mute");
        buttonList.add(menuDropdownButton = new DropdownButton<>(-1337, -1000, -1000, 100, 20, "Menu", new Menu(strings), true));
        buttonList.add(friendsButton = new GuiButton(-80088, 5, 5, 100, 20, "Friends list"));
        buttonList.add(cancelButton = new GuiButton(-800885, width - 100 - 5, height - 5 - 20, 100, 20, "Cancel"));
        buttonList.add(reconnectionButton = new GuiButton(-80084, 5 + 80, height - 5 - 20, 100, 20, "Reconnect"));
        reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries < 5);
        send.setMaxStringLength(120);
        send.setFocused(true);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        String buttonTarget = targetDropdownButton.getSelected().getInternalTarget();
        if (!buttonTarget.equals(currentTarget))
        {
            synchronized (ChatHandler.ircLock)
            {
                currentTarget = buttonTarget;
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
            return;
        }
        synchronized (ChatHandler.ircLock)
        {
            reconnectionButton.visible = reconnectionButton.enabled = !(ChatHandler.tries < 5);
            if (ChatHandler.hasNewMessages(currentTarget))
            {
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
        }

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
        if (status != ChatHandler.ConnectionStatus.CONNECTED)
        {
            send.setDisabled("Cannot send messages as not connected");
            disabledDueToConnection = true;
        } else if(!currentTarget.equals(ChatHandler.CHANNEL) && !ChatHandler.friends.containsKey(currentTarget)) {
            send.setDisabled("Cannot send messages as friend is not online");
            disabledDueToConnection = true;
        } else if (disabledDueToConnection)
        {
            disabledDueToConnection = false;
            send.setEnabled(true);
            processBadwords();
        }
        drawCenteredString(fontRendererObj, "MineTogether Chat", width / 2, 5, 0xFFFFFF);
        ITextComponent comp = new TextComponentString("\u2022").setStyle(new Style().setColor(TextFormatting.getValueByName(status.colour)));
        comp.appendSibling(new TextComponentString(" " + status.display).setStyle(new Style().setColor(TextFormatting.WHITE)));
        drawString(fontRendererObj, comp.getFormattedText(), 10, height - 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(!send.getOurEnabled() && send.isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(send.getDisabledMessage()), mouseX, mouseY);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button == menuDropdownButton)
        {
            if (menuDropdownButton.getSelected().option.equals("Mute"))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                chat.updateLines(currentTarget);
            }
        } else if (button == friendsButton) {
            CreeperHost.proxy.openFriendsGui();
        } else if (button == reconnectionButton) {
            ChatHandler.reInit();
        } else if (button == cancelButton) {
            this.mc.displayGuiScreen(parent);
        }
        chat.actionPerformed(button);
        super.actionPerformed(button);
    }

    boolean disabledDueToBadwords = false;
    public void processBadwords()
    {
        String text = send.getText().replaceAll(ChatHandler.badwordsFormat, "");
        boolean veryNaughty = false;
        for(String bad: ChatHandler.badwords)
        {
            if(bad.startsWith("(")  && bad.endsWith(")"))
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

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if ((keyCode == 28 || keyCode == 156) && send.getOurEnabled() && !send.getText().trim().isEmpty())
        {
            ChatHandler.sendMessage(currentTarget, send.getText());
            send.setText("");
            return;
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

        processBadwords();
    }

    private static Field field;

    static {
        try
        {
            field = GuiScrollingList.class.getDeclaredField("scrollDistance");
            field.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
        }
    }

    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        ClickEvent event = component.getStyle().getClickEvent();
        if (event.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
        {
            int mouseX = Mouse.getX() * GuiOurChat.this.width / GuiOurChat.this.mc.displayWidth;
            menuDropdownButton.xPosition = mouseX;
            menuDropdownButton.yPosition = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        return super.handleComponentClick(component);
    }

    private static ITextComponent formatLine(Pair<String, String> message)
    {
        String inputNick = message.getLeft();
        String outputNick = inputNick;
        boolean friend = false;
        if (inputNick.startsWith("MT"))
        {
            if (inputNick.equals(CreeperHost.instance.ourNick))
            {
                outputNick = playerName;
            } else {
                if (CreeperHost.instance.mutedUsers.contains(inputNick))
                    return null;

                String newNick = ChatHandler.getNameForUser(inputNick);
                if (!inputNick.equals(newNick) && !newNick.startsWith("Anonymous"))
                {
                    friend = true;
                }
                outputNick = newNick;
            }
        } else if(!inputNick.equals("System")) {
            return null;
        }

        ITextComponent base = new TextComponentString("");

        ITextComponent userComp = new TextComponentString("<" + outputNick + ">");

        if (!inputNick.equals(CreeperHost.instance.ourNick) && inputNick.startsWith("MT"))
        {
            userComp.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, inputNick)));
        }

        String messageStr = message.getRight();

        for(String swear : ChatHandler.badwords)
        {
            messageStr = messageStr.replace(swear, StringUtils.repeat("*", swear.length()));
        }

        ITextComponent messageComp = new TextComponentString(messageStr).setStyle(new Style().setColor(TextFormatting.WHITE));

        if (friend)
        {
            userComp.getStyle().setColor(TextFormatting.YELLOW);
        } else if (outputNick.equals("System")) {
            userComp.getStyle().setColor(TextFormatting.AQUA);
        }

        base.appendSibling(userComp);
        base.appendSibling(new TextComponentString(" ").setStyle(new Style().setColor(TextFormatting.WHITE)));

        return base.appendSibling(messageComp);
    }

    private class GuiScrollingChat extends GuiScrollingList
    {

        private ArrayList<ITextComponent> lines;

        GuiScrollingChat(int entryHeight)
        {
            super(Minecraft.getMinecraft(), GuiOurChat.this.width-20, GuiOurChat.this.height-30, 30, GuiOurChat.this.height -50, 10, entryHeight, GuiOurChat.this.width, GuiOurChat.this.height);
            lines = new ArrayList<>();
            updateLines(ChatHandler.CHANNEL);
        }

        @Override
        protected int getContentHeight()
        {
            int viewHeight = this.bottom - this.top - 4;
            return super.getContentHeight() < viewHeight ? viewHeight : super.getContentHeight();
        }

        protected void updateLines(String key)
        {
            if(ChatHandler.messages.size() == 0)
                return;
            ArrayList<ITextComponent> oldLines = lines;
            int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
            lines = new ArrayList<>();
            LimitedSizeQueue<Pair<String, String>> tempMessages = ChatHandler.messages.get(key);
            if (tempMessages == null)
                return;
            for(Pair<String, String> message : tempMessages)
            {
                ITextComponent display = formatLine(message);
                if (display == null)
                    continue;
                List<ITextComponent> strings = GuiUtilRenderComponents.splitText(display, listWidth - 6, fontRendererObj, false, true);
                for(ITextComponent string: strings)
                {
                    lines.add(string);
                }
            }
            try
            {
                if (lines.size() > oldLines.size() && ((float)field.get(this) == listHeight) || listHeight < 0)
                {
                    listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
                    field.set(this, listHeight);
                }
            }
            catch (IllegalAccessException e)
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
            int mouseX = Mouse.getX() * GuiOurChat.this.width / GuiOurChat.this.mc.displayWidth;
            mouseX -= this.left;
            int totalWidth = 0;
            for(ITextComponent sibling: component.getSiblings())
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
        protected void drawBackground()
        {
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            ITextComponent component = lines.get(slotIdx);
            int mouseX = Mouse.getX() * GuiOurChat.this.width / GuiOurChat.this.mc.displayWidth;
            mouseX -= this.left;
            int totalWidth = 5;
            for(ITextComponent sibling: component.getSiblings())
            {
                int oldTotal = totalWidth;
                totalWidth += fontRendererObj.getStringWidth(sibling.getFormattedText());
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > slotTop && mouseY < slotTop + slotHeight;
                if (sibling.getStyle().getClickEvent() != null)
                {
                    if (hovering)
                    {
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
        }
    }

    private class Menu implements DropdownButton.IDropdownOption
    {
        List<DropdownButton.IDropdownOption> possibleValsCache;
        String option;

        public Menu(List<String> options)
        {
            possibleValsCache = new ArrayList<>();
            possibleValsCache.add(this);
            option = options.get(0);
            options.remove(0);
            for(String option: options)
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