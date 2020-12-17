package net.creeperhost.minetogether.client.screen.chat.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.Message;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.proxy.Client;
import net.creeperhost.minetogether.util.LimitedSizeQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
            Iterator<ChatLine<IReorderingProcessor>> iterator = this.drawnChatLines.iterator();
            
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
            List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(chatComponent, i, this.mc.fontRenderer);
            boolean flag = this.getChatOpen();
            
            for (IReorderingProcessor itextcomponent : list)
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
            super.drawnChatLines.clear(); // instantly clear so that no surprises happen whilst we're in our chat (I'm looking at you, Quark!)
        }
    }

//    private boolean getChatOpen() {
//        return this.mc.currentScreen instanceof ChatScreen;
//    }

    private final Minecraft mc;
    
    private final List<ChatLine<IReorderingProcessor>> chatLines = Lists.newArrayList();
    //Last lines added to chatLines and their associated Message,
    // Used as a lookup to remember what update times each line has had.
    private Map<Message, ChatLine> lineLookup = new HashMap<>();
    /**
     * List of the ChatLines currently drawn
     */
    public final List<ChatLine<IReorderingProcessor>> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    
    private final List<String> sentMessages = Lists.<String>newArrayList();
    
    public final ITextComponent closeComponent;
    
    public GuiNewChatOurs(Minecraft mcIn)
    {
        super(mcIn);
        mc = mcIn;
        chatTarget = ChatHandler.CHANNEL;
        
        closeComponent = new StringTextComponent(new String(Character.toChars(10006))).setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.RED)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to close group chat"))));
    }
    
    @Override
    public int getChatWidth()
    {
        return (int) (super.getChatWidth() - (16 * 0.75));
    }

    private long tickCounter = 0;

    @Override
    public void func_238492_a_(MatrixStack matrixStack, int updateCounter)
    {
        this.updateCounter = updateCounter;
        List<ChatLine<IReorderingProcessor>> tempDrawnChatLines = drawnChatLines;
        int minLines = isBase() ? (14 + ((ChatHandler.hasGroup) ? 6 : 0)) : 20;
        int lines = Math.max(minLines, Math.min(tempDrawnChatLines.size(), getLineCount()));
        
        if (isBase()) {
            super.func_238492_a_(matrixStack, updateCounter);
        }
        else
        {
            if((ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTING && ChatHandler.connectionStatus != ChatHandler.ConnectionStatus.CONNECTED) && ChatHandler.isOnline() && updateCounter % 6000 == 0)
            {
                rebuildChat(ChatHandler.CHANNEL);
                if(!ChatHandler.isInitting.get())
                {
                    ChatHandler.reInit();
                }
            }

            if (this.mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN)
            {
                int i = this.getLineCount();
                int j = this.drawnChatLines.size();
                double f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

                if(tickCounter % 20 == 0 && ChatHandler.rebuildChat)
                {
                    if(ChatHandler.rebuildChat) ChatHandler.rebuildChat = false;
                    rebuildChat(ChatHandler.CHANNEL);
                }

                tickCounter++;
                
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
                                    fill(matrixStack, -2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
                                    RenderSystem.enableBlend();
                                }
                            }
                        }
                    }
                    
                    if (!isBase() && getChatOpen())
                        MTChatScreen.drawLogo(matrixStack, mc.fontRenderer, k + 4 + 2, 40, -2, (int) (-lines * 4.5), 0.75F);
                    
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
                                    IReorderingProcessor s = (IReorderingProcessor) chatline.getLineString();
                                    RenderSystem.enableBlend();
                                    this.mc.fontRenderer.func_238407_a_(matrixStack, s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
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
                            //fill(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                            //fill(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                        }
                    }
                    
                    RenderSystem.popMatrix();
                }
            }
        }
        
        if (isBase())
        {
            tempDrawnChatLines = super.drawnChatLines;
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
                fill(matrixStack, -2, j2 - 9, k + 4, j2, l1 / 2 << 24);
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

    public static CompletableFuture rebuildChatFuture;

    public void rebuildChat(String chatKey)
    {
        if(rebuildChatFuture != null)
        {
            if (!rebuildChatFuture.isDone()) return;
        }

        rebuildChatFuture = CompletableFuture.runAsync(() ->
        {
            int scroll = scrollPos;

            chatTarget = chatKey;
            chatLines.clear();
            drawnChatLines.clear();
            LimitedSizeQueue<Message> messages = ChatHandler.messages.get(chatKey);
            if (messages == null) return;
            Map<Message, ChatLine> oldLookup = lineLookup;
            lineLookup = new HashMap<>();
            synchronized (ChatHandler.ircLock) {
                for (Message message : messages) {
                    ChatLine existing = oldLookup.get(message);
                    int counter = existing != null ? existing.getUpdatedCounter() : 0;
                    int lineId = existing != null ? existing.getChatLineID() : 0;
                    ITextComponent component = MTChatScreen.formatLine(message);
                    if (component == null) continue;
                    setChatLine(message, component, lineId, counter, false);
                }
                scrollPos = scroll;
            }
        });
    }

    public void setChatLine(Message message, ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
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
        List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(chatComponent, i, mc.fontRenderer);// splitText(chatComponent, i, this.mc.fontRenderer, false, false);
        boolean flag = this.getChatOpen();
        
        for (IReorderingProcessor itextcomponent : list)
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
            ChatLine line = new ChatLine(updateCounter, chatComponent, chatLineId);
            if (message != null) {
                lineLookup.put(message, line);
            }
            this.chatLines.add(0, line);
            List<ChatLine<IReorderingProcessor>> removed = trimTo(chatLines, 100);
            if (!removed.isEmpty()) {
                lineLookup.values().removeAll(removed);
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

    private boolean chatHidden() {
        return this.mc.gameSettings.chatVisibility == ChatVisibility.HIDDEN;
    }

    @Nullable
    public Style func_238494_b_(double mouseX, double mouseY)
    {
        if (isBase()) return super.func_238494_b_(mouseX, mouseY);

        if (this.getChatOpen() && !this.mc.gameSettings.hideGUI && !this.chatHidden())
        {
            double d0 = mouseX - 2.0D;
            double d1 = (double)this.mc.getMainWindow().getScaledHeight() - mouseY - 40.0D;
            d0 = (double)MathHelper.floor(d0 / this.getScale());
            d1 = (double)MathHelper.floor(d1 / (this.getScale() * (this.mc.gameSettings.chatLineSpacing + 1.0D)));
            if (!(d0 < 0.0D) && !(d1 < 0.0D)) {
                int i = Math.min(this.getLineCount(), this.drawnChatLines.size());
                if (d0 <= (double)MathHelper.floor((double)this.getChatWidth() / this.getScale()) && d1 < (double)(9 * i + i))
                {
                    int j = (int)(d1 / 9.0D + (double)this.scrollPos);
                    if (j >= 0 && j < this.drawnChatLines.size())
                    {
                        ChatLine chatline = this.drawnChatLines.get(j);
                        return this.mc.fontRenderer.getCharacterManager().func_243239_a((IReorderingProcessor)chatline.getLineString(), (int)d0);

                    }
                }

                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
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
            super.drawnChatLines.clear();
            Client.chatType = 1;
        }
    }

    private static <T> List<T> trimTo(List<T> list, int size) {
        List<T> removed = new ArrayList<>();
        while (list.size() > size) {
            removed.add(list.remove(list.size() - 1));
        }
        return removed;
    }
}
