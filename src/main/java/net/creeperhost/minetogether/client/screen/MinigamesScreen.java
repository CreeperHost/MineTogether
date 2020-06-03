package net.creeperhost.minetogether.client.screen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Minigame;
import net.creeperhost.minetogether.aries.Aries;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.client.screen.element.GuiActiveFake;
import net.creeperhost.minetogether.client.screen.element.GuiTextFieldCompatCensor;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.lib.Constants;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.Pair;
import net.creeperhost.minetogether.util.WebUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.creeperhost.minetogether.paul.Callbacks.getPlayerHash;

public class MinigamesScreen extends Screen
{
    private static MinigamesScreen current;
    private List<Minigame> minigames;
    private List<Minigame> vanillaMinigames;
    private GuiScrollingMinigames minigameScroll;
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
    
    private boolean isModded = true;
    private GuiActiveFake moddedButton;
    private GuiActiveFake vanillaButton;
    private Screen parent;
    private Button cancelButton;
    
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
    
    Minigame lastMinigame = null;
    
    public boolean spinDown = false;
    
    public MinigamesScreen(Screen parent, boolean spinDown)
    {
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
            for (Minigame minigame : minigameTemp)
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
    public void tick()
    {
        ticks++;
        Minigame minigame = minigameScroll.getMinigame();
        if (lastMinigame != minigame)
        {
            if (minigame == null)
            {
                quote = -1;
            } else
            {
                executor.submit(() ->
                {
                    try
                    {
                        Map<String, String> sendMap = new HashMap<>();
                        
                        sendMap.put("id", String.valueOf(minigame.id));
                        sendMap.put("hash", getPlayerHash(MineTogether.proxy.getUUID()));
                        sendMap.put("key2", key);
                        sendMap.put("secret2", secret);
                        
                        Aries aries = new Aries(key, secret);
                        
                        Map map = aries.doApiCall("minetogether", "minigamequote", sendMap);
                        
                        if (map.get("status").equals("success"))
                        {
                            quote = Float.valueOf(String.valueOf(map.get("quote")));
                        } else
                        {
                            quote = -1;
                        }
                    } catch (Throwable t)
                    {
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
        RenderSystem.translated(width / 2, height / 2 + 20 + 10, 0);
        RenderSystem.pushMatrix();
        float scale = 1F + ((throbTicks >= (throbTickMax / 2) ? (throbTickMax - (throbTicks + partialTicks)) : (throbTicks + partialTicks)) * (2F / throbTickMax));
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.rotatef((rotateTicks + partialTicks) * (360F / rotateTickMax), 0, 0, 1);
        RenderSystem.pushMatrix();
        
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderItemAndEffectIntoGUI(stack, -8, -8);
        
        RenderSystem.popMatrix();
        RenderSystem.popMatrix();
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
        GuiScrollingMinigames tempMinigameScroll = new GuiScrollingMinigames(34);
        tempMinigameScroll.tick(minigameScroll);
        minigameScroll = tempMinigameScroll;
        addButton(settingsButton = new Button(width - 10 - 100, 5, 100, 20, "Login", p ->
        {
            Minecraft.getInstance().displayGuiScreen(settings = new Settings());
        }));
        addButton(spinupButton = new Button(width - 10 - 100, height - 5 - 20, 100, 20, "Start minigame", p ->
        {
            if ((State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameScroll.getMinigame() != null)
            {
                Minecraft.getInstance().displayGuiScreen(new StartMinigame(minigameScroll.getMinigame()));
            }
        }));
        addButton(moddedButton = new GuiActiveFake(10, 30, (width / 2) - 5, 20, "Modded", p ->
        {
            isModded = true;
            minigameScroll.setSelected(null);
            moddedButton.setActive(true);
            vanillaButton.setActive(false);
        }));
        addButton(vanillaButton = new GuiActiveFake(width - 10 - ((width / 2) - 10), 30, (width / 2) - 5, 20, "Vanilla", p ->
        {
            isModded = false;
            minigameScroll.setSelected(null);
            vanillaButton.setActive(true);
            moddedButton.setActive(false);
        }));
        addButton(cancelButton = new Button(10, height - 5 - 20, 100, 20, "Cancel", p ->
        {
            Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
        }));
        moddedButton.setActive(isModded);
        vanillaButton.setActive(!isModded);
        State.refreshState();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(1);
        if (Config.getInstance().isChatEnabled() && !ChatHandler.isOnline())
        {
            spinupButton.visible = spinupButton.active = vanillaButton.visible = vanillaButton.active =
                    moddedButton.visible = moddedButton.active = settingsButton.visible = settingsButton.active = false;
            drawCenteredString(minecraft.fontRenderer, I18n.format("minetogether.minigames.notavailable"), width / 2, height / 2, 0xFFFFFFFF);
            super.render(mouseX, mouseY, partialTicks);
            return;
        }
        if (!spinDown)
        {
            spinupButton.active = minigameScroll != null && (State.getCurrentState() == State.CREDENTIALS_OK || State.getCurrentState() == State.CREDENTIALS_INVALID) && minigameScroll.getMinigame() != null && credit >= quote;
            minigameScroll.tick(minigameScroll);
            super.render(mouseX, mouseY, partialTicks);
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
            
            drawCenteredString(minecraft.fontRenderer, "MineTogether Minigames", width / 2, 5, 0xFFFFFFFF);
            
            drawString(minecraft.fontRenderer, creditStr, 5, 5, 0xFFFFFFFF);
            drawStatusString(width / 2, height - 40);
            
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
                drawString(minecraft.fontRenderer, formattedQuote, 5, height - 40, 0xFFFFFFFF);
                int stringLen = minecraft.fontRenderer.getStringWidth(formattedQuote);
                if (!creditType.equals("credit") && !curPrefix.equals("£") && mouseX >= 5 && mouseX <= 5 + stringLen && mouseY >= height - 40 && mouseY <= height - 30)
                {
                    renderTooltip(Arrays.asList("Figure provided based on exchange rate of " + exchangeRate), mouseX, mouseY);
                } else
                {
                    if (spinupButton.isHovered() && credit < quote)
                    {
                        renderTooltip(Arrays.asList("Cannot start minigame as you do not have enough credit"), mouseX, mouseY);
                    }
                }
            }
        } else
        {
            drawCenteredSplitString("Spinning down minigame", width / 2, height / 2, width, 0xFFFFFFFF);
            loadingSpin(partialTicks);
            if (doSpindown)
            {
                Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
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
        this.minecraft.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        blit(p_178012_1_, p_178012_2_, 0.0F, 0.0F, width, height, texturew, textureh);
        GlStateManager.disableBlend();
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
    
    private void drawCenteredSplitString(String drawText, int x, int y, int width, int drawColour)
    {
        List<String> strings = font.listFormattedStringToWidth(drawText, width);
        for (String str : strings)
        {
            drawCenteredString(font, str, x, y, drawColour);
            y += font.FONT_HEIGHT;
        }
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
//                        GuiMinigames.settings.emailLabel.visible = false;
//                        GuiMinigames.settings.passwordLabel.visible = false;
//                        GuiMinigames.settings.oneCodeLabel.visible = false;
                        MinigamesScreen.settings.loginButton.setMessage("Log in again");
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
//                        GuiMinigames.settings.emailLabel.visible = true;
//                        GuiMinigames.settings.passwordLabel.visible = true;
//                        GuiMinigames.settings.oneCodeLabel.visible = false;
                        MinigamesScreen.settings.loginButton.setMessage("Log in");
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
//                        GuiMinigames.settings.emailLabel.visible = false;
//                        GuiMinigames.settings.passwordLabel.visible = false;
//                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
                    break;
                case TWOFACTOR_FAILURE:
                    if (MinigamesScreen.settings != null)
                    {
                        MinigamesScreen.settings.oneCodeField.setEnabled(true);
                        MinigamesScreen.settings.oneCodeField.setVisible(true);
//                        GuiMinigames.settings.oneCodeLabel.visible = true;
                    }
                    break;
            }
            currentState = state;
            if (state == CREDENTIALS_OK)
            {
                MinigamesScreen.current.settingsButton.setMessage("Logged in");
            } else
            {
                MinigamesScreen.current.settingsButton.setMessage("Log in");
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
    
    private class GuiScrollingMinigames extends ExtendedList
    {
        public GuiScrollingMinigames(int entryHeight)
        {
            super(Minecraft.getInstance(), MinigamesScreen.this.width - 20, MinigamesScreen.this.height - 50, 50, MinigamesScreen.this.height - 50, 10);
        }
        
        @Override
        protected int getItemCount()
        {
            List<Minigame> minigames = isModded ? MinigamesScreen.this.minigames : MinigamesScreen.this.vanillaMinigames;
            return minigames == null ? 1 : minigames.size();
        }
        
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            List<Minigame> minigames = isModded ? MinigamesScreen.this.minigames : MinigamesScreen.this.vanillaMinigames;
            if (minigames == null)
            {
                drawCenteredString(font, "Loading minigames...", width / 2, slotTop, 0xFFFFFFFF);
            } else
            {
                Minigame game = minigames.get(slotIdx);
                
                if (!minigameTexturesCache.containsKey(game.id))
                {
                    ResourceLocation resourceLocation = new ResourceLocation(Constants.MOD_ID, "minigame/" + game.id);
                    BufferedImage imageData = null;
                    try
                    {
                        imageData = ImageIO.read(new URL(game.displayIcon));
                    } catch (IOException ignored)
                    {
                    }
                    
                    if (imageData != null)
                    {
//                        DynamicTexture texture = new DynamicTexture(imageData);
//                        minecraft.getTextureManager().loadTexture(resourceLocation, texture);
//                        texture.updateDynamicTexture();
                        minigameTexturesCache.put(game.id, resourceLocation);
                        minigameTexturesSize.put(game.id, new Pair<>(imageData.getWidth(), imageData.getHeight()));
                    } else
                    {
                        minigameTexturesCache.put(game.id, new ResourceLocation("minecraft", "textures/misc/unknown_server.png"));
                        minigameTexturesSize.put(game.id, new Pair(32, 32));
                    }
                }
                
                ResourceLocation resourceLocation = minigameTexturesCache.get(game.id);
                
                Pair<Integer, Integer> wh = minigameTexturesSize.get(game.id);
                
                drawTextureAt(13, slotTop + 1, 28, 28, 28, 28, resourceLocation);
                
                RenderSystem.pushMatrix();
                float scale = 1.5f;
                RenderSystem.scalef(scale, scale, scale);
                int x = width / 2;
                int y = slotTop;
                x = (int) (x / scale);
                y = (int) (y / scale);
                
                int gameWidth = (int) (font.getStringWidth(minigames.get(slotIdx).displayName) * scale);
                int newX = (width / 2) + (gameWidth / 2);
                
                drawCenteredString(font, minigames.get(slotIdx).displayName, x, y, 0xFFFFFFFF);
                
                RenderSystem.popMatrix();
                
                drawString(font, " by " + minigames.get(slotIdx).author, newX, slotTop + 2, 0xFFAAAAAA);
                
                String displayDescription = minigames.get(slotIdx).displayDescription;
                if (font.getStringWidth(displayDescription) > (width - 96) * 2)
                {
                    while (font.getStringWidth(displayDescription + "...") > (width - 96) * 2)
                    {
                        displayDescription = displayDescription.substring(0, displayDescription.lastIndexOf(" "));
                    }
                    displayDescription += "...";
                }
                
                drawCenteredSplitString(displayDescription, width / 2, slotTop + 12, width - 84, 0xFFAAAAAA);
            }
        }
        
        public Minigame getMinigame()
        {
            List<Minigame> minigames = isModded ? MinigamesScreen.this.minigames : MinigamesScreen.this.vanillaMinigames;
            return null;//getSelected() >= 0 ? minigames.get(selectedIndex) : null;
        }
        
        public void tick(GuiScrollingMinigames previous)
        {
            if (previous == null)
                return;
            setSelected(previous.getSelected());
        }
    }
    
    public class Settings extends Screen
    {
        public TextFieldWidget emailField;
        //        public GuiLabel emailLabel;
        public TextFieldWidget passwordField;
        //        public GuiLabel passwordLabel;
        public TextFieldWidget oneCodeField;
        //        public GuiLabel oneCodeLabel;
        public Button cancelButton;
        public Button loginButton;
        private boolean previous2fa;
        
        protected Settings()
        {
            super(new StringTextComponent(""));
        }
        
        @Override
        public void init()
        {
            super.init();
//            labelList.clear();
            
            emailField = new TextFieldWidget(font, width / 2 - 100, height / 2 - 20, 200, 20, "");
//            labelList.add(emailLabel = new GuiLabel(fontRendererObj, 80856, emailField.xPosition, emailField.yPosition - 10, 200, 20, 0xFFFFFFFF));
//            emailLabel.addLine("Email");
            
            oneCodeField = new TextFieldWidget(font, width / 2 - 100, emailField.y - 10, 200, 20, "");
//            labelList.add(oneCodeLabel = new GuiLabel(fontRendererObj, 80856, oneCodeField.xPosition, oneCodeField.yPosition - 10, 200, 20, 0xFFFFFFFF));
//            oneCodeLabel.addLine("One-time code");
            
            passwordField = new GuiTextFieldCompatCensor(font, width / 2 - 100, height / 2 + 10, 200, 20, "Password");
//            labelList.add(passwordLabel = new GuiLabel(fontRendererObj, 80856, passwordField.xPosition, passwordField.yPosition - 10, 200, 20, 0xFFFFFFFF));
//            passwordLabel.addLine("Password");
            
            addButton(cancelButton = new Button(width - 10 - 100, height - 5 - 20, 100, 20, "Go back", p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
            }));
            addButton(loginButton = new Button(width / 2 - 50, height / 2 + 40, 100, 20, "Save", p ->
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
        public void render(int mouseX, int mouseY, float partialTicks)
        {
            renderDirtBackground(0);
            emailField.render(mouseX, mouseY, partialTicks);
            passwordField.render(mouseX, mouseY, partialTicks);
            oneCodeField.render(mouseX, mouseY, partialTicks);
            super.render(mouseX, mouseY, partialTicks);
            if (State.getCurrentState() == State.CREDENTIALS_OK)
            {
                drawCenteredSplitString("You have valid credentials. If you wish to change your credentials, please log in again.", width / 2, height / 2 - 30, width, 0xFFFFFFFF);
            } else
            {
                drawCenteredSplitString("If you would like to use your CreeperHost credit balance instead of the free minigame credits, please login with your CreeperHost username and password here.", width / 2, height / 2 - 60, width, 0xFFFFFFFF);
            }
            
            drawStatusString(width / 2, height - 40);
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
            addButton(joinServerButton = new Button(width / 2 - 50, height / 2 + 20, 100, 20, "Join server", p ->
            {
                if (State.getCurrentState() == State.READY_TO_JOIN)
                {
                    MineTogether.instance.joinTime = System.currentTimeMillis();
//                    FMLClientHandler.instance().connectToServerAtStartup(ip, port);
                } else
                    Minecraft.getInstance().displayGuiScreen(new MinigamesScreen(parent));
            }));
            joinServerButton.active = false;
            joinServerButton.visible = false;
            State.refreshState();
        }
        
        @Override
        public void render(int mouseX, int mouseY, float partialTicks)
        {
            renderDirtBackground(0);
            if (State.getCurrentState() == State.MINIGAME_FAILED)
            {
                drawCenteredSplitString("Minigame failed. Reason: " + failedReason, width / 2, height / 2, width, 0xFFFF0000);
            } else
            {
                drawStatusString(width / 2, height / 2);
            }
            super.render(mouseX, mouseY, partialTicks);
            if (State.getCurrentState() != State.READY_TO_JOIN && State.getCurrentState() != State.MINIGAME_FAILED)
                loadingSpin(partialTicks);
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
                joinServerButton.setMessage("Go back");
            }
        }
    }
    
    public interface IStateHandler
    {
        void handleStatePush(State state);
    }
}
