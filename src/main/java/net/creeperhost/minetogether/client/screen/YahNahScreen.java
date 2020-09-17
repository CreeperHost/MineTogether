package net.creeperhost.minetogether.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.List;

/*
 * Our own version of GuiYesNo which handles \n
 */
public class YahNahScreen extends Screen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    protected ConfirmScreen parentScreen;
    protected ITextComponent messageLine1;
    private final ITextComponent messageLine2;
    private final List<String> listLines = Lists.newArrayList();
    protected String confirmButtonText;
    protected String cancelButtonText;
    protected final BooleanConsumer booleanConsumer;
    
    @SuppressWarnings("all")
    public YahNahScreen(BooleanConsumer booleanConsumer, ITextComponent messageLine1In, ITextComponent messageLine2In, String p_i51120_4_, String p_i51120_5_)
    {
        super(messageLine1In);
        this.booleanConsumer = booleanConsumer;
        this.messageLine1 = messageLine1In;
        this.messageLine2 = messageLine2In;
        this.confirmButtonText = I18n.format("gui.yes");
        this.cancelButtonText = I18n.format("gui.no");
    }
    
    public void initGui()
    {
        this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, new StringTextComponent(this.confirmButtonText), (p_213002_1_) ->
        {
            booleanConsumer.accept(true);
        }));
        this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, new StringTextComponent(this.cancelButtonText), (p_213002_1_) ->
        {
            booleanConsumer.accept(false);
        }));
        this.listLines.clear();
//        this.listLines.addAll(this.font.listFormattedStringToWidth(this.messageLine2.getString(), this.width - 50));
//
//        List<String> tempList = Arrays.asList(
//                messageLine2.getString().replace("\\n", "\n").split("\n") // I have no idea wht I can't just regex the literal "\n" but whatever, this works. Fuck Java Regex.
//        );
//        tempList.forEach(str -> listLines.addAll(font.listFormattedStringToWidth(str, this.width - 50)));
    }
    

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        this.drawCenteredString(matrixStack, this.font, this.messageLine1.getString(), this.width / 2, 70, 16777215);
        int i = 90;
        
        for (String s : this.listLines)
        {
            this.drawCenteredString(matrixStack, this.font, s, this.width / 2, i, 16777215);
            i += this.font.FONT_HEIGHT;
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}