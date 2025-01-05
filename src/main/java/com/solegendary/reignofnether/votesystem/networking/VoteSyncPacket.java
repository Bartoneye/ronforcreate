package com.solegendary.reignofnether.votesystem.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VoteSyncPacket {
    private final Map<String, Integer> votes;

    public VoteSyncPacket(Map<String, Integer> votes) {
        this.votes = votes;
    }

    public VoteSyncPacket(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        votes = new HashMap<>();
        for (int i = 0; i < size; i++) {
            votes.put(buffer.readUtf(32767), buffer.readInt());
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(votes.size());
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    public static void handle(VoteSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientVoteHandler.updateVotes(message.votes);
        });
        ctx.get().setPacketHandled(true);
    }
}
