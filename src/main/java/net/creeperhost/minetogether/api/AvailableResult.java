package net.creeperhost.minetogether.api;

/**
 * Created by Aaron on 26/04/2017.
 */
public class AvailableResult
{
    
    private boolean success;
    private String message;
    
    public AvailableResult(boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }
    
    public boolean getSuccess()
    {
        return this.success;
    }
    
    public String getMessage()
    {
        return this.message;
    }
}

