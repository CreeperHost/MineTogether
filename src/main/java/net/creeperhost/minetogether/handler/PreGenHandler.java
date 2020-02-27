package net.creeperhost.minetogether.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.server.pregen.PregenTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;

public class PreGenHandler
{
    public HashMap<DimensionType, PregenTask> pregenTasks = new HashMap<DimensionType, PregenTask>();

    public PreGenHandler()
    {
        deserializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    public boolean createTask(PregenTask task)
    {
        if (pregenTasks.get(task.dimension) != null)
            return false;

        pregenTasks.put(task.dimension, task);
        return true;
    }

    public boolean createTask(DimensionType dimension, int xMin, int xMax, int zMin, int zMax, int chunksPerTick, boolean preventJoin)
    {
        if (pregenTasks.get(dimension) != null)
            return false;

        pregenTasks.put(dimension, new PregenTask(dimension, xMin, xMax, zMin, zMax, chunksPerTick, preventJoin));

        return true;
    }

    public void clear()
    {
        pregenTasks.clear();
    }

    private void deserializePreload(File file)
    {
        Gson gson = new GsonBuilder().create();
        HashMap output = null;
        Type listOfPregenTask = new TypeToken<HashMap<DimensionType, PregenTask>>() {}.getType();
        try
        {
            output = gson.fromJson(IOUtils.toString(file.toURI()), listOfPregenTask);
        } catch (Exception ignored)
        {
        }
        if (output == null)
            pregenTasks = new HashMap<>();
        else
            pregenTasks = output;

        Collection<PregenTask> tasks = pregenTasks.values();

        for (PregenTask task : tasks)
        {
            task.init();
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
        serializePreload(new File(getSaveFolder(), "pregenData.json"));
    }

    public File getSaveFolder()
    {
        MinecraftServer server = MineTogether.server;
        if (server != null && !server.isSinglePlayer())
            return server.getFile(".");
        return null;
    }
}
