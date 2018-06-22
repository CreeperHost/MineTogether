package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class GuiMinigames extends GuiScreen
{
    private List<Minigame> minigames;
    private GuiScrollingMinigames minigameScroll;
    private boolean first = true;
    private static HashMap<Integer, ResourceLocation> minigameTexturesCache = new HashMap<>();
    private static HashMap<Integer, Pair<Integer, Integer>> minigameTexturesSize = new HashMap<>();
    private GuiButton settingsButton;

    public GuiMinigames()
    {

    }

    @Override
    public void initGui()
    {
        super.initGui();
        minigameScroll = new GuiScrollingMinigames(34);
        buttonList.add(settingsButton = new GuiButton(808, width - 10 - 100, 5, 100, 20, "Login"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        minigameScroll.drawScreen(mouseX, mouseY, partialTicks);
        if (first)
        {
            first = false;
            minigames = Callbacks.getMinigames(false);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void drawTextureAt(int p_178012_1_, int p_178012_2_, int texturew, int textureh, int width, int height, ResourceLocation p_178012_3_)
    {
        this.mc.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, width, height, texturew, textureh);
        GlStateManager.disableBlend();
    }


    @Override
    public void actionPerformed(GuiButton button)
    {
        if (button == settingsButton)
        {
            Minecraft.getMinecraft().displayGuiScreen(new Settings());
        }
    }

    private class GuiScrollingMinigames extends GuiScrollingList {
        public GuiScrollingMinigames(int entryHeight) {
            super(Minecraft.getMinecraft(), GuiMinigames.this.width - 20, GuiMinigames.this.height - 30, 30, GuiMinigames.this.height - 50, 10, entryHeight, GuiMinigames.this.width, GuiMinigames.this.height);
        }

        @Override
        protected int getSize() {
            return minigames == null ? 1 : minigames.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            selectedIndex = index;
        }

        @Override
        protected boolean isSelected(int index) {
            return selectedIndex == index;
        }

        @Override
        protected void drawBackground() {

        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            if (minigames == null) {
                drawCenteredString(fontRendererObj, "Loading minigames...", width / 2, slotTop, 0xFFFFFFFF);
            } else {
                Minigame game = minigames.get(slotIdx);

                if (!minigameTexturesCache.containsKey(game.id)) {
                    ResourceLocation resourceLocation = new ResourceLocation(CreeperHost.MOD_ID, "minigame/" + game.id);
                    BufferedImage imageData = null;
                    try {
                        imageData = ImageIO.read(new URL(game.displayIcon));
                    } catch (IOException e) {
                    }

                    if (imageData != null) {
                        DynamicTexture texture = new DynamicTexture(imageData);
                        mc.getTextureManager().loadTexture(resourceLocation, texture);
                        texture.updateDynamicTexture();
                        minigameTexturesCache.put(game.id, resourceLocation);
                        minigameTexturesSize.put(game.id, new Pair<>(imageData.getWidth(), imageData.getHeight()));
                    } else {
                        minigameTexturesCache.put(game.id, new ResourceLocation("minecraft", "textures/misc/unknown_server.png"));
                        minigameTexturesSize.put(game.id, new Pair(32, 32));
                    }
                }

                ResourceLocation resourceLocation = minigameTexturesCache.get(game.id);

                Pair<Integer, Integer> wh = minigameTexturesSize.get(game.id);

                drawTextureAt(13, slotTop + 1, 28, 28, 28, 28, resourceLocation);

                GlStateManager.pushMatrix();
                float scale = 1.5f;
                GlStateManager.scale(scale, scale, scale);
                int x = width / 2;
                int y = slotTop;
                x = (int) (x / scale);
                y = (int) (y / scale);

                drawCenteredString(fontRendererObj, minigames.get(slotIdx).displayName, x, y, 0xFFFFFFFF);

                GlStateManager.popMatrix();

                drawCenteredString(fontRendererObj, minigames.get(slotIdx).displayVersion, width / 2, slotTop + 12, 0xFFAAAAAA);
            }
        }
    }

    public class Settings extends GuiScreen {
        public GuiTextFieldCompat keyField;
        public GuiTextFieldCompat keySecret;
        public GuiButton cancelButton;
        public GuiButton saveButton;

        @Override
        public void initGui() {
            super.initGui();
            keyField = new GuiTextFieldCompat(80856, fontRendererObj, width / 2 - 100, height / 2 - 20, 200, 20);
            keySecret = new GuiTextFieldCompat(80855, fontRendererObj, width / 2 - 100, height / 2 + 10, 200, 20);
            buttonList.add(cancelButton = new GuiButton(8085, width - 10 - 100, height - 5 - 20, 100, 20, "Cancel"));
            buttonList.add(saveButton = new GuiButton(8089, 5, height - 5 - 20, 100, 20, "Save"));
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            keyField.myMouseClicked(mouseX, mouseY, mouseButton);
            keySecret.myMouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            keyField.textboxKeyTyped(typedChar, keyCode);
            keySecret.textboxKeyTyped(typedChar, keyCode);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();
            keyField.drawTextBox();
            keySecret.drawTextBox();
            super.drawScreen(mouseX, mouseY, partialTicks);

        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            if (button == cancelButton)
            {
                Minecraft.getMinecraft().displayGuiScreen(GuiMinigames.this);
            } else if (button == saveButton) {

            }
        }
    }
}
