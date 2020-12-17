package net.creeperhost.minetogether.client.screen.chat.ingame;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.ISuggestionProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 13/6/20.
 */
public class CommandSuggestionHelperMT extends CommandSuggestionHelper {

    public CommandSuggestionHelperMT(Minecraft p_i225919_1_, Screen p_i225919_2_, TextFieldWidget p_i225919_3_, FontRenderer p_i225919_4_, boolean p_i225919_5_, boolean p_i225919_6_, int p_i225919_7_, int p_i225919_8_, boolean p_i225919_9_, int p_i225919_10_) {
        super(p_i225919_1_, p_i225919_2_, p_i225919_3_, p_i225919_4_, p_i225919_5_, p_i225919_6_, p_i225919_7_, p_i225919_8_, p_i225919_9_, p_i225919_10_);
    }

    @Override
    public void init() {
        super.init();
        String text = inputField.getText();
        int pos = inputField.getCursorPosition();
        String toCursor = text.substring(0, pos);
        int end = getLastWhitespace(toCursor);
        try
        {
            List<String> users = ChatHandler.getOnlineUsers().stream()//
                    .filter(name -> ChatHandler.knownUsers.findByDisplay(name) != null || ChatHandler.friends.containsKey(name))//
                    .map(MineTogether.instance::getNameForUser).collect(Collectors.toList());
            if(users != null && !users.isEmpty())
            {
                SuggestionsBuilder builder = new SuggestionsBuilder(toCursor, end);
                suggestionsFuture = ISuggestionProvider.suggest(users, builder);
            }
        } catch (Exception ignored) {}
    }
}
