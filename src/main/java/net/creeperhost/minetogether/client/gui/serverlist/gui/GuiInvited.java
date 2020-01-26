package net.creeperhost.minetogether.client.gui.serverlist.gui;

import net.creeperhost.minetogether.client.gui.serverlist.data.Invite;
import net.creeperhost.minetogether.client.gui.serverlist.gui.elements.ServerListEntryPublic;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.util.Util;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiInvited extends Screen
{
    private final Invite invite;
    private final ServerListEntryPublic server;
    private final Screen parent;
    private final boolean canConnect;
    private Button connectButton;
    private Button cancelButton;
    private GuiCheckBox checkBox;
    private boolean addToServerList = true;
    
    public GuiInvited(Invite invite, Screen parent)
    {
        super(new StringTextComponent(""));
        this.invite = invite;
        this.parent = parent;
        server = null;//new ServerListEntryPublic(new MockServerListEntryNormal(new ServerDataPublic(invite.server)));
        canConnect = invite.project == Integer.valueOf(Config.getInstance().curseProjectID);
    }
    
    @SuppressWarnings("Duplicates")
    @Override
    public void init()
    {
        int yBase = this.height / 2 - (106 / 2);
        addButton(connectButton = new Button(width / 2 - 40, this.height - 30, 80, 20, I18n.format("creeperhost.multiplayer.connect"), p ->
        {
            if (addToServerList)
            {
                ServerList savedServerList = new ServerList(this.minecraft);
                savedServerList.loadServerList();
                savedServerList.addServerData(server.getServerData());
                savedServerList.saveServerList();
            }
            if (minecraft.world != null)
            {
                this.minecraft.world.sendQuittingDisconnectingPacket();
                this.minecraft.loadWorld(null);
            }
//            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(null, server.getServerData());
        }));
        
        connectButton.active = canConnect;
        
        addButton(cancelButton = new Button(width - 100, this.height - 30, 80, 20, I18n.format("creeperhost.multiplayer.cancel"), p ->
        {
            ServerList savedServerList = new ServerList(this.minecraft);
            savedServerList.loadServerList();
            savedServerList.addServerData(server.getServerData());
            savedServerList.saveServerList();
            minecraft.displayGuiScreen(parent);
        }));
        
        String checkText = I18n.format("creeperhost.multiplayer.addlist");
        int checkWidth = 11 + 2 + font.getStringWidth(checkText);
        addButton(checkBox = new GuiCheckBox((width / 2) - (checkWidth / 2), yBase + 36 + 30 + 30, checkText, addToServerList));
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderDirtBackground(0);
        int yBase = this.height / 2 - (106 / 2);
        
        this.drawCenteredString(this.font, I18n.format("creeperhost.multiplayer.invite"), this.width / 2, 10, -1);
        
        this.drawCenteredString(this.font, I18n.format("creeperhost.multiplayer.invited", invite.by), this.width / 2, yBase, -1);
        server.ourDrawEntry(0, (this.width / 2) - 125, yBase + 20, 250, 36, Integer.MAX_VALUE, Integer.MAX_VALUE, false);
        this.drawCenteredString(this.font, Util.localize(canConnect ? "multiplayer.join" : "multiplayer.cantjoin", invite.by), this.width / 2, yBase + 36 + 30, -1);
        
        super.render(mouseX, mouseY, partialTicks);
    }
}
