package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogether.screen.SettingsScreen;
import net.creeperhost.minetogether.util.ComponentUtils;
import net.creeperhost.minetogethergui.ScreenHelpers;
import net.creeperhost.minetogethergui.gif.AnimatedGif;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogethergui.widgets.DropdownButton;
import net.creeperhost.minetogethergui.widgets.Target;
import net.creeperhost.minetogetherlib.chat.*;
import net.creeperhost.minetogetherlib.chat.data.Message;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogetherlib.util.LimitedSizeQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogetherlib.chat.ChatHandler.ircLock;

public class ChatScreen extends Screen
{
    private final Screen parent;
    private ScrollingChat chat;
    private EditBox send;
    private String currentTarget = ChatHandler.CHANNEL;
    private ButtonString connectionStatus;
    public DropdownButton<Target> targetDropdownButton;
    private DropdownButton<Menu> menuDropdownButton;
    private String activeDropdown;
    private AnimatedGif gifImage;
    private AnimatedGif.GifPlayer gifPlayer;

    public ChatScreen(Screen parent)
    {
        super(new TranslatableComponent("MineTogether Chat"));
        this.parent = parent;
    }

    public ChatScreen(Screen parent, String currentTarget)
    {
        super(new TranslatableComponent("MineTogether Chat"));
        this.parent = parent;
        this.currentTarget = currentTarget;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        chat = new ScrollingChat(width, height);
        chat.setLeftPos(10);
        send = new EditBox(minecraft.font, 11, this.height - 48, width - 22, 20, new TranslatableComponent(""));
        send.setFocus(true);
        send.setMaxLength(256);

        addButtons();
        super.init();
    }

