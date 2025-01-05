package com.solegendary.reignofnether.votesystem.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// Packet for sending a vote to the server
public class VotePacket {
    private final String mapName;
    private final UUID playerUUID;

    // Constructor
    public VotePacket(String mapName, UUID playerUUID) {
        this.mapName = mapName;
        this.playerUUID = playerUUID;
    }

    // Decoder constructor
    public VotePacket(FriendlyByteBuf buffer) {
        this.mapName = buffer.readUtf(32767);
        this.playerUUID = buffer.readUUID();
    }

    // Encoder
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.mapName);
        buffer.writeUUID(this.playerUUID);
    }

    // Handler with player verification
    public static void handle(VotePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            // Verify that the sender's UUID matches the UUID in the packet
            if (player == null || !player.getUUID().equals(message.playerUUID)) {
                System.err.println("VotePacket verification failed: invalid player UUID.");
                return;
            }

            // Valid player - process the vote
            ServerVoteHandler.handleVote(player, message.mapName);
        });
        ctx.get().setPacketHandled(true);
    }
}
