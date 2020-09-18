package net.creeperhost.minetogether.client.screen.minigames;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.GDPRScreen;
import net.creeperhost.minetogether.client.screen.element.GuiTextFieldCompatCensor;
import net.creeperhost.minetogether.client.screen.list.GuiList;
import net.creeperhost.minetogether.client.screen.list.GuiListEntryMinigame;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Pair;
import net.creeperhost.minetogether.util.RenderUtils;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

import static net.creeperhost.minetogether.paul.Callbacks.getPlayerHash;

public class MinigamesScreen extends Screen
{
    private static MinigamesScreen current;
    private List<Minigame> moddedList = new ArrayList<>();
    private List<Minigame> vanillaList = new ArrayList<>();
    private GuiList<GuiListEntryMinigame> minigameList;
    private static HashMap<Integer, ResourceLocation> minigameTexturesCache = new HashMap<>();
    private static HashMap<Integer, Pair<Integer, Integer>> minigameTexturesSize = new HashMap<>();
    private Button settingsButton;
    private Button spinupButton;
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
    
    private Button moddedButton;
    private Button vanillaButton;
    private Screen parent;
    private Button cancelButton;
    private boolean isModded;
    private int ticks;

    public MinigamesScreen(Screen parent)
    {
        super(new StringTextComponent(""));
        this.parent = parent;
        current = this;
        State.pushState(State.CHECKING_CREDENTIALS);
        loadCredentials();
        refreshMinigames();
        isModded = true;
    }

    public boolean spinDown = false;
    
    public MinigamesScreen(Screen parent, boolean spinDown)
    {
        this(parent);
        this.spinDown = spinDown;
    }

    private void refreshMinigames()
    {
        CompletableFuture.runAsync(() ->
        {
            List<Minigame> minigameTemp = Callbacks.getMinigames(true);
            if(minigameTemp == null) return;

            //Clear out the cached list;
            moddedList.clear();
            vanillaList.clear();

            for (Minigame minigame : minigameTemp)
            {
                if (minigame.project == 0) vanillaList.add(minigame);
                    else moddedList.add(minigame);
            }
        });
    }
    