    public void addButtons()
    {
        addButton(targetDropdownButton = new DropdownButton<>(width - 5 - 100, 5, 100, 20, new TranslatableComponent("Chat: %s"), Target.getMainTarget(), true, p ->
        {
            if(!targetDropdownButton.dropdownOpen) return;
            if(!targetDropdownButton.getSelected().getInternalTarget().equals(currentTarget)) currentTarget = targetDropdownButton.getSelected().getInternalTarget();

            chat.updateLines(currentTarget);

            targetDropdownButton.wasJustClosed = false;
            targetDropdownButton.dropdownOpen = false;
        }));
        targetDropdownButton.setSelected(Target.getMainTarget());
        List<String> strings = new ArrayList<>();
        strings.add(I18n.get("minetogether.chat.button.mute"));
        strings.add(I18n.get("minetogether.chat.button.addfriend"));
        strings.add(I18n.get("minetogether.chat.button.mention"));
        addButton(menuDropdownButton = new DropdownButton<>(-1000, -1000, 100, 20, new TranslatableComponent("Menu"), new Menu(strings), false, p ->
        {
            if(!menuDropdownButton.dropdownOpen) return;

            if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.mute")))
            {
                ChatModule.muteUser(KnownUsers.findByDisplay(activeDropdown).longHash);
                ChatHandler.addStatusMessage("Locally muted " + currentTarget);
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.addfriend")))
            {
                minecraft.setScreen(new FriendRequestScreen(new ChatScreen(parent), Minecraft.getInstance().getUser().getName(), KnownUsers.findByDisplay(activeDropdown), ChatCallbacks.getFriendCode(MineTogetherClient.getUUID()), "", false));
            }
            else if (menuDropdownButton.getSelected().option.equalsIgnoreCase(I18n.get("minetogether.chat.button.mention")))
            {
                this.send.setFocus(true);
                this.send.setValue(this.send.getValue() + " " + activeDropdown + " ");
            }
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            menuDropdownButton.dropdownOpen = false;
        }));

        addButton(new Button(5, 5, 100, 20, new TranslatableComponent("Friends list"), p ->
        {
            this.minecraft.setScreen(new FriendsListScreen(this));
        }));
        addButton(new ButtonMultiple(width - 124, 5, 3, p ->
        {
            this.minecraft.setScreen(new SettingsScreen(this));
        }));
        addButton(new Button(width - 100 - 5, height - 5 - 20, 100, 20, new TranslatableComponent("Cancel"), p ->
        {
            this.minecraft.setScreen(parent);
        }));
        addButton(connectionStatus = new ButtonString(8, height - 20, 70, 20, () ->
        {
            ChatConnectionStatus status = ChatHandler.connectionStatus;
            return new TranslatableComponent(ChatFormatting.getByName(status.colour) + "\u2022" + " " + ChatFormatting.WHITE + status.display);
        }, ButtonString.RenderPlace.EXACT, button ->
        {
            if(ChatHandler.connectionStatus == ChatConnectionStatus.BANNED)
            {
                ConfirmScreen confirmScreen = new ConfirmScreen(t ->
                {
                    if(t)
                    {
                        try
                        {
                            Util.getPlatform().openUrl(new URL("https://minetogether.io/profile/standing"));
                        } catch (MalformedURLException e) { e.printStackTrace(); }
                    }
                    minecraft.setScreen(this);
                }, new TranslatableComponent("minetogether.bannedscreen.line1"), new TranslatableComponent("minetogether.bannedscreen.line2"));

                minecraft.setScreen(confirmScreen);
            }
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);

        renderConnectionStatus();
        chat.render(poseStack, mouseX, mouseY, partialTicks);
        menuDropdownButton.render(poseStack, mouseX, mouseY, partialTicks);
        send.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 5, 0xFFFFFF);

        if(gifPlayer != null) gifPlayer.render(poseStack, mouseX + 5, mouseY + 5, 60, 40, partialTicks);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void renderConnectionStatus()
    {
        ChatConnectionStatus chatConnectionStatus = ChatHandler.connectionStatus;
        Component comp = new TranslatableComponent(ChatFormatting.getByName(chatConnectionStatus.colour) + "\u2022" + " " + ChatFormatting.WHITE + chatConnectionStatus.display);
        connectionStatus.setMessage(comp);
    }

    @Override
    public void tick()
    {
        if(gifPlayer != null) gifPlayer.tick();
        String buttonTarget = targetDropdownButton.getSelected().getInternalTarget();
        if (!buttonTarget.equals(currentTarget)) currentTarget = buttonTarget;

        send.active = ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED;
        send.setEditable(ChatHandler.connectionStatus == ChatConnectionStatus.VERIFIED);
        //Remove focus if the client is not verified
        if(send.isFocused() && ChatHandler.connectionStatus != ChatConnectionStatus.VERIFIED)
        {
            send.setFocus(false);
        }

        switch (ChatHandler.connectionStatus)
        {
            case VERIFYING:
                send.setSuggestion(I18n.get("minetogether.chat.message.unverified"));
                break;
            case BANNED:
                send.setSuggestion(I18n.get("minetogether.chat.message.banned"));
                break;
            case DISCONNECTED:
                send.setSuggestion(I18n.get("minetogether.chat.message.disconnect"));
                break;
            case CONNECTING:
                send.setSuggestion(I18n.get("minetogether.chat.message.connecting"));
                break;
            case VERIFIED:
                send.setSuggestion("");
                break;
        }

        synchronized (ircLock)
        {
            if (ChatHandler.hasNewMessages(currentTarget))
            {
                chat.updateLines(currentTarget);
                ChatHandler.setMessagesRead(currentTarget);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;

        if (send.mouseClicked(mouseX, mouseY, mouseButton)) {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            menuDropdownButton.dropdownOpen = false;
            return true;
        }
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.x = menuDropdownButton.y = -10000;
            menuDropdownButton.wasJustClosed = false;
            return true;
        }
        chat.mouseClicked(mouseX, mouseY, mouseButton);
        return false;//super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g)
    {
        chat.mouseDragged(d, e, i, f, g);
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f)
    {
        chat.mouseScrolled(d, e, f);
        return super.mouseScrolled(d, e, f);
    }

    @Deprecated
    public void rebuildChat()
    {
        double scroll = chat.getScrollAmount();
        chat.updateLines(currentTarget);
        chat.setScrollAmount(scroll);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if ((p_keyPressed_1_ == GLFW.GLFW_KEY_ENTER || p_keyPressed_1_ == GLFW.GLFW_KEY_KP_ENTER) && !send.getValue().trim().isEmpty())
        {
            ChatHandler.sendMessage(currentTarget, getStringForSending(send.getValue()));
            send.setValue("");
        }
        send.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        send.charTyped(c, i);
        return super.charTyped(c, i);
    }

    public boolean handleComponentClick(Style style, double mouseX, double mouseY)
    {
        if(style == null) return false;
        if(style.getClickEvent() == null) return false;
        ClickEvent event = style.getClickEvent();
        if (event == null)
        {
            return false;
        }

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

                Profile targetProfile = KnownUsers.findByNick(chatInternalName);
                if(targetProfile == null) targetProfile = KnownUsers.add(chatInternalName);

                Minecraft.getInstance().setScreen(new FriendRequestScreen(this, Minecraft.getInstance().getUser().getName(), targetProfile, friendCode, friendName, true));
                return true;
            }

            menuDropdownButton.x = (int) mouseX;
            menuDropdownButton.y = (int) mouseY;
            menuDropdownButton.flipped = mouseY > 150;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        if (event.getAction() == ClickEvent.Action.OPEN_URL)
        {
            this.handleComponentClicked(style);
        }
        return false;
    }

    //Fuck java regex, |(OR) operator doesn't work for shit, regex checked out on regex101, regexr etc.
    final static Pattern patternA = Pattern.compile("((?:user)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternB = Pattern.compile("((?:@)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternC = Pattern.compile("((?:@user)([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternD = Pattern.compile("((?:@user)#([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternE = Pattern.compile("((?:user)#([a-zA-Z0-9]+))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternF = Pattern.compile("([a-zA-Z0-9]+)#([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    final static Pattern patternG = Pattern.compile("(@[a-zA-Z0-9]+)#([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static String getStringForSending(String text)
    {
        String[] split = text.split(" ");
        boolean replaced = false;
        for (int i = 0; i < split.length; i++)
        {
            String word = split[i].toLowerCase();
            final String subst = "User#$2";
            final String substr2 = "$1#$2";

            final Matcher matcher  = patternA.matcher(word);
            final Matcher matcherb = patternB.matcher(word);
            final Matcher matcherc = patternC.matcher(word);
            final Matcher matcherd = patternD.matcher(word);
            final Matcher matchere = patternE.matcher(word);
            final Matcher matcherf = patternF.matcher(word);
            final Matcher matcherg = patternG.matcher(word);

            String justNick = word;
            String result = word;
            String result2 = "";
            if(matcher.matches())
            {
                result = matcher.replaceAll(subst);
            } else if(matcherb.matches())
            {
                result = matcherb.replaceAll(subst);
            } else if(matcherc.matches())
            {
                result = matcherc.replaceAll(subst);
            }
            else if(matcherd.matches())
            {
                result = matcherd.replaceAll(subst);
            }
            else if(matchere.matches())
            {
                result = matchere.replaceAll(subst);
            }
            else if(matcherg.matches())
            {
                result2 = matcherg.replaceAll(substr2);
            } else if(matcherf.matches())
            {
                result2 = matcherf.replaceAll(substr2);
            }
            if(result.startsWith("User") || result2.length() > 0)
            {
                if(result2.length() > 0)
                {
                    justNick = result2.replaceAll("[^A-Za-z0-9#]", "");
                } else {
                    justNick = result.replaceAll("[^A-Za-z0-9#]", "");
                }
                Profile profile = KnownUsers.findByDisplay(justNick);
                if(profile == null)
                {
                    continue;
                }
                String tempWord = profile.getShortHash();
                if (tempWord != null)
                {
                    split[i] = result.replaceAll(justNick, tempWord);
                    replaced = true;
                }
                else if (justNick.toLowerCase().equals(Minecraft.getInstance().getUser().getName()))
                {
                    split[i] = result.replaceAll(justNick, MineTogetherChat.INSTANCE.ourNick);
                    replaced = true;
                }
            }
        }
        if(replaced)
        {
            text = String.join(" ", split);
        }
        return text;
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
            super(Minecraft.getInstance(), width - 20, height - 50, 30, height - 50, 10);
            this.height = height - 50;
            this.width = width - 20;
            this.top = 30;
            this.bottom = height - 50;
            this.itemHeight = 10;
            lines = new ArrayList<>();
            updateLines(currentTarget);
        }

        public void renderEntry(PoseStack poseStack, int index, int mouseX, int mouseY, float partialTicks)
        {
            try
            {
                FormattedCharSequence component = lines.get(index);
                int totalWidth = 5;

                int oldTotal = totalWidth;
                totalWidth += minecraft.font.width(component);

                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(index) && mouseY < getRowTop(index) + itemHeight;

                Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);

                if(hovering)
                {
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1, 1, 1, 0.90F);
                    minecraft.font.draw(poseStack, component, 8 + oldTotal, getRowTop(index), 0xBBFFFFFF);
                    renderComponentHoverEffect(poseStack, style , mouseX, mouseY);
                    if(style.getHoverEvent() != null && style.getHoverEvent().getAction() == ComponentUtils.RENDER_GIF)
                    {
                        Component urlComponent = (Component)style.getHoverEvent().getValue(ComponentUtils.RENDER_GIF);
                        String url = urlComponent.getString();
                        gifImage = AnimatedGif.fromURL(new URL(url));
                        if(gifImage != null && gifPlayer == null)
                        {
                            gifPlayer = gifImage.makeGifPlayer();
                            gifPlayer.setAutoplay(true);
                            gifPlayer.setLooping(true);
                        }
                    }
                    else
                    {
                        gifPlayer = null;
                    }
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                else
                {
                    minecraft.font.draw(poseStack, component, 8 + oldTotal, getRowTop(index), 0xFFFFFF);
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
            return width + 4;
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

            ScreenHelpers.drawLogo(matrixStack, font, width - 20, height + 18, 20, 30, 0.75F);
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
            int k1 = this.getMaxScroll();
            if (k1 > 0) {
                int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
                int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
                if (i2 < this.y0) {
                    i2 = this.y0;
                }

                bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.end();
            }

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

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
        {
            for (int i = 0; i < lines.size(); i++)
            {
                FormattedCharSequence component = lines.get(i);
                int totalWidth = 5;
                int oldTotal = totalWidth;
                totalWidth += minecraft.font.width(component);
                boolean hovering = mouseX > oldTotal && mouseX < totalWidth && mouseY > getRowTop(i) && mouseY < getRowTop(i) + itemHeight;

                if (hovering)
                {
                    Style style = minecraft.font.getSplitter().componentStyleAtWidth(component, (int) mouseX);
                    handleComponentClick(style, mouseX, mouseY);
                    return true;
                }
            }
            return false;
        }
    }

    public static class Menu implements DropdownButton.IDropdownOption
    {
        List<DropdownButton.IDropdownOption> possibleValsCache;
        public String option;

        public Menu(List<String> options)
        {
            possibleValsCache = new ArrayList<>();
            possibleValsCache.add(this);
            option = options.get(0);
            options.remove(0);
            for (String option : options)
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
