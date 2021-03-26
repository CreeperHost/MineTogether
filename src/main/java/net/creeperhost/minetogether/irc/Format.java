package net.creeperhost.minetogether.irc;

import java.util.regex.Pattern;

public enum Format
{
    /**
     * Black.
     */
    BLACK(1),
    /**
     * Blue.
     */
    BLUE(12),
    /**
     * It’s a BOLD strategy Cotton. Let’s see if it pays off for him.
     */
    BOLD('\u0002'),
    /**
     * Brown.
     */
    BROWN(5),
    /**
     * Cyan.
     */
    CYAN(11),
    /**
     * Dark blue.
     */
    DARK_BLUE(2),
    /**
     * Dark gray.
     */
    DARK_GRAY(14),
    /**
     * Dark green.
     */
    DARK_GREEN(3),
    /**
     * Green.
     */
    GREEN(9),
    /**
     * Italic.
     */
    ITALIC('\u001d'),
    /**
     * Light gray.
     */
    LIGHT_GRAY(15),
    /**
     * Magenta.
     */
    MAGENTA(13),
    /**
     * Reset formatting.
     */
    RESET('\u000f'),
    /**
     * Olive.
     */
    OLIVE(7),
    /**
     * Purple.
     */
    PURPLE(6),
    /**
     * Red.
     */
    RED(4),
    /**
     * esreveR.
     */
    REVERSE('\u0016'),
    /**
     * Teal.
     */
    TEAL(10),
    /**
     * Underline.
     */
    UNDERLINE('\u001f'),
    /**
     * White.
     */
    WHITE(0),
    /**
     * Yello. Oh yeah.
     */
    YELLOW(8);

    private final int color;
    private final boolean isColor;
    private final String toString;

    Format(char ch) {
        this.color = -1;
        this.isColor = false;
        this.toString = String.valueOf(ch);
    }

    Format(int color)
    {
        this.color = color;
        this.isColor = true;
        this.toString = COLOR_CHAR + ((color < 10) ? "0" : "") + color;
    }

    public static final char COLOR_CHAR = '\u0003';
    private static final Pattern COLOR_REGEX = Pattern.compile(COLOR_CHAR + "[0-9]{1,2}");
    private static final Pattern FORMAT_REGEX = Pattern.compile("[" + BOLD + RESET + REVERSE + UNDERLINE + ']');

    public static String stripAll(String input)
    {
        return stripColor(stripFormatting(input));
    }

    public static String stripColor(String input)
    {
        return COLOR_REGEX.matcher(input).replaceAll("");
    }

    public static String stripFormatting(String input)
    {
        return FORMAT_REGEX.matcher(input).replaceAll("");
    }

    public boolean isColor()
    {
        return this.isColor;
    }

    public int getColorChar()
    {
        return this.color;
    }

    @Override
    public String toString()
    {
        return this.toString;
    }
}
