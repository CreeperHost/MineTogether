package net.creeperhost.minetogether.module.multiplayer.data;

import net.creeperhost.minetogethergui.widgets.DropdownButton;

import java.util.Arrays;
import java.util.List;

public enum ServerListType implements DropdownButton.IDropdownOption
{
    PUBLIC, INVITE, APPLICATION;

    private static List<DropdownButton.IDropdownOption> enumCache;

    @Override
    public List<DropdownButton.IDropdownOption> getPossibleVals()
    {
        if (enumCache == null)
            enumCache = Arrays.asList(ServerListType.values());

        return enumCache;
    }

    @Override
    public String getTranslate(DropdownButton.IDropdownOption currentDO, boolean dropdownOpen)
    {
        return "minetogether.multiplayer.list." + this.name().toLowerCase();
    }
}
