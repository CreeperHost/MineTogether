package net.creeperhost.minetogether.client.screen.order;

import com.google.common.base.Splitter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.DefferedValidation;
import net.creeperhost.minetogether.client.screen.element.TextFieldDetails;
import net.creeperhost.minetogether.common.IOrderValidation;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.RegexValidator;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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
    private Button loginButton;
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
    
    @SuppressWarnings("Duplicates")
    @Override
    public void init()
    {
        super.init();
        
        this.loginButton = addButton(new Button(this.width / 2 - 40, (this.height / 2) - 10, 80, 20, new StringTextComponent(Util.localize("button.login")), p ->
        {
            if (orderPressed && !isSure)
            {
                isSure = true;
                buttonNext.onPress();
                return;
            }
            loggingIn = true;
            loginButton.active = false;
            loginButton.setMessage(new StringTextComponent(Util.localize("button.logging")));
            Runnable runnable = () ->
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
                    loginButton.setMessage(new StringTextComponent(Util.localize("button.done")));
                } else
                {
                    loggingIn = false;
                    loggedIn = false;
                    loggingInError = result;
                    loginButton.active = true;
                    loginButton.setMessage(new StringTextComponent(Util.localize("button.logintryagain")));
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            return;
            
        }));
        
        loginButton.visible = loginMode;
        
        if (orderPressed && !isSure)
        {
            loginButton.setMessage(new StringTextComponent(Util.localize("button.order")));
            loginButton.active = true;
            loginButton.visible = true;
            buttonNext.visible = false;
        } else if (loggingIn)
        {
            loginButton.setMessage(new StringTextComponent(Util.localize("button.logging")));
            loginButton.active = false;
        } else if (loggedIn)
        {
            loginButton.setMessage(new StringTextComponent(Util.localize("button.done")));
            loginButton.active = false;
        } else if (!loggingInError.isEmpty())
        {
            loginButton.setMessage(new StringTextComponent(Util.localize("button.logintryagain")));
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
        
        this.fields.add(new TextFieldDetails(this, 0, Util.localize("info.e_mail"), this.order.emailAddress, x - 205, 45, fieldWidths, 20, emailValidators));
        
        // Validation done, I guess - the website itself doesn't do any password strength validation etc so I won't force it here
        
        this.fields.add(new TextFieldDetails(this, 1, Util.localize("info.password"), this.order.password, x + 5, 45, fieldWidths, 20, defaultValidators, "*"));
        
        this.fields.add(new TextFieldDetails(this, 2, Util.localize("info.first_name"), this.order.firstName, x - 205, 75, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 3, Util.localize("info.last_name"), this.order.lastName, x + 5, 75, fieldWidths, 20, defaultValidators));
        
        this.fields.add(new TextFieldDetails(this, 4, Util.localize("info.address"), this.order.address, x - 205, 105, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 5, Util.localize("info.city"), this.order.city, x + 5, 105, fieldWidths, 20, defaultValidators));
        
        this.fields.add(new TextFieldDetails(this, 6, Util.localize("info.zip"), this.order.zip, x - 205, 135, fieldWidths, 20, defaultValidators));
        this.fields.add(new TextFieldDetails(this, 7, Util.localize("info.state"), this.order.state, x + 5, 135, fieldWidths, 20, defaultValidators));
        
        TextFieldDetails countryField = new TextFieldDetails(this, 8, Util.localize("info.country"), Callbacks.getCountries().get(this.order.country), x - 205, 165, fieldWidths, 20, defaultValidators, false);
        this.fields.add(countryField);
        
        this.fields.add(new TextFieldDetails(this, 9, Util.localize("info.phone"), this.order.phone, x + 5, 165, fieldWidths, 20, defaultValidators));
        
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
                    component = new StringTextComponent(part);
                else
                    component.deepCopy().append(new StringTextComponent(part));
            }
            
            lastEnd = end;
            ITextComponent link = new StringTextComponent(matcher.group(1));
            Style style = link.getStyle();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)));
            style.setColor(Color.fromTextFormatting(TextFormatting.BLUE));
            style.setUnderlined(true);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(Util.localize("order.url"))));
            
            if (component == null)
                component = link;
            else
                component.deepCopy().append(new StringTextComponent(link.getString()));
        }
        info2 = component;
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void tick()
    {
        super.tick();
        
        this.buttonNext.active = true;
        this.loginButton.visible = loginMode || (orderPressed && !isSure);
        
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
    
    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        TextFieldDetails field = this.focusedField;
        
        if (focusedField == null)
            return false;
        
        if (field.charTyped(typedChar, keyCode))
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
        }
        return super.charTyped(typedChar, keyCode);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (focusedField != null)
        {
            if (focusedField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            {
                int id = focusedField.getId();
                String text = focusedField.getText().trim();
                
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
            } else if (p_keyPressed_1_ == 258) {
                TextFieldDetails field = this.focusedField;
                int adjustAm = 1;

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
                    } else
                    {
                        newField = (newField + adjustAm) % fieldsSize;
                        if (newField == -1)
                            newField = fieldsSize - 1;
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        fill(matrixStack, 0, this.height - 20, width, 20, 0x99000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        if ((!orderPressed || !isSure) && !loginMode)
        {
            this.drawCenteredString(matrixStack, font, "No data will be sent until you complete the order.", this.width / 2, this.height - 45, 0xFFFFFF);
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
                } else
                {
                    field.render(matrixStack, mouseX, mouseY, partialTicks);
                }
            }
            
            if (loginMode)
            {
                if (loggingIn)
                {
                    this.drawCenteredString(matrixStack, font, Util.localize("details.login"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                } else if (!loggingInError.isEmpty())
                {
                    this.drawCenteredString(matrixStack, font, Util.localize("details.loginerror") + loggingInError, this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                } else if (loggedIn)
                {
                    this.drawCenteredString(matrixStack, font, Util.localize("details.loginsuccess"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                } else
                {
                    this.drawCenteredString(matrixStack, font, Util.localize("details.accountexists"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
                }
            }
        } else
        {
            int info2Start = (this.height / 2) - 50;
            
            this.drawCenteredString(matrixStack, font, Util.localize("order.info1"), this.width / 2, (this.height / 2) - 60, 0xFFFFFF);
            this.drawCenteredString(matrixStack, font, info2.getString(), this.width / 2, (this.height / 2) - 50, 0xFFFFFF);
            this.drawCenteredString(matrixStack, font, Util.localize("order.info3"), this.width / 2, (this.height / 2) - 30, 0xFFFFFF);
            this.drawCenteredString(matrixStack, font, Util.localize("order.info4"), this.width / 2, (this.height / 2) - 20, 0xFFFFFF);
            
            if (mouseY >= info2Start && mouseY <= info2Start + font.FONT_HEIGHT)
            {
                ITextComponent component = getComponent(mouseX, mouseY);
                
                if (component != null)
                {
                    HoverEvent event = component.getStyle().getHoverEvent();
                    if (event != null)
                    {
                        if (event.getAction() == HoverEvent.Action.SHOW_TEXT)
                        {
                            this.renderTooltip(matrixStack, new StringTextComponent(event.toString()), mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        int info2Start = (this.height / 2) - 50;
        
        if (orderPressed && !isSure && mouseY >= info2Start && mouseY <= info2Start + font.FONT_HEIGHT)
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
                    return true;
                } catch (Throwable t)
                {
                    MineTogether.logger.error("Can\'t open url for " + clickevent, t);
                }
                return false;
            }
        }
        
        for (TextFieldDetails field : this.fields)
        {
            if (field.mouseClicked(mouseX, mouseY, mouseButton))
            {
                field.setFocused(true);
            }
        }
        return false;
    }
    
    private ITextComponent getComponent(double mouseX, double mouseY)
    {
        int stringWidth = font.getStringWidth(info2.getString());
        int begin = (width / 2) - (stringWidth / 2);
        
        if (info2 instanceof StringTextComponent)
        {
            StringTextComponent comp = (StringTextComponent) info2;
            
            int prevWidth = begin;
            
            for (ITextComponent inner : comp.getSiblings())
            {
                StringBuilder stringbuilder = new StringBuilder();
                String s = inner.getUnformattedComponentText();
                
                if (!s.isEmpty())
                {
                    stringbuilder.append(inner.getStyle());
                    stringbuilder.append(s);
                    stringbuilder.append(TextFormatting.RESET);
                }
                int width = font.getStringWidth(stringbuilder.toString());
                if (mouseX >= prevWidth && mouseX <= prevWidth + width)
                {
                    return inner;
                }
                prevWidth += width;
            }
        }
        return null;
    }
    
    public void validationChanged(TextFieldDetails details, boolean valid, IOrderValidation validator, IOrderValidation.ValidationPhase phase)
    {
        if (details.getId() == 0)
        {
            if (!valid && validator.getName().equals("NotEmailExistsValidator") && (!validator.isAsync() || validator == null))
            {
                isEmailValid = false;
                loginMode = true;
            } else
            {
                loginMode = false;
                if (phase.equals(IOrderValidation.ValidationPhase.FOCUSLOST))
                {
                    isEmailValid = true;
                } else
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
        } else
        {
            validationChanged(textFieldDetails, true, null, pendingValidation.getPhase());
        }
    }
}
