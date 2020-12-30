package net.creeperhost.minetogether.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.server.pregen.PregenTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;

public class PreGenHandler
{
    public HashMap<ServerWorld, PregenTask> pregenTasks = new HashMap<>();

    public PreGenHandler()
    {
        try {
            deserializePreload(new File(getSaveFolder(), "pregenData.json"));
        } catch (Exception ignored) {}
    }

    public boolean createTask(PregenTask task)
    {
        if (pregenTasks.get(task.dimension) != null) return false;
            pregenTasks.put(task.dimension, task);
        return true;
    }

    public boolean createTask(ServerWorld dimension, int xMin, int xMax, int zMin, int zMax, int chunksPerTick, boolean preventJoin)
    {
        if (pregenTasks.get(dimension) != null) return false;
        pregenTasks.put(dimension, new PregenTask(dimension, xMin, xMax, zMin, zMax, chunksPerTick, preventJoin));
        return true;
    }

    public void clear()
    {
        pregenTasks.clear();
    }

    private void deserializePreload(File file)
    {
        if(!file.exists())
        {
//            MineTogether.logger.error("File does not exist");
            return;
        }

        Gson gson = new GsonBuilder().create();
        HashMap output = null;
        Type listOfPregenTask = new TypeToken<HashMap<ServerWorld, PregenTask>>() {}.getType();
        try
        {
//            output = gson.fromJson(IOUtils.toString(file.toURI(), Charset.defaultCharset()), listOfPregenTask);
        } catch (Exception ignored) {}
        if (output == null)
            pregenTasks = new HashMap<>();
            else pregenTasks = output;

        Collection<PregenTask> tasks = pregenTasks.values();

        if(!pregenTasks.isEmpty())
        {
            for (PregenTask task : tasks)
            {
                task.init();
            }
        }
    }

    private void serializePreload(File file)
    {
        FileOutputStream pregenOut = null;
        Type listOfPregenTask = new TypeToken<HashMap<Integer, PregenTask>>() {}.getType();
        try
        {
            pregenOut = new FileOutputStream(file);
            Gson gson = new GsonBuilder().create();
            String output = gson.toJson(pregenTasks, listOfPregenTask);
            IOUtils.write(output, pregenOut, Charset.defaultCharset());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void serializePreload()
    {
//        serializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    public File getSaveFolder()
    {
        return FMLPaths.GAMEDIR.get().toFile();
    }
}
