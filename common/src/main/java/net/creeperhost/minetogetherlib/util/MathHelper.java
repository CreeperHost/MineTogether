package net.creeperhost.minetogetherlib.util;

public class MathHelper
{
    public static int floor(float value)
    {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static int ceil(float value)
    {
        int i = (int)value;
        return value > (float)i ? i + 1 : i;
    }
}
