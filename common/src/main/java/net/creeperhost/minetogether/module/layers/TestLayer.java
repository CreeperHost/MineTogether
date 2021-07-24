package net.creeperhost.minetogether.module.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class TestLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public TestLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent)
    {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer entity, float f, float g, float h, float j, float k, float l)
    {
//        poseStack.pushPose();
//
//        getParentModel().body.translateAndRotate(poseStack);
//
//        poseStack.translate(0.0D, 0.4D, 0.3D);
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(180f));
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(180f));

//        float rotation = (float) (entity.level.getGameTime() % 80);
//        poseStack.scale(.6f, .6f, .6f);

//        poseStack.mulPose(Vector3f.ZP.rotationDegrees(360f * rotation / 80f));

//        ResourceLocation resourceLocationMineTogetherLogo = new ResourceLocation(Constants.MOD_ID, "textures/minetogether25.png");
//        ShieldModel shieldModel = new ShieldModel();
//
//        VertexConsumer ivertexbuilder = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.crumbling(resourceLocationMineTogetherLogo), false, false);
//
//        shieldModel.renderToBuffer(poseStack, ivertexbuilder, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//        renderColoredCutoutModel(getParentModel(), resourceLocationMineTogetherLogo, poseStack, multiBufferSource, i, entity, 1.0F, 1.0F, 1.0F);


        //        renderItem(new ItemStack(Items.COOKED_BEEF), poseStack, multiBufferSource, 0xF000F0);

//        poseStack.popPose();

    }

    public static void renderItem(ItemStack stack, PoseStack ms, MultiBufferSource buffers, int light)
    {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, ms, buffers);
    }
}
