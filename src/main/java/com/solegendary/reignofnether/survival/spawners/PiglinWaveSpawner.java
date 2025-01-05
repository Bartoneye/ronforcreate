package com.solegendary.reignofnether.survival.spawners;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.survival.Wave;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Rotation;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;

public class PiglinWaveSpawner {

    private static final Random random = new Random();

    private static final Map<Integer, List<EntityType<? extends Mob>>> PIGLIN_UNITS = new HashMap<>();

    static {
        PIGLIN_UNITS.put(1, List.of(
                EntityRegistrar.BRUTE_UNIT.get(),
                EntityRegistrar.HEADHUNTER_UNIT.get()
        ));
        PIGLIN_UNITS.put(2, List.of(
                EntityRegistrar.HOGLIN_UNIT.get()
        ));
        PIGLIN_UNITS.put(3, List.of(
                EntityRegistrar.BLAZE_UNIT.get(),
                EntityRegistrar.HOGLIN_UNIT.get()
                // + Hoglin riders
        ));
        PIGLIN_UNITS.put(4, List.of(
                EntityRegistrar.WITHER_SKELETON_UNIT.get()
                // + shields and heavy tridents

        ));
        PIGLIN_UNITS.put(5, List.of(
                EntityRegistrar.GHAST_UNIT.get()
                // + bloodlust
        ));
    }

    public static EntityType<? extends Mob> getRandomUnitOfTier(int tier) {
        List<EntityType<? extends Mob>> units = PIGLIN_UNITS.get(tier);
        return units.get(random.nextInt(units.size()));
    }

    // spawn portals which spawn half of the wave immediately, and trickle in constantly
    public static void spawnPiglinWave(ServerLevel level, Wave wave) {
        int numPortals = wave.getNumPortals();
        int failedPortalPlacements = 0;
        ArrayList<BlockPos> portalBps = new ArrayList<>();

        for (int i = 0; i < numPortals; i++) {

            BlockPos spawnBp = null;
            int attempts = 0;
            boolean tooCloseToAnotherPortal;

            do {
                tooCloseToAnotherPortal = false;
                List<BlockPos> spawnBps = WaveSpawner.getValidSpawnPoints(1, level, false);
                if (!spawnBps.isEmpty())
                    spawnBp = spawnBps.get(0);
                attempts += 1;

                for (BlockPos bp : portalBps)
                    if (spawnBp != null && bp.distSqr(spawnBp) < 25)
                        tooCloseToAnotherPortal = true;
                for (BlockPos bp : portals.stream().map(p -> p.portal.originPos).toList())
                    if (spawnBp != null && bp.distSqr(spawnBp) < 25)
                        tooCloseToAnotherPortal = true;

            } while((spawnBp == null || tooCloseToAnotherPortal) && attempts < 100);

            if (spawnBp != null) {
                portalBps.add(spawnBp);
                Building building = BuildingServerEvents.placeBuilding(Portal.buildingName,
                        new BlockPos(spawnBp).above(),
                        Rotation.NONE,
                        ENEMY_OWNER_NAME,
                        new int[] {},
                        false,
                        false
                );
            } else
                failedPortalPlacements += 1;
        }
        if (failedPortalPlacements > 0)
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + failedPortalPlacements + " portals!");

        lastFaction = Faction.PIGLINS;
    }
}
