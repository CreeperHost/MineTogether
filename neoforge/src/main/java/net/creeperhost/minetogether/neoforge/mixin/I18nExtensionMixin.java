package net.creeperhost.minetogether.neoforge.mixin;

import net.neoforged.neoforge.common.I18nExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by covers1624 on 6/11/22.
 */
@Mixin (
        value = I18nExtension.class,
        remap = false
)
public class I18nExtensionMixin {

    @Inject (
            method = "parseFormat",
            at = @At ("HEAD"),
            cancellable = true
    )
    private static void onParseFormat(String format, Object[] args, CallbackInfoReturnable<String> cir) {
        int numOpenBrace = 0;
        int numCloseBrace = 0;
        for (char c : format.toCharArray()) {
            if (c == '{') {
                numOpenBrace++;
            } else if (c == '}') {
                numCloseBrace++;
            }
        }
        if (numOpenBrace != numCloseBrace) {
            // No format, unmatched braces.
            cir.setReturnValue(format);
        }
    }
}
