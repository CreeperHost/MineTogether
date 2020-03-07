package net.creeperhost.minetogether.universe7;

import net.minecraft.entity.player.PlayerEntity;
import java.util.UUID;

public class EntityData
{
    String NAME;
    UUID UUID;

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
