package net.creeperhost.minetogether.common;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

import static net.creeperhost.minetogether.common.GDPR.identity;

public class IngameChat
{
    private final File ingameChatFile;
    private boolean disabledIngameChat;

    public IngameChat(File ingameChatFile)
    {
        this.ingameChatFile = ingameChatFile;
        this.disabledIngameChat = this.loadingameChatFile(ingameChatFile);
    }

    private boolean loadingameChatFile(File inFile)
    {
        FileInputStream fileinputstream = null;
        boolean flag = false;

        try
        {
            String hardwareAddress = identity();
            Properties properties = new Properties();
            fileinputstream = new FileInputStream(inFile);
            properties.load(fileinputstream);
            flag = properties.getProperty("ingameChat", "false").equals(hardwareAddress);
        }
        catch (Exception var8)
        {
            this.createingameChatFile();
        }
        finally
        {
            IOUtils.closeQuietly((InputStream)fileinputstream);
        }

        return flag;
    }

    public boolean hasDisabledIngameChat()
    {
        return this.disabledIngameChat;
    }

    public void setDisabledIngameChat(boolean state)
    {
        FileOutputStream fileoutputstream = null;
        try
        {
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.ingameChatFile);
            properties.setProperty("ingameChat", state ? identity() : "false");
            properties.store(fileoutputstream, "");
            disabledIngameChat = state;
        }
        catch (Exception exception)
        {
        }
        finally
        {
            IOUtils.closeQuietly((OutputStream)fileoutputstream);
        }
    }

    public void setDisabledIngameChat()
    {
        setDisabledIngameChat(true);
    }

    public void createingameChatFile()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            ingameChatFile.getParentFile().mkdirs();
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.ingameChatFile);
            properties.setProperty("ingameChat", "false");
            properties.store(fileoutputstream, "By changing the setting below to your mac address you are indicating your agreement to CreeperHost's privacy policy (https://www.creeperhost.net/privacy) and Terms of Service (https://www.creeperhost.net/tos).");
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