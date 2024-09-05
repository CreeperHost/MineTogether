package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.chat.gui.MTStyle;
import net.creeperhost.minetogether.gui.dialogs.ItemSelectDialog;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiScreen;
import net.creeperhost.polylib.client.modulargui.elements.*;
import net.creeperhost.polylib.client.modulargui.lib.*;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Random;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.*;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 13/04/2024
 */
public class ProfileGui implements GuiProvider {
    private static final Random RANDOM = new Random();

    private boolean selectingName = false;
    private String nameLeft = "";
    private String nameRight = "";
    private ConsoleFeedbackElement feedbackElement;
    private TextState nameState;
    private GuiButton submitButton;

    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return MTStyle.Flat.background(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.renderScreenBackground(false);
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.translatable("minetogether:gui.profile.title"));

        Profile profile = MineTogetherChat.CHAT_STATE.profileManager.getOwnProfile();
        boolean premium = profile.isPremium();

        GuiElement<?> root = gui.getRoot();
        int uiWidth = 250;

        GuiText screenTitle = new GuiText(root, gui.getGuiTitle())
                .constrain(TOP, relative(root.get(TOP), 10))
                .constrain(HEIGHT, literal(8))
                .constrain(LEFT, relative(root.get(LEFT), 10))
                .constrain(RIGHT, relative(root.get(RIGHT), -10));

        GuiText nameTitle = new GuiText(root, Component.translatable("minetogether:gui.profile.display_name").withStyle(ChatFormatting.UNDERLINE))
                .setAlignment(Align.LEFT);
        Constraints.size(nameTitle, uiWidth, 8);
        Constraints.placeOutside(nameTitle, screenTitle, Constraints.LayoutPos.BOTTOM_CENTER, 0, 50);

        var nameField = GuiTextField.create(root, 0xFF000000, 0xFF303030, 0xFFFFFF);
        nameField.primary.setFocusable(premium);
        Constraints.size(nameField.container, uiWidth - 50, 14);
        Constraints.placeInside(nameField.container, nameTitle, Constraints.LayoutPos.BOTTOM_LEFT, 0, 18);
        nameState = nameField.primary.getTextState();
        nameState.setText(trimHash(profile.getDisplayName()));

        GuiButton nameButton = MTStyle.Flat.buttonPrimary(root, Component.translatable("minetogether:gui.profile.button." + (premium ? "save" : "change")))
                .onPress(() -> handleNameChange(premium, nameField.primary));
        Constraints.size(nameButton, uiWidth - nameField.container.xSize() - 1, 14);
        Constraints.placeOutside(nameButton, nameField.container, Constraints.LayoutPos.MIDDLE_RIGHT, 1, 0);

        GuiTextList nextChange = new GuiTextList(root, () -> List.of(Component.translatable("minetogether:gui.profile.next_name_change").withStyle(ChatFormatting.YELLOW), ProfileRequests.getCanChangeComp().withStyle(ChatFormatting.GRAY)))
                .setEnabled(() -> !ProfileRequests.canChangeName())
                .constrain(WIDTH, literal(uiWidth))
//                .setAlignment(Align.LEFT)
                .autoHeight();
        Constraints.placeOutside(nextChange, nameTitle, Constraints.LayoutPos.BOTTOM_CENTER, 0, 20);

