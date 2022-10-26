package net.creeperhost.minetogether.orderform.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.orderform.data.Order;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class OrderServerScreen extends Screen {

    private static final int STEP_AMOUNT = 5;
    protected final int stepId;
    protected final Order order;
    protected Button buttonPrev;
    protected Button buttonNext;
    protected Button buttonCancel;
    private Screen parent;

    public OrderServerScreen(int stepId, Screen parent, Order order) {
        super(new TranslatableComponent("minetogether.screen.orderscreen"));
        this.stepId = stepId;
        this.order = order;
        this.parent = parent;
    }

    public OrderServerScreen(int stepId, Order order) {
        super(new TranslatableComponent("minetogether.screen.orderscreen"));
        this.stepId = stepId;
        this.order = order;
    }

    @Override
    public void init() {
        clearWidgets();

        super.init();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        addNavigationButtons();
    }

    public void addNavigationButtons() {
        addRenderableWidget(this.buttonPrev = new Button(10, this.height - 30, 80, 20, new TranslatableComponent("minetogether.button.prev"), (button) -> this.minecraft.setScreen(getByStep(this.stepId - 1, this.order, parent))));

        if (stepId != 4) {
            addRenderableWidget(this.buttonCancel = new Button(this.width / 2 - 40, this.height - 30, 80, 20, new TranslatableComponent("minetogether.button.cancel"), (button) -> cancelOrder()));
        }

        addRenderableWidget(this.buttonNext = new Button(this.width - 90, this.height - 30, 80, 20, new TranslatableComponent("minetogether.button.next"), (button) ->
        {
            if ((this.stepId + 1) == STEP_AMOUNT) {
                this.minecraft.setScreen(parent);
            } else {
                this.minecraft.setScreen(getByStep(this.stepId + 1, this.order, parent));
            }
        }));

        this.buttonPrev.active = this.stepId > 0;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        drawCenteredString(poseStack, minecraft.font, "Step " + (this.stepId + 1 + " / ") + STEP_AMOUNT, this.width - 30, 10, -1);
        drawCenteredString(poseStack, minecraft.font, this.getStepName(), this.width / 2, 10, -1);
        super.render(poseStack, i, j, f);
    }

    @SuppressWarnings ("Duplicates")
    public static Screen getByStep(int step, Order order, Screen parent) {
        switch (step) {
            case 0:
            default:
                return new GeneralServerInfoScreen(0, order, parent);
            case 1:
                return new MapScreen(1, order);
            case 2:
                return new PersonalDetailsScreen(2, order);
            case 3:
                return new QuoteScreen(3, order);
            case 4:
                return new OrderDetailsScreen(4, order);
        }
    }

    public abstract String getStepName();

    public void cancelOrder() {
        this.minecraft.setScreen(null);
    }
}