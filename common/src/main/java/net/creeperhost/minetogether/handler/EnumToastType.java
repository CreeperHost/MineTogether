package net.creeperhost.minetogether.handler;

public enum EnumToastType
{
    DEFAULT(0, 0),
    WHITE(0, 32),
    WARNING(0, 64);

    int x;
    int y;

    EnumToastType(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