        GuiText canChange = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.name_can_be_changed").withStyle(ChatFormatting.GRAY))
                .setEnabled(ProfileRequests::canChangeName)
                .constrain(WIDTH, literal(uiWidth))
                .autoHeight();
        Constraints.placeOutside(canChange, nameTitle, Constraints.LayoutPos.BOTTOM_CENTER, 0, 20);

        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back"))
                .onPress(() -> gui.mc().setScreen(gui.getParentScreen()))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -30))
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), -150 / 2D))
                .constrain(WIDTH, literal(150))
                .constrain(HEIGHT, literal(16));

        buildNameSelectionScreen(root, uiWidth);
        buildBanScreen(root, uiWidth);

        GuiRectangle spinnerBg = new GuiRectangle(root)
                .setEnabled(ProfileRequests::requestsInProgress)
                .setOpaque(true)
                .fill(0x80000000);
        Constraints.bind(spinnerBg, root);
        LoadingSpinner spinner = new LoadingSpinner(spinnerBg);
        Constraints.size(spinner, 80, 80);
        Constraints.center(spinner, root);

        feedbackElement = new ConsoleFeedbackElement(root);
        Constraints.bind(feedbackElement, root);
        gui.onTick(this::tick);

        feedbackElement.addMessage(Component.translatable("minetogether:gui.profile.fetching_data").withStyle(ChatFormatting.GRAY));
        ProfileRequests.fetchBannedStatus(null);
        ProfileRequests.fetchNameOptions(this::randomizeName);
        ProfileRequests.fetchName(null);
        ProfileRequests.guiOpened(feedbackElement::addMessage);
    }

    private void buildNameSelectionScreen(GuiElement<?> uiRoot, int uiWidth) {
        GuiRectangle root = new GuiRectangle(uiRoot)
                .setOpaque(true)
                .setEnabled(() -> selectingName)
                .fill(0xC0000000);
        Constraints.bind(root, uiRoot);
        Constraints.bind(new GuiButton(root).onPress(() -> selectingName = false), root);

        GuiText title = new GuiText(root, Component.translatable("minetogether:gui.profile.choose_name").withStyle(ChatFormatting.UNDERLINE))
                .constrain(TOP, midPoint(root.get(TOP), root.get(BOTTOM), -40))
                .constrain(HEIGHT, literal(8))
                .constrain(WIDTH, literal(uiWidth))
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), -(uiWidth / 2D)));

        GuiButton left = MTStyle.Flat.button(root, () -> Component.literal(nameLeft))
                .onPress(() -> new ItemSelectDialog<>(root, Component.translatable("minetogether:gui.profile.select_option"), ProfileRequests.getNameOptions()).setCloseOnOutsideClick(true).setOnItemSelected(s -> nameLeft = s));
        Constraints.size(left, ((uiWidth - 50) / 2D) - 0.5, 14);
        Constraints.placeInside(left, title, Constraints.LayoutPos.BOTTOM_LEFT, 0, 20);

        GuiButton right = MTStyle.Flat.button(root, () -> Component.literal(nameRight))
                .onPress(() -> new ItemSelectDialog<>(root, Component.translatable("minetogether:gui.profile.select_option"), ProfileRequests.getNameOptions()).setCloseOnOutsideClick(true).setOnItemSelected(s -> nameRight = s));
        Constraints.size(right, ((uiWidth - 50) / 2D) - 0.5, 14);
        Constraints.placeOutside(right, left, Constraints.LayoutPos.MIDDLE_RIGHT, 1, 0);

        GuiButton save = MTStyle.Flat.buttonPrimary(root, Component.translatable("minetogether:gui.profile.button.save"))
                .onPress(() -> {
                    ProfileRequests.setName(nameLeft + nameRight, this::updateName);
                    selectingName = false;
                });
        Constraints.size(save, uiWidth - (right.xMax() - left.xMin()), 14);
        Constraints.placeOutside(save, right, Constraints.LayoutPos.MIDDLE_RIGHT, 1, 0);

        GuiButton randomize = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.profile.button.randomize"))
                .onPress(this::randomizeName);
        Constraints.size(randomize, (uiWidth / 2D) - 0.5, 14);
        Constraints.placeInside(randomize, left, Constraints.LayoutPos.BOTTOM_LEFT, 0, 40);

        GuiButton cancel = MTStyle.Flat.buttonCaution(root, Component.translatable("minetogether:gui.button.cancel"));
        Constraints.size(cancel, (uiWidth / 2D) - 0.5, 14);
        Constraints.placeInside(cancel, save, Constraints.LayoutPos.BOTTOM_RIGHT, 0, 40);
    }

    private void buildBanScreen(GuiElement<?> uiRoot, int uiWidth) {
        GuiRectangle root = new GuiRectangle(uiRoot)
                .setOpaque(true)
                .fill(0xFF000000)
                .setEnabled(ProfileRequests::isBanned);
        Constraints.bind(root, uiRoot);

        GuiText youAreBanned = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.you_are_banned").withStyle(ChatFormatting.RED))
                .constrain(WIDTH, literal(uiWidth))
                .setWrap(true)
                .autoHeight();
        Constraints.placeInside(youAreBanned, root, Constraints.LayoutPos.TOP_CENTER, 0, 20);

        GuiText currentBan = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.current_ban").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE))
                .constrain(WIDTH, literal(uiWidth))
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(currentBan, youAreBanned, Constraints.LayoutPos.BOTTOM_CENTER, 0, 10);

        GuiText banId = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.ban_id", ProfileRequests.getBanId().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.YELLOW))
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(banId, currentBan, Constraints.LayoutPos.BOTTOM_CENTER, 0, 3);

        GuiText moderator = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.moderator", ProfileRequests.getModerator().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.YELLOW))
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(moderator, banId, Constraints.LayoutPos.BOTTOM_CENTER, 0, 1);

        GuiText reason = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.reason", ProfileRequests.getReason().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.YELLOW))
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(reason, moderator, Constraints.LayoutPos.BOTTOM_CENTER, 0, 1);

        GuiText timeStamp = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.timestamp", ProfileRequests.getTimestamp().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.YELLOW))
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(timeStamp, reason, Constraints.LayoutPos.BOTTOM_CENTER, 0, 1);

        GuiText appealStatus = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.appeal_status", ProfileRequests.getAppealStatus().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.BLUE))
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(appealStatus, timeStamp, Constraints.LayoutPos.BOTTOM_CENTER, 0, 1);

        GuiText appealNotes = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.appeal_notes").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE))
                .setEnabled(() -> !ProfileRequests.getNotes().isEmpty())
                .setTooltip(ProfileRequests::getNotesTooltip)
                .setTooltipDelay(0)
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(appealNotes, appealStatus, Constraints.LayoutPos.BOTTOM_CENTER, 0, 2);

        GuiText nextAppeal = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.next_appeal", ProfileRequests.getNextAppealComp().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.YELLOW))
                .setEnabled(() -> ProfileRequests.getNextAppeal() != -1)
                .constrain(WIDTH, literal(uiWidth))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(nextAppeal, appealNotes, Constraints.LayoutPos.BOTTOM_CENTER, 0, 2);

        GuiText submitAppeal = new GuiText(root, () -> Component.translatable("minetogether:gui.profile.ban.submit_an_appeal").withStyle(ChatFormatting.BLUE))
                .constrain(WIDTH, literal(uiWidth))
                .setWrap(true)
                .autoHeight();
        Constraints.placeOutside(submitAppeal, nextAppeal, Constraints.LayoutPos.BOTTOM_CENTER, 0, 15);

        var appealText = GuiTextField.create(root, 0xA0202020, 0xA0202020, 0xe0e0e0);
        appealText.container
                .constrain(WIDTH, literal(uiWidth))
                .constrain(HEIGHT, literal(14));
        appealText.primary
                .setMaxLength(140)
                .setSuggestion(Component.translatable("minetogether:gui.profile.ban.submit_an_appeal_hint"));
        Constraints.placeOutside(appealText.container, submitAppeal, Constraints.LayoutPos.BOTTOM_CENTER, 0, 2);

        submitButton = MTStyle.Flat.buttonPrimary(root, Component.translatable("minetogether:gui.profile.button.submit"))
                .onPress(() -> ProfileRequests.submitAppeal(appealText.primary.getValue(), (success, message) -> onAppealSubmitted(root.getModularGui(), success, message)))
                .constrain(WIDTH, literal(150))
                .constrain(HEIGHT, literal(16));
        Constraints.placeOutside(submitButton, appealText.container, Constraints.LayoutPos.BOTTOM_CENTER, 0, 2);

        GuiButton back = MTStyle.Flat.button(root, Component.translatable("minetogether:gui.button.back"))
                .onPress(() -> root.mc().setScreen(root.getModularGui().getParentScreen()))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -30))
                .constrain(LEFT, midPoint(root.get(LEFT), root.get(RIGHT), -150 / 2D))
                .constrain(WIDTH, literal(150))
                .constrain(HEIGHT, literal(16));
    }

    private void onAppealSubmitted(ModularGui gui, boolean success, Component message) {
        if (success) submitButton.setEnabled(false);
        GuiDialog.infoDialog(gui.getRoot(), null, message, 250, () -> {
                    if (success) gui.getScreen().onClose();
                })
                .setBlockMouseInput(true);
    }

    private void randomizeName() {
        List<String> nams = ProfileRequests.getNameOptions();
        if (nams.isEmpty()) return;
        nameLeft = nams.get(RANDOM.nextInt(nams.size()));
        nameRight = nams.get(RANDOM.nextInt(nams.size()));
    }

    private void handleNameChange(boolean premium, GuiTextField nameField) {
        if (!premium) {
            selectingName = true;
        } else {
            ProfileRequests.setName(nameField.getValue(), null);
        }
    }

    private String trimHash(String displayName) {
        return displayName.contains("#") ? displayName.substring(0, displayName.lastIndexOf("#")) : displayName;
    }

    private void updateName() {
        String custom = ProfileRequests.getCustomName();
        if (custom != null && !custom.isEmpty()) {
            nameState.setText(custom);
        } else {
            Profile profile = MineTogetherChat.CHAT_STATE.profileManager.getOwnProfile();
            nameState.setText(trimHash(profile.getDisplayName()));
        }
    }

    private void tick() {
        ProfileRequests.updateRequests();
    }

    public static class Screen extends ModularGuiScreen {
        public Screen(net.minecraft.client.gui.screens.Screen parent) {
            super(new ProfileGui(), parent);
        }
    }
}
