package net.creeperhost.minetogether.gui.config;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.common.Config;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aaron on 02/06/2017.
 */
public class GuiCreeperConfig extends GuiConfig
{
    public GuiCreeperConfig(GuiScreen parentScreen)
    {
        super(parentScreen, getConfigElements(), CreeperHost.MOD_ID, false, false, getTitle(parentScreen));
    }
    
    @SuppressWarnings("Duplicates")
    private static List<IConfigElement> getConfigElements()
    {
        final Config defaultConfig = new Config();
        
        List<IConfigElement> array = new ArrayList<IConfigElement>();
        Field[] fields = Config.class.getDeclaredFields();
        for (Field field : fields)
        {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
            {
                continue;
            }
            Object defValue = null;
            try
            {
                field.setAccessible(true);
                defValue = field.get(defaultConfig);
                
            } catch (Throwable ignored) {}
            
            array.add(new ReflectionConfigElement(field, defValue));
        }
        return array;
    }
    
    private static String getTitle(GuiScreen parent)
    {
        return I18n.format("creeperhost.config.title");
    }
}
