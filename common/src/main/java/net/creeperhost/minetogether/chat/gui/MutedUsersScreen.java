package net.creeperhost.minetogether.chat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.profile.Profile;
import net.creeperhost.minetogether.polylib.gui.SimpleSelectionList;
import net.creeperhost.minetogether.polylib.gui.SimpleSelectionList.SimpleEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by covers1624 on 24/8/22.
 */
public class MutedUsersScreen extends Screen {

    private static final String CROSS = Character.toString(10006);

    private static final Logger LOGGER = LogManager.getLogger();

    private final Screen previous;
    private SimpleSelectionList<MutedEntry> list;
    private EditBox searchBox;

    public MutedUsersScreen(Screen previous) {
        super(Component.translatable("minetogether:screen.muted.title"));
        this.previous = previous;
    }

    @Override
    protected void init() {
        super.init();
        if (list == null) {
            list = new SimpleSelectionList<>(minecraft, width, height, 32, height - 64, 36);
        } else {
            list.updateSize(width, height, 32, height - 64);
        }
        addRenderableWidget(list);

        searchBox = new EditBox(font, width / 2 - 80, height - 32, 160, 20, Component.empty());
        searchBox.setSuggestion("Search");
        addRenderableWidget(searchBox);

        addRenderableWidget(new Button(5, height - 26, 100, 20, Component.translatable("minetogether:button.cancel"), e -> minecraft.setScreen(previous)));
        addRenderableWidget(new Button(width - 105, height - 26, 100, 20, Component.translatable("minetogether:button.refresh"), e -> refreshList()));
        if (!(previous instanceof FriendsListScreen)) {
            addRenderableWidget(new Button(width - 105, 5, 100, 20, Component.translatable("minetogether:button.friends"), e -> minecraft.setScreen(new FriendsListScreen(this))));
        }

        refreshList();
    }

    public void refreshList() {
        list.clearEntries();
        String searchTerm = searchBox.getValue();
        for (Profile mutedProfile : MineTogetherChat.CHAT_STATE.profileManager.getMutedProfiles()) {
            if (StringUtils.isEmpty(searchTerm) || StringUtils.containsAnyIgnoreCase(mutedProfile.getDisplayName(), searchTerm)) {
                list.addEntry(new MutedEntry(list, mutedProfile));
            }
        }
    }

    @Override
    public boolean charTyped(char c, int i) {
        boolean ret = super.charTyped(c, i);
        if (searchBox.isFocused()) {
            refreshList();
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        boolean ret = super.keyPressed(i, j, k);
        if (searchBox.isFocused()) {
            searchBox.setSuggestion(null);
            refreshList();
        }
        return ret;
    }

    private class MutedEntry extends SimpleEntry<MutedEntry> {

        private final Profile profile;
        private float transparency = 0.5F;

        public MutedEntry(SimpleSelectionList<MutedEntry> list, Profile profile) {
            super(list);
            this.profile = profile;
        }

        @Override
        public void render(PoseStack poseStack, int idx, int top, int left, int width, int height, int mx, int my, boolean hovered, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();
            if (hovered) {
                if (transparency <= 1.0F) transparency += 0.04;
            } else {
                if (transparency >= 0.5F) transparency -= 0.04;
            }

            mc.font.draw(poseStack, profile.getDisplayName(), left + 5, top + 5, 0xFFFFFF);

            RenderSystem.enableBlend();
            mc.font.draw(poseStack, CROSS, width + left - mc.font.width(CROSS) - 4, top, 0xFF0000 + ((int) (transparency * 254) << 24));
            RenderSystem.disableBlend();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int i) {
            Minecraft mc = Minecraft.getInstance();
            int listWidth = (list.getWidth() - list.getRowWidth()) / 2 + list.getRowWidth();
            int yTop = list.getRowTop(this);

            if (mouseX >= listWidth - mc.font.width(CROSS) - 4 && mouseX <= listWidth - 3 && mouseY - yTop >= 0 && mouseY - yTop <= 7) {
                profile.unmute();
                refreshList();
            }

            return super.mouseClicked(mouseX, mouseY, i);
        }
    }
}
