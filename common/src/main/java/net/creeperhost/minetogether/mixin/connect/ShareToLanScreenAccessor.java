package net.creeperhost.minetogether.mixin.connect;

import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin (ShareToLanScreen.class)
public interface ShareToLanScreenAccessor {

    @Accessor
    GameType getGameMode();

    @Accessor
    boolean getCommands();
}
