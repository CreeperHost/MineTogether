package net.creeperhost.minetogether.chat.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.creeperhost.minetogether.gui.MTTextures;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.lib.BackgroundRender;
import net.creeperhost.polylib.client.modulargui.lib.GuiRender;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.creeperhost.polylib.client.modulargui.sprite.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Map;

/**
 * Created by brandon3055 on 28/10/2023
 */
public class PlayerIconElement extends GuiElement<PlayerIconElement> implements BackgroundRender {
    @Nullable
    private RenderType skinType;
    public boolean textureFail = false;
    @Nullable
    private GameProfile profile;
    private Material fallback = MTTextures.get("player_offline");

    /**
     * @param parent parent {@link GuiParent}.
     */
    public PlayerIconElement(@NotNull GuiParent<?> parent, @Nullable GameProfile profile) {
        super(parent);
        this.profile = profile;
    }

    public void setProfile(GameProfile profile) {
        this.profile = profile;
        skinType = null;
        textureFail = false;
    }

    @Override
    public void renderBehind(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        if (skinType == null && profile != null && !textureFail) {
            updateGameProfile();
        }

        if (skinType != null) {
            draw(render);
        } else {
            render.texRect(fallback, getRectangle());
        }
    }

    public void draw(GuiRender render) {
        float texMin = 8/64F;
        float texMax = 16/64F;

        VertexConsumer buffer = render.buffers().getBuffer(skinType);
        Matrix4f mat = render.pose().last().pose();
        buffer.vertex(mat, (float) xMax(), (float) yMax(), 0).uv(texMax, texMax).endVertex();  //R-B
        buffer.vertex(mat, (float) xMax(), (float) yMin(), 0).uv(texMax, texMin).endVertex();  //R-T
        buffer.vertex(mat, (float) xMin(), (float) yMin(), 0).uv(texMin, texMin).endVertex();  //L-T
        buffer.vertex(mat, (float) xMin(), (float) yMax(), 0).uv(texMin, texMax).endVertex();  //L-B
        render.flush();
    }


    private void updateGameProfile() {
        if (profile == null) return;

        if (!profile.getProperties().containsKey("textures")) {
            //TODO Off thread
            mc().getMinecraftSessionService().fetchProfile(profile.getId(), true);
        }

        PlayerSkin skin = mc().getSkinManager().getOrLoad(profile).getNow(null);
        if (skin != null) {
            skinType = GuiRender.texType(skin.texture());
        } else {
            textureFail = true;
        }
    }
}
