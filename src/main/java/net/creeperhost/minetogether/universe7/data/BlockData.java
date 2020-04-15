package net.creeperhost.minetogether.universe7.data;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

public class BlockData
{
    public ResourceLocation RESOURCE_LOCATION;
    public BlockState BLOCK_STATE;
    public Object[] PROPERTIES;
    
    public BlockData(Block block)
    {
        this.RESOURCE_LOCATION = block.getRegistryName();
        this.BLOCK_STATE = block.getStateContainer().getBaseState();
        this.PROPERTIES = block.getStateContainer().getProperties().toArray();
    }
    
    public BlockData(ResourceLocation resourceLocation, BlockState blockState, Object[] properties)
    {
        this.RESOURCE_LOCATION = resourceLocation;
        this.BLOCK_STATE = blockState;
        this.PROPERTIES = properties;
    }
}
