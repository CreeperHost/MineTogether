package net.creeperhost.minetogether.common;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

public class GDPR
{
    private final File gdprFile;
    private boolean acceptedGDPR;

    public GDPR(File gdprFile)
    {
        this.gdprFile = gdprFile;
        this.acceptedGDPR = this.loadGDPRFile(gdprFile);
    }

    private boolean loadGDPRFile(File inFile)
    {
        FileInputStream fileinputstream = null;
        boolean flag = false;

        try
        {
            Properties properties = new Properties();
            fileinputstream = new FileInputStream(inFile);
            properties.load(fileinputstream);
            flag = Boolean.parseBoolean(properties.getProperty("gdpr", "false"));
        }
        catch (Exception var8)
        {
            this.createGDPRFile();
        }
        finally
        {
            IOUtils.closeQuietly((InputStream)fileinputstream);
        }

        return flag;
    }

    public boolean hasAcceptedGDPR()
    {
        return this.acceptedGDPR;
    }

    public void setAcceptedGDPR()
    {
        FileOutputStream fileoutputstream = null;
        try
        {
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.gdprFile);
            properties.setProperty("gdpr", "true");
            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to CreeperHost's privacy policy (https://www.creeperhost.net/privacy).");
            acceptedGDPR = true;
        }
        catch (Exception exception)
        {
        }
        finally
        {
            IOUtils.closeQuietly((OutputStream)fileoutputstream);
        }
    }

    public void createGDPRFile()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            gdprFile.getParentFile().mkdirs();
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.gdprFile);
            properties.setProperty("gdpr", "false");
            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to CreeperHost's privacy policy (https://www.creeperhost.net/privacy).");
        }
        catch (Exception exception)
        {
        }
        finally
        {
            IOUtils.closeQuietly((OutputStream)fileoutputstream);
        }
    }
}