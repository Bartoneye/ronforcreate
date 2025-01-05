package com.solegendary.reignofnether.gamemode;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.GameRuleRegistrar;
import com.solegendary.reignofnether.survival.SurvivalServerEvents;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GameModeServerEvents {

    private static GameMode getGameMode() {
        // if a beacon exists, return ClientGameMode.KOTH;
        if (SurvivalServerEvents.isEnabled())
            return GameMode.SURVIVAL;

        return GameMode.STANDARD;
    }

    private static boolean isGameModeLocked() {
        return !PlayerServerEvents.rtsPlayers.isEmpty();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (isGameModeLocked())
            GameModeClientboundPacket.setAndLockAllClientGameModes(getGameMode());
        if (evt.getEntity().getLevel().getGameRules().getRule(GameRuleRegistrar.DISALLOW_WAVE_SURVIVAL).get())
            GameModeClientboundPacket.disallowSurvival();
    }
}
