package net.creeperhost.minetogether.orderform;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.minetogether.gui.dialogs.ItemSelectDialog;
import net.creeperhost.minetogether.gui.dialogs.OptionDialog;
import net.creeperhost.minetogether.orderform.data.AvailableResult;
import net.creeperhost.minetogether.orderform.data.Order;
import net.creeperhost.minetogether.orderform.data.OrderSummary;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.TextState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Axis;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.helpers.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.RED;

/**
 * Created by brandon3055 on 04/10/2023
 */
public class OrderGui implements GuiProvider {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RAND = new Random();

    private final Order order = new Order();
    private final Map<String, Integer> regionPing = new ConcurrentHashMap<>();
    private final Map<String, Integer> dataCenterDistance = new ConcurrentHashMap<>();
    private final Map<String, String> dataCenterUrls = new HashMap<>();

    private GuiTextField nameField;
    private GuiElement<?> locations;
    private String confirmPassword = "";

    private CompletableFuture<?> initTask;
    private CompletableFuture<?> pingTask;
    private CompletableFuture<?> orderTask;
    private volatile boolean nameValid = false;
    private volatile Component nameMessage = null;
    private int nameCheckTimer = 60;
    private int pingTimer = 0;
    private volatile boolean pingUpdated = false;

    private volatile boolean emailValid = false;
    private volatile Component emailMessage;
    private int emailCheckTimer = 60;

    private boolean inputsValid = false;
    private Component invalidMessage = null;

    private boolean summaryUpdateRequired = false;
    private volatile boolean summaryUpdating = false;
    private volatile OrderSummary summary = new OrderSummary("Loading Summary...");

    private volatile boolean loginMode = false;
    private boolean loggingIn = false;
    private boolean loggedIn = false;
    private String loggingInError = "";

    private int orderNumber;
    private String invoiceID;

    private volatile boolean processing = false;
    private Component processingText = Component.empty();
    private GuiButton processingButton;
    private boolean processingShowCloseButton = false;

    public OrderGui() {
    }

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    private void initDefaults() {
        initTask = CompletableFuture.runAsync(() -> {
            for (String region : ServerOrderCallbacks.getRegionMap().keySet()) {
                regionPing.put(region, -1);
            }
            try {
                dataCenterDistance.putAll(ServerOrderCallbacks.getDataCentres());
                dataCenterUrls.putAll(ServerOrderCallbacks.getDataCentreURLs());
            } catch (IOException | URISyntaxException ex) {
                LOGGER.error("Failed to poll Data Centers.", ex);
            }
            order.serverLocation = datacentreToRegion(ServerOrderCallbacks.getRecommendedLocation());
            order.country = Countries.getOurCountry();
            summaryUpdateRequired = true;
        });
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
        GuiElement<?> lastElement;

        Constraint left = relative(background.get(LEFT), 5);
        Constraint right = relative(background.get(RIGHT), -5);

        lastElement = configSection(background, left, right);
        lastElement = locationSection(background, lastElement, left, right);
        lastElement = detailsSection(background, lastElement, left, right);
    }

