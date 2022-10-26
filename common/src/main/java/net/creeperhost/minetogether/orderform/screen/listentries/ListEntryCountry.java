package net.creeperhost.minetogether.orderform.screen.listentries;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.polylib.client.screen.widget.ScreenList;
import net.creeperhost.polylib.client.screen.widget.ScreenListEntry;

public class ListEntryCountry extends ScreenListEntry {

    public final String countryID;
    public final String countryName;

    public ListEntryCountry(ScreenList list, String countryID, String countryName) {
        super(list);
        this.countryID = countryID;
        this.countryName = countryName;
    }

    @Override
    public void render(PoseStack poseStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float p_render_9_) {
        this.mc.font.draw(poseStack, this.countryName, x + 5, y + 5, 16777215);
    }

    public String getCountryID() {
        return countryID;
    }

    public String getCountryName() {
        return countryName;
    }
}