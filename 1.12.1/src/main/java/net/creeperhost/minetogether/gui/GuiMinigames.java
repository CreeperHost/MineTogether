package net.creeperhost.minetogether.gui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompatCensor;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GuiMinigames extends GuiScreen
{
    private List<Minigame> minigames;
    private GuiScrollingMinigames minigameScroll;
    private static HashMap<Integer, ResourceLocation> minigameTexturesCache = new HashMap<>();
    private static HashMap<Integer, Pair<Integer, Integer>> minigameTexturesSize = new HashMap<>();
    private GuiButton settingsButton;
    private static File credentialsFile = new File("config/minetogether/credentials.json");
    private static String key = "";
    private static String secret = "";
    private boolean credentialsValid = false;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    private String loginFailureMessage;
    private static Settings settings;

    public GuiMinigames()
    {
        loadCredentials();
        executor.submit(() -> minigames = Callbacks.getMinigames(false));
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
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawStatusString();
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
            Minecraft.getMinecraft().displayGuiScreen(settings = new Settings());
        }
    }

    private boolean areCredentialsValid()
    {
        Aries aries = new Aries(key, secret);
        Map resp = aries.doApiCall("os", "systemstate");
        System.out.println(resp.containsKey("status") && resp.get("status").equals("success"));
        return resp.containsKey("status") && resp.get("status").equals("success");
    }

    private Future<Boolean> checkCredentials()
    {
        return executor.submit( () -> {
            State.pushState(State.CHECKING_CREDENTIALS);
            credentialsValid = areCredentialsValid();
            State.pushState(credentialsValid ? State.CREDENTIALS_OK : State.CREDENTIALS_INVALID);
            return credentialsValid;
        });
    }

    private void loadCredentials()
    {
        if (credentialsFile.exists())
        {
            try {
                String creds = FileUtils.readFileToString(credentialsFile);
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(creds);
                if (el.isJsonObject())
                {
                    JsonObject obj = el.getAsJsonObject();
                    key = obj.get("key").getAsString();
                    secret = obj.get("secret").getAsString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            credentialsFile.getParentFile().mkdirs();
        }
        checkCredentials();
    }

    private void saveCredentials()
    {
        credentialsFile.getParentFile().mkdirs();

        HashMap<String, String> creds = new HashMap<>();
        creds.put("key", key);
        creds.put("secret", secret);
        try {
            FileUtils.writeStringToFile(credentialsFile, new Gson().toJson(creds));
        } catch (IOException e) {
        }
    }

    private void drawStatusString()
    {
        String drawText;
        int drawColour;
        State state = State.getCurrentState();
        switch (state)
        {
            case LOGGING_IN:
                drawText = "Logging in...";
                drawColour = 0xFFFFFFFF;
                break;
            case LOGIN_FAILURE:
                drawText = loginFailureMessage;
                drawColour = 0xFFFFFFFF;
                break;
            case CREDENTIALS_OK:
                drawText = "Credentials are stored and are valid";
                drawColour = 0xFF00FF00;
                break;
            case STARTING_MINIGAME:
                drawText = "Starting Minigame";
                drawColour = 0xFFFFFFFF;
                break;
            case CREDENTIALS_INVALID:
                drawText = "No credentials found or are invalid - please log in";
                drawColour = 0xFFFF0000;
                break;
            case TWOFACTOR_FAILURE:
                drawText = loginFailureMessage;
                drawColour = 0xFFFF0000;
                break;
            case TWOFACTOR_NEEDED:
                drawText = loginFailureMessage;
                drawColour = 0xFFFFFFFF;
                break;

            case CHECKING_CREDENTIALS:
            default:
                drawText = "Checking credentials...";
                drawColour = 0xFFFFFFFF;
        }

        drawCenteredSplitString(drawText, width / 2, height / 2, drawColour);
    }

    private void drawCenteredSplitString(String drawText, int x, int y, int width, int drawColour)
    {

        List<String> strings = fontRendererObj.listFormattedStringToWidth(drawText, width);
        for(String str: strings)
        {
            drawCenteredString(fontRendererObj, str, x, y, drawColour);
            y += fontRendererObj.FONT_HEIGHT;
        }
    }

    private enum State {
        LOGGING_IN, CHECKING_CREDENTIALS, CREDENTIALS_OK, CREDENTIALS_INVALID, LOGIN_FAILURE, TWOFACTOR_NEEDED, STARTING_MINIGAME, LOGIN_SUCCESS, TWOFACTOR_FAILURE;

        private static State currentState = CHECKING_CREDENTIALS;

        public static void pushState(State state)
        {
            switch (state) {
                case LOGGING_IN:
                    if (GuiMinigames.settings != null) {
                        GuiMinigames.settings.emailField.setEnabled(false);
                        GuiMinigames.settings.passwordField.setEnabled(false);
                        GuiMinigames.settings.oneCodeField.setEnabled(false);
                    }
                    break;
                case TWOFACTOR_NEEDED:
                    if (GuiMinigames.settings != null) {
                        GuiMinigames.settings.emailField.setVisible(false);
                        GuiMinigames.settings.passwordField.setVisible(false);
                        GuiMinigames.settings.oneCodeField.setVisible(true);
                        GuiMinigames.settings.oneCodeField.setEnabled(true);
                        GuiMinigames.settings.emailLabel.visible = false;
                        GuiMinigames.settings.passwordLabel.visible = false;
                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
                    break;
                case CREDENTIALS_OK:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.emailField.setVisible(false);
                        GuiMinigames.settings.passwordField.setVisible(false);
                        GuiMinigames.settings.oneCodeField.setVisible(false);
                        GuiMinigames.settings.emailLabel.visible = false;
                        GuiMinigames.settings.passwordLabel.visible = false;
                        GuiMinigames.settings.oneCodeLabel.visible = false;
                    }
                    break;
                case LOGIN_FAILURE:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.emailField.setEnabled(true);
                        GuiMinigames.settings.passwordField.setEnabled(true);
                        GuiMinigames.settings.emailField.setVisible(true);
                        GuiMinigames.settings.passwordField.setVisible(true);
                        GuiMinigames.settings.emailLabel.visible = true;
                        GuiMinigames.settings.passwordLabel.visible = true;
                    }
                    break;
                case TWOFACTOR_FAILURE:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.oneCodeField.setEnabled(true);
                        GuiMinigames.settings.oneCodeField.setVisible(true);
                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
            }
            currentState = state;
        }

        public static State getCurrentState()
        {
            return currentState;
        }

        public static void refreshState() {
            pushState(currentState);
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
        public GuiTextFieldCompat emailField;
        public GuiLabel emailLabel;
        public GuiTextFieldCompat passwordField;
        public GuiLabel passwordLabel;
        public GuiTextFieldCompat oneCodeField;
        public GuiLabel oneCodeLabel;
        public GuiButton cancelButton;
        public GuiButton loginButton;
        public GuiButton loginAgainButton;
        private boolean previous2fa;

        @Override
        public void initGui() {
            super.initGui();
            labelList.clear();

            emailField = new GuiTextFieldCompat(80856, fontRendererObj, width / 2 - 100, height / 2 - 20, 200, 20);
            labelList.add(emailLabel = new GuiLabel(fontRendererObj, 80856, emailField.xPosition, emailField.yPosition - 10, 200, 20, 0xFFFFFFFF));
            emailLabel.addLine("Email");

            oneCodeField = new GuiTextFieldCompat(808567, fontRendererObj, width / 2 - 100, emailField.yPosition - 10, 200, 20);
            labelList.add(oneCodeLabel = new GuiLabel(fontRendererObj, 80856, oneCodeField.xPosition, oneCodeField.yPosition - 10, 200, 20, 0xFFFFFFFF));
            oneCodeLabel.addLine("One-time code");

            passwordField = new GuiTextFieldCompatCensor(80855, fontRendererObj, width / 2 - 100, height / 2 + 10, 200, 20);
            labelList.add(passwordLabel = new GuiLabel(fontRendererObj, 80856, passwordField.xPosition, passwordField.yPosition - 10, 200, 20, 0xFFFFFFFF));
            passwordLabel.addLine("Password");

            buttonList.add(cancelButton = new GuiButton(8085, width - 10 - 100, height - 5 - 20, 100, 20, "Cancel"));
            buttonList.add(loginButton = new GuiButton(8089, 5, height - 5 - 20, 100, 20, "Save"));
            buttonList.add(loginAgainButton = new GuiButton(8090, width / 2 - 50, height / 2 - 10, 100, 20, "Login again"));

            State.refreshState();
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            emailField.myMouseClicked(mouseX, mouseY, mouseButton);
            passwordField.myMouseClicked(mouseX, mouseY, mouseButton);
            oneCodeField.myMouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            emailField.textboxKeyTyped(typedChar, keyCode);
            passwordField.textboxKeyTyped(typedChar, keyCode);
            oneCodeField.textboxKeyTyped(typedChar, keyCode);
            if (oneCodeField.getText().trim().length() == 6)
            {
                actionPerformed(loginButton);
            }
            super.keyTyped(typedChar, keyCode);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();
            emailField.drawTextBox();
            passwordField.drawTextBox();
            oneCodeField.drawTextBox();
            super.drawScreen(mouseX, mouseY, partialTicks);
            if (State.getCurrentState() == State.CREDENTIALS_OK)
            {
                drawCenteredSplitString("You have valid credentials. If you wish to change your credentials, please log in again.",width / 2, height / 2 - 30, 0xFFFFFFFF);
            }
            drawStatusString();
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            if (button == cancelButton)
            {
                Minecraft.getMinecraft().displayGuiScreen(GuiMinigames.this);
            } else if (button == loginButton) {
                executor.submit(
                        () -> {
                    Map<String, String> credentials = new HashMap<>();
                    credentials.put("email", emailField.getText());
                    credentials.put("password", passwordField.getText());
                    credentials.put("oneCode", oneCodeField.getText());
                    State.pushState(State.LOGGING_IN);
                    String resp = WebUtils.postWebResponse("https://staging-panel.creeper.host/mt.php", credentials);


                    System.out.println(resp);


                    JsonParser parser = new JsonParser();
                    JsonElement el = parser.parse(resp);
                    if (el.isJsonObject())
                    {
                        JsonObject obj = el.getAsJsonObject();
                        if (obj.get("success").getAsBoolean())
                        {
                            key = obj.get("key").getAsString();
                            secret = obj.get("secret").getAsString();
                            try {
                                if (checkCredentials().get())
                                {
                                    saveCredentials();
                                }
                            } catch (InterruptedException e) {
                            } catch (ExecutionException e) {
                            }
                        } else {
                            if (obj.has("_2fa") && !obj.get("_2fa").isJsonNull() && obj.get("_2fa").getAsBoolean())
                            {
                                if (previous2fa)
                                {
                                    State.pushState(State.TWOFACTOR_FAILURE);
                                    loginFailureMessage = "Invalid code. Please try again or reset it by logging into the CreeperPanel";
                                }
                                State.pushState(State.TWOFACTOR_NEEDED);
                                loginFailureMessage = "Please enter your two-factor code";
                                previous2fa = true;
                                return;
                            }
                            State.pushState(State.LOGIN_FAILURE);
                            String tempLoginFailure = obj.get("message").getAsString();
                            loginFailureMessage = tempLoginFailure.isEmpty() ? "Login failed. Please ensure you have entered your username and password correctly." : loginFailureMessage;
                        }
                    }
                });
            } else if (button == loginAgainButton) {
                State.pushState(State.CREDENTIALS_INVALID);
            }
        }
    }
}
