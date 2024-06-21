package net.creeperhost.minetogether.mixin.order;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.creeperhost.minetogether.config.Config;
import net.minecraft.client.gui.screens.TitleScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Shadow
    private RealmsNotificationsScreen realmsNotificationsScreen;

    @Redirect(method = "init()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/TitleScreen;realmsNotificationsScreen:Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;", opcode = Opcodes.PUTFIELD))
    private void injected(TitleScreen titleScreen, RealmsNotificationsScreen realmsNotificationsScreen) {
        if (!Config.instance().replaceRealms) {
            this.realmsNotificationsScreen = realmsNotificationsScreen;
        }
    }
}
