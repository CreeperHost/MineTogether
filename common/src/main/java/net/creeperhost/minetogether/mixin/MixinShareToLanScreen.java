package net.creeperhost.minetogether.mixin;

import net.minecraft.client.gui.screens.ShareToLanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShareToLanScreen.class)
public interface MixinShareToLanScreen {
    @Accessor
    String getGameModeName();

    @Accessor
    boolean getCommands();
}
