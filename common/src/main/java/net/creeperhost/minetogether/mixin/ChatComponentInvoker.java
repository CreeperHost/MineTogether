package net.creeperhost.minetogether.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentInvoker
{
    @Invoker("addMessage")
    void invokeAddMessage(Component component, int i, int j, boolean bl);
}
