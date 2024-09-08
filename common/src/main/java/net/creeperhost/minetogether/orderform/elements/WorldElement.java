package net.creeperhost.minetogether.orderform.elements;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.dialogs.ItemSelectDialog;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.minetogether.orderform.WorldUploader;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.elements.GuiDialog;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiRectangle;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;
import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 28/06/2024
 */
public class WorldElement extends GuiElement<WorldElement> {

    private final OrderGui gui;

    public String worldName = "";
    public WorldUploader worldUploader = null;

    public WorldElement(@NotNull GuiParent<?> parent, OrderGui gui) {
        super(parent);
        this.gui = gui;

        GuiElement<?> lastElement = new GuiText(this, Component.translatable("minetogether:gui.order.world").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, match(get(TOP)))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        Component worldInfo = Component.translatable("minetogether:gui.order.world.info").withStyle(ChatFormatting.GRAY);
        lastElement = new GuiText(this, worldInfo)
                .setWrap(true)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)));
        lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(worldInfo, (int) xMax() - (int) xMin())));

        List<LevelSummary> levels = loadLevels(this.mc());
        if (levels.isEmpty()) {
            Component noWorlds = Component.translatable("minetogether:gui.order.world.no_worlds").withStyle(RED);
            lastElement = new GuiText(this, noWorlds)
                    .setWrap(true)
                    .setAlignment(Align.LEFT)
                    .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(RIGHT, match(get(RIGHT)));
            lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(noWorlds, (int) xMax() - (int) xMin())));
        } else {
            lastElement = MTStyle.Flat.button(this, () -> worldUploader != null && worldUploader.errored() ? Component.literal(worldUploader.getError()).withStyle(RED) : Component.translatable("minetogether:gui.order.world.select"))
                    .setDisabled(() -> worldUploader != null || !StringUtil.isNullOrEmpty(gui.order.worldUrl))
                    .onPress(() -> new ItemSelectDialog<>(this.getModularGui().getRoot(), Component.translatable("minetogether:gui.order.world.select"), levels, levels.get(0), e -> Component.empty().append(Component.literal(e.getLevelName()).withStyle(GREEN)).append("\n").append(e.getInfo()).withStyle(GRAY))
                            .setCloseOnOutsideClick(true)
                            .setOnItemSelected(selected -> {
                                Path worldFolder = this.mc().getLevelSource().getLevelPath(selected.getLevelId());
                                confirmStartUpload(worldFolder, selected.getLevelName());
                            })
                    )
                    .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(RIGHT, match(get(RIGHT)))
                    .constrain(HEIGHT, literal(12));

            //Right Cancel/Remove button
            lastElement = MTStyle.Flat.buttonCaution(this, this::cancelWorldText)
                    .setDisabled(() -> worldUploader == null && StringUtil.isNullOrEmpty(gui.order.worldUrl))
                    .onPress(this::cancelWorldAction)
                    .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                    .constrain(WIDTH, literal(60))
                    .constrain(RIGHT, match(get(RIGHT)))
                    .constrain(HEIGHT, literal(12));

            //Left Progress/Copy/Retry button
            MTStyle.Flat.button(this, this::worldBtnLeft)
                    .setDisabled(() -> !(worldUploader != null && worldUploader.errored()))
                    .onPress(this::worldBtnLeftAction)
                    .constrain(TOP, match(lastElement.get(TOP)))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(RIGHT, relative(lastElement.get(LEFT), -2))
                    .constrain(HEIGHT, literal(12));
        }

        constrain(BOTTOM, match(lastElement.get(BOTTOM)));
//        Constraints.bind(new GuiRectangle(this).border(0xFFFF0000), this);
    }

    private Component worldBtnLeft() {
        if (!StringUtil.isNullOrEmpty(gui.order.worldUrl)) {
            return Component.translatable("minetogether:gui.order.world.upload_complete", worldName).withStyle(GREEN);
        } else if (worldUploader != null) {
            if (worldUploader.errored()) {
                return Component.translatable("minetogether:gui.order.world.retry");
            }
            return worldUploader.getStatus();
        }
        return Component.empty();
    }

    private void worldBtnLeftAction() {
        if (!StringUtil.isNullOrEmpty(gui.order.worldUrl)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(gui.order.worldUrl);
        } else if (worldUploader != null && worldUploader.errored()) {
            worldUploader.start();
        }
    }

    private Component cancelWorldText() {
        if (!StringUtil.isNullOrEmpty(gui.order.worldUrl)) {
            return Component.translatable("minetogether:gui.order.world.remove");
        } else if (worldUploader != null) {
            return Component.translatable("gui.cancel");
        }
        return Component.empty();
    }

    private void cancelWorldAction() {
        if (!StringUtil.isNullOrEmpty(gui.order.worldUrl)) {
            gui.order.worldUrl = "";
        } else if (worldUploader != null) {
            worldUploader.cancel();
            worldUploader = null;
        }
    }

    private List<LevelSummary> loadLevels(Minecraft minecraft) {
        LevelStorageSource.LevelCandidates candidates;
        try {
            candidates = minecraft.getLevelSource().findLevelCandidates();
        } catch (LevelStorageException e) {
            OrderGui.LOGGER.error("Couldn't load level list", e);
            return Collections.emptyList();
        }

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        } else {
            try {
                return minecraft.getLevelSource()
                        .loadLevelSummaries(candidates)
                        .exceptionally((e) -> {
                            OrderGui.LOGGER.error("Couldn't load level list", e);
                            return List.of();
                        })
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                OrderGui.LOGGER.error("Couldn't load level list", e);
                return Collections.emptyList();
            }
        }
    }

    private void confirmStartUpload(Path worldFolder, String levelName) {
        GuiDialog.optionsDialog(this, Component.translatable("minetogether:gui.order.confirm_upload",
                        Component.literal(levelName).withStyle(GOLD)).withStyle(BLUE),
                Component.translatable("minetogether:gui.order.confirm_upload.info").withStyle(GRAY),
                250,
                GuiDialog.primary(Component.translatable("minetogether:gui.order.world.upload"), () -> startWorldUpload(worldFolder, levelName)),
                GuiDialog.caution(Component.translatable("gui.cancel"), () -> {})
        );
    }

    private void startWorldUpload(Path worldFolder, String levelName) {
        if (worldUploader != null) return;
        worldName = levelName;
        worldUploader = new WorldUploader(worldFolder);
        worldUploader.start();
    }

    public void uploadCurrentWorld(ModularGui gui) {
        IntegratedServer server = gui.mc().getSingleplayerServer();
        if (server == null) return;
        LevelStorageSource.LevelStorageAccess storage = server.storageSource;
        Path levelPath = storage.getLevelDirectory().path().toAbsolutePath();
        confirmStartUpload(levelPath, server.getWorldData().getLevelName());
    }

//    private void uploadCurrentWorld(ModularGui gui) {

//        world.startWorldUpload();
//
//
//    }
}
