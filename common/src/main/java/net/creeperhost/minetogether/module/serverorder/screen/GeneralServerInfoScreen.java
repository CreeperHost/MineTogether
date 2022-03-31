package net.creeperhost.minetogether.module.serverorder.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.creeperhost.minetogether.lib.Order;
import net.creeperhost.minetogether.lib.serverorder.AvailableResult;
import net.creeperhost.minetogether.lib.serverorder.ServerOrderCallbacks;
import net.creeperhost.polylib.client.screen.widget.TextFieldValidate;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class GeneralServerInfoScreen extends OrderServerScreen
{
    private static final ResourceLocation lockIcon = new ResourceLocation(Constants.MOD_ID, "textures/lock.png");
    private static final Random random = new Random();
    private String message = "Name can not be blank";
    private EditBox nameField;
    private boolean isAcceptable = false;
    private boolean nameChecked = false;
    private long lastKeyTyped;
    private Screen parent;

    public GeneralServerInfoScreen(int stepId, Order order, Screen parent)
    {
        super(stepId, order);
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        int halfWidth = this.width / 2;
        int halfHeight = this.height / 2;
        int checkboxWidth = font.width(I18n.get("minetogether.screen.generalinfo.pregen")) + 13;

        addRenderableWidget(this.nameField = new TextFieldValidate(this.font, halfWidth - 100, halfHeight - 50, 200, 20, "([A-Za-z0-9]*)", ""));
        addRenderableWidget(new Checkbox(halfWidth - (checkboxWidth / 2), halfHeight - 8, 20, 20, new TranslatableComponent("minetogether.screen.generalinfo.pregen"), order.pregen));

        this.nameField.setMaxLength(16);
        this.nameField.setValue(this.order.name.isEmpty() ? getDefaultName() : this.order.name);
        this.order.name = this.nameField.getValue().trim();

        addButtons();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        fill(poseStack, 0, this.height - 20, width, 20, 0x99000000);

        drawCenteredString(poseStack, this.font, I18n.get("minetogether.info.server_name"), this.width / 2, this.height / 2 - 65, -1);

        RenderSystem.setShaderTexture(0, lockIcon);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(poseStack, (this.width / 2) - 8, (this.height / 2) + 40, 0.0F, 0.0F, 16, 16, 16, 16);

        int strStart = 61;

        drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.screen.generalinfo.secure.line1"), this.width / 2, (this.height / 2) + strStart, 0xFFFFFF);
        drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.screen.generalinfo.secure.line2"), this.width / 2, (this.height / 2) + strStart + 10, 0xFFFFFF);
        drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.screen.generalinfo.secure.line3"), this.width / 2, (this.height / 2) + strStart + 20, 0xFFFFFF);

        this.nameField.render(poseStack, i, j, f);

        int colour;

        if (nameChecked && isAcceptable)
        {
            colour = 0x00FF00;
        }
        else
        {
            colour = 0xFF0000;
        }
        drawCenteredString(poseStack, font, message, (this.width / 2), (this.height / 2) - 26, colour);

        super.render(poseStack, i, j, f);
    }

    @Override
    public void tick()
    {
        super.tick();
        validateName();
    }

    public void validateName()
    {
        final String nameToCheck = this.nameField.getValue().trim();
        boolean isEmpty = nameToCheck.isEmpty();

        if (lastKeyTyped + 400 < System.currentTimeMillis() && !nameChecked)
        {
            nameChecked = true;
            if (isEmpty)
            {
                message = "Name cannot be blank";
                isAcceptable = false;
            }
            else
            {

                CompletableFuture.runAsync(() ->
                {
                    AvailableResult result = ServerOrderCallbacks.getNameAvailable(nameToCheck);
                    isAcceptable = result.getSuccess();
                    message = result.getMessage();
                });
            }
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode)
    {
        String nameFieldOldValue = nameField.getValue();
        if (!this.nameField.charTyped(typedChar, keyCode))
        {
            super.charTyped(typedChar, keyCode);
        }
        else
        {
            if (!nameFieldOldValue.equals(nameField.getValue()))
            {
                nameChecked = false;
                message = "Name not yet checked";
                this.order.name = this.nameField.getValue().trim();
                lastKeyTyped = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }

    public static String getDefaultName()
    {
        String[] nm1 = {"amber", "angel", "spirit", "basin", "lagoon", "basin", "arrow", "autumn", "bare", "bay", "beach", "bear", "bell", "black", "bleak", "blind", "bone", "boulder", "bridge", "brine", "brittle", "bronze", "castle", "cave", "chill", "clay", "clear", "cliff", "cloud", "cold", "crag", "crow", "crystal", "curse", "dark", "dawn", "dead", "deep", "deer", "demon", "dew", "dim", "dire", "dirt", "dog", "dragon", "dry", "dusk", "dust", "eagle", "earth", "east", "ebon", "edge", "elder", "ember", "ever", "fair", "fall", "false", "far", "fay", "fear", "flame", "flat", "frey", "frost", "ghost", "glimmer", "gloom", "gold", "grass", "gray", "green", "grim", "grime", "hazel", "heart", "high", "hollow", "honey", "hound", "ice", "iron", "kil", "knight", "lake", "last", "light", "lime", "little", "lost", "mad", "mage", "maple", "mid", "might", "mill", "mist", "moon", "moss", "mud", "mute", "myth", "never", "new", "night", "north", "oaken", "ocean", "old", "ox", "pearl", "pine", "pond", "pure", "quick", "rage", "raven", "red", "rime", "river", "rock", "rogue", "rose", "rust", "salt", "sand", "scorch", "shade", "shadow", "shimmer", "shroud", "silent", "silk", "silver", "sleek", "sleet", "sly", "small", "smooth", "snake", "snow", "south", "spring", "stag", "star", "steam", "steel", "steep", "still", "stone", "storm", "summer", "sun", "swamp", "swan", "swift", "thorn", "timber", "trade", "west", "whale", "whit", "white", "wild", "wilde", "wind", "winter", "wolf"};
        String[] nm2 = {"acre", "band", "barrow", "bay", "bell", "born", "borough", "bourne", "breach", "break", "brook", "burgh", "burn", "bury", "cairn", "call", "chill", "cliff", "coast", "crest", "cross", "dale", "denn", "drift", "fair", "fall", "falls", "fell", "field", "ford", "forest", "fort", "front", "frost", "garde", "gate", "glen", "grasp", "grave", "grove", "guard", "gulch", "gulf", "hall", "hallow", "ham", "hand", "harbor", "haven", "helm", "hill", "hold", "holde", "hollow", "horn", "host", "keep", "land", "light", "maw", "meadow", "mere", "mire", "mond", "moor", "more", "mount", "mouth", "pass", "peak", "point", "pond", "port", "post", "reach", "rest", "rock", "run", "scar", "shade", "shear", "shell", "shield", "shore", "shire", "side", "spell", "spire", "stall", "wich", "minster", "star", "storm", "strand", "summit", "tide", "town", "vale", "valley", "vault", "vein", "view", "ville", "wall", "wallow", "ward", "watch", "water", "well", "wharf", "wick", "wind", "wood", "yard"};

        int rnd = random.nextInt(nm1.length);
        int rnd2 = random.nextInt(nm2.length);
        while (nm1[rnd] == nm2[rnd2])
        {
            rnd2 = random.nextInt(nm2.length);
        }
        return nm1[rnd] + nm2[rnd2] + random.nextInt(999);
    }

    public void addButtons()
    {

    }

    @Override
    public String getStepName()
    {
        return I18n.get("minetogether.order.screen.generalinfo");
    }
}
