package net.creeperhost.minetogether.gui.serverlist;

import net.creeperhost.minetogether.Util;
import net.creeperhost.minetogether.common.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.io.IOException;

public class GuiInvited extends GuiScreen
{
    private final Invite invite;
    private final ServerListEntryNormal server;
    private final GuiScreen parent;
    private final boolean canConnect;
    private GuiButton connectButton;
    private GuiButton cancelButton;
    private GuiCheckBox checkBox;
    private boolean addToServerList = true;

    public GuiInvited(Invite invite, GuiScreen parent)
    {
        this.invite = invite;
        this.parent = parent;
        server = new MockServerListEntryNormal(new ServerData(invite.name, invite.ip + ":" + invite.port, false));
        canConnect = invite.project == Integer.valueOf(Config.getInstance().curseProjectID);
    }

    @Override
    public void initGui()
    {
        int yBase = this.height / 2 - (106 / 2);
        connectButton = new GuiButton(0, width / 2 - 40, this.height - 30, 80, 20, Util.localize("multiplayer.connect"));
        if (canConnect)
        {
            buttonList.add(connectButton);
        }
        cancelButton = new GuiButton(1, width - 100, this.height - 30, 80, 20, Util.localize("multiplayer.cancel"));
        buttonList.add(cancelButton);
        String checkText = Util.localize("multiplayer.addlist");
        int checkWidth = 11 + 2 + fontRendererObj.getStringWidth(checkText);
        checkBox = new GuiCheckBox(2, (width / 2) - (checkWidth / 2), yBase + 36 + 30 + 30, checkText, addToServerList);
        buttonList.add(checkBox);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        if (button.id == cancelButton.id)
        {
            ServerList savedServerList = new ServerList(this.mc);
            savedServerList.loadServerList();
            savedServerList.addServerData(server.getServerData());
            savedServerList.saveServerList();
            mc.displayGuiScreen(parent);
        } else if (button.id == connectButton.id)
        {
            if (addToServerList)
            {
                ServerList savedServerList = new ServerList(this.mc);
                savedServerList.loadServerList();
                savedServerList.addServerData(server.getServerData());
                savedServerList.saveServerList();
            }
            if (mc.world != null)
            {
                this.mc.world.sendQuittingDisconnectingPacket();
                this.mc.loadWorld(null);
            }
            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(null, server.getServerData());
        } else if (button.id == checkBox.id)
        {
            addToServerList = checkBox.isChecked();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground(0);
        int yBase = this.height / 2 - (106 / 2);

        this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.invite"), this.width/2, 10, -1);

        this.drawCenteredString(this.fontRendererObj, Util.localize("multiplayer.invited", invite.by), this.width/2, yBase, -1);
        Util.getWrapper().draw(server, 0, (this.width / 2) - 125, yBase + 20, 250, 36, Integer.MAX_VALUE, Integer.MAX_VALUE,false);
        this.drawCenteredString(this.fontRendererObj, Util.localize(canConnect ? "multiplayer.join" : "multiplayer.cantjoin", invite.by), this.width/2, yBase + 36 + 30, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);

    }
}
