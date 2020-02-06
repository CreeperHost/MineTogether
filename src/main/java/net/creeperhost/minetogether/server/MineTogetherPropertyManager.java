package net.creeperhost.minetogether.server;

import net.creeperhost.minetogether.MineTogether;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@OnlyIn(Dist.DEDICATED_SERVER)
public class MineTogetherPropertyManager
{
    private final Properties serverProperties = new Properties();
    private final File serverPropertiesFile;

    public MineTogetherPropertyManager(File propertiesFile)
    {
        this.serverPropertiesFile = propertiesFile;

        if (propertiesFile.exists())
        {
            FileInputStream fileinputstream = null;

            try
            {
                fileinputstream = new FileInputStream(propertiesFile);
                this.serverProperties.load(fileinputstream);
            } catch (Exception exception)
            {
                MineTogether.logger.warn("Failed to load {}", propertiesFile, exception);
                this.generateNewProperties();
            } finally
            {
                if (fileinputstream != null)
                {
                    try
                    {
                        fileinputstream.close();
                    } catch (IOException ignored)
                    {
                    }
                }
            }
        } else
        {
            MineTogether.logger.warn("{} does not exist", (Object) propertiesFile);
            this.generateNewProperties();
        }
    }

    /**
     * Generates a new properties file.
     */
    public void generateNewProperties()
    {
        this.saveProperties();
    }

    @SuppressWarnings("Duplicates")
    public void saveProperties()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            fileoutputstream = new FileOutputStream(this.serverPropertiesFile);
            this.serverProperties.store(fileoutputstream, "MineTogether properties");
        } catch (Exception exception)
        {
            MineTogether.logger.warn("Failed to save {}", this.serverPropertiesFile, exception);
            this.generateNewProperties();
        } finally
        {
            if (fileoutputstream != null)
            {
                try
                {
                    fileoutputstream.close();
                } catch (IOException ignored)
                {
                }
            }
        }
    }

    public String getStringProperty(String key, String defaultValue)
    {
        if (!this.serverProperties.containsKey(key))
        {
            this.serverProperties.setProperty(key, defaultValue);
            this.saveProperties();
            this.saveProperties();
        }

        return this.serverProperties.getProperty(key, defaultValue);
    }
}