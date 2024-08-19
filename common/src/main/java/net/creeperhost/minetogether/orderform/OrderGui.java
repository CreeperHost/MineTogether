package net.creeperhost.minetogether.orderform;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.dialogs.OptionDialog;
import net.creeperhost.minetogether.lib.web.ApiResponse;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.data.OrderSummary;
import net.creeperhost.minetogether.orderform.elements.DetailsElement;
import net.creeperhost.minetogether.orderform.elements.LocationElement;
import net.creeperhost.minetogether.orderform.elements.ServerConfigElement;
import net.creeperhost.minetogether.orderform.elements.WorldElement;
import net.creeperhost.minetogether.orderform.requests.GetDataCentresRequest.DC;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.sprite.Material;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 04/10/2023
 */
public class OrderGui implements GuiProvider {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setNameFormat("MT Order Requests Thread %d")
                    .setDaemon(true)
                    .build()
    );

    private static final ExecutorService PING_EXECUTOR = Executors.newFixedThreadPool(12,
            new ThreadFactoryBuilder()
                    .setNameFormat("MT Ping Thread %d")
                    .setDaemon(true)
                    .build()
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Random RAND = new Random();

    public final Order order = new Order();
    private final Map<String, String> dcIdMap = new ConcurrentHashMap<>();
    public final Map<String, DC> dcMap = new ConcurrentHashMap<>();
    public final Map<String, Integer> dcPing = new ConcurrentHashMap<>();
    public final Map<String, Long> dcDistance = new ConcurrentHashMap<>();

    private CompletableFuture<?> initTask;
    private CompletableFuture<?> pingTask;
    private CompletableFuture<?> orderTask;
    private CompletableFuture<?> summaryTask;
    private volatile boolean nameValid = false;
    private volatile Component nameMessage = null;
    private int nameCheckTimer = 60;
    private int pingTimer = 0;
    private volatile boolean pingUpdated = false;

    private volatile boolean emailValid = false;
    private volatile Component emailMessage;
    private int emailCheckTimer = 60;

    private boolean inputsValid = false;
    public Component invalidMessage = null;

    private boolean summaryUpdateRequired = false;
    private volatile boolean summaryUpdating = false;
    private volatile OrderSummary summary = new OrderSummary("Loading Summary...");

    private String invoiceID;

    private volatile boolean processing = false;
    private Component processingText = Component.empty();
    private GuiButton processingButton;
    private boolean processingShowCloseButton = false;

    public LocationElement locations;
    public DetailsElement details;
    public WorldElement world;

    private OrderGui() {}

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    private void initDefaults() {
        initTask = CompletableFuture.runAsync(() -> {
            OrderRequests.getLocations().forEach((dc, id) -> dcIdMap.put(dc, String.valueOf(id)));
            OrderRequests.getDataCenters(512).forEach(dc -> dcMap.put(dc.slug, dc));
            dcIdMap.forEach((dc, i) -> dcPing.put(dc, -1));

            var byDistance = OrderRequests.getDCsByDistance();
            if (byDistance != null) {
                order.serverLocation = byDistance.getDataCenter().getName();
                byDistance.getDataCenters().forEach(dc -> dcDistance.put(dc.getName(), dc.getDistance()));
            }

            order.country = Countries.getOurCountry();
            summaryUpdateRequired = true;
        }, EXECUTOR);
        order.name = getDefaultName();
    }

    //=== Build the GUI ===//

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.translatable("minetogether:gui.order.title"));

        GuiElement<?> root = gui.getRoot();

        GuiText title = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 5))
                .constrain(HEIGHT, Constraint.literal(8))
                .constrain(LEFT, match(root.get(LEFT)))
                .constrain(RIGHT, match(root.get(RIGHT)));

        GuiElement<?> orderBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(root.get(LEFT), 5))
                .constrain(RIGHT, relative(root.get(RIGHT), -170))
                .constrain(TOP, relative(root.get(TOP), 22))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -5));

        GuiElement<?> priceBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(orderBg.get(RIGHT), 5))
                .constrain(RIGHT, relative(root.get(RIGHT), -5))
                .constrain(BOTTOM, relative(orderBg.get(BOTTOM), -17));

        GuiElement<?> summaryBg = MTStyle.Flat.contentArea(root)
                .constrain(LEFT, relative(orderBg.get(RIGHT), 5))
                .constrain(RIGHT, relative(root.get(RIGHT), -5))
                .constrain(TOP, match(orderBg.get(TOP)))
                .constrain(BOTTOM, relative(priceBg.get(TOP), -2));

        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back_arrow"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(BOTTOM, relative(orderBg.get(TOP), -4))
                .constrain(LEFT, match(orderBg.get(LEFT)))
                .constrain(WIDTH, literal(50))
                .constrain(HEIGHT, literal(14));

        GuiButton placeOrder = MTStyle.Flat.buttonPrimary(root, this::getOrderButtonText)
                .onPress(() -> {
                    new OptionDialog(locations.getModularGui().getRoot(),
                            Component.translatable("minetogether:gui.order.place_order.confirm"),
                            Component.translatable("minetogether:gui.button.confirm").withStyle(GREEN),
                            Component.translatable("minetogether:gui.button.cancel").withStyle(RED))
                            .onButtonPress(0, () -> placeOrder(gui))
                            .constrain(HEIGHT, literal(70));
                })
                .setDisabled(() -> !inputsValid || orderTask != null || summaryUpdateRequired || summaryUpdating)
                .constrain(TOP, relative(priceBg.get(BOTTOM), 2))
                .constrain(LEFT, match(priceBg.get(LEFT)))
                .constrain(RIGHT, match(priceBg.get(RIGHT)))
                .constrain(BOTTOM, match(orderBg.get(BOTTOM)));

        GuiScrolling scrollArea = new GuiScrolling(orderBg);
        scrollArea.getContentElement().constrain(RIGHT, match(scrollArea.get(RIGHT)));
        Constraints.bind(scrollArea, orderBg);

        var scrollBar = MTStyle.Flat.scrollBar(orderBg, Axis.Y);
        scrollBar.primary
                .setSliderState(scrollArea.scrollState(Axis.Y))
                .setScrollableElement(scrollArea);
        scrollBar.container
                .setEnabled(() -> scrollArea.hiddenSize(Axis.Y) > 0)
                .constrain(LEFT, relative(orderBg.get(RIGHT), -4))
                .constrain(TOP, match(orderBg.get(TOP)))
                .constrain(BOTTOM, match(orderBg.get(BOTTOM)))
                .constrain(WIDTH, literal(4));

        setupOrderPanel(scrollArea.getContentElement());
        setupSummaryPanel(summaryBg);
        setupPricePanel(priceBg);
        buildProcessingScreen(root);
        gui.onTick(() -> tick(gui));
        initDefaults();
    }

    private void setupOrderPanel(GuiElement<?> background) {
        int sidePadding = 5;
        int spacing = 4;

        ServerConfigElement config = new ServerConfigElement(background, this)
                .constrain(LEFT, relative(background.get(LEFT), sidePadding))
                .constrain(RIGHT, relative(background.get(RIGHT), -sidePadding))
                .constrain(TOP, relative(background.get(TOP), spacing));

        locations = new LocationElement(background, this)
                .constrain(LEFT, relative(background.get(LEFT), sidePadding))
                .constrain(RIGHT, relative(background.get(RIGHT), -sidePadding))
                .constrain(TOP, relative(config.get(BOTTOM), spacing));

        details = new DetailsElement(background, this)
                .constrain(LEFT, relative(background.get(LEFT), sidePadding))
                .constrain(RIGHT, relative(background.get(RIGHT), -sidePadding))
                .constrain(TOP, relative(locations.get(BOTTOM), spacing));

        world = new WorldElement(background, this)
                .constrain(LEFT, relative(background.get(LEFT), sidePadding))
                .constrain(RIGHT, relative(background.get(RIGHT), -sidePadding))
                .constrain(TOP, relative(details.get(BOTTOM), spacing));

        Constraints.placeOutside(new GuiElement<>(background).setSize(10, spacing), world, Constraints.LayoutPos.BOTTOM_CENTER);
    }

    private void setupSummaryPanel(GuiElement<?> background) {
        Constraint left = match(background.get(LEFT));
        Constraint right = match(background.get(RIGHT));

        GuiScrolling scrollArea = new GuiScrolling(background);
        GuiElement<?> scrollPane = scrollArea.getContentElement();
        scrollPane.constrain(RIGHT, match(scrollArea.get(RIGHT)));
        Constraints.bind(scrollArea, background);

        var scrollBar = MTStyle.Flat.scrollBar(background, Axis.Y);
        scrollBar.primary
                .setSliderState(scrollArea.scrollState(Axis.Y))
                .setScrollableElement(scrollArea);
        scrollBar.container
                .setEnabled(() -> scrollArea.hiddenSize(Axis.Y) > 0)
                .constrain(LEFT, match(background.get(RIGHT)))
                .constrain(TOP, match(background.get(TOP)))
                .constrain(BOTTOM, match(background.get(BOTTOM)))
                .constrain(WIDTH, literal(4));

        GuiElement<?> lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .constrain(TOP, relative(scrollPane.get(TOP), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        new GuiText(scrollPane, () -> Component.literal(summary.summaryError).withStyle(ChatFormatting.YELLOW))
                .setEnabled(() -> !summary.summaryError.isEmpty())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        //Server Details
        lastElement = new GuiTextList(scrollPane, () -> summary.serverFeatures.stream().map(Component::literal).toList())
                .setScroll(false)
                .setLineSpacing(1)
                .setVerticalAlign(Align.CENTER)
                .setTextColour(GREEN.getColor())
                .setEnabled(() -> summary.summaryError.isEmpty())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();

        //Location
        Component locText = Component.translatable("minetogether:gui.order.summary.location");
        int locWidth = scrollPane.font().width(locText);
        lastElement = new GuiText(scrollPane, locText)
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTooltipSingle(this::fallbackToolTip)
                .setTooltipDelay(0)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        new GuiText(lastElement, Component.literal("I").withStyle(UNDERLINE, GREEN))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTooltipSingle(this::fallbackToolTip)
                .setTooltipDelay(0)
                .setScroll(false)
                .constrain(TOP, match(lastElement.get(TOP)))
                .constrain(LEFT, relative(lastElement.get(RIGHT), () -> 2 - ((right.get() - left.get()) - locWidth) / 2D))
                .constrain(WIDTH, literal(10))
                .constrain(HEIGHT, literal(8));

        lastElement = new GuiText(scrollPane, () -> Component.literal(getDCName(order.serverLocation)))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTextColour(GREEN.getColor())
                .setWrap(true)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();

        //Plan
        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.plan"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        lastElement = new GuiText(scrollPane, () -> Component.literal(summary.serverHostName))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTextColour(GREEN.getColor())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        //Includes
        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.features"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        for (int i = 1; i < 6; i++) {
            lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.feature" + i))
                    .setEnabled(() -> summary.summaryError.isEmpty())
                    .setTextColour(GREEN.getColor())
                    .constrain(TOP, relative(lastElement.get(BOTTOM), i == 1 ? 2 : 1))
                    .constrain(LEFT, left)
                    .constrain(RIGHT, right)
                    .constrain(HEIGHT, literal(8));
        }

        //Paying for
        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.paying_for"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setWrap(true)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();

        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.paying_for_details1"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTextColour(GREEN.getColor())
                .setWrap(true)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();

        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.paying_for_details2"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTextColour(GREEN.getColor())
                .setWrap(true)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 1))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();

        //Cancel any time
        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.cancel_any_time"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setWrap(true)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .autoHeight();
    }

    private Component fallbackToolTip() {
        return Component.translatable("minetogether:gui.order.summary.location_fallback", Component.literal(getDCName(order.serverLocation)).withStyle(GOLD), Component.literal(getDCName(computeFallbackLocation())).withStyle(GOLD));
    }

    private void setupPricePanel(GuiElement<?> background) {
        background.constrain(HEIGHT, dynamic(() -> 22D + (summary.tax != 0 ? 10 : 0) + (summary.discount != 0 ? 10 : 0)));

        GuiText total = new GuiText(background, Component.translatable("minetogether:gui.order.summary.total"))
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(background.get(BOTTOM), -1))
                .constrain(HEIGHT, literal(10));

        GuiText totalValue = new GuiText(background, () -> Component.literal(summary.prefix + String.format("%.2f", summary.total) + " " + summary.suffix))
                .setAlignment(Align.RIGHT);
        Constraints.bind(totalValue, total);

        GuiText tax = new GuiText(background, Component.translatable("minetogether:gui.order.summary.tax"))
                .setEnabled(() -> summary.tax != 0)
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(total.get(TOP), -0))
                .constrain(HEIGHT, dynamic(() -> summary.tax != 0 ? 10D : 0D));

        GuiText taxValue = new GuiText(background, () -> Component.literal(summary.prefix + String.format("%.2f", summary.tax) + " " + summary.suffix))
                .setEnabled(() -> summary.tax != 0)
                .setAlignment(Align.RIGHT);
        Constraints.bind(taxValue, tax);

        GuiText discount = new GuiText(background, Component.translatable("minetogether:gui.order.summary.discount"))
                .setEnabled(() -> summary.discount != 0)
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(tax.get(TOP), -0))
                .constrain(HEIGHT, dynamic(() -> summary.discount != 0 ? 10D : 0D));

        GuiText discountValue = new GuiText(background, () -> Component.literal(summary.prefix + String.format("%.2f", summary.discount) + " " + summary.suffix))
                .setEnabled(() -> summary.discount != 0)
                .setAlignment(Align.RIGHT);
        Constraints.bind(discountValue, discount);

        GuiText subTotal = new GuiText(background, Component.translatable("minetogether:gui.order.summary.sub_total"))
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(discount.get(TOP), -0))
                .constrain(HEIGHT, literal(10));

        GuiText subTotalValue = new GuiText(background, () -> Component.literal(summary.prefix + String.format("%.2f", summary.preDiscount) + " " + summary.suffix))
                .setAlignment(Align.RIGHT);
        Constraints.bind(subTotalValue, subTotal);
    }

    //Greys out UI and shows current "processing" step
    private void buildProcessingScreen(GuiElement<?> root) {
        GuiElement<?> background = new GuiRectangle(root)
                .setEnabled(() -> processing)
                .setOpaque(true)
                .fill(0xE0000000);
        Constraints.bind(background, root);

        GuiText textList = new GuiText(background, () -> processingText)
                .setWrap(true)
                .constrain(LEFT, relative(background.get(LEFT), 10))
                .constrain(RIGHT, relative(background.get(RIGHT), -10))
                .constrain(BOTTOM, midPoint(background.get(TOP), background.get(BOTTOM), -5))
                .autoHeight();

        processingButton = MTStyle.Flat.button(background, Component.empty())
                .constrain(LEFT, midPoint(background.get(LEFT), background.get(RIGHT), -50))
                .constrain(RIGHT, midPoint(background.get(LEFT), background.get(RIGHT), 50))
                .constrain(TOP, midPoint(background.get(TOP), background.get(BOTTOM), 5))
                .constrain(HEIGHT, literal(14));

        MTStyle.Flat.button(background, Component.translatable("minetogether:gui.button.close"))
                .setEnabled(() -> processingShowCloseButton)
                .onPress(() -> root.getModularGui().getScreen().onClose())
                .constrain(LEFT, match(processingButton.get(LEFT)))
                .constrain(RIGHT, match(processingButton.get(RIGHT)))
                .constrain(TOP, relative(processingButton.get(BOTTOM), 10))
                .constrain(HEIGHT, literal(14));
    }

    private void setProcessing(@Nullable Component buttonText, @Nullable Runnable action, Component text) {
        processing = true;
        processingButton.setEnabled(buttonText != null && action != null);
        if (processingButton.isEnabled()) {
            processingButton.getLabel().setText(buttonText);
            processingButton.onPress(action);
        }
        processingText = text;
    }

    private void clearProcessing() {
        processing = false;
    }

    //=== Logic ===//

    private void validateInputs() {
        inputsValid = false;
        if (!nameValid) {
            invalidMessage = nameMessage;
        } else if (!emailValid) {
            invalidMessage = emailMessage;
        } else if (!details.loginMode && !details.confirmPassword.equals(order.password)) {
            invalidMessage = Component.translatable("minetogether:gui.order.passwords_dont_match");
        } else if (order.password.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.password");
        } else if (!details.loginMode && order.firstName.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.first_name");
        } else if (!details.loginMode && order.lastName.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.last_name");
        } else if (!details.loginMode && order.address.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.address");
        } else if (!details.loginMode && order.city.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.city");
        } else if (!details.loginMode && order.zip.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.zip");
        } else if (!details.loginMode && order.state.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.state");
        } else if (!details.loginMode && order.phone.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.phone");
        } else if (details.loginMode && !details.loggedIn) {
            invalidMessage = Component.translatable("minetogether:gui.order.login_required");
        } else if (world.worldUploader != null) {
            invalidMessage = Component.translatable("minetogether:gui.order.waiting_for_world_upload");
        } else {
            inputsValid = true;
        }
    }

    public void emailDirty() {
        emailValid = false;
        if (!EMAIL_PATTERN.matcher(order.emailAddress.toLowerCase()).matches()) {
            emailMessage = Component.translatable("minetogether:gui.order.email_invalid");
            return;
        }
        emailCheckTimer = 60;
        emailMessage = Component.translatable("minetogether:gui.order.email_not_checked");
    }

    public void nameDirty() {
        nameValid = false;
        nameMessage = Component.translatable("minetogether:gui.order.name_not_checked");
        nameCheckTimer = 60;
    }

    public void summaryDirty() {
        summaryUpdateRequired = true;
    }

    private void placeOrder(ModularGui gui) {
        if (orderTask != null) return;

        orderTask = CompletableFuture.runAsync(() -> {
            //Create Account
            if (!details.loginMode) {
                setProcessing(null, null, Component.translatable("minetogether:gui.order.account_creating"));
                var result = OrderRequests.createAccount(order);
                if (result.getStatus().equals("success")) {
                    order.currency = result.currency != null ? result.currency : "1";
                    order.clientID = result.userid != null ? result.userid : "0"; // random test account fallback
                } else {
                    setProcessing(Component.translatable("minetogether:gui.button.ok"), this::clearProcessing, Component.translatable("minetogether:gui.order.account_error", result));
                    return;
                }
            }

            //Place Order
            setProcessing(null, null, Component.translatable("minetogether:gui.order.order_placing"));
            var result = OrderRequests.placeOrder(order, getDCId(order.serverLocation), String.valueOf(Config.instance().pregenDiameter), computeFallbackLocation());
            if (result.getStatus().equals("success")) {
                invoiceID = result.more == null || result.more.invoiceid == null ? "0" : result.more.invoiceid;
            } else {
                setProcessing(Component.translatable("minetogether:gui.button.ok"), this::clearProcessing, Component.translatable("minetogether:gui.order.order_error", result));
                return;
            }

            processingShowCloseButton = true;
            setProcessing(Component.translatable("minetogether:gui.button.invoice"), () -> {
                try {
                    Util.getPlatform().openUri(new URI(getPaymentLink(invoiceID)));
                } catch (Throwable throwable) {
                    gui.mc().keyboardHandler.setClipboard(getPaymentLink(invoiceID));
                    processingText = Component.literal("Something went wrong while attempting to open the link,\nSo the link has been copied to your clipboard.");
                    LOGGER.error("Couldn't open link", throwable);
                }
            }, Component.translatable("minetogether:gui.order.order_success"));
        }, EXECUTOR);
    }

    private void tick(ModularGui gui) {
        if (!nameValid && --nameCheckTimer == 0) {
            if (order.name.isEmpty()) {
                nameMessage = Component.translatable("minetogether:gui.order.blank.name");
            } else {
                nameMessage = Component.translatable("minetogether:gui.order.name_checking");
                CompletableFuture.runAsync(() -> {
                    ApiResponse result = OrderRequests.getNameAvailable(order.name);
                    nameValid = "success".equals(result.getStatus());
                    nameMessage = Component.literal(result.getMessage());
                }, EXECUTOR);
            }
        }

        if (!emailValid && --emailCheckTimer == 0) {
            if (order.emailAddress.isEmpty()) {
                emailMessage = Component.translatable("minetogether:gui.order.blank.email");
            } else {
                emailMessage = Component.translatable("minetogether:gui.order.email_checking");
                CompletableFuture.runAsync(() -> {
                    details.loginMode = OrderRequests.doesAccountExist(order.emailAddress);
                    emailValid = true;
                    emailMessage = null;
                }, EXECUTOR);
            }
        }

        if (initTask != null && initTask.isDone() && locations != null) {
            initTask = null;
            locations.updateLocations();
        }

        //Update data-center pings.
        if (initTask == null && pingTask == null && pingTimer-- <= 0) {
            pingTask = CompletableFuture.runAsync(() -> {
                List<CompletableFuture<?>> pingers = new ArrayList<>();
                dcMap.forEach((name, dc) -> pingers.add(CompletableFuture.runAsync(() -> {
                    long distance = dcDistance.get(name);
                    if (dc.latencyUrl == null || distance == -1) {
                        dcPing.put(name, -2);
                    } else {
                        dcPing.put(name, OrderRequests.getDCLatency(dc.latencyUrl, distance));
                    }
                    pingUpdated = true;
                }, PING_EXECUTOR)));
                boolean allDone;
                do {
                    allDone = pingers.stream().allMatch(CompletableFuture::isDone);
                } while (!allDone);
                pingTimer = 200;
            }, PING_EXECUTOR);
        } else if (pingTask != null && pingTask.isDone()) {
            pingTask = null;
            locations.updateLocations();
        }

        if (pingUpdated) {
            pingUpdated = false;
            locations.updateLocations();
        }

        if (summaryUpdateRequired && !summaryUpdating) {
            summaryUpdating = true;
            summaryUpdateRequired = false;
            summaryTask = CompletableFuture.runAsync(() -> {
                summary = OrderRequests.getSummary(order, Config.instance().promoCode);
                order.productID = summary.productID;
                order.currency = summary.currency;
                summaryUpdating = false;
                CompletableFuture.runAsync(() -> OrderRequests.getDataCenters(summary.ram + 4096).forEach(dc -> dcMap.put(dc.slug, dc)), EXECUTOR);
            }, EXECUTOR);
        }

        if (summaryTask != null && summaryTask.isDone()) {
            summaryTask = null;
            locations.updateLocations();
        }

        if (orderTask != null && orderTask.isDone()) {
            orderTask = null;
        }

        if (world.worldUploader != null && !world.worldUploader.errored()) {
            if (world.worldUploader.isFinished()) {
                order.worldUrl = world.worldUploader.getResultFileURL();
                world.worldUploader = null;
            }
        }

        validateInputs();
    }

    //=== Getters ===//

    private Component getOrderButtonText() {
        if (orderTask != null) {
            return Component.translatable("minetogether:gui.order.order_in_progress");
        } else if (summaryUpdating || summaryUpdateRequired) {
            return Component.translatable("minetogether:gui.order.summary.updating");
        } else if (inputsValid) {
            return Component.translatable("minetogether:gui.order.place_order");
        } else {
            return invalidMessage == null ? Component.empty() : invalidMessage.copy().withStyle(ChatFormatting.RED);
        }
    }

    public Country getSelectedCountry() {
        return new Country(order.country, Countries.COUNTRIES.get(order.country));
    }

    private String getDCId(String dc) {
        return dcIdMap.getOrDefault(dc, dc);
    }

    private String getDCName(String dc) {
        return dcMap.containsKey(dc) ? dcMap.get(dc).name : dc;
    }

    private String computeFallbackLocation() {
        if (!order.useFallback) return "";
        String fallBack = "";
        long lowest = Integer.MAX_VALUE;
        for (String dc : dcPing.keySet()) {
            int ping = dcPing.get(dc);
            if (ping > 0 && !dc.equals(order.serverLocation) && ping < lowest) {
                lowest = ping;
                fallBack = dc;
            }
        }

        if (fallBack.isEmpty()) {
            for (String dc : dcDistance.keySet()) {
                long distance = dcDistance.get(dc);
                if (distance > 0 && !dc.equals(order.serverLocation) && distance < lowest) {
                    lowest = distance;
                    fallBack = dc;
                }
            }
        }

        return fallBack;
    }

    public static String getDefaultName() {
        String[] nm1 = {"amber", "angel", "spirit", "basin", "lagoon", "basin", "arrow", "autumn", "bare", "bay", "beach", "bear", "bell", "black", "bleak", "blind", "bone", "boulder", "bridge", "brine", "brittle", "bronze", "castle", "cave", "chill", "clay", "clear", "cliff", "cloud", "cold", "crag", "crow", "crystal", "curse", "dark", "dawn", "dead", "deep", "deer", "demon", "dew", "dim", "dire", "dirt", "dog", "dragon", "dry", "dusk", "dust", "eagle", "earth", "east", "ebon", "edge", "elder", "ember", "ever", "fair", "fall", "false", "far", "fay", "fear", "flame", "flat", "frey", "frost", "ghost", "glimmer", "gloom", "gold", "grass", "gray", "green", "grim", "grime", "hazel", "heart", "high", "hollow", "honey", "hound", "ice", "iron", "kil", "knight", "lake", "last", "light", "lime", "little", "lost", "mad", "mage", "maple", "mid", "might", "mill", "mist", "moon", "moss", "mud", "mute", "myth", "never", "new", "night", "north", "oaken", "ocean", "old", "ox", "pearl", "pine", "pond", "pure", "quick", "rage", "raven", "red", "rime", "river", "rock", "rogue", "rose", "rust", "salt", "sand", "scorch", "shade", "shadow", "shimmer", "shroud", "silent", "silk", "silver", "sleek", "sleet", "sly", "small", "smooth", "snake", "snow", "south", "spring", "stag", "star", "steam", "steel", "steep", "still", "stone", "storm", "summer", "sun", "swamp", "swan", "swift", "thorn", "timber", "trade", "west", "whale", "whit", "white", "wild", "wilde", "wind", "winter", "wolf"};
        String[] nm2 = {"acre", "band", "barrow", "bay", "bell", "born", "borough", "bourne", "breach", "break", "brook", "burgh", "burn", "bury", "cairn", "call", "chill", "cliff", "coast", "crest", "cross", "dale", "denn", "drift", "fair", "fall", "falls", "fell", "field", "ford", "forest", "fort", "front", "frost", "garde", "gate", "glen", "grasp", "grave", "grove", "guard", "gulch", "gulf", "hall", "hallow", "ham", "hand", "harbor", "haven", "helm", "hill", "hold", "holde", "hollow", "horn", "host", "keep", "land", "light", "maw", "meadow", "mere", "mire", "mond", "moor", "more", "mount", "mouth", "pass", "peak", "point", "pond", "port", "post", "reach", "rest", "rock", "run", "scar", "shade", "shear", "shell", "shield", "shore", "shire", "side", "spell", "spire", "stall", "wich", "minster", "star", "storm", "strand", "summit", "tide", "town", "vale", "valley", "vault", "vein", "view", "ville", "wall", "wallow", "ward", "watch", "water", "well", "wharf", "wick", "wind", "wood", "yard"};

        int rnd = RAND.nextInt(nm1.length);
        int rnd2 = RAND.nextInt(nm2.length);
        while (Objects.equals(nm1[rnd], nm2[rnd2])) {
            rnd2 = RAND.nextInt(nm2.length);
        }
        return nm1[rnd] + nm2[rnd2] + RAND.nextInt(999);
    }

    public Material getFlag(DC dc) {
        String code = dc.country.toUpperCase(Locale.ROOT);
        if (Countries.COUNTRIES.containsKey(code)) {
            code = code.toLowerCase(Locale.ROOT);
        } else {
            code = "unknown";
        }
        return MTTextures.get("flags/" + code);
    }

    public double flagWidth(DC dc, double height) {
        TextureAtlasSprite sprite = getFlag(dc).sprite();
        return (sprite.getWidth() / (double)sprite.getHeight()) * height;
    }

    public static String getPaymentLink(String invoiceID) {
        return "https://billing.creeperhost.net/viewinvoice.php?id=" + invoiceID;
    }

    public record Country(String key, String name) {
        @Override
        public String toString() {
            return name == null ? "" : name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Country country = (Country) o;
            return Objects.equals(key, country.key) && Objects.equals(name, country.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, name);
        }
    }

    public static class Screen extends ModularGuiScreen {
        public Screen(net.minecraft.client.gui.screens.Screen parentScreen) {
            super(new OrderGui(), parentScreen);
        }
    }
}
