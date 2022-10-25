package net.creeperhost.minetogether.serverlist.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.polylib.gui.LargeButton;
import net.creeperhost.minetogether.serverlist.data.ListType;
import net.creeperhost.minetogether.serverlist.data.SortType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ServerTypeScreen extends Screen {

    private final Screen parent;

    public ServerTypeScreen(Screen parent) {
        super(new TranslatableComponent("minetogether:screen.servertype.title").withStyle(e -> e.withBold(true)));
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new LargeButton((width / 2) - 180, (height / 8) + 20, 120, 165, new TextComponent("PUBLIC"), new TranslatableComponent("minetogether:screen.servertype.listing.public"), new ItemStack(Items.GUNPOWDER), p -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ListType.PUBLIC, SortType.RANDOM));
        }));
        addRenderableWidget(new LargeButton((width / 2) - 60, (height / 8) + 20, 120, 165, new TextComponent("COMMUNITY"), new TranslatableComponent("minetogether:screen.servertype.listing.community"), new ItemStack(Items.FISHING_ROD), p -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ListType.INVITE, SortType.RANDOM));
        }));
        addRenderableWidget(new LargeButton((width / 2) + 60, (height / 8) + 20, 120, 165, new TextComponent("CLOSED"), new TranslatableComponent("minetogether:screen.servertype.listing.closed"), new ItemStack(Items.CHAINMAIL_CHESTPLATE), p -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ListType.APPLICATION, SortType.RANDOM));
        }));
        addRenderableWidget(new Button((width / 2) - 110, height - 22, 220, 20, new TranslatableComponent("gui.cancel"), p -> {
            Minecraft.getInstance().setScreen(parent);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(1);
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        drawCenteredString(poseStack, font, getTitle(), (width / 3), 12, -1);
        poseStack.popPose();
        super.render(poseStack, i, j, f);
    }
}
