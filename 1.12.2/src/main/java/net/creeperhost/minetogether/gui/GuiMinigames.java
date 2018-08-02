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
import net.creeperhost.minetogether.gui.element.GuiActiveFake;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompat;
import net.creeperhost.minetogether.gui.element.GuiTextFieldCompatCensor;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.serverstuffs.CreeperHostServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

import static net.creeperhost.minetogether.paul.Callbacks.getPlayerHash;

public class GuiMinigames extends GuiScreen
{
    private static GuiMinigames current;
    private List<Minigame> minigames;
    private List<Minigame> vanillaMinigames;
    private GuiScrollingMinigames minigameScroll;
    private static HashMap<Integer, ResourceLocation> minigameTexturesCache = new HashMap<>();
    private static HashMap<Integer, Pair<Integer, Integer>> minigameTexturesSize = new HashMap<>();
    private GuiButton settingsButton;
    private GuiButton spinupButton;
    private static File credentialsFile = new File("config/minetogether/credentials.json");
    private static String key = "";
    private static String secret = "";
    private boolean credentialsValid = false;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    private String loginFailureMessage = "";
    private static Settings settings;
    private float credit = -1;
    private String creditType = "none";
    private float exchangeRate;
    private float quote = -1;
    private String curPrefix = "";
    private String curSuffix = "";

    private boolean isModded = true;
    private GuiActiveFake moddedButton;
    private GuiActiveFake vanillaButton;
    private GuiScreen parent;
    private GuiButton cancelButton;

    public GuiMinigames(GuiScreen parent)
    {
        this.parent = parent;
        current = this;
        State.pushState(State.CHECKING_CREDENTIALS);
        loadCredentials();
        refreshMinigames();
        isModded = true;
    }

    Minigame lastMinigame = null;

    public boolean spinDown = false;

    public GuiMinigames(GuiScreen parent, boolean spinDown) {
        this(parent);
        this.spinDown = spinDown;
    }

    private void refreshMinigames()
    {
        executor.submit(() ->
        {
            List<Minigame> minigameTemp = Callbacks.getMinigames(true);
            List<Minigame> tempVanilla = new ArrayList<>();
            List<Minigame> tempModded = new ArrayList<>();
            for(Minigame minigame : minigameTemp)
            {
                if (minigame.project == 0)
                    tempVanilla.add(minigame);
                else
                    tempModded.add(minigame);

            }
            minigames = tempModded;
            vanillaMinigames = tempVanilla;
        });

    }

