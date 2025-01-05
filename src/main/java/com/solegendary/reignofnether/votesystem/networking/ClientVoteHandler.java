package com.solegendary.reignofnether.votesystem.networking;

import java.util.HashMap;
import java.util.Map;

public class ClientVoteHandler {
    private static Map<String, Integer> votes = new HashMap<>();

    public static void updateVotes(Map<String, Integer> newVotes) {
        votes = newVotes;
    }

    public static Map<String, Integer> getVotes() {
        return votes;
    }
}
