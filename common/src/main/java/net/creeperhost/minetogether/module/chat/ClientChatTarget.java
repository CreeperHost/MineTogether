package net.creeperhost.minetogether.module.chat;

public enum ClientChatTarget
{
    DEFAULT(0), MINETOGETHER(1), PARTY(2);

    public final int id;

    ClientChatTarget(int id)
    {
        this.id = id;
    }
}
