package net.creeperhost.minetogether.module.serverorder.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.serverorder.screen.PersonalDetailsScreen;
import net.creeperhost.minetogetherlib.serverorder.DefferedValidation;
import net.creeperhost.minetogetherlib.serverorder.IOrderValidation;
import net.creeperhost.minetogetherlib.serverorder.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class TextFieldDetails extends EditBox
{
    private final PersonalDetailsScreen gui;
    private final String displayString;
    private final boolean canBeFocused;
    private final int ourID;
    public boolean isValidated;
    public String validationError = "";
    private String censorText = "";
    private boolean isChangeValidated = false;
    private final String acceptString = new String(Character.toChars(10004));
    private final String denyString = new String(Character.toChars(10006));
    private final ArrayList<IOrderValidation> validators;
    private boolean doNotValidate = false;
    private DefferedValidation pendingValidation = null;
    
    public TextFieldDetails(PersonalDetailsScreen gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators, boolean canBeFocused)
    {
        super(Minecraft.getInstance().font, x, y, width, height, new TranslatableComponent(""));
        
        this.ourID = id;
        
        this.validators = validators;
        this.gui = gui;
        this.canBeFocused = canBeFocused;
        this.displayString = displayString;
        
        this.setValue(def);
        
        setFocused(true);
        setFocused(false);
        
        this.setMaxLength(64);
    }
    
    public TextFieldDetails(PersonalDetailsScreen gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators, String censorText)
    {
        this(gui, id, displayString, def, x, y, width, height, validators);
        this.censorText = censorText;
    }
    
    public TextFieldDetails(PersonalDetailsScreen gui, int id, String displayString, String def, int x, int y, int width, int height, ArrayList<IOrderValidation> validators)
    {
        this(gui, id, displayString, def, x, y, width, height, validators, true);
    }
    
    @SuppressWarnings("Duplicates")
    public void checkPendingValidations()
    {
        if (pendingValidation != null && pendingValidation.isDone())
        {
            gui.validationChangedDeferred(this, pendingValidation);
            isValidated = pendingValidation.isValid("");
            validationError = pendingValidation.getValidationMessage();
            pendingValidation.reset();
            pendingValidation = null;
        }
    }
    
    public int getId()
    {
        return ourID;
    }
    
    
    @SuppressWarnings("Duplicates")
    @Override
    public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
    {
        if (!this.censorText.isEmpty())
        {
            String text = this.getValue();
            
            double censorLength = censorText.length();
            
            double mainLength = text.length();
            
            double timesRaw = mainLength / censorLength;
            
            int times = (int) Math.ceil(timesRaw);
            
            String obscure = new String(new char[times]).replace("\0", censorText).substring(0, (int) mainLength);
            boolean oldNotValidate = doNotValidate;
            doNotValidate = true;
            this.setValue(obscure);
            super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
            
            this.setValue(text);
            doNotValidate = oldNotValidate;
        } else
        {
            super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
        }
        
        int startX = (this.x + this.width + 3) / 2;
        int startY = (this.y + 4) / 2;
        
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        
        if (isValidated)
        {
            drawString(matrixStack, Minecraft.getInstance().font, acceptString, startX, startY, 0x00FF00);
        } else
        {
            drawString(matrixStack, Minecraft.getInstance().font, denyString, startX, startY, 0xFF0000);
        }
        
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        
        if (!this.isFocused() && this.getValue().trim().isEmpty())
        {
            int x = this.x + 4;
            int y = this.y + (this.height - 8) / 2;
            
            Minecraft.getInstance().font.drawShadow(matrixStack, "\u00A7o" + this.displayString, x, y, 14737632);
        }
    }
    
    public boolean canBeFocused()
    {
        return canBeFocused;
    }
    
    @SuppressWarnings("Duplicates")
    private Pair<Boolean, IOrderValidation> validateAtPhase(IOrderValidation.ValidationPhase phase, String string, boolean ignoreAsync)
    {
        if (pendingValidation != null || doNotValidate)
            return new Pair(false, null);
        boolean validatorsExist = false;
        for (IOrderValidation validator : validators)
        {
            if (!validatorsExist && !isChangeValidated && phase.equals(IOrderValidation.ValidationPhase.FOCUSLOST))
            {
                return new Pair(false, null);
            }
            if (validator.validationCheckAtPhase(phase))
            {
                if (validator.isAsync())
                {
                    if (ignoreAsync)
                    {
                        continue;
                    }
//                    this.setEnabled(false);
                    pendingValidation = (DefferedValidation) validator;
                    pendingValidation.setPhase(phase);
                    pendingValidation.doAsync(string);
                }
                validatorsExist = true;
                if (validator.isValid(string))
                {
                    continue;
                } else
                {
                    gui.validationChanged(this, false, validator, phase);
                    return new Pair(true, validator);
                }
            }
        }
        if (validatorsExist)
        {
            gui.validationChanged(this, true, null, phase);
        }
        return new Pair(validatorsExist, null);
    }
    
    private Pair<Boolean, IOrderValidation> validateAtPhase(IOrderValidation.ValidationPhase phase, String string)
    {
        return validateAtPhase(phase, string, false);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void setFocused(boolean focused)
    {
        if (focused)
        {
            gui.focusedField = this;
            if (!canBeFocused)
                return; // to prevent weirdness, we set focused anyway so that tab works as expected
        } else if (this.isFocused())
        {
            Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.FOCUSLOST, getValue());
            if (validatorPair.getLeft())
            {
                IOrderValidation validator = validatorPair.getRight();
                if (validator != null)
                {
                    validationError = validator.getValidationMessage();
                    isValidated = false;
                } else
                {
                    validationError = "This is fine";
                    isValidated = true;
                }
            }
        }
        super.setFocused(focused);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void insertText(String string)
    {
        super.insertText(string);
        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getValue());
        if (validatorPair.getLeft())
        {
            IOrderValidation validator = validatorPair.getRight();
            if (validator != null)
            {
                validationError = validator.getValidationMessage();
                isValidated = false;
                isChangeValidated = false;
            }
            else
            {
                validationError = "This is fine";
                isValidated = true;
                isChangeValidated = true;
            }
        }
    }

//    @SuppressWarnings("Duplicates")
//    @Override
//    public void deleteFromCursor ( int num)
//    {
//        super.deleteFromCursor(num);
//        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getText());
//        if (validatorPair.getLeft()) {
//            IOrderValidation validator = validatorPair.getRight();
//            if (validator != null) {
//                validationError = validator.getValidationMessage();
//                isValidated = false;
//                isChangeValidated = false;
//            } else {
//                validationError = "This is fine";
//                isValidated = true;
//                isChangeValidated = true;
//            }
//        }
//    }

    @SuppressWarnings("Duplicates")
    public void setValue(String string)
    {
        super.setValue(string);
        Pair<Boolean, IOrderValidation> validatorPair = validateAtPhase(IOrderValidation.ValidationPhase.CHANGED, getValue());
        if (validatorPair.getLeft()) {
            IOrderValidation validator = validatorPair.getRight();
            if (validator != null) {
                validationError = validator.getValidationMessage();
                isValidated = false;
                isChangeValidated = false;
            } else {
                validationError = "This is fine";
                isValidated = true;
                isChangeValidated = true;
            }
        }
    }
}
