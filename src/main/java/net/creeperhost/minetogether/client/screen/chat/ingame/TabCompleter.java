package net.creeperhost.minetogether.client.screen.chat.ingame;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class TabCompleter
{
    public final TextFieldWidget textField;
    public String[] suggestions;
    
    public TabCompleter(TextFieldWidget textFieldIn)
    {
        this.textField = textFieldIn;
    }
    
    public void complete()
    {
        if (textField.getText().startsWith("@"))
        {
            String text = textField.getText();
            String[] words = text.split(" ");
            int length = words.length;
            String lastWord = length == 0 ? "" : words[words.length - 1];
            suggestions = new String[0];
            
            suggestions = ChatHandler.getOnlineUsers().stream().filter(name -> ChatHandler.anonUsers.containsKey(name) || ChatHandler.friends.containsKey(name)).map(s -> MineTogether.instance.getNameForUser(s)).filter(nick -> nick.toLowerCase().startsWith(lastWord.toLowerCase().replaceFirst("@", ""))).toArray(String[]::new);
        }
    }
    
    public void render(int mouseX, int mouseY, float partialTicks)
    {
    
    }
}
