package net.creeperhost.minetogether.orderform.data;

import java.util.regex.Pattern;

/**
 * Created by Aaron on 01/05/2017.
 */
public class RegexValidator implements IOrderValidation {

    private final Pattern pattern;
    private final String failMessage;

    public RegexValidator(String regex, String failMessage) {
        pattern = Pattern.compile(regex);
        this.failMessage = failMessage;
    }

    @Override
    public boolean validationCheckAtPhase(ValidationPhase phase) {
        return phase.equals(ValidationPhase.CHANGED);
    }

    @Override
    public boolean isValid(String string) {
        return pattern.matcher(string.toLowerCase()).matches();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public String getValidationMessage() {
        return failMessage;
    }

    @Override
    public String getName() {
        return "RegexValidator";
    }
}
