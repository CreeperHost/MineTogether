package net.creeperhost.creeperhost;

import net.creeperhost.creeperhost.api.CreeperHostAPI;
import net.creeperhost.creeperhost.api.ICreeperHostMod;
import net.creeperhost.creeperhost.api.IServerHost;
import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.paul.Callbacks;
import net.creeperhost.creeperhost.paul.CreeperHostServerHost;
import net.creeperhost.creeperhost.siv.QueryGetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Random;

@Mod(modid = CreeperHost.MOD_ID, name = CreeperHost.NAME, version = CreeperHost.VERSION, clientSideOnly = true, acceptableRemoteVersions="*", acceptedMinecraftVersions = "1.8,1.8.8,1.8.9,1.9.4,1.10.2,1.11.2")
public class CreeperHost implements ICreeperHostMod
{

    public static final String MOD_ID = "creeperhost";
    public static final String NAME = "CreeperHost";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("creeperhost");

    @Mod.Instance
    public static CreeperHost instance;

    public ArrayList<IServerHost> implementations = new ArrayList<IServerHost>();
    public IServerHost currentImplementation;

    public File configFile;

    private QueryGetter queryGetter;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        if (event.getSide() == Side.SERVER) {
            logger.info("Client side only mod - not doing anything on the server!");
            return;
        }

        MinecraftForge.EVENT_BUS.register(new EventHandler());
        configFile = event.getSuggestedConfigurationFile();

        InputStream configStream = null;
        try
        {
            String configString;
            if (configFile.exists()) {
                configStream = new FileInputStream(configFile);
                configString = IOUtils.toString(configStream);
            } else {
                configString = "{}";
            }

            Config.loadConfig(configString);
        } catch (Throwable t)
        {
            logger.error("Unable to read config", t);
            throw new RuntimeException("Fatal error, unable to read config");
        } finally {
            try {
                if (configStream != null) {
                    configStream.close();
                }
            } catch (Throwable t) {
            }

        }

        if (Config.getInstance().isCreeperhostEnabled()) {
            Config.getInstance().setVersion(Callbacks.getVersionFromCurse(Config.getInstance().curseProjectID));
            CreeperHostAPI.registerImplementation(new CreeperHostServerHost());
        }


    }

    private Random randomGenerator;

    public void saveConfig(){
        FileOutputStream configOut;
        try
        {
            configOut = new FileOutputStream(configFile);
            IOUtils.write(Config.saveConfig(), configOut);
            configOut.close();
        } catch (Throwable t)
        {
        }
    }

    public void setRandomImplementation() {
        if (randomGenerator == null)
            randomGenerator = new Random();
        if (implementations.size() == 0)
            return;
        int random = randomGenerator.nextInt(implementations.size());
        currentImplementation = implementations.get(random);
    }

    public IServerHost getImplementation()
    {
        return currentImplementation;
    }


    @Override
    public void registerImplementation(IServerHost serverHost)
    {
        implementations.add(serverHost);
    }

    public void makeQueryGetter() {
        try {
            if (FMLClientHandler.instance().getClientToServerNetworkManager() != null) {
                SocketAddress socketAddress = FMLClientHandler.instance().getClientToServerNetworkManager().getRemoteAddress();

                String host = "127.0.0.1";
                int port = 25565;

                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress add = (InetSocketAddress) socketAddress;
                    host = add.getHostName();
                    port = add.getPort();
                }

                queryGetter = new QueryGetter(host, port);
            }
        } catch (Throwable t) {
            // Catch _ALL_ errors. We should _NEVER_ crash.
        }

    }
    
    public QueryGetter getQueryGetter(){
        if(queryGetter == null) {
            makeQueryGetter();
        }
        return queryGetter;
    }
}
