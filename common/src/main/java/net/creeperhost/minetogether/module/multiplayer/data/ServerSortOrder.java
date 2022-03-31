package net.creeperhost.minetogether.module.multiplayer.data;

import net.creeperhost.polylib.client.screen.widget.buttons.DropdownButton;

import java.util.Arrays;
import java.util.List;

public enum ServerSortOrder implements DropdownButton.IDropdownOption
{
    RANDOM("random"), PLAYER("player"), NAME("name"), UPTIME("uptime"), LOCATION("location"), PING("ping", true);

    public final boolean constant;

    private static List<DropdownButton.IDropdownOption> enumCache;

    public String translate;

    ServerSortOrder(String translate, boolean constant)
    {
        this.translate = translate;
        this.constant = constant;
    }

    ServerSortOrder(String translate)
    {
        this(translate, false);
    }

    @Override
    public String getTranslate(DropdownButton.IDropdownOption current, boolean dropdownOpen)
    {
        return "minetogether.multiplayer.sort." + translate;
    }

    @Override
    public List<DropdownButton.IDropdownOption> getPossibleVals()
    {
        if (enumCache == null) enumCache = Arrays.asList(ServerSortOrder.values());

        return enumCache;
    }
}
