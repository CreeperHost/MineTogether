//package net.creeperhost.minetogethergui.lists;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.AbstractSelectionList;
//
//public class ScreenListEntry extends AbstractSelectionList.Entry
//{
//    protected final Minecraft mc;
//    protected final ScreenList list;
//
//    public ScreenListEntry(ScreenList list)
//    {
//        this.list = list;
//        this.mc = Minecraft.getInstance();
//    }
//
//    //Do nothing, We don't want the default render
//    @Override
//    public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {}
//
//    @Override
//    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
//    {
//        list.setSelected(this);
//        return false;
//    }
//}
