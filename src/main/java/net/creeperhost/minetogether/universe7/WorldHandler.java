package net.creeperhost.minetogether.universe7;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.FileUtil;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldHandler
{
    private Minecraft minecraft = Minecraft.getInstance();
    private String worldName = "universe7";
    private String seed = worldName;
    private CompoundNBT chunkProviderSettingsJson = new CompoundNBT();
    private WorldType type = WorldType.WORLD_TYPES[0];
    private String saveDirName;
    
    public void createWorld()
    {
        this.minecraft.displayGuiScreen((Screen) null);
        calcSaveDirName();
        long hash = 637265657;//Long.parseLong(seed);
        
        WorldSettings worldsettings = new WorldSettings(hash, GameType.SURVIVAL, true, false, type);
        worldsettings.setGeneratorOptions(Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, this.chunkProviderSettingsJson));
        //TODO this is only here for testing
        worldsettings.enableCommands();
        
        this.minecraft.launchIntegratedServer(this.saveDirName, worldName, worldsettings);
    }
    
    private void calcSaveDirName()
    {
        this.saveDirName = worldName;
        
        if (this.saveDirName.isEmpty())
        {
            this.saveDirName = "World";
        }
        
        try
        {
            this.saveDirName = FileUtil.func_214992_a(this.minecraft.getSaveLoader().getSavesDir(), this.saveDirName, "");
        } catch (Exception var4)
        {
            this.saveDirName = "World";
            
            try
            {
                this.saveDirName = FileUtil.func_214992_a(this.minecraft.getSaveLoader().getSavesDir(), this.saveDirName, "");
            } catch (Exception exception)
            {
                throw new RuntimeException("Could not create save folder", exception);
            }
        }
    }
}
