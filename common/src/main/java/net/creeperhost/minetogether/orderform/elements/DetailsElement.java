package net.creeperhost.minetogether.orderform.elements;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.dialogs.ItemSelectDialog;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.minetogether.orderform.OrderRequests;
import net.creeperhost.minetogether.util.Countries;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.TextState;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 28/06/2024
 */
public class DetailsElement extends GuiElement<DetailsElement> {

    private final OrderGui gui;
    public volatile boolean loginMode = false;
    private boolean loggingIn = false;
    public boolean loggedIn = false;
    private String loggingInError = "";
    public String confirmPassword = "";

    public DetailsElement(@NotNull GuiParent<?> parent, OrderGui gui) {
        super(parent);
        this.gui = gui;

        GuiElement<?> lastElement = new GuiText(this, new TranslatableComponent("minetogether:gui.order.details").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, match(get(TOP)))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        Constraint centerLeft = Constraint.midPoint(get(LEFT), get(RIGHT), -1);
        Constraint centerRight = Constraint.midPoint(get(LEFT), get(RIGHT), 1);

        GuiElement<?> email = emailBox(match(get(LEFT)), match(get(RIGHT)))
                .constrain(TOP, relative(lastElement.get(BOTTOM), 4));

        GuiElement<?> password = passwordBox(match(get(LEFT)), centerLeft, TextState.create(() -> gui.order.password, val -> gui.order.password = val), new TranslatableComponent("minetogether.info.password"))
                .constrain(TOP, relative(email.get(BOTTOM), 4));
        GuiElement<?> password2 = passwordBox(centerRight, match(get(RIGHT)), TextState.create(() -> confirmPassword, val -> confirmPassword = val), new TranslatableComponent("minetogether.info.password_confirm"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(email.get(BOTTOM), 4));

        GuiElement<?> firstName = detailsBox(match(get(LEFT)), centerLeft, TextState.create(() -> gui.order.firstName, val -> gui.order.firstName = val), new TranslatableComponent("minetogether.info.first_name"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(password.get(BOTTOM), 4));
        GuiElement<?> lastName = detailsBox(centerRight, match(get(RIGHT)), TextState.create(() -> gui.order.lastName, val -> gui.order.lastName = val), new TranslatableComponent("minetogether.info.last_name"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(password.get(BOTTOM), 4));

        GuiElement<?> address = detailsBox(match(get(LEFT)), centerLeft, TextState.create(() -> gui.order.address, val -> gui.order.address = val), new TranslatableComponent("minetogether.info.address"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(firstName.get(BOTTOM), 4));
        GuiElement<?> city = detailsBox(centerRight, match(get(RIGHT)), TextState.create(() -> gui.order.city, val -> gui.order.city = val), new TranslatableComponent("minetogether.info.city"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(firstName.get(BOTTOM), 4));

        GuiElement<?> zipCode = detailsBox(match(get(LEFT)), centerLeft, TextState.create(() -> gui.order.zip, val -> gui.order.zip = val), new TranslatableComponent("minetogether.info.zip"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(address.get(BOTTOM), 4));
        GuiElement<?> state = detailsBox(centerRight, match(get(RIGHT)), TextState.create(() -> gui.order.state, val -> gui.order.state = val), new TranslatableComponent("minetogether.info.state"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(address.get(BOTTOM), 4));

        Map<String, OrderGui.Country> countryMap = getCountries();
        GuiButton country = MTStyle.Flat.button(this, () -> new TextComponent(gui.getSelectedCountry().toString()))
                .setEnabled(() -> !loginMode)
                .onPress(() -> new ItemSelectDialog<>(this.getModularGui().getRoot(), new TranslatableComponent("minetogether:gui.order.select_country"), new ArrayList<>(countryMap.values()), countryMap.get(gui.order.country)).setOnItemSelected(item -> {
                    gui.order.country = item.key();
                    gui.summaryDirty();
                }))
                .constrain(TOP, relative(zipCode.get(BOTTOM), 4))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, centerLeft)
                .constrain(HEIGHT, literal(12));

        GuiElement<?> phone = detailsBox(centerRight, match(get(RIGHT)), TextState.create(() -> gui.order.phone, val -> gui.order.phone = val), new TranslatableComponent("minetogether.info.phone"))
                .setEnabled(() -> !loginMode)
                .constrain(TOP, relative(zipCode.get(BOTTOM), 4));

        GuiButton login = MTStyle.Flat.button(this, new TranslatableComponent("minetogether:gui.button.login"))
                .setEnabled(() -> loginMode)
                .onPress(this::doLogin)
                .setDisabled(() -> gui.order.password.isEmpty() || loggingIn || loggedIn)
                .constrain(TOP, relative(email.get(BOTTOM), 4))
                .constrain(LEFT, centerRight)
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(12));

        GuiText loginInfo = new GuiText(this, new TextComponent(""))
                .setEnabled(() -> loginMode)
                .setWrap(true)
                .setTextSupplier(() -> {
                    if (loggingIn) {
                        return new TranslatableComponent("minetogether:gui.order.logging_in");
                    } else if (loggedIn) {
                        return new TranslatableComponent("minetogether:gui.order.login_success");
                    } else if (!loggingInError.isEmpty()) {
                        return new TranslatableComponent("minetogether:gui.order.login_error", loggingInError);
                    }
                    return new TranslatableComponent("minetogether:gui.order.account_exists");
                })
                .setAlignment(Align.CENTER)
                .constrain(TOP, relative(login.get(BOTTOM), 4))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .autoHeight();

        constrain(BOTTOM, dynamic(() -> loginMode ? loginInfo.yMax() : country.yMax()));
//        Constraints.bind(new GuiRectangle(this).border(0xFF0000FF), this);
    }

    private GuiElement<?> emailBox(Constraint left, Constraint right) {
        GuiElement<?> background = MTStyle.Flat.contentArea(this)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiElement<?> highlight = new GuiRectangle(background)
                .border(0x50FFFFFF)
                .fill(0x30FFFFFF);
        Constraints.bind(highlight, background);

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(new TranslatableComponent("minetogether.info.e_mail"))
                .setTextState(TextState.create(() -> gui.order.emailAddress, s -> {
                    gui.order.emailAddress = s;
                    gui.emailDirty();
                }));
        textField.setSuggestionColour(() -> 0xFFFFFF);
        highlight.setEnabled(textField::isFocused);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }

    private GuiElement<?> passwordBox(Constraint left, Constraint right, TextState textState, Component suggestion) {
        GuiElement<?> background = MTStyle.Flat.contentArea(this)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiElement<?> highlight = new GuiRectangle(background)
                .border(0x50FFFFFF)
                .fill(0x30FFFFFF);
        Constraints.bind(highlight, background);

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(suggestion)
                .setFormatter((s, integer) -> new TextComponent(StringUtils.repeat('*', s.length())).getVisualOrderText())
                .setTextState(textState);
        textField.setSuggestionColour(() -> 0xFFFFFF);
        highlight.setEnabled(textField::isFocused);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }

    private GuiElement<?> detailsBox(Constraint left, Constraint right, TextState textState, Component suggestion) {
        GuiElement<?> background = MTStyle.Flat.contentArea(this)
                .constrain(LEFT, left)
                .constrain(RIGHT, right)
                .constrain(HEIGHT, literal(12));

        GuiElement<?> highlight = new GuiRectangle(background)
                .border(0x50FFFFFF)
                .fill(0x30FFFFFF);
        Constraints.bind(highlight, background);

        GuiTextField textField = new GuiTextField(background)
                .setSuggestion(suggestion)
                .setTextState(textState);
        textField.setSuggestionColour(() -> 0xFFFFFF);
        highlight.setEnabled(textField::isFocused);

        Constraints.bind(textField, background, 0, 2, 0, 2);
        return background;
    }


    private Map<String, OrderGui.Country> getCountries() {
        Map<String, OrderGui.Country> map = new LinkedHashMap<>();
        Countries.COUNTRIES.forEach((key, name) -> map.put(key, new OrderGui.Country(key, name)));
        return map;
    }

    public void doLogin() {
        loggingIn = true;
        CompletableFuture.runAsync(() -> {
            var result = OrderRequests.doLogin(gui.order.emailAddress, gui.order.password);
            if (result.getStatus().equals("success")) {
                gui.order.currency = result.currency != null ? result.currency : "1";
                gui.order.clientID = result.userid != null ? result.userid : "98874"; // random test account fallback
                loggingIn = false;
                loggedIn = true;
                loggingInError = "";
                gui.summaryDirty();
            } else {
                loggingIn = false;
                loggedIn = false;
                loggingInError = result.getMessage();
            }
        }, OrderGui.EXECUTOR);
    }
}
