package net.creeperhost.minetogether.universe7.data;

import net.minecraft.entity.player.PlayerEntity;
import java.util.UUID;

public class EntityData
{
    public String NAME;
    public UUID UUID;

    public EntityData(PlayerEntity playerEntity)
    {
        NAME = playerEntity.getName().toString();
        UUID = playerEntity.getUniqueID();
    }

    public EntityData(String name, UUID uuid)
    {
        this.NAME = name;
        this.UUID = uuid;
    }
}
