package net.creeperhost.minetogether.util;

import net.creeperhost.minetogether.common.IOrderValidation;

import java.util.regex.Pattern;

/**
 * Created by Aaron on 01/05/2017.
 */
public class RegexValidator implements IOrderValidation
{

    private Pattern pattern;
    private String failMessage;

    public RegexValidator(String regex, String failMessage)
    {
        pattern = Pattern.compile(regex);
        this.failMessage = failMessage;
    }

    @Override
    public boolean validationCheckAtPhase(ValidationPhase phase)
    {
        return phase.equals(ValidationPhase.CHANGED);
    }

    @Override
    public boolean isValid(String string)
    {
        return pattern.matcher(string.toLowerCase()).matches();
    }

    @Override
    public boolean isAsync()
    {
        return false;
    }

    @Override
    public String getValidationMessage()
    {
        return failMessage;
    }

    @Override
    public String getName()
    {
        return "RegexValidator";
    }
}
