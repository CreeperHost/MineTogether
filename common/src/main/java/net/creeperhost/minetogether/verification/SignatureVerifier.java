package net.creeperhost.minetogether.verification;

import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.minetogether.MineTogether;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class SignatureVerifier
{
    private final File jarFile;

    public SignatureVerifier()
    {
        this.jarFile = findOurJar(Platform.getGameFolder().resolve("mods").toFile());
    }

    public String verify()
    {
        if (jarFile == null) return "Development";
        MessageDigest messageDigest;
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "Development";
        }

        byte[] fileBytes;
        try
        {
            fileBytes = FileUtils.readFileToByteArray(jarFile);
        } catch (IOException e)
        {
            e.printStackTrace();
            return "Development";
        }

        messageDigest.update(fileBytes);

        //Now we have the hash of the whole jar, not just a single class, previous implementation meant you could change anything except the first class...
        return bytesToHex(messageDigest.digest());
    }

    private File findOurJar(File modsFolder)
    {
        try
        {
            MineTogether.logger.info("Scanning mods directory for MineTogether jar");
            File[] modsDir = modsFolder.listFiles();//FMLPaths.MODSDIR.get().toFile().listFiles();
            if (modsDir == null) return null;

            for (File file : modsDir)
            {
                try
                {
                    //Don't check directories
                    if(file.isDirectory()) continue;
                    if(!file.getName().endsWith(".jar")) continue;

                    JarFile jarFile = new JarFile(file);
                    if (jarFile == null) continue;

                    if (jarFile.getManifest() == null) continue;
                    Map<String, Attributes> attributesMap = jarFile.getManifest().getEntries();

                    if (attributesMap == null) continue;

                    for (String s : attributesMap.keySet())
                    {
                        if (s.equalsIgnoreCase("net/creeperhost/minetogether/MineTogether.class"))
                        {
                            MineTogether.logger.error("Main class found, MineTogether Jar found");
                            try
                            {
                                jarFile.close();
                                jarFile = new JarFile(file, true);
                            } catch (SecurityException ignored)
                            {
                                ignored.printStackTrace();
                                return null;
                            }
                            return file;
                        }
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String bytesToHex(byte[] hash)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++)
        {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
