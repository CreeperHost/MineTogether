package net.creeperhost.minetogether.module.chat.screen;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.module.chat.screen.listentries.ListEntryFriend;
import net.creeperhost.minetogether.module.multiplayer.data.PublicServerEntry;
import net.creeperhost.minetogether.module.multiplayer.sort.ServerNameComparator;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.ircLock;

public class FriendsListScreen extends Screen
{
    private final Screen parent;
    private ScreenList<ListEntryFriend> list;
    private ScrollingChat chat;
    private EditBox chatBox;
    private EditBox searchEntry;
    private int ticks;
    private Profile targetProfile = null;

    public FriendsListScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.friendscreen.title"));
        this.parent = parent;
        String friendCode = ChatCallbacks.getFriendCode(MineTogetherClient.getUUID());
    }

    @Override
    public void init()
    {
        super.init();
        if (list == null)
        {
            list = new ScreenList(this, minecraft, 100, height - 90, 32, this.height - 55, 28, 100);
        } else
        {
            list.updateSize(100, height - 90, 28, this.height - 55);
        }
        chat = new ScrollingChat(width - list.getRowWidth() - 22, this.height - 90, 32, this.height - 55);
        chat.setLeftPos(list.getRowRight());

        chatBox = new EditBox(this.font, list.getRowRight() + 2, this.height -50, width - list.getRowWidth() - 7, 20, new TranslatableComponent(""));
        chatBox.setMaxLength(256);

        searchEntry = new EditBox(this.font, 1, this.height -50, list.width, 20, new TranslatableComponent(""));
        searchEntry.setSuggestion("Search");

        addButtons();
        children.add(list);
        children.add(searchEntry);
        children.add(chatBox);
        children.add(chat);
        refreshFriendsList();
    }

    public void addButtons()
    {
        addButton(new Button(5, height - 26, 100, 20, new TranslatableComponent("Cancel"), p -> minecraft.setScreen(parent)));

        addButton(new ButtonString( width - 105, 5, 120, 20, new TranslatableComponent(MineTogetherChat.profile.get().getFriendCode()), p ->
        {
            minecraft.keyboardHandler.setClipboard(MineTogetherChat.profile.get().getFriendCode());
            MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("Copied to clipboard."), width - 160, 0, 5000, ToastHandler.EnumToastType.DEFAULT, null);
        }));

        addButton(new ButtonMultiple(width - 20, 32, 5, (button) ->
        {

        }));

        addButton(new ButtonMultiple(width - 20, 52, 5, (button) ->
        {
        }));

        addButton(new ButtonMultiple(width - 20, 72, 5, (button) ->
        {
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        list.render(poseStack, i, j, f);
        searchEntry.render(poseStack, i, j, f);
        chatBox.render(poseStack, i, j, f);
        chat.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 12, 0xFFFFFF);
        if(list.children().isEmpty()) drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.friendslist.empty"), width / 2, (this.height / 2) - 20, -1);
    }

    @Override
    public void tick()
    {
        ticks++;
        if(ticks % 600 == 0)
        {
            refreshFriendsList();
        }
        if(list.getCurrSelected() != null && targetProfile != null && !targetProfile.equals(list.getCurrSelected().getProfile()))
        {
            targetProfile = list.getCurrSelected().getProfile();
            chat.updateLines(targetProfile.getMediumHash());
        }

        if(targetProfile != null)
        {
            chatBox.setSuggestion(targetProfile.isOnline() ? "" : "Friend is offline");
            chatBox.setEditable(targetProfile.isOnline());
            if (ChatHandler.hasNewMessages(targetProfile.getMediumHash()))
            {
                chat.updateLines(targetProfile.getMediumHash());
                ChatHandler.setMessagesRead(targetProfile.getMediumHash());
            }
        }
    }

    public static ArrayList<Profile> removedFriends = new ArrayList<>();
    protected boolean refreshFriendsList()
    {
        List<Profile> friends = new ArrayList<Profile>();
        List<Profile> onlineFriends = KnownUsers.getFriends().stream().filter(Profile::isOnline).collect(Collectors.toList());
        onlineFriends.sort(NameComparator.INSTANCE);
        List<Profile> offlineFriends = KnownUsers.getFriends().stream().filter(profile -> !profile.isOnline()).collect(Collectors.toList());
        offlineFriends.sort(NameComparator.INSTANCE);

        friends.addAll(onlineFriends);
        friends.addAll(offlineFriends);

        list.clearList();
        if (friends != null)
        {
            for (Profile friendProfile : friends)
            {
                ListEntryFriend friendEntry = new ListEntryFriend(this, list, friendProfile);
                if(searchEntry != null && !searchEntry.getValue().isEmpty())
                {
                    String s = searchEntry.getValue();
                    if(friendProfile.friendName.toLowerCase().contains(s.toLowerCase()))
                    {
                        if(!removedFriends.contains(friendProfile)) list.add(friendEntry);
                    }
                }
                else
                {
                    if(!removedFriends.contains(friendProfile)) list.add(friendEntry);
                }
                if(targetProfile != null && friendProfile.getFriendName().equals(targetProfile.getFriendName())) list.setSelected(friendEntry);
            }
            List<Profile> removedCopy = new ArrayList<Profile>(removedFriends);
            for(Profile removed : removedCopy)
            {
                boolean isInList = false;
                for(Profile friend : friends)
                {
                    if(friend.friendCode.equalsIgnoreCase(removed.friendCode))
                    {
                        isInList=true;
                        break;
                    }
                }
                if(!isInList)
                {
                    removedFriends.remove(removed);
                }
            }
        }
        return true;
    }

    @SuppressWarnings("all")
    public void removeFriend(Profile profile)
    {
        ConfirmScreen confirmScreen = new ConfirmScreen(t ->
        {
            if(t)
            {
                CompletableFuture.runAsync(() ->
                {
                   removedFriends.add(profile);
                   refreshFriendsList();
                   if(!ChatCallbacks.removeFriend(profile.getFriendCode(), MineTogetherClient.getUUID()))
                   {
                       profile.setFriend(false);
                       refreshFriendsList();
                   }
                });
            }
            minecraft.setScreen(new FriendsListScreen(parent));
        }, new TranslatableComponent("minetogether.removefriend.sure1"), new TranslatableComponent("minetogether.removefriend.sure2"));
        minecraft.setScreen(confirmScreen);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if(searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(c, i);
            refreshFriendsList();
            return flag;
        }
        if(chatBox.isFocused())
        {
            return chatBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if(searchEntry.isFocused())
        {
            searchEntry.setSuggestion("");
            boolean flag = searchEntry.keyPressed(i, j, k);
            refreshFriendsList();
            return flag;
        }
        if(targetProfile != null && chatBox.isFocused())
        {
            if ((i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && !chatBox.getValue().trim().isEmpty())
            {
                ChatHandler.sendMessage(targetProfile.getMediumHash(), ChatScreen.getStringForSending(chatBox.getValue()));
                chatBox.setValue("");
            }
            return chatBox.keyPressed(i, j, k);
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if(list.getCurrSelected() != null)
        {
            boolean flag = targetProfile == null || !targetProfile.equals(list.getCurrSelected().getProfile());
            if (flag) {
                Profile profile = list.getCurrSelected().getProfile();
                if (profile != null && profile.isFriend()) {
                    targetProfile = profile;
                    chat.updateLines(profile.getMediumHash());
                }
                return flag;
            }
        }
        return super.mouseClicked(d, e, i);
    }

    public static class NameComparator implements Comparator<Profile>
    {
        public static final NameComparator INSTANCE = new NameComparator();

        @Override
        public int compare(Profile profile1, Profile profile2)
        {
            String str1 = profile1.friendName;
            String str2 = profile2.friendName;

            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }

    public class ScrollingChat extends ObjectSelectionList
    {
        private ArrayList<FormattedCharSequence> lines;
        private int height;
        private int Width;
        private int top;
        private int bottom;
        private int itemHeight;

        public ScrollingChat(int width, int height)
        {
            super(Minecraft.getInstance(), width, height, 30, height, 10);
            this.height = height;
            this.width = width;
            this.top = 30;
            this.bottom = height;
            this.itemHeight = 10;
            lines = new ArrayList<>();
            if(targetProfile != null) updateLines(targetProfile.getMediumHash());
        }

        public ScrollingChat(int widthIn, int heightIn, int topIn, int bottomIn)
        {
            super(Minecraft.getInstance(), widthIn, heightIn, topIn, bottomIn, 10);
            this.height = height;
            this.width = width;
            this.top = 30;
            this.bottom = height;
            this.itemHeight = 10;
            lines = new ArrayList<>();
            if(targetProfile != null) updateLines(targetProfile.getMediumHash());
        }

        public void renderEntry(PoseStack poseStack, int index, int mouseX, int mouseY, float partialTicks)
        {
            try
            {
                FormattedCharSequence component = lines.get(index);
                int totalWidth = 110;

                int oldTotal = totalWidth;
                totalWidth += minecraft.font.width(component);

                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;

                Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);

                if(hovering)
                {
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1, 1, 1, 0.90F);
                    minecraft.font.draw(poseStack, component, oldTotal, getRowTop(index), 0xBBFFFFFF);
                    renderComponentHoverEffect(poseStack, style , mouseX, mouseY);
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                else
                {
                    minecraft.font.draw(poseStack, component, oldTotal, getRowTop(index), 0xFFFFFF);
                }
            } catch (Exception ignored) {}
        }

        @Override
        protected int getItemCount()
        {
            return lines.size();
        }

        protected void updateLines(String key)
        {
            LimitedSizeQueue<Message> tempMessages;
            int oldMaxScroll = this.getMaxScroll();
            synchronized (ircLock)
            {
                if (ChatHandler.messages == null || ChatHandler.messages.size() == 0) return;
                tempMessages = ChatHandler.messages.get(key);
            }

            ArrayList<FormattedCharSequence> oldLines = lines;
            int listHeight = this.height - (this.bottom - this.top - 4);
            lines = new ArrayList<>();
            if (tempMessages == null)
                return;
            try {
                for (Message message : tempMessages) {
                    Component display = ChatFormatter.formatLine(message);
                    if (display == null)
                        continue;
                    lines.addAll(ComponentRenderUtils.wrapComponents(display, width - 10, font));
                }
            } catch (Exception ignored) {}
            if (lines.size() > oldLines.size() && this.getScrollAmount() == oldMaxScroll) ;
            {
                this.setScrollAmount(this.getMaxScroll());
            }
        }

        private int getRowBottom(int p_getRowBottom_1_)
        {
            return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
        }

        @Override
        protected boolean isSelectedItem(int i)
        {
            return false;
        }

        @Override
        protected int getScrollbarPosition()
        {
            return 0;
        }

        @Override
        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            this.renderBackground(matrixStack);
            int i = this.getScrollbarPosition();
            int j = i + 6;
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tessellator.end();
            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int)this.getScrollAmount();

//            ScreenHelpers.drawLogo(matrixStack, font, width - 20, height + 18, 20, 30, 0.75F);
            this.renderList(matrixStack, k, l, mouseX, mouseY, partialTicks);
            this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int i1 = -100;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tessellator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            int j1 = 4;
            bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tessellator.end();

            this.renderDecorations(matrixStack, mouseX, mouseY);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        }

        @Override
        protected void renderList(PoseStack poseStack, int p_renderList_1_, int p_renderList_2_, int mouseX, int mouseY, float p_renderList_5_)
        {
            int i = lines.size();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();

            if(!lines.isEmpty())
                for (int j = 0; j < i; ++j)
                {
                    int k = this.getRowTop(j);
                    int l = this.getRowBottom(j);
                    if (l >= this.y0 && k <= this.y1)
                    {
                        int i1 = p_renderList_2_ + j * this.itemHeight + this.headerHeight;
                        int j1 = this.itemHeight - 4;
                        int k1 = this.getRowWidth();
                        //this.renderSelection &&
                        if (this.isSelectedItem(j))
                        {
                            int l1 = this.x0 + this.width / 2 - k1 / 2;
                            int i2 = this.x0 + this.width / 2 + k1 / 2;
                            RenderSystem.disableTexture();
                            float f = this.isFocused() ? 1.0F : 0.5F;
                            RenderSystem.color4f(f, f, f, 1.0F);
                            bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                            bufferbuilder.vertex((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                            bufferbuilder.vertex((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                            bufferbuilder.vertex((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                            bufferbuilder.vertex((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                            tessellator.end();
                            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                            bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                            bufferbuilder.vertex((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                            bufferbuilder.vertex((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                            bufferbuilder.vertex((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                            bufferbuilder.vertex((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
                            tessellator.end();
                            RenderSystem.enableTexture();
                        }
                        renderEntry(poseStack, j, mouseX, mouseY, p_renderList_5_);
                    }
                }
        }
    }
}
