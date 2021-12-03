package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.lib.chat.KnownUsers;
import net.creeperhost.minetogether.lib.chat.data.Profile;
import net.creeperhost.minetogether.module.chat.screen.listentries.ListEntryMuted;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class MutedListScreen extends Screen
{
    private final Screen parent;
    private ScreenList<ListEntryMuted> list;

    private String hoveringText = null;
    private EditBox searchEntry;


    public MutedListScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.mutedscreen.title"));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        if (list == null)
        {
            list = new ScreenList(this, minecraft, width, height, 32, this.height - 64, 36);
        }
        else
        {
            list.updateSize(width, height, 32, this.height - 64);
        }

        addButtons();
        searchEntry = new EditBox(this.font, this.width / 2 - 80, this.height - 32, 160, 20, new TranslatableComponent(""));
        searchEntry.setSuggestion("Search");
        addRenderableWidget(list);
        addRenderableWidget(searchEntry);
        refreshMutedList();
    }

    public void addButtons()
    {
        addWidget(new Button(5, height - 26, 100, 20, new TranslatableComponent("Cancel"), p -> minecraft.setScreen(parent)));

        addWidget(new Button(width - 105, height - 26, 100, 20, new TranslatableComponent("minetogether.button.refresh"), p -> refreshMutedList()));

        addWidget(new Button(width - 105, 5, 100, 20, new TranslatableComponent("Friends List"), button -> minecraft.setScreen(new FriendsListScreen(parent))));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        list.render(poseStack, i, j, f);
        searchEntry.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 5, 0xFFFFFF);

        if (list.children().isEmpty())
            drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.mutedscreen.empty"), width / 2, (this.height / 2) - 20, -1);
    }

    public void setHoveringText(String hoveringText)
    {
        this.hoveringText = hoveringText;
    }

    public void refreshMutedList()
    {
        List<Profile> mutedUsers = KnownUsers.getMuted();
        list.clearList();
        if (mutedUsers != null)
        {
            for (Profile mute : mutedUsers)
            {
                ListEntryMuted mutedEntry = new ListEntryMuted(this, list, mute);
                if (searchEntry != null && !searchEntry.getValue().isEmpty())
                {
                    String s = searchEntry.getValue();
                    if (mute.getUserDisplay().toLowerCase().contains(s.toLowerCase()))
                    {
                        list.add(mutedEntry);
                    }
                }
                else
                {
                    list.add(mutedEntry);
                }
            }
        }
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if (searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(c, i);
            refreshMutedList();
            return flag;
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if (searchEntry.isFocused())
        {
            searchEntry.setSuggestion("");
            boolean flag = searchEntry.keyPressed(i, j, k);
            refreshMutedList();
            return flag;
        }
        return super.keyPressed(i, j, k);
    }
}