    @Override
    public void updateScreen()
    {
        ticks++;
        Minigame minigame = minigameScroll.getMinigame();
        if (lastMinigame != minigame)
        {
            if (minigame == null)
            {
                quote = -1;
            } else {
                executor.submit(() -> {
                    try {
                        Map<String, String> sendMap = new HashMap<>();

                        sendMap.put("id", String.valueOf(minigame.id));
                        sendMap.put("hash", getPlayerHash(CreeperHost.proxy.getUUID()));
                        sendMap.put("key2", key);
                        sendMap.put("secret2", secret);

                        Aries aries = new Aries(key, secret);

                        Map map = aries.doApiCall("minetogether", "minigamequote", sendMap);

                        if (map.get("status").equals("success")) {
                            quote = Float.valueOf(String.valueOf(map.get("quote")));
                        } else {
                            quote = -1;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
            }

        }
        lastMinigame = minigame;
    }

    private int ticks = 0;
    private ItemStack stack = new ItemStack(Items.BEEF, 1);

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
    public void initGui()
    {
        if (!CreeperHost.instance.gdpr.hasAcceptedGDPR())
        {
            mc.displayGuiScreen(new GuiGDPR(parent, () -> new GuiMinigames(parent)));
            return;
        }
        super.initGui();
        GuiScrollingMinigames tempMinigameScroll = new GuiScrollingMinigames(34);
        tempMinigameScroll.update(minigameScroll);
        minigameScroll = tempMinigameScroll;
        buttonList.add(settingsButton = new GuiButton(808, width - 10 - 100, 5, 100, 20, "Login"));
        buttonList.add(spinupButton = new GuiButton(809, width - 10 - 100, height - 5 - 20, 100, 20, "Start minigame"));
        buttonList.add(moddedButton = new GuiActiveFake(0xb00b, 10, 30, (width / 2) - 5, 20, "Modded"));
        buttonList.add(vanillaButton = new GuiActiveFake(0xb00b5, width - 10 - ((width / 2) - 10), 30, (width / 2) - 5, 20, "Vanilla"));
        buttonList.add(cancelButton = new GuiButton(909, 10, height - 5 - 20, 100, 20, "Cancel"));
        moddedButton.setActive(isModded);
        vanillaButton.setActive(!isModded);
        State.refreshState();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        if (!spinDown) {
            spinupButton.enabled = minigameScroll != null && (State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameScroll.getMinigame() != null && credit >= quote;
            minigameScroll.drawScreen(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            String creditStr;
            switch (creditType)
            {
                case "credit":
                    creditStr = (int)credit + " trial credit" + (credit == 1 ? "" : "s");
                    break;
                default:
                case "none":
                    creditStr = "Retrieving...";
                    break;
                case "currency":
                    String formattedCredit = new DecimalFormat("0.00##").format(credit);
                    creditStr = "CreeperHost credit: " + curPrefix + formattedCredit + curSuffix;
            }

            drawCenteredString(fontRendererObj, "MineTogether Minigames", width / 2, 5, 0xFFFFFFFF);

            drawString(fontRendererObj, creditStr, 5, 5, 0xFFFFFFFF);
            drawStatusString(width / 2, height - 40);

            String currencyFormat = String.valueOf((int)quote);;

            if (quote > 0)
            {
                double exchangedQuote = round(quote * exchangeRate, 2);
                String first = "";
                switch(creditType)
                {
                    case "credit":
                        first = "Credit" + (quote > 1 ? "s" : "") + " needed: ";
                        break;
                    default:
                    case "none":
                        first = "";
                        break;
                    case "currency":
                        first = "Estimated cost: ";
                        currencyFormat = new DecimalFormat("0.00##").format(exchangedQuote);
                }

                String formattedQuote = first + curPrefix + currencyFormat + curSuffix;
                drawString(fontRendererObj, formattedQuote, 5, height - 40, 0xFFFFFFFF);
                int stringLen = fontRendererObj.getStringWidth(formattedQuote);
                if (!creditType.equals("credit") && !curPrefix.equals("£") && mouseX >= 5 && mouseX <= 5 + stringLen && mouseY >= height - 40 && mouseY <= height - 30)
                {
                    drawHoveringText(Arrays.asList("Figure provided based on exchange rate of " + exchangeRate), mouseX, mouseY);
                } else {
                    if (spinupButton.isMouseOver() && credit < quote)
                    {
                        drawHoveringText(Arrays.asList("Cannot start minigame as you do not have enough credit"), mouseX, mouseY);
                    }
                }
            }
        } else {
            drawCenteredSplitString("Spinning down minigame", width / 2, height / 2, width, 0xFFFFFFFF);
            loadingSpin(partialTicks);
            if (doSpindown)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
            }
        }
    }

    public static double round(double value, int places)
    {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
        } else if (button == spinupButton && (State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameScroll.getMinigame() != null) {
            Minecraft.getMinecraft().displayGuiScreen(new StartMinigame(minigameScroll.getMinigame()));
        } else if (button == vanillaButton) {
            isModded = false;
            minigameScroll.clearSelected();
            vanillaButton.setActive(true);
            moddedButton.setActive(false);
        } else if (button == moddedButton)
        {
            isModded = true;
            minigameScroll.clearSelected();
            moddedButton.setActive(true);
            vanillaButton.setActive(false);
        } else if (button == cancelButton)
        {
            this.mc.displayGuiScreen(parent);
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
                try {
                    Map<String, String> map = new HashMap<>();
                    map.put("key2", key);
                    map.put("secret2", secret);
                    map.put("hash", Callbacks.getPlayerHash(CreeperHost.proxy.getUUID()));

                    String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/minigamecredit", new Gson().toJson(map), true, false);
                    Map creditResp = new Gson().fromJson(resp, Map.class);
                    credit = Float.parseFloat(String.valueOf(creditResp.get("credit")));
                    creditType = String.valueOf(creditResp.get("responsetype"));
                    if (creditType.equals("currency"))
                    {
                        Map creditMap = (Map)creditResp.get("currency");
                        exchangeRate = Float.valueOf(String.valueOf(creditMap.get("exchange_rate")));
                        curPrefix = String.valueOf(creditMap.get("prefix"));
                        if (curPrefix.equals("null"))
                            curPrefix = "";
                        curSuffix = String.valueOf(creditMap.get("suffix"));
                        if (curSuffix.equals("null"))
                            curSuffix = "";
                    } else {
                        exchangeRate = 1;
                        curPrefix = "";
                        curSuffix = "";
                    }
                    State.pushState(credentialsValid ? State.CREDENTIALS_OK : State.CREDENTIALS_INVALID);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }


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
                drawText = "Starting Minigame. This may take up to 60 seconds!";
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

    private boolean doSpindown = false;

    private void doSpindown()
    {
        final boolean[] started = {false};
        executor.submit(() -> {
            Map<String, String> sendMap = new HashMap<>();

            Aries aries = new Aries(key, secret);

            sendMap.put("uuid", CreeperHost.instance.activeMinigame);
            sendMap.put("key2", key);
            sendMap.put("secret2", secret);

            CreeperHost.instance.activeMinigame = null;


            started[0] = true;

            Map map = aries.doApiCall("minetogether", "stopminigame", sendMap);
        });

        while (!started[0])
        {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }

        doSpindown = true;
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

                    if (GuiMinigames.current.spinDown)
                    {
                        GuiMinigames.current.doSpindown();
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
            super(Minecraft.getMinecraft(), GuiMinigames.this.width - 20, GuiMinigames.this.height - 50, 50, GuiMinigames.this.height - 50, 10, entryHeight, GuiMinigames.this.width, GuiMinigames.this.height);
        }

        @Override
        protected int getSize() {
            List<Minigame> minigames = isModded ? GuiMinigames.this.minigames : GuiMinigames.this.vanillaMinigames;
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
            List<Minigame> minigames = isModded ? GuiMinigames.this.minigames : GuiMinigames.this.vanillaMinigames;
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

                int gameWidth = (int) (fontRendererObj.getStringWidth(minigames.get(slotIdx).displayName) * scale);
                int newX = (width / 2) + (gameWidth / 2);

                drawCenteredString(fontRendererObj, minigames.get(slotIdx).displayName, x, y, 0xFFFFFFFF);

                GlStateManager.popMatrix();

                drawString(fontRendererObj, " by " + minigames.get(slotIdx).author, newX, slotTop + 2, 0xFFAAAAAA);

                String displayDescription = minigames.get(slotIdx).displayDescription;
                if (fontRendererObj.getStringWidth(displayDescription) > (width - 96) * 2)
                {
                    while (fontRendererObj.getStringWidth(displayDescription + "...") > (width - 96) * 2)
                    {
                        displayDescription = displayDescription.substring(0, displayDescription.lastIndexOf(" "));
                    }
                    displayDescription += "...";
                }

                drawCenteredSplitString(displayDescription, width / 2, slotTop + 12, width - 84, 0xFFAAAAAA);
            }
        }

        public Minigame getMinigame() {
            List<Minigame> minigames = isModded ? GuiMinigames.this.minigames : GuiMinigames.this.vanillaMinigames;
            return selectedIndex >= 0 ? minigames.get(selectedIndex) : null;
        }

        public void clearSelected() {
            selectedIndex = -1;
        }

        private int getSelected()
        {
            return selectedIndex;
        }

        public void update(GuiScrollingMinigames previous)
        {
            if (previous == null)
                return;
            selectedIndex = previous.getSelected();
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
            if (keyCode == Keyboard.KEY_TAB)
            {
                if (emailField.isFocused())
                {
                    emailField.setFocused(false);
                    passwordField.setFocused(true);
                } else if (passwordField.isFocused()) {
                    passwordField.setFocused(false);
                    emailField.setFocused(true);
                }
            }
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
            oneCodeField.drawTextBox();super.drawScreen(mouseX, mouseY, partialTicks);
            if (State.getCurrentState() == State.CREDENTIALS_OK)
            {
                drawCenteredSplitString("You have valid credentials. If you wish to change your credentials, please log in again.",width / 2, height / 2 - 30, width, 0xFFFFFFFF);
            } else {
                drawCenteredSplitString("If you would like to use your CreeperHost credit balance instead of the free minigame credits, please login with your CreeperHost username and password here.",width / 2, height / 2 - 60, width, 0xFFFFFFFF);
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
                    checkCredentials();
                } else {
                    executor.submit(() ->
                    {
                        try {
                            Map<String, String> credentials = new HashMap<>();
                            credentials.put("email", emailField.getText());
                            credentials.put("password", passwordField.getText());
                            credentials.put("oneCode", oneCodeField.getText().replaceAll("[^0-9]", ""));

                            State.pushState(State.LOGGING_IN);
                            String resp = WebUtils.postWebResponse("https://staging-panel.creeper.host/mt.php", credentials);


                            JsonParser parser = new JsonParser();
                            JsonElement el = parser.parse(resp);
                            if (el.isJsonObject()) {
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.get("success").getAsBoolean()) {
                                    key = obj.get("key").getAsString();
                                    secret = obj.get("secret").getAsString();
                                    try {
                                        if (checkCredentials().get()) {
                                            saveCredentials();
                                        }
                                    } catch (InterruptedException e) {
                                    } catch (ExecutionException e) {
                                    }
                                    emailField.setText("");
                                    passwordField.setText("");
                                    oneCodeField.setText("");
                                } else {
                                    if (obj.has("_2fa") && !obj.get("_2fa").isJsonNull() && obj.get("_2fa").getAsBoolean()) {
                                        if (previous2fa) {
                                            loginFailureMessage = "Invalid code. Please try again or reset it by logging into the CreeperPanel";
                                            State.pushState(State.TWOFACTOR_FAILURE);
                                            return;
                                        }
                                        loginFailureMessage = "Please enter your two-factor code";
                                        State.pushState(State.TWOFACTOR_NEEDED);
                                        oneCodeField.setFocused(true);
                                        emailField.setFocused(false);
                                        passwordField.setFocused(false);
                                        previous2fa = true;
                                        oneCodeField.setText("");
                                        return;
                                    }
                                    String tempLoginFailure = obj.get("message").isJsonNull() ? "" : obj.get("message").getAsString();
                                    loginFailureMessage = tempLoginFailure.isEmpty() ? "Login failed. Please ensure you have entered your username and password correctly." : tempLoginFailure;
                                    State.pushState(State.LOGIN_FAILURE);
                                    passwordField.setText("");
                                    oneCodeField.setText("");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

                    if (creditResp.get("status").equals("success")) {
                        String credit = creditResp.get("credit").toString();
                        if (true) // credit check here
                        {
                            Map<String, String> sendMap = new HashMap<>();

                            sendMap.put("id", String.valueOf(minigame.id));
                            sendMap.put("hash", getPlayerHash(CreeperHost.proxy.getUUID()));
                            sendMap.put("key2", key);
                            sendMap.put("secret2", secret);

                            Map map = aries.doApiCall("minetogether", "startminigame", sendMap);

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
                                            CreeperHost.instance.activeMinigame = String.valueOf(map.get("uuid"));
                                            CreeperHost.instance.minigameID = minigame.id;
                                            CreeperHost.instance.trialMinigame = map.get("type").equals("trial");
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
        public void updateScreen()
        {
            ticks++;
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
                if (State.getCurrentState() == State.READY_TO_JOIN) {
                    CreeperHost.instance.joinTime = System.currentTimeMillis();
                    FMLClientHandler.instance().connectToServerAtStartup(ip, port);
                } else
                    Minecraft.getMinecraft().displayGuiScreen(new GuiMinigames(parent));
            }
        }
    }

    public interface IStateHandler
    {
        void handleStatePush(State state);
    }
}
