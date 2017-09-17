package net.creeperhost.creeperhost.gui.config;

import net.creeperhost.creeperhost.common.Config;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Aaron on 02/06/2017.
 */
public class ReflectionConfigElement implements IConfigElement
{

    final Field field;
    final Object defValue;

    ReflectionConfigElement(Field field, Object defValue) {
        this.field = field;
        this.defValue = defValue;
    }

    @Override
    public boolean isProperty()
    {
        return true;
    }

    private HashMap<String, Class<? extends GuiConfigEntries.IConfigEntry>> lookupEntry = new HashMap<String, Class<? extends GuiConfigEntries.IConfigEntry>>() {{
        put("boolean", GuiConfigEntries.BooleanEntry.class);
        put("String", GuiConfigEntries.StringEntry.class);
        put("int", GuiConfigEntries.IntegerEntry.class);
    }};

    @Override
    public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass()
    {
        return null;
    }

    @Override
    public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return field.getName();
    }

    @Override
    public String getQualifiedName()
    {
        return "creeperhost.config." + getName();
    }

    @Override
    public String getLanguageKey()
    {
        return "creeperhost.config." + getName().toLowerCase();
    }

    @Override
    public String getComment()
    {
        return "creeperhost.config.comment." + getName().toLowerCase();
    }

    @Override
    public List<IConfigElement> getChildElements()
    {
        return null;
    }

    private HashMap<String, ConfigGuiType> lookup = new HashMap<String, ConfigGuiType>() {{
        put("boolean", ConfigGuiType.BOOLEAN);
        put("String", ConfigGuiType.STRING);
        put("int", ConfigGuiType.INTEGER);
    }};

    @Override
    public ConfigGuiType getType()
    {
        ConfigGuiType type = lookup.get(field.getType().toString());
        return type != null ? type : ConfigGuiType.STRING;
    }

    @Override
    public boolean isList()
    {
        return false;
    }

    @Override
    public boolean isListLengthFixed()
    {
        return false;
    }

    @Override
    public int getMaxListLength()
    {
        return 0;
    }

    @Override
    public boolean isDefault()
    {
        return defValue != null && defValue.equals(get());
    }

    @Override
    public Object getDefault()
    {
        return defValue;
    }

    @Override
    public Object[] getDefaults()
    {
        return new Object[0];
    }

    @Override
    public void setToDefault()
    {

    }

    @Override
    public boolean requiresWorldRestart()
    {
        return false;
    }

    @Override
    public boolean showInGui()
    {
        return true;
    }

    @Override
    public boolean requiresMcRestart()
    {
        return false;
    }

    @Override
    public Object get()
    {
        try
        {
            field.setAccessible(true);
            return field.get(Config.getInstance());
        } catch (Throwable e)
        {
        }
        return null;
    }

    @Override
    public Object[] getList()
    {
        return new Object[0];
    }

    @Override
    public void set(Object value)
    {
        try
        {
            field.setAccessible(true);
            field.set(Config.getInstance(), value);
        } catch (Throwable e)
        {
        }
    }

    @Override
    public void set(Object[] aVal)
    {

    }

    @Override
    public String[] getValidValues()
    {
        return null;
    }

    @Override
    public Object getMinValue()
    {
        return 0;
    }

    @Override
    public Object getMaxValue()
    {
        return 400;
    }

    @Override
    public Pattern getValidationPattern()
    {
        return null;
    }
}
