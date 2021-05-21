//package net.creeperhost.minetogether.module.multiplayer.data;
//
//import com.google.common.collect.Lists;
//import com.google.common.hash.Hashing;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.creeperhost.minetogether.MineTogether;
//import net.creeperhost.minetogether.module.multiplayer.screen.JoinMultiplayerScreenPublic;
//import net.creeperhost.minetogetherlib.serverlists.EnumFlag;
//import net.creeperhost.minetogetherlib.serverlists.Server;
//import net.creeperhost.minetogetherlib.serverorder.ServerOrderCallbacks;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
//import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
//import net.minecraft.client.multiplayer.ServerData;
//import net.minecraft.client.multiplayer.ServerList;
//import net.minecraft.client.renderer.texture.DynamicTexture;
//import net.minecraft.client.server.LanServer;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import org.apache.commons.lang3.Validate;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//
//public class ServerSelectionListPublic extends ServerSelectionList
//{
//    public List<ServerListEntryPublic> serverListInternetOurs = Lists.newArrayList();
//    public JoinMultiplayerScreen multiplayerScreen;
//
//    public ServerSelectionListPublic(JoinMultiplayerScreen multiplayerScreen, Minecraft mc, int width, int height, int top, int bottom, int slotHeight)
//    {
//        super(multiplayerScreen, mc, width, height, top, bottom, slotHeight);
//        this.multiplayerScreen = multiplayerScreen;
//    }
//
//    @Override
//    public void setList()
//    {
//        this.clearEntries();
//        this.serverListInternetOurs.forEach(this::addEntry);
//    }
//
//    @Override
//    protected int addEntry(Entry p_addEntry_1_)
//    {
//        return super.addEntry(p_addEntry_1_);
//    }
//
//    @Override
//    public void updateOnlineServers(ServerList p_148195_1_)
//    {
//        this.serverListInternet.clear();
//        this.serverListLan.clear();
//        this.serverListInternetOurs.clear();
//
//        for (int i = 0; i < p_148195_1_.size(); ++i)
//        {
//            this.serverListInternetOurs.add(new ServerSelectionListPublic.ServerListEntryPublic((JoinMultiplayerScreen) this.multiplayerScreen, p_148195_1_.getServerData(i)));
//        }
//
//        this.setList();
//    }
//
//    @Override
//    public void updateNetworkServers(List<LanServer> p_148194_1_) {}
//
//    public class ServerListEntryPublic extends ServerSelectionList.OnlineServerEntry
//    {
//        JoinMultiplayerScreenPublic multiplayerScreen;
//        ServerData wrappedEntry;
//        Minecraft mc = Minecraft.getInstance();
//        private ResourceLocation flags = new ResourceLocation(MineTogether.MOD_ID, "textures/flags/flags.png");
//        private ResourceLocation applicationGui = new ResourceLocation(MineTogether.MOD_ID, "textures/gui.png");
//        private String lastIconB64;
//        private ResourceLocation serverIcon;
//        private DynamicTexture icon;
//        private long lastClickTime;
//
//        public ServerListEntryPublic(JoinMultiplayerScreenPublic multiplayerScreen, ServerData wrappedEntry)
//        {
//            super(multiplayerScreen, wrappedEntry);
//            this.multiplayerScreen = multiplayerScreen;
//            this.wrappedEntry = wrappedEntry;
//            this.serverIcon = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(wrappedEntry.serverIP) + "/icon");
//            this.icon = (DynamicTexture)this.mc.getTextureManager().getTexture(this.serverIcon);
//        }
//
//        @Override
//        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isHovering, float p_render_9_)
//        {
//            renderVanilla(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isHovering, p_render_9_);
//
//            Server server = getServerData().server;
//            EnumFlag flag = server.flag;
//            String applicationURL = server.applicationURL;
//            if (flag != null)
//            {
//                Minecraft.getInstance().getTextureManager().bind(flags);
//                int flagWidth = 16;
//                int flagHeight = flag.height / (flag.width / flagWidth);
//                drawScaledCustomSizeModalRect(x + listWidth - 5 - flagWidth, y + slotHeight - 10 - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
//                if (mouseX >= x + listWidth - 5 - flagWidth
//                        && mouseX <= x + listWidth - 5
//                        && mouseY >= y + slotHeight - 10 - flagHeight
//                        && mouseY <= y + slotHeight - flagHeight + flagHeight)
//                {
//                    List<Component> tooltipList = new ArrayList<>();
//
//                    String countryName = ServerOrderCallbacks.getCountries().get(flag.name());
//                    if (countryName == null)
//                    {
//                        countryName = flag.name();
//                    }
//                    tooltipList.add(new TranslatableComponent(countryName));
//                    multiplayerScreen.func_238854_b_(tooltipList);//(countryName + (server.subdivision.equals("Unknown") ? "" : "\n" + server.subdivision));
//                }
//            }
//            if (applicationURL != null)
//            {
//                Minecraft.getInstance().getTextureManager().bind(applicationGui);
//                int flagWidth = 16;
//                int flagHeight = flag.height / (flag.width / flagWidth);
//                blit(matrixStack, x, y + slotHeight - 10 - flagHeight, flag.x, flag.y, flag.width, flag.height, flagWidth, flagHeight, 512, 512);
//                if (mouseX >= x
//                        && mouseX <= x + flagWidth
//                        && mouseY >= y + slotHeight - flagHeight
//                        && mouseY <= y + slotHeight - flagHeight + flagHeight)
//                {
//                    List<Component> tooltipList = new ArrayList<>();
//                    tooltipList.add(new TranslatableComponent("Click here to open the application link in a browser window!"));
//                    multiplayerScreen.func_238854_b_(tooltipList);
//                }
//            }
//        }
//
//        ServerData server = getServerData();
//
//        public void renderVanilla(PoseStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_) {
//            if (!server.pinged) {
//                server.pinged = true;
//                server.pingToServer = -2L;
//                server.serverMOTD = StringTextComponent.EMPTY;
//                server.populationInfo = StringTextComponent.EMPTY;
//                ServerSelectionList.field_214358_b.submit(() -> {
//                    try {
//                        this.multiplayerScreen.getOldServerPinger().ping(server, () -> {
//                            this.mc.execute(this::func_241613_a_);
//                        });
//                    } catch (UnknownHostException unknownhostexception) {
//                        server.pingToServer = -1L;
//                        server.serverMOTD = ServerSelectionList.field_243366_s;
//                    } catch (Exception exception) {
//                        server.pingToServer = -1L;
//                        server.serverMOTD = ServerSelectionList.field_243367_t;
//                    }
//
//                });
//            }
//
//            boolean flag = server.version > SharedConstants.getVersion().getProtocolVersion();
//            boolean flag1 = server.version < SharedConstants.getVersion().getProtocolVersion();
//            boolean flag2 = flag || flag1;
//            this.mc.fontRenderer.drawString(p_230432_1_, server.serverName, (float)(p_230432_4_ + 32 + 3), (float)(p_230432_3_ + 1), 16777215);
//            List<IReorderingProcessor> list = this.mc.fontRenderer.trimStringToWidth(server.serverMOTD, p_230432_5_ - 32 - 2);
//
//            for(int i = 0; i < Math.min(list.size(), 2); ++i) {
//                this.mc.fontRenderer.func_238422_b_(p_230432_1_, list.get(i), (float)(p_230432_4_ + 32 + 3), (float)(p_230432_3_ + 12 + 9 * i), 8421504);
//            }
//
//            ITextComponent itextcomponent1 = (ITextComponent)(server.populationInfo);
//            int j = this.mc.fontRenderer.getStringPropertyWidth(itextcomponent1);
//            this.mc.fontRenderer.func_243248_b(p_230432_1_, itextcomponent1, (float)(p_230432_4_ + p_230432_5_ - j - 15 - 2), (float)(p_230432_3_ + 1), 8421504);
//            int k = 0;
//            int l;
//            List<ITextComponent> list1;
//            ITextComponent itextcomponent;
//            if (server.pinged && server.pingToServer != -2L) {
//                if (server.pingToServer < 0L) {
//                    l = 5;
//                } else if (server.pingToServer < 150L) {
//                    l = 0;
//                } else if (server.pingToServer < 300L) {
//                    l = 1;
//                } else if (server.pingToServer < 600L) {
//                    l = 2;
//                } else if (server.pingToServer < 1000L) {
//                    l = 3;
//                } else {
//                    l = 4;
//                }
//
//                if (server.pingToServer < 0L) {
//                    itextcomponent = ServerSelectionList.field_243370_w;
//                    list1 = Collections.emptyList();
//                } else {
//                    itextcomponent = new TranslationTextComponent("multiplayer.status.ping", server.pingToServer);
//                    list1 = server.playerList;
//                }
//            } else {
//                k = 1;
//                l = (int)(Util.milliTime() / 100L + (long)(p_230432_2_ * 2) & 7L);
//                if (l > 4) {
//                    l = 8 - l;
//                }
//
//                itextcomponent = ServerSelectionList.field_243371_x;
//                list1 = Collections.emptyList();
//            }
//
//            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            this.mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
//            AbstractGui.blit(p_230432_1_, p_230432_4_ + p_230432_5_ - 15, p_230432_3_, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256, 256);
//            String s = server.getBase64EncodedIconData();
//            if (!Objects.equals(s, this.lastIconB64)) {
//                if (this.func_241614_a_(s)) {
//                    this.lastIconB64 = s;
//                } else {
//                    server.setBase64EncodedIconData((String)null);
//                    this.func_241613_a_();
//                }
//            }
//
//            if (this.icon != null) {
//                this.func_238859_a_(p_230432_1_, p_230432_4_, p_230432_3_, this.serverIcon);
//            } else {
//                this.func_238859_a_(p_230432_1_, p_230432_4_, p_230432_3_, ServerSelectionList.field_214359_c);
//            }
//
//            int i1 = p_230432_7_ - p_230432_4_;
//            int j1 = p_230432_8_ - p_230432_3_;
//            if (i1 >= p_230432_5_ - 15 && i1 <= p_230432_5_ - 5 && j1 >= 0 && j1 <= 8) {
//                this.multiplayerScreen.func_238854_b_(Collections.singletonList(itextcomponent));
//            } else if (i1 >= p_230432_5_ - j - 15 - 2 && i1 <= p_230432_5_ - 15 - 2 && j1 >= 0 && j1 <= 8) {
//                this.multiplayerScreen.func_238854_b_(list1);
//            }
//
//            if (this.mc.gameSettings.touchscreen || p_230432_9_) {
//                this.mc.getTextureManager().bindTexture(ServerSelectionList.field_214360_d);
//                AbstractGui.fill(p_230432_1_, p_230432_4_, p_230432_3_, p_230432_4_ + 32, p_230432_3_ + 32, -1601138544);
//                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//                int k1 = p_230432_7_ - p_230432_4_;
//                int l1 = p_230432_8_ - p_230432_3_;
//                if (this.canJoin()) {
//                    if (k1 < 32 && k1 > 16) {
//                        AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 0.0F, 32.0F, 32, 32, 256, 256);
//                    } else {
//                        AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 0.0F, 0.0F, 32, 32, 256, 256);
//                    }
//                }
//
//                if (p_230432_2_ < this.multiplayerScreen.getServerList().countServers() - 1) {
//                    if (k1 < 16 && l1 > 16) {
//                        AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 64.0F, 32.0F, 32, 32, 256, 256);
//                    } else {
//                        AbstractGui.blit(p_230432_1_, p_230432_4_, p_230432_3_, 64.0F, 0.0F, 32, 32, 256, 256);
//                    }
//                }
//            }
//        }
//
//        //Vanilla Copy paste
//        private boolean func_241614_a_(@Nullable String p_241614_1_) {
//            ServerData server = getServerData();
//            if (p_241614_1_ == null) {
//                this.mc.getTextureManager().deleteTexture(this.serverIcon);
//                if (this.icon != null && this.icon.getTextureData() != null) {
//                    this.icon.getTextureData().close();
//                }
//
//                this.icon = null;
//            } else {
//                try {
//                    NativeImage nativeimage = NativeImage.readBase64(p_241614_1_);
//                    Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
//                    Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
//                    if (this.icon == null) {
//                        this.icon = new DynamicTexture(nativeimage);
//                    } else {
//                        this.icon.setTextureData(nativeimage);
//                        this.icon.updateDynamicTexture();
//                    }
//
//                    this.mc.getTextureManager().loadTexture(this.serverIcon, this.icon);
//                } catch (Throwable throwable) {
//                    ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", server.serverName, server.serverIP, throwable);
//                    return false;
//                }
//            }
//
//            return true;
//        }
//
//        public void func_241613_a_() {
//            this.multiplayerScreen.getServerList().saveServerList();
//        }
//
//        private boolean canJoin()
//        {
//            return true;
//        }
//
//        @Override
//        public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_) {
//            double d0 = mouseX - (double)ServerSelectionListOurs.this.getRowLeft();
//            double d1 = mouseY - (double)ServerSelectionListOurs.this.getRowTop(ServerSelectionListOurs.this.getEventListeners().indexOf(this));
//            if (d0 <= 32.0D) {
//                if (d0 < 32.0D && d0 > 16.0D && this.canJoin()) {
//                    this.multiplayerScreen.func_214287_a(this);
//                    this.multiplayerScreen.connectToSelected();
//                    return true;
//                }
//            }
//
//            this.multiplayerScreen.func_214287_a(this);
//            if (Util.milliTime() - this.lastClickTime < 250L) {
//                this.multiplayerScreen.connectToSelected();
//            }
//
//            this.lastClickTime = Util.milliTime();
//            return false;
//        }
//
//        public void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
//        {
//            float f = 1.0F / tileWidth;
//            float f1 = 1.0F / tileHeight;
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder bufferbuilder = tessellator.getBuffer();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//            bufferbuilder.pos((double) x, (double) (y + height), 0.0D).tex((u * f), ((v + (float) vHeight) * f1)).endVertex();
//            bufferbuilder.pos((double) (x + width), (double) (y + height), 0.0D).tex(((u + (float) uWidth) * f), ((v + (float) vHeight) * f1)).endVertex();
//            bufferbuilder.pos((double) (x + width), (double) y, 0.0D).tex(((u + (float) uWidth) * f), (v * f1)).endVertex();
//            bufferbuilder.pos((double) x, (double) y, 0.0D).tex((u * f), (v * f1)).endVertex();
//            tessellator.draw();
//        }
//
//        @Override
//        public ServerDataPublic getServerData()
//        {
//            return (ServerDataPublic) super.getServerData();
//        }
//    }
//}
