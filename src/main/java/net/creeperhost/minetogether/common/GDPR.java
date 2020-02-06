package net.creeperhost.minetogether.common;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
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
            String hardwareAddress = identity();
            Properties properties = new Properties();
            fileinputstream = new FileInputStream(inFile);
            properties.load(fileinputstream);
            flag = properties.getProperty("gdpr", "false").equals(hardwareAddress);
        } catch (Exception var8)
        {
            this.createGDPRFile();
        } finally
        {
            IOUtils.closeQuietly((InputStream) fileinputstream);
        }

        return flag;
    }

    public static String identity()
    {
        try
        {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++)
            {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            String s = sb.toString();

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());

            byte byteData[] = md.digest();

            sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++)
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

            return sb.toString();
        } catch (Exception e)
        {
        }

        return "";
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
            properties.setProperty("gdpr", identity());
            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to CreeperHost's privacy policy (https://www.creeperhost.net/privacy) and Terms of Service (https://www.creeperhost.net/tos).");
            acceptedGDPR = true;
        } catch (Exception exception)
        {
        } finally
        {
            IOUtils.closeQuietly((OutputStream) fileoutputstream);
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
            properties.store(fileoutputstream, "By changing the setting below to your mac address you are indicating your agreement to CreeperHost's privacy policy (https://www.creeperhost.net/privacy) and Terms of Service (https://www.creeperhost.net/tos).");
        } catch (Exception exception)
        {
        } finally
        {
            IOUtils.closeQuietly((OutputStream) fileoutputstream);
        }
    }
}