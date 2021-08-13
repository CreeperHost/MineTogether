package net.creeperhost.minetogether.mixin;

import net.creeperhost.minetogether.module.layers.TestLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRender extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public MixinPlayerRender(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f)
    {
        super(context, entityModel, f);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer entity)
    {
        return null;
    }
    //    public MixinPlayerRender(EntityRenderDispatcher entityRenderDispatcher, PlayerModel<AbstractClientPlayer> entityModel, float f)
//    {
//        super(entityRenderDispatcher, entityModel, f);
//    }
//
//    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Z)V")
//    public void onInit(EntityRenderDispatcher entityRenderDispatcher, boolean bl, CallbackInfo ci)
//    {
//        addLayer(new TestLayer((PlayerRenderer) (Object) this));
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(AbstractClientPlayer entity) { return  entity.getSkinTextureLocation(); }
}
