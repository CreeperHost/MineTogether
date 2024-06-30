package net.creeperhost.minetogether.orderform.elements;

import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.dialogs.GuiDialog;
import net.creeperhost.minetogether.gui.dialogs.ItemSelectDialog;
import net.creeperhost.minetogether.orderform.OrderGui;
import net.creeperhost.minetogether.orderform.WorldUploader;
import net.creeperhost.polylib.client.modulargui.elements.GuiElement;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.GuiParent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
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

        GuiElement<?> lastElement = new GuiText(this, new TranslatableComponent("minetogether:gui.order.world").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD))
                .setAlignment(Align.LEFT)
                .constrain(TOP, match(get(TOP)))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(HEIGHT, literal(8));

        Component worldInfo = new TranslatableComponent("minetogether:gui.order.world.info").withStyle(ChatFormatting.GRAY);
        lastElement = new GuiText(this, worldInfo)
                .setWrap(true)
                .setAlignment(Align.LEFT)
                .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                .constrain(LEFT, match(get(LEFT)))
                .constrain(RIGHT, match(get(RIGHT)));
        lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(worldInfo.getString(), (int) xMax() - (int) xMin())));

        List<LevelSummary> levels = loadLevels(this.mc());
        if (levels.isEmpty()) {
            Component noWorlds = new TranslatableComponent("minetogether:gui.order.world.no_worlds").withStyle(RED);
            lastElement = new GuiText(this, noWorlds)
                    .setWrap(true)
                    .setAlignment(Align.LEFT)
                    .constrain(TOP, relative(lastElement.get(BOTTOM), 3))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(RIGHT, match(get(RIGHT)));
            lastElement.constrain(HEIGHT, dynamic(() -> (double) this.font().wordWrapHeight(noWorlds.getString(), (int) xMax() - (int) xMin())));
        } else {
            lastElement = MTStyle.Flat.button(this, () -> worldUploader != null && worldUploader.errored() ? new TextComponent(worldUploader.getError()).withStyle(RED) : new TranslatableComponent("minetogether:gui.order.world.select"))
                    .setDisabled(() -> worldUploader != null || !StringUtil.isNullOrEmpty(gui.order.worldUrl))
                    .onPress(() -> new ItemSelectDialog<>(this.getModularGui().getRoot(), new TranslatableComponent("minetogether:gui.order.world.select"), levels, levels.get(0), e -> new TextComponent("").append(new TextComponent(e.getLevelName()).withStyle(GREEN)).append("\n").append(e.getInfo()).withStyle(GRAY))
                            .setCloseOnOutsideClick(true)
                            .setOnItemSelected(selected -> {
                                Path worldFolder = this.mc().getLevelSource().getBaseDir().resolve(selected.getLevelId());
                                GuiDialog.optionsDialog(this, new TranslatableComponent("minetogether:gui.order.confirm_upload",
                                                new TextComponent(selected.getLevelName()).withStyle(GOLD)).withStyle(BLUE),
                                        new TranslatableComponent("minetogether:gui.order.confirm_upload.info").withStyle(GRAY),
                                        250,
                                        GuiDialog.primary(new TranslatableComponent("minetogether:gui.order.world.upload"), () -> startWorldUpload(worldFolder, selected)),
                                        GuiDialog.caution(new TranslatableComponent("gui.cancel"), () -> {})
                                );
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
            return new TranslatableComponent("minetogether:gui.order.world.upload_complete", worldName).withStyle(GREEN);
        } else if (worldUploader != null) {
            if (worldUploader.errored()) {
                return new TranslatableComponent("minetogether:gui.order.world.retry");
            }
            return worldUploader.getStatus();
        }
        return new TextComponent("");
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
            return new TranslatableComponent("minetogether:gui.order.world.remove");
        } else if (worldUploader != null) {
            return new TranslatableComponent("gui.cancel");
        }
        return new TextComponent("");
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
        List<LevelSummary> levels;
        try {
            levels = minecraft.getLevelSource().getLevelList();
        } catch (LevelStorageException e) {
            OrderGui.LOGGER.error("Couldn't load level list", e);
            return Collections.emptyList();
        }
        Collections.sort(levels);
        return levels;
    }

    private void startWorldUpload(Path worldFolder, LevelSummary summary) {
        if (worldUploader != null) return;
        worldName = summary.getLevelName();
        worldUploader = new WorldUploader(worldFolder);
        worldUploader.start();
    }
}
