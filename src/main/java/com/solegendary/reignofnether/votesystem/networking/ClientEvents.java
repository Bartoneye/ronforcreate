package com.solegendary.reignofnether.votesystem.networking;

import com.solegendary.reignofnether.votesystem.MapData;
import com.solegendary.reignofnether.votesystem.MapSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    private static final Minecraft MC = Minecraft.getInstance();
    public static void loadVoteScreen(ClientboundOpenVotenScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        List<MapData> maps = msg.getMaps();
        MC.setScreen(new MapSelectionScreen(maps));
    }
}
