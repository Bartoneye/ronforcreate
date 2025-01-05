package com.solegendary.reignofnether.survival.spawners;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.survival.Wave;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;
import static com.solegendary.reignofnether.survival.spawners.WaveSpawner.*;

public class IllagerWaveSpawner {

    private static final Random random = new Random();

    private static final Map<Integer, List<EntityType<? extends Mob>>> ILLAGER_UNITS = new HashMap<>();

    static {
        ILLAGER_UNITS.put(1, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get()
                // no enchants
        ));
        ILLAGER_UNITS.put(2, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.IRON_GOLEM_UNIT.get()
                //EntityRegistrar.WITCH.get()
                // + low tier enchants
        ));
        ILLAGER_UNITS.put(3, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.EVOKER_UNIT.get()
                // + high tier enchants on vindicators and pillagers
        ));
        ILLAGER_UNITS.put(4, List.of(
                EntityRegistrar.EVOKER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + evokers can use vexes
        ));
        ILLAGER_UNITS.put(5, List.of(
                EntityRegistrar.VINDICATOR_UNIT.get(),
                EntityRegistrar.PILLAGER_UNIT.get(),
                EntityRegistrar.RAVAGER_UNIT.get()
                // + Illager captain with resistance/strength and enchanted armor
                // + Ravager Artillery
        ));
    }

    public static EntityType<? extends Mob> getRandomUnitOfTier(int tier) {
        List<EntityType<? extends Mob>> units = ILLAGER_UNITS.get(tier);
        return units.get(random.nextInt(units.size()));
    }

    // spawn illagers from one direction
    public static void spawnIllagerWave(ServerLevel level, Wave wave) {
        final int pop = wave.population * PlayerServerEvents.rtsPlayers.size();
        int remainingPop = wave.population * PlayerServerEvents.rtsPlayers.size();
        List<BlockPos> spawnBps = getValidSpawnPoints(remainingPop, level, true);
        int spawnsThisDir = 0;
        int spawnUntilNextTurn = -2;

        if (!spawnBps.isEmpty()) {
            BlockPos bp = spawnBps.get(0).above();
            Direction dir = Direction.EAST;

            while (remainingPop > 0) {
                switch (dir) {
                    case NORTH -> {
                        bp = bp.north();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.EAST;
                        }
                    }
                    case EAST -> {
                        bp = bp.east();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.SOUTH;
                        }
                    }
                    case SOUTH -> {
                        bp = bp.south();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.WEST;
                        }
                    }
                    case WEST -> {
                        bp = bp.west();
                        if (spawnsThisDir >= spawnUntilNextTurn) {
                            spawnsThisDir = 0;
                            spawnUntilNextTurn += 2;
                            dir = Direction.NORTH;
                        }
                    }
                }

                int tier = random.nextInt(wave.highestUnitTier) + 1;
                EntityType<? extends Mob> mobType = IllagerWaveSpawner.getRandomUnitOfTier(tier);

                Entity entity = UnitServerEvents.spawnMob(mobType, level, bp.above(), ENEMY_OWNER_NAME);

                if (wave.highestUnitTier >= 5 && entity instanceof RavagerUnit ravagerUnit) {
                    Entity entityPassenger = UnitServerEvents.spawnMob(EntityRegistrar.PILLAGER_UNIT.get(),
                            level, bp.above(), ENEMY_OWNER_NAME);
                    if (entityPassenger instanceof Unit unit) {
                        entityPassenger.startRiding(ravagerUnit);
                        remainingPop -= getModifiedPopCost(unit);
                    }
                }

                if (entity instanceof Unit unit) {
                    placeIceOrMagma(bp, level);
                    remainingPop -= getModifiedPopCost(unit);
                    spawnsThisDir += 1;
                }
            }
        }
        if (remainingPop > 0) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + remainingPop + "/" + pop + " population worth of monster units");
        }
        lastFaction = Faction.VILLAGERS;
    }
}
