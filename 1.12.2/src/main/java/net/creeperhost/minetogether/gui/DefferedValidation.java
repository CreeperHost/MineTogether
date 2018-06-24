package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.common.IOrderValidation;

/**
 * Created by Aaron on 02/05/2017.
 */
public abstract class DefferedValidation implements IOrderValidation
{
    private boolean isDone;

    private ValidationPhase phase;
    private boolean result;

    @Override
    public boolean isAsync()
    {
        return !isDone;
    }

    public abstract boolean isValidReal(String string);

    public abstract String getMessageReal();

    public void doAsync(final String string)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                result = isValidReal(string);
                isDone = true;
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public boolean isValid(String string)
    {
        // We don't actually use the string. Passed for interface compatibility.
        return isDone && result;
    }

    @Override
    public String getValidationMessage()
    {
        return !isDone ? "Still checking, please wait" : getMessageReal();
    }

    public boolean isDone()
    {
        return isDone;
    }

    public ValidationPhase getPhase()
    {
        return phase;
    }

    public void setPhase(ValidationPhase phase)
    {
        this.phase = phase;
    }

    public void reset()
    {
        isDone = false;
        result = false;
    }
}
