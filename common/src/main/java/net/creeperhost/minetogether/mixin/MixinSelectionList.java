package net.creeperhost.minetogether.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSelectionList.class)
public interface MixinSelectionList
{
    @Invoker("getRowTop")
    int invokeRowTop(int i);

    @Accessor
    int getWidth();


}
