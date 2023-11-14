package net.creeperhost.minetogether.orderform.screen;

import net.creeperhost.minetogether.orderform.data.Order;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public abstract class OrderServerScreen extends Screen {

    private static final int STEP_AMOUNT = 5;
    protected final int stepId;
    protected final Order order;
    protected Button buttonPrev;
    protected Button buttonNext;
    protected Button buttonCancel;
    private Screen parent;

    public OrderServerScreen(int stepId, Screen parent, Order order) {
        super(Component.translatable("minetogether.screen.order"));
        this.stepId = stepId;
        this.order = order;
        this.parent = parent;
    }

    public OrderServerScreen(int stepId, Order order) {
        super(Component.translatable("minetogether.screen.order"));
        this.stepId = stepId;
        this.order = order;
    }

    @Override
    public void init() {
        clearWidgets();

        super.init();
        addNavigationButtons();
    }

    public void addNavigationButtons() {
        addRenderableWidget(this.buttonPrev = Button.builder(Component.translatable("minetogether.button.prev"), (button) -> this.minecraft.setScreen(getByStep(this.stepId - 1, this.order, parent)))
                .bounds(10, this.height - 30, 80, 20)
                .build()
        );

        addRenderableWidget(this.buttonCancel = Button.builder(Component.translatable("minetogether.button.cancel"), (button) -> cancelOrder())
                .bounds(this.width / 2 - 40, this.height - 30, 80, 20)
                .build()
        );
        buttonCancel.visible = stepId != 4;

        addRenderableWidget(this.buttonNext = Button.builder(Component.translatable("minetogether.button.next"), (button) ->
                        {
                            if ((this.stepId + 1) == STEP_AMOUNT) {
                                this.minecraft.setScreen(parent);
                            } else {
                                this.minecraft.setScreen(getByStep(this.stepId + 1, this.order, parent));
                            }
                        })
                        .bounds(this.width - 90, this.height - 30, 80, 20)
                        .build()
        );

        this.buttonPrev.active = this.stepId > 0;
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        graphics.drawCenteredString(minecraft.font, "Step " + (this.stepId + 1 + " / ") + STEP_AMOUNT, this.width - 30, 10, -1);
        graphics.drawCenteredString(minecraft.font, this.getStepName(), this.width / 2, 10, -1);
        super.render(graphics, i, j, f);
    }

    @SuppressWarnings("Duplicates")
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
