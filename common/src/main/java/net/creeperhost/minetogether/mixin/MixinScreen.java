package net.creeperhost.minetogether.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface MixinScreen
{
    @Accessor("renderables")
    List<AbstractWidget> getRenderables();
}
