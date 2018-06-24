package net.creeperhost.minetogether.gui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.common.Pair;
import net.creeperhost.minetogether.common.WebUtils;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompatCensor;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.serverstuffs.CreeperHostServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;
import org.lwjgl.opengl.GLSync;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static net.creeperhost.minetogether.paul.Callbacks.getPlayerHash;

public class GuiMinigames extends GuiScreen
{
    private static GuiMinigames current;
    private List<Minigame> minigames;
    private GuiScrollingMinigames minigameScroll;
    private static HashMap<Integer, ResourceLocation> minigameTexturesCache = new HashMap<>();
    private static HashMap<Integer, Pair<Integer, Integer>> minigameTexturesSize = new HashMap<>();
    private GuiButton settingsButton;
    private GuiButton spinupButton;
    private static File credentialsFile = new File("config/minetogether/credentials.json");
    private static String key = "";
    private static String secret = "";
    private boolean credentialsValid = false;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    private String loginFailureMessage;
    private static Settings settings;
    private String credit = "Retrieving...";
    private String creditType = "none";

    public GuiMinigames()
    {
        current = this;
        State.pushState(State.CHECKING_CREDENTIALS);
        loadCredentials();
        executor.submit(() -> minigames = Callbacks.getMinigames(false));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        minigameScroll = new GuiScrollingMinigames(34);
        buttonList.add(settingsButton = new GuiButton(808, width - 10 - 100, 5, 100, 20, "Login"));
        buttonList.add(spinupButton = new GuiButton(808, width - 10 - 100, height - 5 - 20, 100, 20, "Start minigame"));
        State.refreshState();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        minigameScroll.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        String creditStr;
        switch (creditType)
        {
            case "credit":
                creditStr = credit + " credit(s)";
                break;
            default:
            case "currency":
            case "none":
                creditStr = credit;
        }

        drawString(fontRendererObj, creditStr, 5, 5, 0xFFFFFFFF);
        drawStatusString(width / 2, height - 40);
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
        } else if (button == spinupButton && State.getCurrentState() == State.CREDENTIALS_OK && minigameScroll.getMinigame() != null) {
            Minecraft.getMinecraft().displayGuiScreen(new StartMinigame(minigameScroll.getMinigame()));
        }
    }

    private boolean areCredentialsValid()
    {
        Aries aries = new Aries(key, secret);
        Map resp = aries.doApiCall("os", "systemstate");
        return resp.containsKey("status") && resp.get("status").equals("success");
    }

    private Future<Boolean> checkCredentials()
    {
        return executor.submit( () -> {
            try {
                State.pushState(State.CHECKING_CREDENTIALS);
                credentialsValid = areCredentialsValid();

                Map<String, String> map = new HashMap<>();
                map.put("key2", key);
                map.put("secret2", secret);
                map.put("hash", Callbacks.getPlayerHash(CreeperHost.proxy.getUUID()));

                String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/minigamecredit", new Gson().toJson(map), true, false);
                System.out.println(resp);
                Map creditResp = new Gson().fromJson(resp, Map.class);
                credit = String.valueOf(creditResp.get("credit"));
                creditType = String.valueOf(creditResp.get("responsetype"));
                State.pushState(credentialsValid ? State.CREDENTIALS_OK : State.CREDENTIALS_INVALID);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            }
        } else {
            credentialsFile.getParentFile().mkdirs();
            State.pushState(State.CREDENTIALS_INVALID);
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

    private void drawStatusString(int x, int y)
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
            case MINIGAME_ACTIVE:
                drawText = "Minigame active - adding to server list";
                drawColour = 0xFF00FF00;
                break;
            case MINIGAME_FAILED:
                drawText = "Minigame launch failed.";
                drawColour = 0xFFFF0000;
                break;
            case READY_TO_JOIN:
                drawText = "Ready to join game - press button to join. You can invite people from the friends menu in the top right after joining!";
                drawColour = 0xFFFFFFFF;
                break;
            case CREDENTIALS_INVALID:
                drawText = "No credentials found - trial mode";
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

        drawCenteredSplitString(drawText, x, y, width, drawColour);
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
        LOGGING_IN, CHECKING_CREDENTIALS, CREDENTIALS_OK, CREDENTIALS_INVALID, LOGIN_FAILURE, TWOFACTOR_NEEDED, STARTING_MINIGAME, LOGIN_SUCCESS, TWOFACTOR_FAILURE, MINIGAME_ACTIVE, MINIGAME_FAILED, NOT_ENOUGH_CREDIT, UNKNOWN_ERROR, READY_TO_JOIN;

        private static State currentState = CHECKING_CREDENTIALS;

        public static void pushState(State state)
        {
            if (current.settingsButton == null)
                return;
            GuiMinigames.current.settingsButton.enabled = true;
            switch (state) {
                case LOGGING_IN:
                    if (GuiMinigames.settings != null) {
                        GuiMinigames.settings.emailField.setEnabled(false);
                        GuiMinigames.settings.passwordField.setEnabled(false);
                        GuiMinigames.settings.oneCodeField.setEnabled(false);
                        GuiMinigames.settings.loginButton.enabled = false;
                    }
                    break;
                case CREDENTIALS_OK:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.emailField.setVisible(false);
                        GuiMinigames.settings.passwordField.setVisible(false);
                        GuiMinigames.settings.oneCodeField.setVisible(false);
                        GuiMinigames.settings.emailField.setEnabled(false);
                        GuiMinigames.settings.passwordField.setEnabled(false);
                        GuiMinigames.settings.oneCodeField.setEnabled(false);
                        GuiMinigames.settings.emailLabel.visible = false;
                        GuiMinigames.settings.passwordLabel.visible = false;
                        GuiMinigames.settings.oneCodeLabel.visible = false;
                        GuiMinigames.settings.loginButton.displayString = "Log in again";
                        GuiMinigames.settings.loginButton.enabled = true;
                        GuiMinigames.settings.loginButton.visible = true;
                    }
                    break;
                case CHECKING_CREDENTIALS:
                    GuiMinigames.current.settingsButton.enabled = false;
                    break;
                case CREDENTIALS_INVALID:
                case LOGIN_FAILURE:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.emailField.setEnabled(true);
                        GuiMinigames.settings.passwordField.setEnabled(true);
                        GuiMinigames.settings.oneCodeField.setEnabled(false);
                        GuiMinigames.settings.emailField.setVisible(true);
                        GuiMinigames.settings.passwordField.setVisible(true);
                        GuiMinigames.settings.oneCodeField.setVisible(false);
                        GuiMinigames.settings.emailLabel.visible = true;
                        GuiMinigames.settings.passwordLabel.visible = true;
                        GuiMinigames.settings.oneCodeLabel.visible = false;
                        GuiMinigames.settings.loginButton.displayString = "Log in";
                        GuiMinigames.settings.loginButton.enabled = true;
                        GuiMinigames.settings.loginButton.visible = true;
                    }
                    break;
                case TWOFACTOR_NEEDED:
                    if (GuiMinigames.settings != null) {
                        GuiMinigames.settings.emailField.setVisible(false);
                        GuiMinigames.settings.passwordField.setVisible(false);
                        GuiMinigames.settings.emailField.setEnabled(false);
                        GuiMinigames.settings.passwordField.setEnabled(false);
                        GuiMinigames.settings.oneCodeField.setVisible(true);
                        GuiMinigames.settings.oneCodeField.setEnabled(true);
                        GuiMinigames.settings.emailLabel.visible = false;
                        GuiMinigames.settings.passwordLabel.visible = false;
                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
                    break;
                case TWOFACTOR_FAILURE:
                    if (GuiMinigames.settings != null)
                    {
                        GuiMinigames.settings.oneCodeField.setEnabled(true);
                        GuiMinigames.settings.oneCodeField.setVisible(true);
                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
                    break;
            }
            currentState = state;
            if (state == CREDENTIALS_OK)
            {
                GuiMinigames.current.settingsButton.displayString = "Logged in";
            } else {
                GuiMinigames.current.settingsButton.displayString = "Log in";
            }

            GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
            if (curScreen instanceof IStateHandler)
            {
                ((IStateHandler)curScreen).handleStatePush(state);
            }
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

        public Minigame getMinigame() {
            return selectedIndex >= 0 ? minigames.get(selectedIndex) : null;
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

            buttonList.add(cancelButton = new GuiButton(8085, width - 10 - 100, height - 5 - 20, 100, 20, "Go back"));
            buttonList.add(loginButton = new GuiButton(8089, width / 2 - 50, height / 2 + 40, 100, 20, "Save"));

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
            if (oneCodeField.getText().replaceAll("[^0-9]", "").length() == 6 && String.valueOf(typedChar).replaceAll("[^0-9]", "").equals(""))
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
                drawCenteredSplitString("You have valid credentials. If you wish to change your credentials, please log in again.",width / 2, height / 2 - 30, width, 0xFFFFFFFF);
            }
            drawStatusString(width / 2, height - 40);
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            if (button == cancelButton)
            {
                Minecraft.getMinecraft().displayGuiScreen(GuiMinigames.this);
            } else if (button == loginButton) {
                if (State.getCurrentState() == State.CREDENTIALS_OK)
                {
                    key = "";
                    secret = "";
                    saveCredentials();
                    State.pushState(State.CREDENTIALS_INVALID);
                } else {
                    executor.submit(() ->
                    {
                        Map<String, String> credentials = new HashMap<>();
                        credentials.put("email", emailField.getText());
                        credentials.put("password", passwordField.getText());
                        credentials.put("oneCode", oneCodeField.getText().replaceAll("[^0-9]", ""));

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
                                emailField.setText("");
                                passwordField.setText("");
                                oneCodeField.setText("");
                            } else {
                                if (obj.has("_2fa") && !obj.get("_2fa").isJsonNull() && obj.get("_2fa").getAsBoolean())
                                {
                                    if (previous2fa)
                                    {
                                        State.pushState(State.TWOFACTOR_FAILURE);
                                        loginFailureMessage = "Invalid code. Please try again or reset it by logging into the CreeperPanel";
                                        return;
                                    }
                                    State.pushState(State.TWOFACTOR_NEEDED);
                                    loginFailureMessage = "Please enter your two-factor code";
                                    previous2fa = true;
                                    oneCodeField.setText("");
                                    return;
                                }
                                State.pushState(State.LOGIN_FAILURE);
                                String tempLoginFailure = obj.get("message").getAsString();
                                loginFailureMessage = tempLoginFailure.isEmpty() ? "Login failed. Please ensure you have entered your username and password correctly." : tempLoginFailure;
                                passwordField.setText("");
                                oneCodeField.setText("");
                            }
                        }
                    });
                }
            }
        }
    }

    public class StartMinigame extends GuiScreen implements IStateHandler
    {
        private final Minigame minigame;
        private String failedReason = "";
        private int port;
        private String ip;
        private int ticks = 0;
        private ItemStack stack = new ItemStack(Items.BEEF, 1);
        private GuiButton joinServerButton;

        public StartMinigame(Minigame minigame)
        {
            this.minigame = minigame;

            State.pushState(State.STARTING_MINIGAME);

            executor.submit(() ->
            {
                try {
                    String url = minigame.template;
                    int ram = minigame.ram;

                    Aries aries = new Aries(key, secret);

                    Map creditResp = aries.doApiCall("minetogether", "minigamecredit");

                    System.out.println(creditResp);

                    if (creditResp.get("status").equals("success")) {
                        String credit = creditResp.get("credit").toString();
                        if (true) // credit check here
                        {
                            Map<String, String> sendMap = new HashMap<>();

                            sendMap.put("id", String.valueOf(minigame.id));
                            sendMap.put("hash", getPlayerHash(CreeperHost.proxy.getUUID()));
                            sendMap.put("key", key);
                            sendMap.put("secret", secret);

                            Map map = aries.doApiCall("minetogether", "trialminigame", sendMap);
                            System.out.println(map);

                            if (map.get("status").equals("success")) {
                                try {
                                    State.pushState(State.MINIGAME_ACTIVE);
                                    ip = map.get("ip").toString();
                                    port = Double.valueOf(map.get("port").toString()).intValue();
                                    CreeperHostServer.serverOn = true;
                                    CreeperHostServer.startMinetogetherThread(map.get("ip").toString(), "Minigame: " + Minecraft.getMinecraft().getSession().getUsername(), Config.getInstance().curseProjectID, port, CreeperHostServer.Discoverability.INVITE);
                                    while (true) {
                                        if (CreeperHostServer.isActive) {
                                            State.pushState(State.READY_TO_JOIN);
                                            CreeperHost.instance.curServerId = CreeperHostServer.updateID;
                                            break;
                                        } else if (CreeperHostServer.failed) {
                                            State.pushState(State.MINIGAME_FAILED);
                                            break;
                                        }
                                        Thread.sleep(1000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                failedReason = (String) map.get("message");
                                State.pushState(State.MINIGAME_FAILED);
                            }
                        } else {
                            State.pushState(State.NOT_ENOUGH_CREDIT);
                        }
                    } else {
                        State.pushState(State.UNKNOWN_ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void initGui() {
            super.initGui();
            buttonList.add(joinServerButton = new GuiButton(800008, width / 2 - 50, height / 2 + 20, 100, 20,"Join server"));
            joinServerButton.enabled = false;
            joinServerButton.visible = false;
            State.refreshState();
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            spinupButton.enabled = minigameScroll != null && (State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameScroll.getMinigame() != null;
            ticks++;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();
            if (State.getCurrentState() == State.MINIGAME_FAILED)
            {
                drawCenteredSplitString("Minigame failed. Reason: " + failedReason, width / 2, height / 2, width, 0xFFFF0000);
            }
            else
            {
                drawStatusString(width / 2, height / 2);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
            if (State.getCurrentState() != State.READY_TO_JOIN && State.getCurrentState() != State.MINIGAME_FAILED)
                loadingSpin(partialTicks);
        }

        private void loadingSpin(float partialTicks)
        {
            int rotateTickMax = 30;
            int throbTickMax = 20;
            int rotateTicks = ticks % rotateTickMax;
            int throbTicks = ticks % throbTickMax;
            GlStateManager.translate(width / 2, height / 2 + 20 + 10, 0);
            GlStateManager.pushMatrix();
            float scale = 1F + ((throbTicks >= (throbTickMax / 2) ? (throbTickMax - (throbTicks + partialTicks)) : (throbTicks + partialTicks)) * (2F / throbTickMax));
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate((rotateTicks + partialTicks) * (360F / rotateTickMax), 0, 0, 1);
            GlStateManager.pushMatrix();

            itemRender.renderItemAndEffectIntoGUI(stack, -8, -8);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        @Override
        public void handleStatePush(State state)
        {
            if (state == State.READY_TO_JOIN)
            {
                joinServerButton.enabled = true;
                joinServerButton.visible = true;
            } else if (state == State.MINIGAME_FAILED) {
                joinServerButton.enabled = true;
                joinServerButton.visible = true;
                joinServerButton.displayString = "Go back";
            }
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            super.actionPerformed(button);
            if (button == joinServerButton)
            {
                if (State.getCurrentState() == State.READY_TO_JOIN)
                    FMLClientHandler.instance().connectToServerAtStartup(ip, port);
                else
                    Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames());
            }
        }
    }

    public interface IStateHandler
    {
        void handleStatePush(State state);
    }
}
