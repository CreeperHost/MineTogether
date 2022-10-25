package net.creeperhost.minetogether.server;

import net.creeperhost.minetogether.MineTogetherServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PregenTask {

    public boolean preventJoin = true;
    public ResourceKey<Level> dimension;
    public transient List<Pair<Integer, Integer>> chunksToGen;
    public int chunksPerTick;
    public int storedCurX;
    public int storedCurZ;
    public int minX;
    public int maxX;
    public int minZ;
    public int maxZ;
    public int diameterX = 0;
    public int diameterZ = 0;
    public long startTime = 0;
    public transient long lastCheckedTime = -9001;
    public int chunksDone = 0;
    public int totalChunks = 0;
    public transient int lastChunksDone = 0;
    public transient String lastPregenString = "No status yet!";
    public transient int chunkLoadCount;
    public transient int curChunksPerTick;

    public PregenTask(ResourceKey<Level> dimension, int minX, int maxX, int minZ, int maxZ, int chunksPerTick, boolean preventJoin) {
        this.dimension = dimension;
        this.chunksPerTick = chunksPerTick;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.storedCurX = minX;
        this.storedCurZ = minZ;
        this.preventJoin = preventJoin;

        init();
    }

    @SuppressWarnings ("Duplicates")
    public void init() {
        startTime = 0;
        if (chunksToGen != null) return;

        ServerLevel world = MineTogetherServer.minecraftServer.getLevel(dimension);
        if (diameterX > 0 && totalChunks == 0) // only the first time
        {
            BlockPos pos = world.getSharedSpawnPos();
            minX = (pos.getX() << 4) - (diameterX / 2);
            maxX = (pos.getX() << 4) + (diameterX / 2);

            minZ = (pos.getZ() << 4) - (diameterZ / 2);
            maxZ = (pos.getZ() << 4) + (diameterZ / 2);
            storedCurX = minX;
            storedCurZ = minZ;
        }

        chunksDone = 0;
        totalChunks = 0;
        chunkLoadCount = world.getChunkSource().getLoadedChunksCount();

        List<Pair<Integer, Integer>> chunks = new ArrayList<>();

        for (int curX = minX; curX <= maxX; curX++) {
            if (curX < storedCurX) { continue; }
            for (int curZ = minZ; curZ <= maxZ; curZ++) {
                if (curX == storedCurX && curZ <= storedCurZ) continue;

                chunks.add(Pair.of(curX, curZ));
                totalChunks++;
            }
        }
        chunksToGen = chunks;
        curChunksPerTick = chunksPerTick;
    }
}
