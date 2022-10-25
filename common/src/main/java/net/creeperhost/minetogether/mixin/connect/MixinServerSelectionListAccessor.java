package net.creeperhost.minetogether.mixin.connect;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.ThreadPoolExecutor;

@Mixin (ServerSelectionList.class)
public interface MixinServerSelectionListAccessor {

    @Accessor ("THREAD_POOL")
    static ThreadPoolExecutor getPingThreadPool() {
        throw new AssertionError();
    }
}
