package net.creeperhost.minetogether.module.serverorder.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.screen.prefab.ScreenList;
import net.creeperhost.minetogetherlib.Order;
import net.creeperhost.minetogetherlib.serverorder.OrderSummary;
import net.minecraft.client.resources.language.I18n;

public class QuoteScreen extends OrderServerScreen
{
    public OrderSummary summary;
    private ScreenList list;

    public QuoteScreen(int stepId, Order order)
    {
        super(stepId, order);
    }

    @Override
    public void init()
    {
        super.init();
        addWidget(this.list = new ScreenList(this, this.minecraft, this.width, this.height, 56, this.height - 36, 36));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        fill(poseStack, 0, this.height - 20, width, 20, 0x99000000);

        super.render(poseStack, i, j, f);
    }

    @Override
    public String getStepName()
    {
        return I18n.get("minetogether.order.screen.generalinfo.quote");
    }
}
