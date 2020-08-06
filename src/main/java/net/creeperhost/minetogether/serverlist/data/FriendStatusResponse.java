package net.creeperhost.minetogether.serverlist.data;

public class FriendStatusResponse
{
    public String message = "";
    public String hash;
    public boolean success;

    public FriendStatusResponse(boolean success, String message, String hash)
    {
        this.message = message;
        this.hash = hash;
        this.success = success;
    }

    public String getHash() {
        return hash;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
