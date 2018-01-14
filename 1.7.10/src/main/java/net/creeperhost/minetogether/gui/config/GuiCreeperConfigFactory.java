package net.creeperhost.minetogether.gui.config;

import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

/**
 * Created by Aaron on 02/06/2017.
 */
public class GuiCreeperConfigFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {
    }

    //@Override
    public boolean hasConfigGui()
    {
        return true;
    }

    //@Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new GuiCreeperConfig(parentScreen);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return GuiCreeperConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @SuppressWarnings("deprecation") // Shh. I need to implement you.
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }
}
