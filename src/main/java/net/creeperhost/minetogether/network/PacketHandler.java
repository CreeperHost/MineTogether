package net.creeperhost.minetogether.network;

import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Predicate;

public class PacketHandler
{
    private static final Predicate<String> validator = v -> "1".equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v);

    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constants.MOD_ID, "main_channel"))
            .clientAcceptedVersions(validator)
            .serverAcceptedVersions(validator)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();
    
    public static void register()
    {
        int disc = 0;
        
        HANDLER.registerMessage(disc++, PacketServerID.class, PacketServerID::encode, PacketServerID::decode, PacketServerID.Handler::handle);
    }
    
    public static void sendTo(Object msg, ServerPlayerEntity player)
    {
        if (!(player instanceof FakePlayer))
        {
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
