package net.creeperhost.minetogether.client.gui.chat.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.client.gui.chat.TimestampComponentString;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class GuiNewChatOurs extends NewChatGui
{
    private boolean base = true;
    public static boolean tabCompletion = false;
    
    int updateCounter;

    @Override
    public void deleteChatLine(int id)
    {
        if (!isBase())
        {
            Iterator<ChatLine> iterator = this.drawnChatLines.iterator();

            while (iterator.hasNext())
            {
                ChatLine chatline = iterator.next();

                if (chatline.getChatLineID() == id)
                {
                    iterator.remove();
                }
            }

            iterator = this.chatLines.iterator();

            while (iterator.hasNext())
            {
                ChatLine chatline1 = iterator.next();

                if (chatline1.getChatLineID() == id)
                {
                    iterator.remove();
                    break;
                }
            }
        } else
        {
            super.deleteChatLine(id);
        }
    }

    @Override
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId)
    {
        ITextComponent chatComponentCopy = chatComponent.deepCopy();
        if (tabCompletion && !isBase())
        {
            // special handling to allow our chat to receive these lines
            int updateCounter = this.updateCounter;
            boolean displayOnly = false;
            if (chatLineId != 0)
            {
                this.deleteChatLine(chatLineId);
            }

            int i = MathHelper.floor((float) this.getChatWidth() / mc.mainWindow.getScaledHeight());
            List<ITextComponent> list = RenderComponentsUtil.splitText(chatComponent, i, this.mc.fontRenderer, false, false);
            boolean flag = this.getChatOpen();

            for (ITextComponent itextcomponent : list)
            {
                if (flag && this.scrollPos > 0)
                {
                    this.isScrolled = true;
//                    this.scroll(1);
                }

                this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
            }

            while (this.drawnChatLines.size() > 100)
            {
                this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
            }

            if (!displayOnly)
            {
                this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

                while (this.chatLines.size() > 100)
                {
                    this.chatLines.remove(this.chatLines.size() - 1);
                }
            }
        } else
        {
            super.printChatMessageWithOptionalDeletion(chatComponentCopy, chatLineId);
        }

        if (!isBase())
        {
            unread = true;
            getVanillaDrawnChatLines().clear(); // instantly clear so that no surprises happen whilst we're in our chat (I'm looking at you, Quark!)
        }
    }
    
    private final Minecraft mc;
    
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    /**
     * List of the ChatLines currently drawn
     */
    public final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    
    private final List<String> sentMessages = Lists.<String>newArrayList();

    public final ITextComponent closeComponent;
    
    public GuiNewChatOurs(Minecraft mcIn)
    {
        super(mcIn);
        mc = mcIn;
        chatTarget = ChatHandler.CHANNEL;

        closeComponent = new StringTextComponent(new String(Character.toChars(10006))).setStyle(new Style().setColor(TextFormatting.RED).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to close group chat"))));
    }
    
    @Override
    public int getChatWidth()
    {
        return (int) (super.getChatWidth() - (16 * 0.75));
    }
    
    private static Field drawnChatLinesField = null;
    private List<ChatLine> vanillaDrawnChatLines = null;
    
    @Override
    public void render(int updateCounter)
    {
        this.updateCounter = updateCounter;
        List<ChatLine> tempDrawnChatLines = drawnChatLines;
        int minLines = isBase() ? (14 + ((ChatHandler.hasGroup) ? 6 : 0)) : 20;
        int lines = Math.max(minLines, Math.min(tempDrawnChatLines.size(), getLineCount()));

        if (isBase())
        super.render(updateCounter);
        else
        {
            if ((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && updateCounter % 6000 == 0)
            {
                if (!ChatHandler.isInitting)
                {
                    ChatHandler.reInit();
                }
            }

            if (this.mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN)
            {
                int i = this.getLineCount();
                int j = this.drawnChatLines.size();
//                float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

                if (j > 0)
                {
                    boolean flag = false;

                    if (this.getChatOpen())
                    {
                        flag = true;
                    }

                    double f1 = this.getScale();
                    int k = MathHelper.ceil((float) this.getChatWidth() / f1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(2.0F, 8.0F, 0.0F);
                    GlStateManager.scaled(f1, f1, 1.0F);
                    int l = 0;

                    for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1)
                    {
                        ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);

                        if (chatline != null)
                        {
                            int j1 = updateCounter - chatline.getUpdatedCounter();

                            if (j1 < 200 || flag)
                            {
                                double d0 = (double) j1 / 200.0D;
                                d0 = 1.0D - d0;
                                d0 = d0 * 10.0D;
                                d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                                d0 = d0 * d0;
                                int l1 = (int) (255.0D * d0);

                                if (flag)
                                {
                                    l1 = 255;
                                }

                                l1 = (int) ((float) l1);
                                ++l;

                                if (l1 > 3)
                                {
                                    int i2 = 0;
                                    int j2 = -i1 * 9;
//                                    drawRect(-2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
                                    GlStateManager.enableBlend();
                                }
                            }
                        }
                    }

//                    if (!isBase() && getChatOpen())
                        GuiMTChat.drawLogo(mc.fontRenderer, k + 4 + 2, 40, -2, (int) (-lines * 4.5), 0.75F);

                    for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1)
                    {
                        ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);

                        if (chatline != null)
                        {
                            int j1 = updateCounter - chatline.getUpdatedCounter();

                            if (j1 < 200 || flag)
                            {
                                double d0 = (double) j1 / 200.0D;
                                d0 = 1.0D - d0;
                                d0 = d0 * 10.0D;
                                d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                                d0 = d0 * d0;
                                int l1 = (int) (255.0D * d0);

                                if (flag)
                                {
                                    l1 = 255;
                                }

                                l1 = (int) ((float) l1);
                                ++l;

                                if (l1 > 3)
                                {
                                    int i2 = 0;
                                    int j2 = -i1 * 9;
                                    String s = chatline.getChatComponent().getFormattedText();
                                    GlStateManager.enableBlend();
                                    this.mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
                                    GlStateManager.disableAlphaTest();
                                    GlStateManager.disableBlend();
                                }
                            }
                        }
                    }

                    if (flag)
                    {
                        int k2 = this.mc.fontRenderer.FONT_HEIGHT;
                        GlStateManager.translatef(-3.0F, 0.0F, 0.0F);
                        int l2 = j * k2 + j;
                        int i3 = l * k2 + l;
                        int j3 = this.scrollPos * i3 / j;
                        int k1 = i3 * i3 / l2;

                        if (l2 != i3)
                        {
                            int k3 = j3 > 0 ? 170 : 96;
                            int l3 = this.isScrolled ? 13382451 : 3355562;
//                            drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
//                            drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                        }
                    }

                    GlStateManager.popMatrix();
                }
            }
        }

        if (isBase())
        {
            tempDrawnChatLines = getVanillaDrawnChatLines();
        }
        
        if (getChatOpen() && !MineTogether.instance.ingameChat.hasDisabledIngameChat())
        {
            double f1 = this.getScale();
            GlStateManager.pushMatrix();
            GlStateManager.translatef(2.0F, 8.0F, 0.0F);
            GlStateManager.scaled(f1, f1, 1.0F);
            
            int k = MathHelper.ceil((float) this.getChatWidth() / f1);

            for (int line = tempDrawnChatLines.size(); line < minLines; line++)
            {
                int l1 = 255;
                int j2 = -line * 9;
                blit(-2, j2 - 9, k + 4, j2, l1 / 2 << 24, 0);
            }
            
            
            //lines = lines - minLines;
            //lines = 1;
            
            //if (!isBase() && getChatOpen())
            //GuiMTChat.drawLogo(mc.fontRendererObj, k + 4 + 2, 40, -2, (int) (-lines * 4.5), 0.75F);
            
            
            GlStateManager.popMatrix();
        }
    }
    
    @Override
    public List<String> getSentMessages()
    {
        return super.getSentMessages();
    }
    
    @Override
    public void addToSentMessages(String message)
    {
        if (isBase())
        super.addToSentMessages(message);
        else
        {
            if (this.sentMessages.isEmpty() || !(this.sentMessages.get(this.sentMessages.size() - 1)).equals(message))
            {
                this.sentMessages.add(message);
            }
        }
    }
    
    public boolean unread;
    public String chatTarget;

    public void rebuildChat(String chatKey)
    {
        chatTarget = chatKey;
        chatLines.clear();
        drawnChatLines.clear();
        LimitedSizeQueue<Message> messages = ChatHandler.messages.get(chatKey);
        if (messages == null)
            return;
        synchronized (ChatHandler.ircLock) {
            int size = messages.size();
            for (Message message : messages) {
                ITextComponent component = GuiMTChat.formatLine(message);
                if (component == null)
                    continue;
                setChatLine(component, size--, 0, false);
            }
        }
    }

    public void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
    {
        if (isBase())
        {
            unread = true;
        }
        if (chatLineId != 0)
        {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor((float) this.getChatWidth() / this.getScale());
        List<ITextComponent> list = splitText(chatComponent, i, this.mc.fontRenderer, false, false);
        boolean flag = this.getChatOpen();

        for (ITextComponent itextcomponent : list)
        {
            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
//                this.scroll(1);
            }

            this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }

        while (this.drawnChatLines.size() > 100)
        {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly)
        {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

            while (this.chatLines.size() > 100)
            {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }
    
    @Override
    public void resetScroll()
    {
        if (isBase())
            super.resetScroll();
        else
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

//    @Override
//    public void scroll(int amount)
//    {
//        if (isBase())
//            super.scroll(amount);
//        else
//        {
//            this.scrollPos += amount;
//            int i = this.drawnChatLines.size();
//
//            if (this.scrollPos > i - this.getLineCount())
//            {
//                this.scrollPos = i - this.getLineCount();
//            }
//
//            if (this.scrollPos <= 0)
//            {
//                this.scrollPos = 0;
//                this.isScrolled = false;
//            }
//        }
//    }
//
    
    @Nullable
    @Override
    public ITextComponent getTextComponent(double mouseX, double mouseY)
    {
        if (isBase())
            return super.getTextComponent(mouseX, mouseY);
        else
        {
            if (!this.getChatOpen())
            {
                return null;
            } else
            {
//                ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                double i = mouseX - 2.0D;
                double f = mc.mainWindow.getScaledHeight() - mouseY - 40.0D;
                double j = mouseX / i - 2;
                double k = mouseY / i - 40;

                j = MathHelper.floor((float) j / f);
                k = MathHelper.floor((float) k / f);

                int l = Math.max(Math.min(this.getLineCount(), this.drawnChatLines.size()), 20);

                int width = getChatWidth() + 3;

                int top = this.mc.fontRenderer.FONT_HEIGHT * l + 1;

                if (!chatTarget.equals(ChatHandler.CHANNEL) && j <= width && j >= width - mc.fontRenderer.getStringWidth(closeComponent.getFormattedText()) && k <= top && k >= top - (this.mc.fontRenderer.FONT_HEIGHT))
                    return closeComponent;

                if (j >= 0 && k >= 0)
                {
                    if (j <= MathHelper.floor((float) this.getChatWidth() / mc.mainWindow.getScaledHeight()) && k < this.mc.fontRenderer.FONT_HEIGHT * l + l)
                    {
                        int i1 = (int) (k / this.mc.fontRenderer.FONT_HEIGHT + this.scrollPos);

                        if (i1 >= 0 && i1 < this.drawnChatLines.size())
                        {
                            ChatLine chatline = this.drawnChatLines.get(i1);
                            int j1 = 0;

                            for (ITextComponent itextcomponent : chatline.getChatComponent())
                            {
                                if (itextcomponent instanceof StringTextComponent)
                                {
                                    j1 += this.mc.fontRenderer.getStringWidth(RenderComponentsUtil.removeTextColorsIfConfigured(((StringTextComponent) itextcomponent).getText(), false));
                                    if (j1 > j)
                                    {
                                        return itextcomponent;
                                    }
                                }
                            }
                        }

                        return null;
                    } else
                    {
                        return null;
                    }
                } else
                {
                    return null;
                }
            }
        }
    }

    @Nullable
    public ITextComponent getBaseChatComponent(int mouseX, int mouseY)
    {
        if (!this.getChatOpen())
        {
            return null;
        }
        else
        {
            double i = mouseX - 2.0D;
            double f = mc.mainWindow.getScaledHeight() - mouseY - 40.0D;
            double j = mouseX / i - 2;
            double k = mouseY / i - 40;
            j = MathHelper.floor((float)j / f);
            k = MathHelper.floor((float)k / f);

            if (j >= 0 && k >= 0)
            {
                int l = Math.min(this.getLineCount(), this.drawnChatLines.size());

                if (j <= MathHelper.floor((float)this.getChatWidth() / mc.mainWindow.getScaledHeight()) && k < this.mc.fontRenderer.FONT_HEIGHT * l + l)
                {
                    int i1 = (int) (k / this.mc.fontRenderer.FONT_HEIGHT + this.scrollPos);

                    if (i1 >= 0 && i1 < this.drawnChatLines.size())
                    {
                        ChatLine chatline = this.drawnChatLines.get(i1);
                        return chatline.getChatComponent();
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public static List<ITextComponent> splitText(ITextComponent textComponent, int maxTextLenght, FontRenderer fontRendererIn, boolean p_178908_3_, boolean forceTextColor)
    {
        int i = 0;
        ITextComponent itextcomponent = new StringTextComponent("");
        List<ITextComponent> list = Lists.<ITextComponent>newArrayList();
        if(textComponent == null) return list;
        List<ITextComponent> list1 = Lists.newArrayList(textComponent);

        for (int j = 0; j < list1.size(); ++j)
        {
            ITextComponent itextcomponent1 = list1.get(j);
            if (itextcomponent1 instanceof TimestampComponentString) {
                itextcomponent.appendSibling(itextcomponent1);
                i += fontRendererIn.getStringWidth(((TimestampComponentString)itextcomponent1).getRawText());
                continue;
            }
            String s = itextcomponent1.getUnformattedComponentText();
            boolean flag = false;

            if (s.contains("\n"))
            {
                int k = s.indexOf(10);
                String s1 = s.substring(k + 1);
                s = s.substring(0, k + 1);
                ITextComponent itextcomponent2 = new StringTextComponent(s1);
                itextcomponent2.setStyle(itextcomponent1.getStyle().createShallowCopy());
                list1.add(j + 1, itextcomponent2);
                flag = true;
            }

            String s4 = RenderComponentsUtil.removeTextColorsIfConfigured(itextcomponent1.getStyle().getFormattingCode() + s, forceTextColor);
            String s5 = s4.endsWith("\n") ? s4.substring(0, s4.length() - 1) : s4;
            int i1 = fontRendererIn.getStringWidth(s5);
            StringTextComponent textcomponentstring = new StringTextComponent(s5);
            textcomponentstring.setStyle(itextcomponent1.getStyle().createShallowCopy());

            if (i + i1 > maxTextLenght)
            {
                String s2 = fontRendererIn.trimStringToWidth(s4, maxTextLenght - i, false);
                String s3 = s2.length() < s4.length() ? s4.substring(s2.length()) : null;

                if (s3 != null && !s3.isEmpty())
                {
                    int l = s2.lastIndexOf(32);

                    if (l >= 0 && fontRendererIn.getStringWidth(s4.substring(0, l)) > 0)
                    {
                        s2 = s4.substring(0, l);

                        if (p_178908_3_)
                        {
                            ++l;
                        }

                        s3 = s4.substring(l);
                    }
                    else if (i > 0 && !s4.contains(" "))
                    {
                        s2 = "";
                        s3 = s4;
                    }

//                    s3 = FontRenderer.getFormatFromString(s2) + s3; //Forge: Fix chat formatting not surviving line wrapping.

                    StringTextComponent textcomponentstring1 = new StringTextComponent(s3);
                    textcomponentstring1.setStyle(itextcomponent1.getStyle().createShallowCopy());
                    list1.add(j + 1, textcomponentstring1);
                }

                i1 = fontRendererIn.getStringWidth(s2);
                textcomponentstring = new StringTextComponent(s2);
                textcomponentstring.setStyle(itextcomponent1.getStyle().createShallowCopy());
                flag = true;
            }

            if (i + i1 <= maxTextLenght)
            {
                i += i1;
                itextcomponent.appendSibling(textcomponentstring);
            }
            else
            {
                flag = true;
            }

            if (flag)
            {
                list.add(itextcomponent);
                i = 0;
                itextcomponent = new StringTextComponent("");
            }
        }

        list.add(itextcomponent);
        return null;
    }

    public boolean isBase() {
        return base;
    }

    public void setBase(boolean base) {
        this.base = base;
        if (base)
        {
            refreshChat();
        } else {
            getVanillaDrawnChatLines().clear();
        }
    }

    public List<ChatLine> getVanillaDrawnChatLines() {
        if (vanillaDrawnChatLines == null)
        {
            if (drawnChatLinesField == null)
            {
//                drawnChatLinesField = ReflectionHelper.findField(GuiNewChat.class, "drawnChatLines", "field_146253_i", "");
            }

            try
            {
                vanillaDrawnChatLines = (List<ChatLine>) drawnChatLinesField.get(this);
            } catch (IllegalAccessException ignored) {}
        }
        return vanillaDrawnChatLines;
    }
}
