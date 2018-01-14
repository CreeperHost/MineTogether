package net.creeperhost.minetogether.gui;

import com.google.common.base.Splitter;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.common.IOrderValidation;
import net.creeperhost.minetogether.common.RegexValidator;
import net.creeperhost.minetogether.gui.element.TextFieldDetails;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiPersonalDetails extends GuiGetServer
{

    public List<TextFieldDetails> fields = null;
    public TextFieldDetails focusedField;
    public boolean isEmailValid = false;
    private boolean loginMode;
    private GuiButton loginButton;
    private boolean loggingIn;
    private String loggingInError = "";
    private boolean loggedIn;
    private boolean isSure;
    private boolean orderPressed;
    private ITextComponent info2 = null;
    private String prevLoginString;
    private boolean prevLoginVisible;
    private boolean prevLoginEnabled;

    public GuiPersonalDetails(int stepId, Order order)
    {
        super(stepId, order);
        order.clientID = "";
    }

    @Override
    public String getStepName()
    {
        return Util.localize("gui.personal_details");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.loginButton = new GuiButton(80085, this.width / 2 - 40, (this.height / 2) - 10, 80, 20, Util.localize("button.login"));
        loginButton.visible = loginMode;

        if (orderPressed && !isSure)
        {
            loginButton.displayString = Util.localize("button.order");
            loginButton.enabled = true;
            loginButton.visible = true;
            buttonNext.visible = false;
        }
        else if (loggingIn)
        {
            loginButton.displayString = Util.localize("button.logging");
            loginButton.enabled = false;
        }
        else if (loggedIn)
        {
            loginButton.displayString = Util.localize("button.done");
            loginButton.enabled = false;
        }
        else if (!loggingInError.isEmpty())
        {
            loginButton.displayString = Util.localize("button.logintryagain");
        }
        this.buttonList.add(loginButton);

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
                return Callbacks.doesEmailExist(string);
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

        this.fields.add(new TextFieldDetails(this, 0, Util.localize("info.e_mail"), this.order.emailAddress, x - 205, 55, fieldWidths, 20, emailValidators));

        // Validation done, I guess - the website itself doesn't do any password strength validation etc so I won't force it here

        this.fields.add(new TextFieldDetails(this, 1, Util.localize("info.password"), this.order.password, x + 5, 55, fieldWidths, 20, defaultValidators, "*"));

        this.fields.add(new TextFieldDetails(this, 2, Util.localize("info.first_name"), this.order.firstName, x - 205, 85, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 3, Util.localize("info.last_name"), this.order.lastName, x + 5, 85, fieldWidths, 20, defaultValidators));

        this.fields.add(new TextFieldDetails(this, 4, Util.localize("info.address"), this.order.address, x - 205, 115, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 5, Util.localize("info.city"), this.order.city, x + 5, 115, fieldWidths, 20, defaultValidators));

        this.fields.add(new TextFieldDetails(this, 6, Util.localize("info.zip"), this.order.zip, x - 205, 145, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 7, Util.localize("info.state"), this.order.state, x + 5, 145, fieldWidths, 20, defaultValidators));

        TextFieldDetails countryField = new TextFieldDetails(this, 8, Util.localize("info.country"), Callbacks.getCountries().get(this.order.country), x - 205, 175, fieldWidths, 20, defaultValidators, false);
        this.fields.add(countryField);

        this.fields.add(new TextFieldDetails(this, 9, Util.localize("info.phone"), this.order.phone, x + 5, 175, fieldWidths, 20, defaultValidators));

        String info2Text = Util.localize("order.info2");

        final String regex = "\\((.*?)\\|(.*?)\\)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(info2Text);

        int lastEnd = 0;

        ITextComponent component = null;

        while (matcher.find())
        {

            int start = matcher.start();
            int end = matcher.end();

            String part = info2Text.substring(lastEnd, start);
            if (part.length() > 0)
            {
                if (component == null)
                    component = new TextComponentString(part);
                else
                    component.appendText(part);
            }

            lastEnd = end;
            ITextComponent link = new TextComponentString(matcher.group(1));
            Style style = link.getStyle();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.setColor(TextFormatting.BLUE);
            style.setUnderlined(true);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(Util.localize("order.url"))));

            if (component == null)
                component = link;
            else
                component.appendSibling(link);
        }

        info2 = component;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        this.buttonNext.enabled = true;
        this.loginButton.visible = loginMode || (orderPressed && !isSure);

        for (TextFieldDetails field : this.fields)
        {
            field.checkPendingValidations();
            field.updateCursorCounter();

            if (!field.isValidated)
            {
                this.buttonNext.enabled = false;
            }
        }

        this.buttonNext.enabled = this.buttonNext.enabled && isEmailValid;

        this.buttonNext.enabled = this.loggedIn || this.buttonNext.enabled;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        TextFieldDetails field = this.focusedField;

        if (focusedField == null)
            return;
        if (keyCode == 15)
        {
            int adjustAm = 1;
            if (isShiftKeyDown())
            {
                adjustAm = -1;
            }

            int fieldsSize = fields.size();

            field.setFocused(false);

            int newField = (field.getId() + adjustAm) % fieldsSize;
            if (newField == -1)
                newField = fieldsSize - 1;

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
                    if (newField == -1)
                        newField = fieldsSize - 1;
                }
            }

            newF.setFocused(true);

            return;
        }
        else if (field.textboxKeyTyped(typedChar, keyCode))
        {
            int id = field.getId();
            String text = field.getText().trim();

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

            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (!orderPressed || isSure)
        {
            for (TextFieldDetails field : this.fields)
            {
                if (loginMode)
                {
                    if (field.getId() < 2)
                    {
                        field.drawTextBox();
                    }
                }
                else
                {
                    field.drawTextBox();
                }
            }

            if (loginMode)
            {
                if (loggingIn)
                {
                    this.drawCenteredString(fontRendererObj, Util.localize("details.login"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                }
                else if (!loggingInError.isEmpty())
                {
                    this.drawCenteredString(fontRendererObj, Util.localize("details.loginerror") + loggingInError, this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                }
                else if (loggedIn)
                {
                    this.drawCenteredString(fontRendererObj, Util.localize("details.loginsuccess"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                }
                else
                {
                    this.drawCenteredString(fontRendererObj, Util.localize("details.accountexists"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                }
            }
        }
        else
        {
            int info2Start = (this.height / 2) - 50;

            this.drawCenteredString(fontRendererObj, Util.localize("order.info1"), this.width / 2, (this.height / 2) - 60, 0xFFFFFF);
            this.drawCenteredString(fontRendererObj, info2.getFormattedText(), this.width / 2, (this.height / 2) - 50, 0xFFFFFF);
            this.drawCenteredString(fontRendererObj, Util.localize("order.info3"), this.width / 2, (this.height / 2) - 30, 0xFFFFFF);
            this.drawCenteredString(fontRendererObj, Util.localize("order.info4"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);

            if (mouseY >= info2Start && mouseY <= info2Start + fontRendererObj.FONT_HEIGHT)
            {
                ITextComponent component = getComponent(mouseX, mouseY);

                if (component != null)
                {
                    HoverEvent event = component.getStyle().getHoverEvent();
                    if (event != null)
                        if (event.getAction() == HoverEvent.Action.SHOW_TEXT)
                            this.drawHoveringText(Splitter.on("\n").splitToList(event.getValue().getFormattedText()), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int info2Start = (this.height / 2) - 50;

        if (orderPressed && !isSure && mouseY >= info2Start && mouseY <= info2Start + fontRendererObj.FONT_HEIGHT)
        {
            ITextComponent comp = getComponent(mouseX, mouseY);

            ClickEvent clickevent = comp.getStyle().getClickEvent();
            if (clickevent != null && clickevent.getAction() == ClickEvent.Action.OPEN_URL)
            {
                try
                {
                    URI uri = new URI(clickevent.getValue());
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop").invoke(null);
                    oclass.getMethod("browse", URI.class).invoke(object, uri);
                }
                catch (Throwable t)
                {
                    CreeperHost.logger.error("Can\'t open url for " + clickevent, t);
                }
                return;
            }
        }

        for (TextFieldDetails field : this.fields)
        {
            field.myMouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private ITextComponent getComponent(int mouseX, int mouseY)
    {
        int stringWidth = fontRendererObj.getStringWidth(info2.getFormattedText());
        int begin = (width / 2) - (stringWidth / 2);

        if (info2 instanceof TextComponentBase)
        {
            TextComponentBase comp = (TextComponentBase) info2;

            int prevWidth = begin;

            for (ITextComponent inner : comp)
            {
                StringBuilder stringbuilder = new StringBuilder();
                String s = inner.getUnformattedComponentText();

                if (!s.isEmpty())
                {
                    stringbuilder.append(inner.getStyle().getFormattingCode());
                    stringbuilder.append(s);
                    stringbuilder.append(TextFormatting.RESET);
                }
                int width = fontRendererObj.getStringWidth(stringbuilder.toString());
                if (mouseX >= prevWidth && mouseX <= prevWidth + width)
                {
                    return inner;
                }
                prevWidth += width;
            }
        }
        return null;
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException
    {
        if (button.id == 80085)
        {
            if (orderPressed && !isSure)
            {
                isSure = true;
                actionPerformed(buttonNext);
                return;
            }
            loggingIn = true;
            button.enabled = false;
            button.displayString = Util.localize("button.logging");
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    String result = Callbacks.doLogin(order.emailAddress, order.password);
                    String[] resultSplit = result.split(":");
                    if (resultSplit[0].equals("success"))
                    {
                        order.currency = resultSplit[1] != null ? resultSplit[1] : "1";
                        order.clientID = resultSplit[2] != null ? resultSplit[2] : "98874"; // random test account fallback
                        loggingIn = false;
                        loggedIn = true;
                        loggingInError = "";
                        button.displayString = Util.localize("button.done");
                    }
                    else
                    {
                        loggingIn = false;
                        loggedIn = false;
                        loggingInError = result;
                        button.enabled = true;
                        button.displayString = Util.localize("button.logintryagain");
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            return;
        }
        else if (button.id == buttonNext.id && !isSure)
        {
            orderPressed = true;
            buttonNext.visible = false;
            prevLoginString = loginButton.displayString;
            loginButton.displayString = Util.localize("button.order");
            prevLoginVisible = loginButton.visible;
            loginButton.visible = true;
            prevLoginEnabled = loginButton.enabled;
            loginButton.enabled = true;
            return;
        }
        else if (button.id == buttonNext.id && !isSure)
        {
            orderPressed = true;
            buttonNext.visible = false;
            prevLoginString = loginButton.displayString;
            loginButton.displayString = Util.localize("button.order");
            prevLoginVisible = loginButton.visible;
            loginButton.visible = true;
            prevLoginEnabled = loginButton.enabled;
            loginButton.enabled = true;
            return;
        }
        else if (button.id == buttonPrev.id && orderPressed)
        {
            orderPressed = false;
            buttonNext.visible = true;
            loginButton.displayString = prevLoginString;
            loginButton.visible = prevLoginVisible;
            loginButton.enabled = prevLoginEnabled;
            return;
        }
        super.actionPerformed(button);
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
        textFieldDetails.setEnabled(true);
        if (!pendingValidation.isValid(""))
        {
            validationChanged(textFieldDetails, false, pendingValidation, pendingValidation.getPhase());
        }
        else
        {
            validationChanged(textFieldDetails, true, null, pendingValidation.getPhase());
        }
    }
}
