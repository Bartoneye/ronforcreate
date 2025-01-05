package com.solegendary.reignofnether.votesystem;

import com.mojang.brigadier.CommandDispatcher;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.votesystem.networking.ClientboundOpenVotenScreenPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class VoteCommand {

    /*
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("vote")
                        .executes(context -> {
                            Supplier<ServerPlayer> player = () -> context.getSource().getPlayer();
                            List<MapData> maps = loadMaps(context.getSource());
                            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(player), new ClientboundOpenVotenScreenPacket(maps));
                            return 1;
                        })
        );
    }
     */
    private static List<MapData> loadMaps(CommandSourceStack source) {
        MinecraftServer minecraftServerInstance = source.getServer();
        ResourceManager resourceManager = minecraftServerInstance.getResourceManager();
        List<MapData> maps = MapDataLoader.loadMaps(resourceManager);
        return maps;
    }


}

