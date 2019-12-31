package net.creeperhost.minetogether.common;

/**
 * Created by Aaron on 01/05/2017.
 */
public interface IOrderValidation
{
    boolean validationCheckAtPhase(ValidationPhase phase);
    
    boolean isValid(String string);
    
    boolean isAsync();
    
    String getValidationMessage();
    
    String getName();
    
    enum ValidationPhase
    {
        CHANGED,
        FOCUSLOST
    }
}