    @Override
    public void init()
    {
        if (!MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            minecraft.displayGuiScreen(new GDPRScreen(parent, () -> new MinigamesScreen(parent)));
            return;
        }
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        refreshMinigames();

        if (minigameList == null) minigameList = new GuiList<>(this, minecraft, width, height, 50, this.height - 40, 36);
            else minigameList.updateSize(width + 11, height, 50, this.height - 40);

        this.children.add(minigameList);

        addButton(settingsButton = new Button(width - 10 - 100, 5, 100, 20, new StringTextComponent("Login"), p ->
        {
            Minecraft.getInstance().displayGuiScreen(settings = new Settings());
        }));
        addButton(spinupButton = new Button(width - 10 - 100, height - 5 - 20, 100, 20, new StringTextComponent("Start minigame"), p ->
        {
            if ((State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameList.getCurrSelected() != null)
            {
                Minecraft.getInstance().displayGuiScreen(new StartMinigame(minigameList.getCurrSelected().getMiniGame()));
            }
        }));
        addButton(moddedButton = new Button(10, 30, (width / 2) - 5, 20, new StringTextComponent("Modded"), p ->
        {
            moddedButton.active = false;
            vanillaButton.active = true;
            minigameList.clearList();
            isModded = true;

            if(moddedList != null && !moddedList.isEmpty())
            {
                for (Minigame minigame : moddedList)
                {
                    minigameList.add(new GuiListEntryMinigame(this, minigameList, minigame));
                }
            }
        }));
        addButton(vanillaButton = new Button(width - 10 - ((width / 2) - 10), 30, (width / 2) - 5, 20, new StringTextComponent("Vanilla"), p ->
        {
            moddedButton.active = true;
            vanillaButton.active = false;
            minigameList.clearList();
            isModded = false;

            if(vanillaList != null && !vanillaList.isEmpty())
            {
                for (Minigame minigame : vanillaList)
                {
                    minigameList.add(new GuiListEntryMinigame(this, minigameList, minigame));
                }
            }
        }));
        addButton(cancelButton = new Button(10, height - 5 - 20, 100, 20, new StringTextComponent("Cancel"), p ->
        {
            Minecraft.getInstance().displayGuiScreen(parent);
        }));
        State.refreshState();
        moddedButton.active = !isModded;
        vanillaButton.active = isModded;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (Config.getInstance().isChatEnabled() && !ChatHandler.isOnline())
        {
            spinupButton.visible = spinupButton.active = vanillaButton.visible = vanillaButton.active = moddedButton.visible = moddedButton.active = settingsButton.visible = settingsButton.active = false;
            drawCenteredString(matrixStack, minecraft.fontRenderer, I18n.format("minetogether.minigames.notavailable"), width / 2, height / 2, 0xFFFFFFFF);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            return;
        }

        if (!spinDown)
        {
            spinupButton.active = minigameList.getCurrSelected() != null && (State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameList.getCurrSelected().getMiniGame() != null && credit >= quote;
            this.minigameList.render(matrixStack, mouseX, mouseY, partialTicks);

            String creditStr;
            switch (creditType)
            {
                case "credit":
                    creditStr = (int) credit + " trial credit" + (credit == 1 ? "" : "s");
                    break;
                default:
                case "none":
                    creditStr = "Retrieving...";
                    break;
                case "currency":
                    String formattedCredit = new DecimalFormat("0.00##").format(credit);
                    creditStr = "CreeperHost credit: " + curPrefix + formattedCredit + curSuffix;
            }
            
            drawCenteredString(matrixStack, minecraft.fontRenderer, "MineTogether Minigames", width / 2, 5, 0xFFFFFFFF);
            
            drawString(matrixStack, minecraft.fontRenderer, creditStr, 5, 5, 0xFFFFFFFF);
            drawStatusString(matrixStack, width / 2, height - 40);
            
            String currencyFormat = String.valueOf((int) quote);
            
            if (quote > 0)
            {
                double exchangedQuote = round(quote * exchangeRate, 2);
                String first = "";
                switch (creditType)
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
                drawString(matrixStack, minecraft.fontRenderer, formattedQuote, 5, height - 40, 0xFFFFFFFF);
                int stringLen = minecraft.fontRenderer.getStringWidth(formattedQuote);
                if (!creditType.equals("credit") && !curPrefix.equals("£") && mouseX >= 5 && mouseX <= 5 + stringLen && mouseY >= height - 40 && mouseY <= height - 30)
                {
                    renderTooltip(matrixStack, new StringTextComponent("Figure provided based on exchange rate of " + exchangeRate), mouseX, mouseY);
                } else
                {
                    if (spinupButton.isHovered() && credit < quote)
                    {
                        renderTooltip(matrixStack, new StringTextComponent("Cannot start minigame as you do not have enough credit"), mouseX, mouseY);
                    }
                }
            }
        } else
        {
            drawCenteredSplitString(matrixStack, "Spinning down minigame", width / 2, height / 2, width, 0xFFFFFFFF);
            RenderUtils.loadingSpin(partialTicks, ticks++,  width / 2, height / 2 + 20 + 10, new ItemStack(Items.BEEF));
            if (doSpindown)
            {
                Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
            }
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    public static double round(double value, int places)
    {
        if (places < 0) throw new IllegalArgumentException();
        
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    private boolean areCredentialsValid()
    {
        Aries aries = new Aries(key, secret);
        Map resp = aries.doApiCall("os", "systemstate");
        return resp.containsKey("status") && resp.get("status").equals("success");
    }
    
    private Future<Boolean> checkCredentials()
    {
        return executor.submit(() ->
        {
            try
            {
                State.pushState(State.CHECKING_CREDENTIALS);
                credentialsValid = areCredentialsValid();
                try
                {
                    Map<String, String> map = new HashMap<>();
                    map.put("key2", key);
                    map.put("secret2", secret);
                    map.put("hash", Callbacks.getPlayerHash(MineTogether.proxy.getUUID()));
                    
                    String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/minigamecredit", new Gson().toJson(map), true, false);
                    Map creditResp = new Gson().fromJson(resp, Map.class);
                    credit = Float.parseFloat(String.valueOf(creditResp.get("credit")));
                    creditType = String.valueOf(creditResp.get("responsetype"));
                    if (creditType.equals("currency"))
                    {
                        Map creditMap = (Map) creditResp.get("currency");
                        exchangeRate = Float.valueOf(String.valueOf(creditMap.get("exchange_rate")));
                        curPrefix = String.valueOf(creditMap.get("prefix"));
                        if (curPrefix.equals("null"))
                            curPrefix = "";
                        curSuffix = String.valueOf(creditMap.get("suffix"));
                        if (curSuffix.equals("null"))
                            curSuffix = "";
                    } else
                    {
                        exchangeRate = 1;
                        curPrefix = "";
                        curSuffix = "";
                    }
                    State.pushState(credentialsValid ? State.CREDENTIALS_OK : State.CREDENTIALS_INVALID);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return credentialsValid;
        });
    }
    
    private void loadCredentials()
    {
        if (credentialsFile.exists())
        {
            try
            {
                String creds = FileUtils.readFileToString(credentialsFile, Charset.defaultCharset());
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(creds);
                if (el.isJsonObject())
                {
                    JsonObject obj = el.getAsJsonObject();
                    key = obj.get("key").getAsString();
                    secret = obj.get("secret").getAsString();
                }
            } catch (IOException ignored)
            {
            }
        } else
        {
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
        try
        {
            FileUtils.writeStringToFile(credentialsFile, new Gson().toJson(creds), Charset.defaultCharset(), true);
        } catch (IOException ignored)
        {
        }
    }
    
    private void drawStatusString(MatrixStack matrixStack, int x, int y)
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
        
        drawCenteredSplitString(matrixStack, drawText, x, y, width, drawColour);
    }
    
    private boolean doSpindown = false;
    
    private void doSpindown()
    {
        final boolean[] started = {false};
        executor.submit(() ->
        {
            Map<String, String> sendMap = new HashMap<>();
            
            Aries aries = new Aries(key, secret);
            
            sendMap.put("uuid", MineTogether.instance.activeMinigame);
            sendMap.put("key2", key);
            sendMap.put("secret2", secret);
            
            MineTogether.instance.activeMinigame = null;
            
            started[0] = true;
            
            Map map = aries.doApiCall("minetogether", "stopminigame", sendMap);
        });
        
        while (!started[0])
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException ignored)
            {
            }
        }
        doSpindown = true;
    }
    
    private void drawCenteredSplitString(MatrixStack matrixStack, String drawText, int x, int y, int width, int drawColour)
    {
        List<IReorderingProcessor> strings = RenderComponentsUtil.func_238505_a_(new StringTextComponent(drawText), width, font);
        for (IReorderingProcessor str : strings)
        {
            drawCenteredString(matrixStack, str, x, y, drawColour);
            y += font.FONT_HEIGHT;
        }
    }

    public void drawCenteredString(MatrixStack matrixStack, IReorderingProcessor text, int x, int y, int color)
    {
        Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, text, (float)(x - Minecraft.getInstance().fontRenderer.func_243245_a(text) / 2), (float)y, color);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
    {
        if(minigameList != null)
            minigameList.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);

        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        return true;
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        if(minigameList != null)
            minigameList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);

        super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return true;
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if(minigameList != null)
            minigameList.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);

        super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        return false;
    }

    private enum State
    {
        LOGGING_IN, CHECKING_CREDENTIALS, CREDENTIALS_OK, CREDENTIALS_INVALID, LOGIN_FAILURE, TWOFACTOR_NEEDED, STARTING_MINIGAME, LOGIN_SUCCESS, TWOFACTOR_FAILURE, MINIGAME_ACTIVE, MINIGAME_FAILED, NOT_ENOUGH_CREDIT, UNKNOWN_ERROR, READY_TO_JOIN;
        
        private static State currentState = CHECKING_CREDENTIALS;
        
        public static void pushState(State state)
        {
            if (current.settingsButton == null)
                return;
            MinigamesScreen.current.settingsButton.active = true;
            switch (state)
            {
                case LOGGING_IN:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.emailField.setEnabled(false);
                        MinigamesScreen.settings.passwordField.setEnabled(false);
                        MinigamesScreen.settings.oneCodeField.setEnabled(false);
                        MinigamesScreen.settings.loginButton.active = false;
                    }
                    break;
                case CREDENTIALS_OK:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.emailField.setVisible(false);
                        MinigamesScreen.settings.passwordField.setVisible(false);
                        MinigamesScreen.settings.oneCodeField.setVisible(false);
                        MinigamesScreen.settings.emailField.setEnabled(false);
                        MinigamesScreen.settings.passwordField.setEnabled(false);
                        MinigamesScreen.settings.oneCodeField.setEnabled(false);
                        MinigamesScreen.settings.emailLabel.visible = false;
                        MinigamesScreen.settings.passwordLabel.visible = false;
                        MinigamesScreen.settings.oneCodeLabel.visible = false;
                        MinigamesScreen.settings.loginButton.setMessage(new StringTextComponent("Log in again"));
                        MinigamesScreen.settings.loginButton.active = true;
                        MinigamesScreen.settings.loginButton.visible = true;
                    }
                    
                    if (MinigamesScreen.current.spinDown)
                    {
                        MinigamesScreen.current.doSpindown();
                    }
                    break;
                case CHECKING_CREDENTIALS:
                    MinigamesScreen.current.settingsButton.active = false;
                    break;
                case CREDENTIALS_INVALID:
                case LOGIN_FAILURE:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.emailField.setEnabled(true);
                        MinigamesScreen.settings.passwordField.setEnabled(true);
                        MinigamesScreen.settings.oneCodeField.setEnabled(false);
                        MinigamesScreen.settings.emailField.setVisible(true);
                        MinigamesScreen.settings.passwordField.setVisible(true);
                        MinigamesScreen.settings.oneCodeField.setVisible(false);
                        MinigamesScreen.settings.emailLabel.visible = true;
                        MinigamesScreen.settings.passwordLabel.visible = true;
                        MinigamesScreen.settings.oneCodeLabel.visible = false;
                        MinigamesScreen.settings.loginButton.setMessage(new StringTextComponent("Log in"));
                        MinigamesScreen.settings.loginButton.active = true;
                        MinigamesScreen.settings.loginButton.visible = true;
                    }
                    break;
                case TWOFACTOR_NEEDED:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.emailField.setVisible(false);
                        MinigamesScreen.settings.passwordField.setVisible(false);
                        MinigamesScreen.settings.emailField.setEnabled(false);
                        MinigamesScreen.settings.passwordField.setEnabled(false);
                        MinigamesScreen.settings.oneCodeField.setVisible(true);
                        MinigamesScreen.settings.oneCodeField.setEnabled(true);
                        MinigamesScreen.settings.emailLabel.visible = false;
                        MinigamesScreen.settings.passwordLabel.visible = false;
                        MinigamesScreen.settings.oneCodeLabel.visible = true;
                    }
                    break;
                case TWOFACTOR_FAILURE:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.oneCodeField.setEnabled(true);
                        MinigamesScreen.settings.oneCodeField.setVisible(true);
                        MinigamesScreen.settings.oneCodeLabel.visible = true;
                    }
                    break;
            }
            currentState = state;
            if (state == CREDENTIALS_OK)
            {
                MinigamesScreen.current.settingsButton.setMessage(new StringTextComponent("Logged in"));
            } else
            {
                MinigamesScreen.current.settingsButton.setMessage(new StringTextComponent("Log in"));
            }
            
            Screen curScreen = Minecraft.getInstance().currentScreen;
            if (curScreen instanceof IStateHandler)
            {
                ((IStateHandler) curScreen).handleStatePush(state);
            }
        }
        
        public static State getCurrentState()
        {
            return currentState;
        }
        
        public static void refreshState()
        {
            pushState(currentState);
        }
    }
    
    public class Settings extends Screen
    {
        public TextFieldWidget emailField;
        public GuiLabel emailLabel;
        public TextFieldWidget passwordField;
        public GuiLabel passwordLabel;
        public TextFieldWidget oneCodeField;
        public GuiLabel oneCodeLabel;
        public Button cancelButton;
        public Button loginButton;
        private boolean previous2fa;
        protected List<GuiLabel> labelList = Lists.<GuiLabel>newArrayList();

        protected Settings()
        {
            super(new StringTextComponent(""));
        }
        
        @Override
        public void init()
        {
            super.init();
            labelList.clear();
            
            emailField = new TextFieldWidget(font, width / 2 - 100, height / 2 - 20, 200, 20, new StringTextComponent(""));
            labelList.add(emailLabel = new GuiLabel(font, 80856, emailField.x, emailField.y - 10, 200, 20, 0xFFFFFFFF));
            emailLabel.addLine("Email");
            
            oneCodeField = new TextFieldWidget(font, width / 2 - 100, emailField.y - 10, 200, 20, new StringTextComponent(""));
            labelList.add(oneCodeLabel = new GuiLabel(font, 80856, oneCodeField.x, oneCodeField.y - 10, 200, 20, 0xFFFFFFFF));
            oneCodeLabel.addLine("One-time code");
            
            passwordField = new GuiTextFieldCompatCensor(font, width / 2 - 100, height / 2 + 10, 200, 20, "Password");
            labelList.add(passwordLabel = new GuiLabel(font, 80856, passwordField.x, passwordField.y - 10, 200, 20, 0xFFFFFFFF));
            passwordLabel.addLine("Password");
            
            addButton(cancelButton = new Button(width - 10 - 100, height - 5 - 20, 100, 20, new StringTextComponent("Go back"), p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
            }));
            addButton(loginButton = new Button(width / 2 - 50, height / 2 + 40, 100, 20, new StringTextComponent("Save"), p ->
            {
                if (State.getCurrentState() == State.CREDENTIALS_OK)
                {
                    key = "";
                    secret = "";
                    saveCredentials();
                    checkCredentials();
                } else
                {
                    executor.submit(() ->
                    {
                        try
                        {
                            Map<String, String> credentials = new HashMap<>();
                            credentials.put("email", emailField.getText());
                            credentials.put("password", passwordField.getText());
                            credentials.put("oneCode", oneCodeField.getText().replaceAll("[^0-9]", ""));
                            
                            State.pushState(State.LOGGING_IN);
                            String resp = WebUtils.postWebResponse("https://staging-panel.creeper.host/mt.php", credentials);
                            
                            
                            JsonParser parser = new JsonParser();
                            JsonElement el = parser.parse(resp);
                            if (el.isJsonObject())
                            {
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.get("success").getAsBoolean())
                                {
                                    key = obj.get("key").getAsString();
                                    secret = obj.get("secret").getAsString();
                                    try
                                    {
                                        if (checkCredentials().get())
                                        {
                                            saveCredentials();
                                        }
                                    } catch (InterruptedException | ExecutionException ignored)
                                    {
                                    }
                                    emailField.setText("");
                                    passwordField.setText("");
                                    oneCodeField.setText("");
                                } else
                                {
                                    if (obj.has("_2fa") && !obj.get("_2fa").isJsonNull() && obj.get("_2fa").getAsBoolean())
                                    {
                                        if (previous2fa)
                                        {
                                            loginFailureMessage = "Invalid code. Please try again or reset it by logging into the CreeperPanel";
                                            State.pushState(State.TWOFACTOR_FAILURE);
                                            return;
                                        }
                                        loginFailureMessage = "Please enter your two-factor code";
                                        State.pushState(State.TWOFACTOR_NEEDED);
                                        oneCodeField.setFocused2(true);
                                        emailField.setFocused2(false);
                                        passwordField.setFocused2(false);
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
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
            }));
            
            State.refreshState();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_)
        {
            emailField.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
            passwordField.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
            oneCodeField.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
            return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
        }
        
        @Override
        public boolean charTyped(char typedChar, int keyCode)
        {
            emailField.charTyped(typedChar, keyCode);
            passwordField.charTyped(typedChar, keyCode);
            oneCodeField.charTyped(typedChar, keyCode);
            return super.charTyped(typedChar, keyCode);
        }
        
        @Override
        public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
        {
            emailField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            passwordField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            oneCodeField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
        
        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            renderDirtBackground(0);
            emailField.render(matrixStack, mouseX, mouseY, partialTicks);
            passwordField.render(matrixStack, mouseX, mouseY, partialTicks);
            oneCodeField.render(matrixStack, mouseX, mouseY, partialTicks);
            for (int j = 0; j < this.labelList.size(); ++j)
            {
                ((GuiLabel)this.labelList.get(j)).drawLabel(matrixStack, this.minecraft, mouseX, mouseY);
            }

            super.render(matrixStack, mouseX, mouseY, partialTicks);
            if (State.getCurrentState() == State.CREDENTIALS_OK)
            {
                drawCenteredSplitString(matrixStack, "You have valid credentials. If you wish to change your credentials, please log in again.", width / 2, height / 2 - 30, width, 0xFFFFFFFF);
            } else
            {
                drawCenteredSplitString(matrixStack, "If you would like to use your CreeperHost credit balance instead of the free minigame credits, please login with your CreeperHost username and password here.", width / 2, height / 2 - 60, width, 0xFFFFFFFF);
            }
            
            drawStatusString(matrixStack, width / 2, height - 40);
        }
    }

    public class StartMinigame extends Screen implements IStateHandler
    {
        private final Minigame minigame;
        private String failedReason = "";
        private int port;
        private String ip;
        private Button joinServerButton;

        public StartMinigame(Minigame minigame)
        {
            super(new StringTextComponent(""));
            this.minigame = minigame;

            State.pushState(State.STARTING_MINIGAME);

            executor.submit(() ->
            {
                try
                {
                    String url = minigame.template;
                    int ram = minigame.ram;

                    Aries aries = new Aries(key, secret);

                    Map creditResp = aries.doApiCall("minetogether", "minigamecredit");

                    if (creditResp.get("status").equals("success"))
                    {
                        String credit = creditResp.get("credit").toString();
                        if (true) // credit check here
                        {
                            Map<String, String> sendMap = new HashMap<>();

                            sendMap.put("id", String.valueOf(minigame.id));
                            sendMap.put("hash", getPlayerHash(MineTogether.proxy.getUUID()));
                            sendMap.put("key2", key);
                            sendMap.put("secret2", secret);

                            Map map = aries.doApiCall("minetogether", "startminigame", sendMap);

                            if (map.get("status").equals("success"))
                            {
                                try
                                {
                                    State.pushState(State.MINIGAME_ACTIVE);
                                    ip = map.get("ip").toString();
                                    port = Double.valueOf(map.get("port").toString()).intValue();
                                    MineTogether.serverOn = true;
                                    MineTogether.startMinetogetherThread(map.get("ip").toString(), "Minigame: " + Minecraft.getInstance().getSession().getUsername(), MineTogether.instance.base64 == null ? Config.getInstance().curseProjectID : MineTogether.instance.base64, port, MineTogether.Discoverability.INVITE);
                                    while (true)
                                    {
                                        if (MineTogether.isActive)
                                        {
                                            State.pushState(State.READY_TO_JOIN);
                                            MineTogether.instance.curServerId = MineTogether.updateID;
                                            MineTogether.instance.activeMinigame = String.valueOf(map.get("uuid"));
                                            MineTogether.instance.minigameID = minigame.id;
                                            MineTogether.instance.trialMinigame = map.get("type").equals("trial");
                                            break;
                                        } else if (MineTogether.failed)
                                        {
                                            State.pushState(State.MINIGAME_FAILED);
                                            break;
                                        }
                                        Thread.sleep(1000);
                                    }
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            } else
                            {
                                failedReason = (String) map.get("message");
                                State.pushState(State.MINIGAME_FAILED);
                            }
                        } else
                        {
                            State.pushState(State.NOT_ENOUGH_CREDIT);
                        }
                    } else
                    {
                        State.pushState(State.UNKNOWN_ERROR);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void tick()
        {
            ticks++;
        }

        @Override
        public void init()
        {
            super.init();
            addButton(joinServerButton = new Button(width / 2 - 50, height / 2 + 20, 100, 20, new StringTextComponent("Join server"), p ->
            {
                if (State.getCurrentState() == State.READY_TO_JOIN)
                {
                    MineTogether.instance.joinTime = System.currentTimeMillis();
                    ServerData serverData = new ServerData("", ip + ":" + port, false);
                    Minecraft.getInstance().currentScreen = new ConnectingScreen(this, Minecraft.getInstance(), serverData);
                } else
                    Minecraft.getInstance().displayGuiScreen(new MinigamesScreen(parent));
            }));
            joinServerButton.active = false;
            joinServerButton.visible = false;
            State.refreshState();
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            renderDirtBackground(0);
            if (State.getCurrentState() == State.MINIGAME_FAILED)
            {
                drawCenteredSplitString(matrixStack, "Minigame failed. Reason: " + failedReason, width / 2, (height / 2) - 10, width, 0xFFFF0000);
            } else
            {
                drawStatusString(matrixStack, width / 2, height / 2);
            }
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            if (State.getCurrentState() != State.READY_TO_JOIN && State.getCurrentState() != State.MINIGAME_FAILED)
                RenderUtils.loadingSpin(partialTicks, ticks++,  width / 2, height / 2 + 20 + 10, new ItemStack(Items.BEEF));
        }

        @Override
        public void handleStatePush(State state)
        {
            if (state == State.READY_TO_JOIN)
            {
                joinServerButton.active = true;
                joinServerButton.visible = true;
            } else if (state == State.MINIGAME_FAILED)
            {
                joinServerButton.active = true;
                joinServerButton.visible = true;
                joinServerButton.setMessage(new StringTextComponent("Go back"));
            }
        }
    }
    
    public interface IStateHandler
    {
        void handleStatePush(State state);
    }
}
