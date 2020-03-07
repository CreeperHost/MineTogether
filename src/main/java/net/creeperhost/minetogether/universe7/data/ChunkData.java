package net.creeperhost.minetogether.universe7.data;

import net.minecraft.world.chunk.Chunk;

public class ChunkData
{
    int chunkX;
    int chunkZ;

    public ChunkData(Chunk chunk)
    {
        this.chunkX = chunk.getPos().x;
        this.chunkZ = chunk.getPos().z;
    }
}
