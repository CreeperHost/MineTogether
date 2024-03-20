package net.creeperhost.minetogether.chat.gui;

import net.creeperhost.minetogether.chat.ChatStatistics;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.config.LocalConfig;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.GuiButton;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.GuiProvider;
import net.minecraft.network.chat.Component;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 24/09/2023
 */
public class NewUserGui implements GuiProvider {
    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();

        GuiElement<?> root = gui.getRoot();

        GuiElement<?> last = null;
        for (int i = 1; i < 5; i++) {
            last = new GuiText(root, Component.translatable("minetogether:new_user." + i, i == 4 ? new Object[]{ChatStatistics.userCount} : new Object[0]))
                    .constrain(LEFT, match(root.get(LEFT)))
                    .constrain(RIGHT, match(root.get(RIGHT)))
                    .constrain(TOP, midPoint(root.get(TOP), root.get(BOTTOM), -40 + (i * 9)))
                    .constrain(HEIGHT, literal(8));
        }

        GuiButton join = MTStyle.Flat.button(root, () -> Component.translatable("minetogether:gui.join.button.accept", ChatStatistics.onlineCount))
                .onPress(() -> {
                    MineTogetherChat.setNewUserResponded();
                    gui.mc().setScreen(new ModularGuiScreen(PublicChatGui.createGui(), gui.getParentScreen()));
                })
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), -150))
                .constrain(TOP, relative(last.get(BOTTOM), 5))
                .constrain(HEIGHT, literal(16))
                .constrain(WIDTH, literal(300));

        GuiButton reject = MTStyle.Flat.buttonCaution(root, Component.translatable("minetogether:gui.join.button.reject"))
                .onPress(() -> {
                    MineTogetherChat.disableChat();
                    LocalConfig.instance().chatEnabled = false;
                    LocalConfig.save();
                    MineTogetherChat.setNewUserResponded();
                    gui.mc().setScreen(gui.getParentScreen());
                })
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), -150))
                .constrain(TOP, relative(join.get(BOTTOM), 2))
                .constrain(HEIGHT, literal(16))
                .constrain(WIDTH, literal(300));
    }
}
