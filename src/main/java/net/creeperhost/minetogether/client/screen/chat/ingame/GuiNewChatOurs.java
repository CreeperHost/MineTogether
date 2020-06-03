package net.creeperhost.minetogether.client.screen.chat.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.chat.TimestampComponentString;
import net.creeperhost.minetogether.proxy.Client;
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
            
            int i = MathHelper.floor((float) this.getChatWidth() / mc.getMainWindow().getScaledHeight());
            List<ITextComponent> list = RenderComponentsUtil.splitText(chatComponent, i, this.mc.fontRenderer, false, false);
            boolean flag = this.getChatOpen();
            
            for (ITextComponent itextcomponent : list)
            {
                if (flag && this.scrollPos > 0)
                {
                    this.isScrolled = true;
                    this.addScrollPos(1);
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
                if (!ChatHandler.isInitting.get())
                {
                    ChatHandler.reInit();
                }
            }
            
            if (this.mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN)
            {
                int i = this.getLineCount();
                int j = this.drawnChatLines.size();
                double f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
                
                if (j > 0)
                {
                    boolean flag = false;
                    
                    if (this.getChatOpen())
                    {
                        flag = true;
                    }
                    
                    double f1 = this.getScale();
                    int k = MathHelper.ceil((float) this.getChatWidth() / f1);
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                    RenderSystem.scaled(f1, f1, 1.0F);
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
                                
                                l1 = (int) ((float) l1 * f);
                                ++l;
                                
                                if (l1 > 3)
                                {
                                    int i2 = 0;
                                    int j2 = -i1 * 9;
                                    fill(-2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
                                    RenderSystem.enableBlend();
                                }
                            }
                        }
                    }
                    
                    if (!isBase() && getChatOpen())
                        MTChatScreen.drawLogo(mc.fontRenderer, k + 4 + 2, 40, -2, (int) (-lines * 4.5), 0.75F);
                    
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
                                    RenderSystem.enableBlend();
                                    this.mc.fontRenderer.drawString(s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
                                    RenderSystem.disableAlphaTest();
                                    RenderSystem.disableBlend();
                                }
                            }
                        }
                    }
                    
                    if (flag)
                    {
                        int k2 = this.mc.fontRenderer.FONT_HEIGHT;
                        RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                        int l2 = j * k2 + j;
                        int i3 = l * k2 + l;
                        int j3 = this.scrollPos * i3 / j;
                        int k1 = i3 * i3 / l2;
                        
                        if (l2 != i3)
                        {
                            int k3 = j3 > 0 ? 170 : 96;
                            int l3 = this.isScrolled ? 13382451 : 3355562;
                            fill(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                            fill(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                        }
                    }
                    
                    RenderSystem.popMatrix();
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
            RenderSystem.pushMatrix();
            RenderSystem.translatef(2.0F, 8.0F, 0.0F);
            RenderSystem.scaled(f1, f1, 1.0F);
            
            int k = MathHelper.ceil((float) this.getChatWidth() / f1);
            
            for (int line = tempDrawnChatLines.size(); line < minLines; line++)
            {
                int l1 = 255;
                int j2 = -line * 9;
                fill(-2, j2 - 9, k + 4, j2, l1 / 2 << 24);
            }
            
            RenderSystem.popMatrix();
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
        synchronized (ChatHandler.ircLock)
        {
            int size = messages.size();
            for (Message message : messages)
            {
                ITextComponent component = MTChatScreen.formatLine(message);
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
                this.addScrollPos(1);
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
    
    @Override
    public void addScrollPos(double amount)
    {
        if (isBase())
            super.addScrollPos(amount);
        else
        {
            this.scrollPos += amount;
            int i = this.drawnChatLines.size();
            
            if (this.scrollPos > i - this.getLineCount())
            {
                this.scrollPos = i - this.getLineCount();
            }
            
            if (this.scrollPos <= 0)
            {
                this.scrollPos = 0;
                this.isScrolled = false;
            }
        }
    }
    
    @Nullable
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
                double lvt_5_1_ = this.getScale();
                double lvt_7_1_ = mouseX - 2.0D;
                double lvt_9_1_ = (double) this.mc.getMainWindow().getScaledHeight() - mouseY - 40.0D;
                lvt_7_1_ = (double) MathHelper.floor(lvt_7_1_ / lvt_5_1_);
                lvt_9_1_ = (double) MathHelper.floor(lvt_9_1_ / lvt_5_1_);
                if (lvt_7_1_ >= 0.0D && lvt_9_1_ >= 0.0D)
                {
                    int lvt_11_1_ = Math.min(this.getLineCount(), this.drawnChatLines.size());
                    if (lvt_7_1_ <= (double) MathHelper.floor((double) this.getChatWidth() / this.getScale()))
                    {
                        this.mc.fontRenderer.getClass();
                        if (lvt_9_1_ < (double) (9 * lvt_11_1_ + lvt_11_1_))
                        {
                            this.mc.fontRenderer.getClass();
                            int lvt_12_1_ = (int) (lvt_9_1_ / 9.0D + (double) this.scrollPos);
                            if (lvt_12_1_ >= 0 && lvt_12_1_ < this.drawnChatLines.size())
                            {
                                ChatLine lvt_13_1_ = (ChatLine) this.drawnChatLines.get(lvt_12_1_);
                                int lvt_14_1_ = 0;
                                Iterator var15 = lvt_13_1_.getChatComponent().iterator();
                                
                                while (var15.hasNext())
                                {
                                    ITextComponent lvt_16_1_ = (ITextComponent) var15.next();
                                    if (lvt_16_1_ instanceof StringTextComponent)
                                    {
                                        lvt_14_1_ += this.mc.fontRenderer.getStringWidth(RenderComponentsUtil.removeTextColorsIfConfigured(((StringTextComponent) lvt_16_1_).getText(), false));
                                        if ((double) lvt_14_1_ > lvt_7_1_)
                                        {
                                            return lvt_16_1_;
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                    }
                    return null;
                } else
                {
                    return null;
                }
            }
        }
    }
    
    public static List<ITextComponent> splitText(ITextComponent textComponent, int maxTextLenght, FontRenderer fontRendererIn, boolean p_178908_3_, boolean forceTextColor)
    {
        int i = 0;
        ITextComponent itextcomponent = new StringTextComponent("");
        List<ITextComponent> list = Lists.<ITextComponent>newArrayList();
        if (textComponent == null) return list;
        List<ITextComponent> list1 = Lists.newArrayList(textComponent);
        
        for (int j = 0; j < list1.size(); ++j)
        {
            ITextComponent itextcomponent1 = list1.get(j);
            if (itextcomponent1 instanceof TimestampComponentString)
            {
                itextcomponent.appendSibling(itextcomponent1);
                i += fontRendererIn.getStringWidth(((TimestampComponentString) itextcomponent1).getRawText());
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
                    } else if (i > 0 && !s4.contains(" "))
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
            } else
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
        return list;
    }
    
    public boolean isBase()
    {
        return base;
    }
    
    public void setBase(boolean base)
    {
        this.base = base;
        if (base)
        {
            refreshChat();
            Client.chatType = 0;
        } else
        {
            getVanillaDrawnChatLines().clear();
            Client.chatType = 1;
        }
    }
    
    public List<ChatLine> getVanillaDrawnChatLines()
    {
        if (vanillaDrawnChatLines == null)
        {
            vanillaDrawnChatLines = drawnChatLines;
        }
        return vanillaDrawnChatLines;
    }
}
