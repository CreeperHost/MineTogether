package net.creeperhost.minetogether.module.serverorder.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogetherCommon;
import net.creeperhost.minetogether.lib.Order;
import net.creeperhost.minetogether.lib.serverorder.DefferedValidation;
import net.creeperhost.minetogether.lib.serverorder.IOrderValidation;
import net.creeperhost.minetogether.lib.serverorder.RegexValidator;
import net.creeperhost.minetogether.lib.serverorder.ServerOrderCallbacks;
import net.creeperhost.minetogether.module.serverorder.screen.listentries.ListEntryCountry;
import net.creeperhost.minetogether.module.serverorder.widget.TextFieldDetails;
import net.creeperhost.polylib.client.screen.widget.ScreenList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalDetailsScreen extends OrderServerScreen
{
    public List<TextFieldDetails> fields = null;
    public TextFieldDetails focusedField;
    public boolean isEmailValid = false;
    private boolean loginMode;
    private Button loginButton;
    private boolean loggingIn;
    private String loggingInError = "";
    private boolean loggedIn;
    private boolean isSure;
    private boolean orderPressed;
    private Component info2 = null;
    private String prevLoginString;
    private boolean prevLoginVisible;
    private boolean prevLoginEnabled;
    private boolean renderList = false;
    private boolean first = false;
    private ScreenList list;
    private Button selectCountry;
    private Button buttonList;
    private EditBox searchEntry;

    public PersonalDetailsScreen(int stepId, Order order)
    {
        super(stepId, order);
        order.clientID = "";
    }

    @Override
    public String getStepName()
    {
        return I18n.get("minetogether.screen.personal_details");
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void init()
    {
        clearWidgets();
        super.init();

        addWidget(this.list = new ScreenList(this, this.minecraft, this.width, this.height, 56, this.height - 36, 36));
        addWidget(this.searchEntry = new EditBox(this.font, this.width / 2 - 80, this.height - 32, 160, 20, Component.empty()));

        updateList();

        this.loginButton = addRenderableWidget(new Button(this.width / 2 - 40, (this.height / 2) - 10, 80, 20, Component.translatable("minetogether.button.login"), p ->
        {
            if (orderPressed && !isSure)
            {
                isSure = true;
                buttonNext.onPress();
                return;
            }
            loggingIn = true;
            loginButton.active = false;
            loginButton.setMessage(Component.translatable("minetogether.button.logging"));

            CompletableFuture.runAsync(() ->
            {
                String result = ServerOrderCallbacks.doLogin(order.emailAddress, order.password);
                String[] resultSplit = result.split(":");
                if (resultSplit[0].equals("success"))
                {
                    order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                    order.clientID = resultSplit[2] != null ? resultSplit[2] : "98874"; // random test account fallback
                    loggingIn = false;
                    loggedIn = true;
                    loggingInError = "";
                    loginButton.setMessage(Component.translatable("minetogether.button.done"));
                }
                else
                {
                    loggingIn = false;
                    loggedIn = false;
                    loggingInError = result;
                    loginButton.active = true;
                    loginButton.setMessage(Component.translatable("minetogether.button.logintryagain"));
                }
            });
            return;
        }));

        loginButton.visible = loginMode;

        if (orderPressed && !isSure)
        {
            loginButton.setMessage(Component.translatable("button.order"));
            loginButton.active = true;
            loginButton.visible = true;
            buttonNext.visible = false;
        }
        else if (loggingIn)
        {
            loginButton.setMessage(Component.translatable("button.logging"));
            loginButton.active = false;
        }
        else if (loggedIn)
        {
            loginButton.setMessage(Component.translatable("button.done"));
            loginButton.active = false;
        }
        else if (!loggingInError.isEmpty())
        {
            loginButton.setMessage(Component.translatable("button.logintryagain"));
        }

        fields = new ArrayList<TextFieldDetails>();

        int x = this.width / 2;

        int fieldWidths = 185;

        ArrayList<IOrderValidation> defaultValidators = new ArrayList<IOrderValidation>();
        defaultValidators.add(new IOrderValidation()
        {
            @Override
            public boolean validationCheckAtPhase(ValidationPhase phase)
            {
                return phase.equals(IOrderValidation.ValidationPhase.CHANGED);
            }

            @Override
            public boolean isValid(String string)
            {
                return !string.isEmpty();
            }

            @Override
            public boolean isAsync()
            {
                return false;
            }

            @Override
            public String getValidationMessage()
            {
                return "Cannot be blank";
            }

            @Override
            public String getName()
            {
                return "NotBlankValidator";
            }
        });

        ArrayList<IOrderValidation> emailValidators = new ArrayList<IOrderValidation>(defaultValidators);

        emailValidators.add(new RegexValidator("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", "Invalid email address"));

        emailValidators.add(new DefferedValidation()
        {
            @Override
            public boolean validationCheckAtPhase(ValidationPhase phase)
            {
                return phase.equals(ValidationPhase.FOCUSLOST);
            }

            @Override
            public boolean isValidReal(String string)
            {
                return ServerOrderCallbacks.doesEmailExist(string);
            }

            public String getMessageReal()
            {
                return "Email already exists";
            }

            @Override
            public String getName()
            {
                return "NotEmailExistsValidator";
            }
        });

        this.fields.add(new TextFieldDetails(this, 0, I18n.get("minetogether.info.e_mail"), this.order.emailAddress, x - 205, 45, fieldWidths, 20, emailValidators));

        // Validation done, I guess - the website itself doesn't do any password strength validation etc so I won't force it here

        this.fields.add(new TextFieldDetails(this, 1, I18n.get("minetogether.info.password"), this.order.password, x + 5, 45, fieldWidths, 20, defaultValidators, "*"));

        this.fields.add(new TextFieldDetails(this, 2, I18n.get("minetogether.info.first_name"), this.order.firstName, x - 205, 75, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 3, I18n.get("minetogether.info.last_name"), this.order.lastName, x + 5, 75, fieldWidths, 20, defaultValidators));

        this.fields.add(new TextFieldDetails(this, 4, I18n.get("minetogether.info.address"), this.order.address, x - 205, 105, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 5, I18n.get("minetogether.info.city"), this.order.city, x + 5, 105, fieldWidths, 20, defaultValidators));

        this.fields.add(new TextFieldDetails(this, 6, I18n.get("minetogether.info.zip"), this.order.zip, x - 205, 135, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 7, I18n.get("minetogether.info.state"), this.order.state, x + 5, 135, fieldWidths, 20, defaultValidators));

        String buttonName = ServerOrderCallbacks.getCountries().get(this.order.country);
        if (buttonName == null || buttonName.isEmpty()) buttonName = "Invalid";

        addRenderableWidget(buttonList = new Button(x - 205, 165, fieldWidths, 20, Component.translatable(buttonName), p -> renderList = true));

        addRenderableWidget(selectCountry = new Button(this.width - 90, this.height - 30, 80, 20, Component.translatable("minetogether.button.select"), (button) ->
        {
            renderList = false;
            ListEntryCountry listEntryCountry = (ListEntryCountry) list.getCurrSelected();
            order.country = listEntryCountry.countryID;
            buttonList.setMessage(Component.translatable(ServerOrderCallbacks.getCountries().get(listEntryCountry.countryID)));
        }));

        this.fields.add(new TextFieldDetails(this, 9, I18n.get("minetogether.info.phone"), this.order.phone, x + 5, 165, fieldWidths, 20, defaultValidators));

        String info2Text = I18n.get("minetogether.order.info2");

        final String regex = "\\((.*?)\\|(.*?)\\)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(info2Text);

        int lastEnd = 0;

        Component component = null;

        while (matcher.find())
        {
            int start = matcher.start();
            int end = matcher.end();

            String part = info2Text.substring(lastEnd, start);
            if (part.length() > 0)
            {
                if (component == null) component = Component.translatable(part);
                else component.copy().append(Component.translatable(part));
            }

            lastEnd = end;
            Component link = Component.translatable(matcher.group(1));
            Style style = link.getStyle();
            style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE));
            style.withUnderlined(true);
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("order.url")));

            if (component == null) component = link;
            else component.copy().append(Component.translatable(link.getString()));
        }
        info2 = component;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void tick()
    {
        super.tick();

        this.selectCountry.active = renderList;
        this.selectCountry.visible = renderList;
        this.buttonCancel.active = !renderList;
        this.buttonCancel.visible = !renderList;
        this.buttonPrev.active = !renderList;
        this.buttonPrev.visible = !renderList;
        this.buttonList.active = !renderList && !loginMode;
        this.buttonList.visible = !renderList && !loginMode;
        this.buttonNext.active = !renderList;
        this.buttonNext.visible = !renderList;

        this.buttonNext.active = true;
        this.loginButton.visible = loginMode || (orderPressed && !isSure);


        if (loggedIn)
        {
            this.minecraft.setScreen(getByStep(this.stepId + 1, this.order, null));
        }

        for (TextFieldDetails field : this.fields)
        {
            field.checkPendingValidations();
            field.tick();

            if (!field.isValidated)
            {
                this.buttonNext.active = false;
            }
        }

        this.buttonNext.active = this.buttonNext.active && isEmailValid;

        this.buttonNext.active = this.loggedIn || this.buttonNext.active;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (renderList)
        {
            list.render(matrixStack, mouseX, mouseY, partialTicks);
            searchEntry.render(matrixStack, mouseX, mouseY, partialTicks);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        else
        {
            if ((!orderPressed || !isSure) && !loginMode)
            {
                drawCenteredString(matrixStack, font, "No data will be sent until you complete the order.", this.width / 2, this.height - 45, 0xFFFFFF);
            }

            if (!orderPressed || isSure)
            {
                for (TextFieldDetails field : this.fields)
                {
                    if (loginMode)
                    {
                        if (field.getId() < 2)
                        {
                            field.render(matrixStack, mouseX, mouseY, partialTicks);
                        }
                    }
                    else
                    {
                        field.render(matrixStack, mouseX, mouseY, partialTicks);
                    }
                }

                if (loginMode)
                {
                    if (loggingIn)
                    {
                        drawCenteredString(matrixStack, font, I18n.get("minetogether.details.login"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                    }
                    else if (!loggingInError.isEmpty())
                    {
                        drawCenteredString(matrixStack, font, I18n.get("minetogether.details.loginerror") + loggingInError, this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                    }
                    else if (loggedIn)
                    {
                        drawCenteredString(matrixStack, font, I18n.get("minetogether.details.loginsuccess"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                    }
                    else
                    {
                        drawCenteredString(matrixStack, font, I18n.get("minetogether.details.accountexists"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                    }
                }
            }
            else
            {
                int info2Start = (this.height / 2) - 50;

                drawCenteredString(matrixStack, font, I18n.get("order.info1"), this.width / 2, (this.height / 2) - 60, 0xFFFFFF);
                drawCenteredString(matrixStack, font, info2.getString(), this.width / 2, (this.height / 2) - 50, 0xFFFFFF);
                drawCenteredString(matrixStack, font, I18n.get("order.info3"), this.width / 2, (this.height / 2) - 30, 0xFFFFFF);
                drawCenteredString(matrixStack, font, I18n.get("order.info4"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);

                if (mouseY >= info2Start && mouseY <= info2Start + font.lineHeight)
                {
                    Component component = getComponent(mouseX, mouseY);

                    if (component != null)
                    {
                        HoverEvent event = component.getStyle().getHoverEvent();
                        if (event != null)
                        {
                            if (event.getAction() == HoverEvent.Action.SHOW_TEXT)
                            {
                                this.renderTooltip(matrixStack, Component.translatable(event.toString()), mouseX, mouseY);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        if (searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(typedChar, keyCode);
            updateList();
            return flag;
        }
        TextFieldDetails field = this.focusedField;

        if (focusedField == null) return false;

        if (field.charTyped(typedChar, keyCode))
        {
            int id = field.getId();
            String text = field.getValue().trim();

            switch (id)
            {
                case 0:
                    this.order.emailAddress = text;
                    break;
                case 1:
                    this.order.password = text;
                    break;
                case 2:
                    this.order.firstName = text;
                    break;
                case 3:
                    this.order.lastName = text;
                    break;
                case 4:
                    this.order.address = text;
                    break;
                case 5:
                    this.order.city = text;
                    break;
                case 6:
                    this.order.zip = text;
                    break;
                case 7:
                    this.order.state = text;
                    break;
                case 8:
                    this.order.country = text;
                    break;
                case 9:
                    this.order.phone = text;
                    break;
            }
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (searchEntry.isFocused())
        {
            boolean flag = searchEntry.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
            updateList();
            return flag;
        }
        if (focusedField != null)
        {
            if (focusedField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            {
                int id = focusedField.getId();
                String text = focusedField.getValue().trim();

                switch (id)
                {
                    case 0:
                        this.order.emailAddress = text;
                        break;
                    case 1:
                        this.order.password = text;
                        break;
                    case 2:
                        this.order.firstName = text;
                        break;
                    case 3:
                        this.order.lastName = text;
                        break;
                    case 4:
                        this.order.address = text;
                        break;
                    case 5:
                        this.order.city = text;
                        break;
                    case 6:
                        this.order.zip = text;
                        break;
                    case 7:
                        this.order.state = text;
                        break;
                    case 8:
                        this.order.country = text;
                        break;
                    case 9:
                        this.order.phone = text;
                        break;
                }
                return true;
            }
            else if (p_keyPressed_1_ == 258)
            {
                TextFieldDetails field = this.focusedField;
                int adjustAm = 1;

                int fieldsSize = fields.size();

                field.setFocused(false);

                int newField = (field.getId() + adjustAm) % fieldsSize;
                if (newField == -1) newField = fieldsSize - 1;

                TextFieldDetails newF = null;
                while (newF == null)
                {
                    TextFieldDetails tempField = fields.get(newField);
                    if (tempField.canBeFocused())
                    {
                        newF = tempField;
                    }
                    else
                    {
                        newField = (newField + adjustAm) % fieldsSize;
                        if (newField == -1) newField = fieldsSize - 1;
                    }
                }

                newF.setFocused(true);
            }
            return true;
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(buttonList != null)
        {
            if (buttonList.isMouseOver(mouseX, mouseY))
            {
                return buttonList.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int info2Start = (this.height / 2) - 50;

        if (orderPressed && !isSure && mouseY >= info2Start && mouseY <= info2Start + font.lineHeight)
        {
            Component comp = getComponent(mouseX, mouseY);

            ClickEvent clickevent = comp.getStyle().getClickEvent();
            if (clickevent != null && clickevent.getAction() == ClickEvent.Action.OPEN_URL)
            {
                try
                {
                    URI uri = new URI(clickevent.getValue());
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop").invoke(null);
                    oclass.getMethod("browse", URI.class).invoke(object, uri);
                    return true;
                } catch (Throwable t)
                {
                    MineTogetherCommon.logger.error("Can\'t open url for " + clickevent, t);
                }
                return false;
            }
        }

        for (TextFieldDetails field : this.fields)
        {
            if (field.mouseClicked(mouseX, mouseY, mouseButton))
            {
                if (focusedField != null) focusedField.setFocused(false);
                focusedField = field;
                field.setFocused(true);
                return true;
            }
        }
        return false;
    }

    private Component getComponent(double mouseX, double mouseY)
    {
        int stringWidth = font.width(info2.getString());

        int prevWidth = (width / 2) - (stringWidth / 2);

        for (Component inner : info2.getSiblings())
        {
            StringBuilder stringbuilder = new StringBuilder();
            String s = inner.getString();

            if (!s.isEmpty())
            {
                stringbuilder.append(inner.getStyle());
                stringbuilder.append(s);
                stringbuilder.append(ChatFormatting.RESET);
            }
            int width = font.width(stringbuilder.toString());
            if (mouseX >= prevWidth && mouseX <= prevWidth + width)
            {
                return inner;
            }
            prevWidth += width;
        }
        return null;
    }

    public void validationChanged(TextFieldDetails details, boolean valid, IOrderValidation validator, IOrderValidation.ValidationPhase phase)
    {
        if (details.getId() == 0)
        {
            if (!valid && validator.getName().equals("NotEmailExistsValidator") && !validator.isAsync())
            {
                isEmailValid = false;
                loginMode = true;
            }
            else
            {
                loginMode = false;
                if (phase.equals(IOrderValidation.ValidationPhase.FOCUSLOST))
                {
                    isEmailValid = true;
                }
                else
                {
                    isEmailValid = false;
                }
            }
        }
    }

    public void validationChangedDeferred(TextFieldDetails textFieldDetails, DefferedValidation pendingValidation)
    {
        if (!pendingValidation.isValid(""))
        {
            validationChanged(textFieldDetails, false, pendingValidation, pendingValidation.getPhase());
        }
        else
        {
            validationChanged(textFieldDetails, true, null, pendingValidation.getPhase());
        }
    }

    @SuppressWarnings("unchecked")
    public void updateList()
    {
        first = false;
        list.clearList();
        Map<String, String> locations = ServerOrderCallbacks.getCountries();
        for (Map.Entry<String, String> entry : locations.entrySet())
        {
            if (searchEntry.getValue().isEmpty() || entry.getValue().toLowerCase().contains(searchEntry.getValue().toLowerCase()))
            {
                ListEntryCountry listEntry = new ListEntryCountry(list, entry.getKey(), entry.getValue());
                list.add(listEntry);

                if (order.country.equals(listEntry.countryID))
                {
                    list.setSelected(listEntry);
                }
            }
        }
    }
}