    private GuiElement<?> configSection(GuiElement<?> background, Constraint left, Constraint right) {
        Constraint midPos = relative(background.get(LEFT), 90);

        GuiElement<?> lastElement = new GuiText(background, Component.translatable("minetogether:gui.order.configure").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(background.get(TOP), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        //Server Name
        lastElement = new GuiText(background, Component.translatable("minetogether:gui.order.server_name"))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 5))
                .constrain(LEFT, left)
                .constrain(RIGHT, midPos)
                .constrain(HEIGHT, literal(14));

        GuiButton randomise = MTStyle.Flat.button(background, Component.translatable("minetogether:gui.order.button.randomize"))
                .onPress(() -> nameField.setValue(getDefaultName()))
                .constrain(TOP, match(lastElement.get(TOP)))
                .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                .constrain(RIGHT, right)
                .constrain(WIDTH, literal(70));

        GuiElement<?> nameBackground = MTStyle.Flat.contentArea(background)
                .constrain(TOP, match(lastElement.get(TOP)))
                .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                .constrain(LEFT, midPos)
                .constrain(RIGHT, relative(randomise.get(LEFT), -2));

        Pattern namePattern = Pattern.compile("([A-Za-z0-9]*)");
        nameField = new GuiTextField(nameBackground)
                .setTextState(TextState.create(() -> order.name, s -> {
                    order.name = s;
                    nameDirty();
                }))
                .setMaxLength(16)
                .setFilter(s -> s.isEmpty() || namePattern.matcher(s).matches());
        Constraints.bind(nameField, nameBackground, 0, 3, 0, 3);

        //Player Count
        lastElement = new GuiText(background, Component.translatable("minetogether:gui.order.player_count"))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 5))
                .constrain(LEFT, left)
                .constrain(RIGHT, midPos)
                .constrain(HEIGHT, literal(14));

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            int count = 5 + (i * 5);
            MTStyle.Flat.button(background, Component.literal(String.valueOf(count)))
                    .setToggleMode(() -> order.playerAmount == count)
                    .onPress(() -> {
                        order.playerAmount = count;
                        summaryDirty();
                    })
                    .constrain(TOP, match(lastElement.get(TOP)))
                    .constrain(BOTTOM, match(lastElement.get(BOTTOM)))
                    .constrain(LEFT, dynamic(() -> midPos.get() + ((((right.get() + 1) - midPos.get()) / 5) * finalI)).precise())
                    .constrain(WIDTH, dynamic(() -> (((right.get() + 1) - midPos.get()) / 5) - 1).precise());
        }

        Component playerCountInfo = Component.translatable("minetogether:gui.order.player_count.info").withStyle(ChatFormatting.GRAY);
        lastElement = new GuiText(background, playerCountInfo)
                .setWrap(true)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, left)
                .constrain(RIGHT, right);
        lastElement.constrain(HEIGHT, dynamic(() -> (double) background.font().wordWrapHeight(playerCountInfo, (int) right.get() - (int) left.get())));

        lastElement = MTStyle.Flat.button(background, () -> Component.translatable("minetogether:gui.order.pregen_" + order.pregen))
                .onPress(() -> order.pregen = !order.pregen)
                .setToggleMode(() -> order.pregen)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(HEIGHT, literal(14))
                .constrain(LEFT, left)
                .constrain(RIGHT, right);

        return lastElement;
    }

    private GuiElement<?> locationSection(GuiElement<?> background, GuiElement<?> lastElement, Constraint left, Constraint right) {
        lastElement = new GuiText(background, Component.translatable("minetogether:gui.order.location").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        lastElement = locations = new GuiElement<>(background)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        GuiText locationLoading = new GuiText(locations, Component.translatable("minetogether:gui.order.loading_locations").withStyle(ChatFormatting.YELLOW))
                .setAlignment(Align.LEFT);
        Constraints.bind(locationLoading, locations);

        return lastElement;
    }

    private void updateLocations() {
        locations.getChildren().forEach(locations::removeChild);
        if (regionPing.isEmpty()) {
            locations.constrain(HEIGHT, literal(8));
            GuiText error = new GuiText(locations, Component.translatable("minetogether:gui.order.loading_locations_fail").withStyle(ChatFormatting.RED))
                    .setAlignment(Align.LEFT);
            Constraints.bind(error, locations);
        } else {
            List<String> regionOrder = new ArrayList<>(regionPing.keySet());
            regionOrder.sort(Comparator.comparingDouble(key -> regionPing.get(key) < 0 ? 9999999 : regionPing.get(key)));
            GuiElement<?> element = null;
            for (String region : regionOrder) {
                element = locationButton(locations, region)
                        .constrain(TOP, element == null ? match(locations.get(TOP)) : relative(element.get(BOTTOM), 1))
                        .constrain(LEFT, match(locations.get(LEFT)))
                        .constrain(RIGHT, match(locations.get(RIGHT)));
            }
            if (element != null) {
                locations.constrain(BOTTOM, match(element.get(BOTTOM)));
            }
        }
    }

    private GuiElement<?> detailsSection(GuiElement<?> background, GuiElement<?> lastElement, Constraint left, Constraint right) {
        lastElement = new GuiText(background, Component.translatable("minetogether:gui.order.details").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        Constraint centerLeft = Constraint.midPoint(background.get(LEFT), background.get(RIGHT), -1);
        Constraint centerRight = Constraint.midPoint(background.get(LEFT), background.get(RIGHT), 1);

        GuiElement<?> email = emailBox(background, left, right)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4));

        GuiElement<?> password = passwordBox(background, left, centerLeft, TextState.create(() -> order.password, val -> order.password = val), Component.translatable("minetogether.info.password"))
                .constrain(TOP, relative(email.get(BOTTOM), 4));
        GuiElement<?> password2 = passwordBox(background, centerRight, right, TextState.create(() -> confirmPassword, val -> confirmPassword = val), Component.translatable("minetogether.info.password_confirm"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(email.get(BOTTOM), 4));

        GuiElement<?> firstName = detailsBox(background, left, centerLeft, TextState.create(() -> order.firstName, val -> order.firstName = val), Component.translatable("minetogether.info.first_name"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(password.get(BOTTOM), 4));
        GuiElement<?> lastName = detailsBox(background, centerRight, right, TextState.create(() -> order.lastName, val -> order.lastName = val), Component.translatable("minetogether.info.last_name"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(password.get(BOTTOM), 4));

        GuiElement<?> address = detailsBox(background, left, centerLeft, TextState.create(() -> order.address, val -> order.address = val), Component.translatable("minetogether.info.address"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(firstName.get(BOTTOM), 4));
        GuiElement<?> city = detailsBox(background, centerRight, right, TextState.create(() -> order.city, val -> order.city = val), Component.translatable("minetogether.info.city"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(firstName.get(BOTTOM), 4));

        GuiElement<?> zipCode = detailsBox(background, left, centerLeft, TextState.create(() -> order.zip, val -> order.zip = val), Component.translatable("minetogether.info.zip"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(address.get(BOTTOM), 4));
        GuiElement<?> state = detailsBox(background, centerRight, right, TextState.create(() -> order.state, val -> order.state = val), Component.translatable("minetogether.info.state"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(address.get(BOTTOM), 4));

        Map<String, Country> countryMap = getCountries();
        GuiButton country = MTStyle.Flat.button(background, () -> Component.literal(getSelectedCountry().toString()))
                .setEnabled(() -> !loginMode)
                .onPress(() -> new ItemSelectDialog<>(background.getModularGui().getRoot(), Component.translatable("minetogether:gui.order.select_country"), new ArrayList<>(countryMap.values()), countryMap.get(order.country)).setOnItemSelected(item -> {
                    order.country = item.key;
                    summaryDirty();
                }))
                .constrain(TOP, relative(zipCode.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, centerLeft)
                .constrain(HEIGHT, literal(12));

        GuiElement<?> phone = detailsBox(background, centerRight, right, TextState.create(() -> order.phone, val -> order.phone = val), Component.translatable("minetogether.info.phone"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(zipCode.get(BOTTOM), 4));

        GuiButton login = MTStyle.Flat.button(background, Component.translatable("minetogether:gui.button.login"))
                .setEnabled(() -> loginMode)
                .onPress(this::doLogin)
                .setDisabled(() -> order.password.isEmpty() || loggingIn || loggedIn)
                .constrain(TOP, relative(email.get(BOTTOM), 4))
                .constrain(LEFT, centerRight)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        new GuiText(background, Component.empty())
                .setEnabled(() -> loginMode)
                .setWrap(true)
                .setTextSupplier(() -> {
                    if (loggingIn) {
                        return Component.translatable("minetogether:gui.order.logging_in");
                    } else if (loggedIn) {
                        return Component.translatable("minetogether:gui.order.login_success");
                    } else if (!loggingInError.isEmpty()) {
                        return Component.translatable("minetogether:gui.order.login_error", loggingInError);
                    }
                    return Component.translatable("minetogether:gui.order.account_exists");
                })
                .setAlignment(Align.CENTER)
                .constrain(TOP, relative(login.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(30));

        return lastElement;
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
        lastElement = new GuiText(scrollPane, Component.translatable("minetogether:gui.order.summary.location"))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

        lastElement = new GuiText(scrollPane, () -> Component.translatable("minetogether:gui.order.region." + order.serverLocation))
                .setEnabled(() -> summary.summaryError.isEmpty())
                .setTextColour(GREEN.getColor())
                .constrain(TOP, relative(lastElement.get(BOTTOM), 2))
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(8));

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

    private void setupPricePanel(GuiElement<?> background) {
        background.constrain(HEIGHT, dynamic(() -> 22D + (summary.tax != 0 ? 10 : 0) + (summary.discount != 0 ? 10 : 0)));

        GuiText total = new GuiText(background, Component.translatable("minetogether:gui.order.summary.total"))
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(background.get(BOTTOM), -1))
                .constrain(HEIGHT, literal(10));

        GuiText totalValue = new GuiText(background, () -> Component.literal(summary.prefix + summary.total + " " + summary.suffix))
                .setAlignment(Align.RIGHT);
        Constraints.bind(totalValue, total);

        GuiText tax = new GuiText(background, Component.translatable("minetogether:gui.order.summary.tax"))
                .setEnabled(() -> summary.tax != 0)
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(total.get(TOP), -0))
                .constrain(HEIGHT, dynamic(() -> summary.tax != 0 ? 10D : 0D));

        GuiText taxValue = new GuiText(background, () -> Component.literal(summary.prefix + summary.tax + " " + summary.suffix))
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

        GuiText discountValue = new GuiText(background, () -> Component.literal(summary.prefix + summary.discount + " " + summary.suffix))
                .setEnabled(() -> summary.discount != 0)
                .setAlignment(Align.RIGHT);
        Constraints.bind(discountValue, discount);

        GuiText subTotal = new GuiText(background, Component.translatable("minetogether:gui.order.summary.sub_total"))
                .setAlignment(Align.LEFT)
                .constrain(LEFT, relative(background.get(LEFT), 2))
                .constrain(RIGHT, relative(background.get(RIGHT), -2))
                .constrain(BOTTOM, relative(discount.get(TOP), -0))
                .constrain(HEIGHT, literal(10));

        GuiText subTotalValue = new GuiText(background, () -> Component.literal(summary.prefix + summary.subTotal + " " + summary.suffix))
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

    //=== GUI Component Builders ===//

    private GuiElement<?> locationButton(GuiElement<?> parent, String region) {
        GuiButton button = MTStyle.Flat.button(parent, (Supplier<Component>) null)
                .setToggleMode(() -> region.equals(order.serverLocation))
                .onPress(() -> {
                    order.serverLocation = region;
                    summaryDirty();
                })
                .constrain(HEIGHT, literal(12));

        GuiText label = new GuiText(button, Component.translatable("minetogether:gui.order.region." + region))
                .setAlignment(Align.LEFT);
        Constraints.bind(label, button, 0, 4, 0, 14);

        double ping = regionPing.getOrDefault(region, -2);
        int distance = dataCenterDistance.getOrDefault(regionToDataCentre(region), -1);
        GuiText pingLabel = new GuiText(button, Component.literal(((int) Math.ceil(ping)) + " ms"))
                .setEnabled(() -> ping > 0)
                .setAlignment(Align.RIGHT);
        Constraints.bind(pingLabel, button, 0, 4, 0, 14);

        GuiTexture signal = new GuiTexture(button, MTTextures.getter(() -> getSignalIcon(ping, distance)))
//                .setTooltip(ping == -2 ? Component.translatable("minetogether:gui.order.region.pinging_fail") : ping == -1 ? Component.translatable("minetogether:gui.order.region.pinging") : Component.translatable("minetogether:gui.order.region.signal"))
                .setTooltipSingle(() -> getSignalTooltip(ping, distance))
                .constrain(TOP, match(button.get(TOP)))
                .constrain(BOTTOM, match(button.get(BOTTOM)))
                .constrain(RIGHT, match(button.get(RIGHT)))
                .constrain(WIDTH, literal(12));

        return button;
    }

    private GuiElement<?> emailBox(GuiElement<?> parent, Constraint left, Constraint right) {
        GuiElement<?> background = MTStyle.Flat.contentArea(parent)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(Component.translatable("minetogether.info.e_mail"))
                .setTextState(TextState.create(() -> order.emailAddress, s -> {
                    order.emailAddress = s;
                    emailDirty();
                }));
        textField.setSuggestionColour(() -> 0xFFFFFF);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }

    private GuiElement<?> passwordBox(GuiElement<?> parent, Constraint left, Constraint right, TextState textState, Component suggestion) {
        GuiElement<?> background = MTStyle.Flat.contentArea(parent)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(suggestion)
                .setFormatter((s, integer) -> Component.literal(StringUtils.repeat('*', s.length())).getVisualOrderText())
                .setTextState(textState);
        textField.setSuggestionColour(() -> 0xFFFFFF);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }

    private GuiElement<?> detailsBox(GuiElement<?> parent, Constraint left, Constraint right, TextState textState, Component suggestion) {
        GuiElement<?> background = MTStyle.Flat.contentArea(parent)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(suggestion)
                .setTextState(textState);
        textField.setSuggestionColour(() -> 0xFFFFFF);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }

    //=== Logic ===//

    private void validateInputs() {
        inputsValid = false;
        if (!nameValid) {
            invalidMessage = nameMessage;
        } else if (!emailValid) {
            invalidMessage = emailMessage;
        } else if (!loginMode && !confirmPassword.equals(order.password)) {
            invalidMessage = Component.translatable("minetogether:gui.order.passwords_dont_match");
        } else if (order.password.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.password");
        } else if (!loginMode && order.firstName.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.first_name");
        } else if (!loginMode && order.lastName.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.last_name");
        } else if (!loginMode && order.address.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.address");
        } else if (!loginMode && order.city.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.city");
        } else if (!loginMode && order.zip.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.zip");
        } else if (!loginMode && order.state.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.state");
        } else if (!loginMode && order.phone.isEmpty()) {
            invalidMessage = Component.translatable("minetogether:gui.order.blank.phone");
        } else if (loginMode && !loggedIn) {
            invalidMessage = Component.translatable("minetogether:gui.order.login_required");
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

    private void nameDirty() {
        nameValid = false;
        nameMessage = Component.translatable("minetogether:gui.order.name_not_checked");
        nameCheckTimer = 60;
    }

    private void summaryDirty() {
        summaryUpdateRequired = true;
    }

    private void doLogin() {
        loggingIn = true;
        CompletableFuture.runAsync(() -> {
            String result = ServerOrderCallbacks.doLogin(order.emailAddress, order.password);
            String[] resultSplit = result.split(":");
            if (resultSplit[0].equals("success")) {
                order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                order.clientID = resultSplit[2] != null ? resultSplit[2] : "98874"; // random test account fallback
                loggingIn = false;
                loggedIn = true;
                loggingInError = "";
                summaryDirty();
            } else {
                loggingIn = false;
                loggedIn = false;
                loggingInError = result;
            }
        });
    }

    private void placeOrder(ModularGui gui) {
        if (orderTask != null) return;

        orderTask = CompletableFuture.runAsync(() -> {
            //Create Account
            if (!loginMode) {
                setProcessing(null, null, Component.translatable("minetogether:gui.order.account_creating"));
                String result = ServerOrderCallbacks.createAccount(order);
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success")) {
                    order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                    order.clientID = resultSplit[2] != null ? resultSplit[2] : "0"; // random test account fallback
                } else {
                    setProcessing(Component.translatable("minetogether:gui.button.ok"), this::clearProcessing, Component.translatable("minetogether:gui.order.account_error", result));
                    return;
                }
            }

            //Place Order
            setProcessing(null, null, Component.translatable("minetogether:gui.order.order_placing"));
            String result = ServerOrderCallbacks.createOrder(order, String.valueOf(Config.instance().pregenDiameter));
            String[] resultSplit = result.split(":");
            if (resultSplit[0].equals("success")) {
                invoiceID = resultSplit[1] != null ? resultSplit[1] : "0";
                orderNumber = Integer.parseInt(resultSplit[2]);
            } else {
                setProcessing(Component.translatable("minetogether:gui.button.ok"), this::clearProcessing, Component.translatable("minetogether:gui.order.order_error", result));
                return;
            }

            processingShowCloseButton = true;
            setProcessing(Component.translatable("minetogether:gui.button.invoice"), () -> {
                try {
                    Util.getPlatform().openUri(new URI(ServerOrderCallbacks.getPaymentLink(invoiceID)));
                } catch (Throwable throwable) {
                    gui.mc().keyboardHandler.setClipboard(ServerOrderCallbacks.getPaymentLink(invoiceID));
                    processingText = Component.literal("Something went wrong while attempting to open the link,\nSo the link has been copied to your clipboard.");
                    LOGGER.error("Couldn't open link", throwable);
                }
            }, Component.translatable("minetogether:gui.order.order_success"));
        });
    }

    private void tick(ModularGui gui) {
        if (!nameValid && --nameCheckTimer == 0) {
            if (order.name.isEmpty()) {
                nameMessage = Component.translatable("minetogether:gui.order.blank.name");
            } else {
                nameMessage = Component.translatable("minetogether:gui.order.name_checking");
                CompletableFuture.runAsync(() -> {
                    AvailableResult result = ServerOrderCallbacks.getNameAvailable(order.name);
                    nameValid = result.getSuccess();
                    nameMessage = Component.literal(result.getMessage());
                });
            }
        }

        if (!emailValid && --emailCheckTimer == 0) {
            if (order.emailAddress.isEmpty()) {
                emailMessage = Component.translatable("minetogether:gui.order.blank.email");
            } else {
                emailMessage = Component.translatable("minetogether:gui.order.email_checking");
                CompletableFuture.runAsync(() -> {
                    loginMode = ServerOrderCallbacks.doesEmailExist(order.emailAddress);
                    emailValid = true;
                    emailMessage = null;
                });
            }
        }

        if (initTask != null && initTask.isDone() && locations != null) {
            initTask = null;
            updateLocations();
        }

        //Update data-center pings.
        if (initTask == null && pingTask == null && pingTimer-- <= 0) {
            pingTask = CompletableFuture.runAsync(() -> {
                List<CompletableFuture<?>> pingers = new ArrayList<>();
                for (String key : regionPing.keySet()) {
                    pingers.add(CompletableFuture.runAsync(() -> {
                        String url = dataCenterUrls.get(regionToDataCentre(key));
                        int distance = dataCenterDistance.get(regionToDataCentre(key));
                        if (url == null || distance == -1) {
                            regionPing.put(key, -2);
                        } else {
                            try {
                                regionPing.put(key, ServerOrderCallbacks.getDataCentreLatency(url, distance));
                            } catch (IOException ignored) {
                                regionPing.put(key, -2);
                            }
                        }
                        pingUpdated = true;
                    }));
                }

                boolean allDone;
                do {
                    allDone = pingers.stream().allMatch(CompletableFuture::isDone);
                } while (!allDone);
                pingTimer = 200;
            });
        } else if (pingTask != null && pingTask.isDone()) {
            pingTask = null;
            updateLocations();
        }

        if (pingUpdated) {
            pingUpdated = false;
            updateLocations();
        }

        if (summaryUpdateRequired && !summaryUpdating) {
            summaryUpdating = true;
            summaryUpdateRequired = false;
            CompletableFuture.runAsync(() -> {
                summary = ServerOrderCallbacks.getSummary(order, Config.instance().promoCode);
                order.productID = summary.productID;
                order.currency = summary.currency;
                summaryUpdating = false;
            });
        }

        if (orderTask != null && orderTask.isDone()) {
            orderTask = null;
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

    private String getSignalIcon(double ping, int distance) {
        if (ping > 0) {
            int icon = MathUtil.clamp(5 - (int) (ping / 42), 1, 5);
            return "signal/signal_" + icon;
        } else if (distance != -1) {
            if (distance < 1000) return "signal/signal_5";
            if (distance > 1000 && distance < 3000) return "signal/signal_4";
            if (distance > 3000 && distance < 5000) return "signal/signal_3";
            if (distance > 5000 && distance < 6000) return "signal/signal_2";
            return "signal/signal_1";
        } else if (ping == -1) {
            int l = (int) (Util.getMillis() / 100L & 7L);
            if (l > 4) {
                l = 8 - l;
            }
            return "signal/scan_" + l;
        }
        return "signal/signal_0";
    }

    private Component getSignalTooltip(double ping, int distance) {
        if (ping > 0) {
            return Component.translatable("minetogether:gui.order.region.signal");
        } else if (distance > 0) {
            return Component.translatable("minetogether:gui.order.region.from_distance");
        } else if (ping == -1) {
            return Component.translatable("minetogether:gui.order.region.pinging");
        }
        return Component.translatable("minetogether:gui.order.region.pinging_fail");
    }

    private Map<String, Country> getCountries() {
        Map<String, Country> map = new LinkedHashMap<>();
        Countries.COUNTRIES.forEach((key, name) -> map.put(key, new Country(key, name)));
        return map;
    }

    private Country getSelectedCountry() {
        return new Country(order.country, Countries.COUNTRIES.get(order.country));
    }

    //This is only used to get an Approximate distance
    private String regionToDataCentre(String region) {
        return switch (region) {
            case "eu-west" -> "grantham";
            case "na-east" -> "buffalo";
            case "na-west" -> "losangeles";
            case "na-south" -> "dallas";
            case "sub-saharan-africa" -> "johannesburg";
            case "south-america" -> "saopaulo";
            case "asia" -> "hongkong";
            case "australia" -> "sydney";
            case "eu-middle-east" -> "bucharest";
            default -> "";
        };
    }

    private String datacentreToRegion(String centre) {
        return switch (centre) {
            case "grantham" -> "eu-west";
            case "buffalo" -> "na-east";
            case "chicago" -> "na-east";
            case "miami" -> "na-south";
            case "dallas" -> "na-south";
            case "seattle" -> "na-west";
            case "losangeles" -> "na-west";
            case "johannesburg" -> "sub-saharan-africa";
            case "tokyo" -> "asia";
            case "saopaulo" -> "south-america";
            case "hongkong" -> "asia";
            case "sydney" -> "australia";
            case "bucharest" -> "eu-middle-east";
            default -> "";
        };
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

    record Country(String key, String name) {
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
}
