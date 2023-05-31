package net.creeperhost.minetogether.connect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.connect.ConnectHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Created by brandon3055 on 21/04/2023
 */
@Deprecated //For now we will stick with overriding the existing lan header. But adding out own additional header is an option.
public class FriendsHeader extends ServerSelectionList.Entry {
    private final Minecraft minecraft = Minecraft.getInstance();

    public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
        int var10000 = j + m / 2;
        Objects.requireNonNull(minecraft.font);
        int p = var10000 - 9 / 2;
        boolean online = ConnectHandler.isEnabled();
        String locString = online ? I18n.get("minetogether.connect.scan") : I18n.get("minetogether.connect.scan.offline");
        minecraft.font.draw(poseStack, locString, (float) (minecraft.screen.width / 2 - minecraft.font.width(locString) / 2), (float) p, online ? 0x00FF00 : 0xFF0000);
        if (online) {
            String string = LoadingDotsText.get(Util.getMillis());
            Font var13 = minecraft.font;
            float var10003 = (float) (minecraft.screen.width / 2 - minecraft.font.width(string) / 2);
            Objects.requireNonNull(minecraft.font);
            var13.draw(poseStack, string, var10003, (float) (p + 9), 8421504);
        }
    }

    public Component getNarration() {
        return CommonComponents.EMPTY;
    }
}
