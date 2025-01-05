package com.solegendary.reignofnether.votesystem.networking;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerVoteHandler {
    private static final Map<String, Integer> votes = new HashMap<>();
    private static final Map<UUID, String> playerVotes = new HashMap<>();

    public static void handleVote(ServerPlayer player, String mapName) {
        String previousVote = playerVotes.get(player.getUUID());
        if (previousVote != null) {
            votes.put(previousVote, votes.get(previousVote) - 1);
        }

        playerVotes.put(player.getUUID(), mapName);
        votes.put(mapName, votes.getOrDefault(mapName, 0) + 1);

        // Send updated votes to all clients
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new VoteSyncPacket(votes));
    }

    public static Map<String, Integer> getVotes() {
        return votes;
    }
}
