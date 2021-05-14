package net.creeperhost.minetogether;

import com.mojang.authlib.exceptions.AuthenticationException;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.creeperhost.minetogether.verification.SignatureVerifier;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MineTogether
{
    public static final String MOD_ID = "minetogether";
    public static Logger logger = LogManager.getLogger();

    public static void init()
    {
        Path configFile = Platform.getConfigFolder().resolve(MOD_ID + ".json");
        Config.init(configFile.toFile());
    }

    public static void clientInit()
    {
        EnvExecutor.runInEnv(EnvType.CLIENT, () -> MineTogetherClient::init);
    }
}
