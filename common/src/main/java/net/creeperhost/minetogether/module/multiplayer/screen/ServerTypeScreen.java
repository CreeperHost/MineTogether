package net.creeperhost.minetogether.module.multiplayer.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.multiplayer.data.ServerListType;
import net.creeperhost.minetogether.module.multiplayer.data.ServerSortOrder;
import net.creeperhost.polylib.client.screen.widget.buttons.GuiButtonLarge;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ServerTypeScreen extends Screen
{
    private final Screen parent;

    public ServerTypeScreen(Screen parent)
    {
        super(Component.translatable("minetogether.screen.servertype"));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();

        addRenderableWidget(new GuiButtonLarge((width / 2) - 180, (height / 8) + 20, 120, 165, "PUBLIC", I18n.get("minetogether.listing.public"), new ItemStack(Items.GUNPOWDER), p ->
        {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ServerListType.PUBLIC, ServerSortOrder.RANDOM));
        }));
        addRenderableWidget(new GuiButtonLarge((width / 2) - 60, (height / 8) + 20, 120, 165, "COMMUNITY", I18n.get("minetogether.listing.community"), new ItemStack(Items.FISHING_ROD), p ->
        {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ServerListType.APPLICATION, ServerSortOrder.RANDOM));
        }));
        addRenderableWidget(new GuiButtonLarge((width / 2) + 60, (height / 8) + 20, 120, 165, "CLOSED", I18n.get("minetogether.listing.closed"), new ItemStack(Items.CHAINMAIL_CHESTPLATE), p ->
        {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreenPublic(parent, ServerListType.INVITE, ServerSortOrder.RANDOM));
        }));
        addRenderableWidget(new Button((width / 2) - 110, height - 22, 220, 20, Component.translatable("gui.cancel"), p ->
        {
            Minecraft.getInstance().setScreen(parent);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        drawCenteredString(poseStack, font, ChatFormatting.BOLD + I18n.get("minetogether.listing.title"), (width / 3), 12, -1);
        poseStack.popPose();
        super.render(poseStack, i, j, f);
    }
}
