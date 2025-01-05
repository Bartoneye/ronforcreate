package com.solegendary.reignofnether.survival.spawners;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.survival.Wave;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import com.solegendary.reignofnether.unit.units.monsters.PoisonSpiderUnit;
import com.solegendary.reignofnether.unit.units.monsters.SpiderUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.ENEMY_OWNER_NAME;
import static com.solegendary.reignofnether.survival.SurvivalServerEvents.lastFaction;
import static com.solegendary.reignofnether.survival.spawners.WaveSpawner.*;
import static net.minecraft.world.entity.monster.Creeper.DATA_IS_POWERED;

public class MonsterWaveSpawner {

    private static final Random random = new Random();

    private static final Map<Integer, List<EntityType<? extends Mob>>> MONSTER_UNITS = new HashMap<>();

    static {
        MONSTER_UNITS.put(1, List.of(
                //EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get(),
                //EntityRegistrar.ZOMBIE_UNIT.get(),
                EntityRegistrar.SKELETON_UNIT.get()
        ));
        MONSTER_UNITS.put(2, List.of(
                EntityRegistrar.HUSK_UNIT.get(),
                EntityRegistrar.STRAY_UNIT.get(),
                EntityRegistrar.SPIDER_UNIT.get()
        ));
        MONSTER_UNITS.put(3, List.of(
                EntityRegistrar.DROWNED_UNIT.get(),
                EntityRegistrar.POISON_SPIDER_UNIT.get(),
                EntityRegistrar.CREEPER_UNIT.get(),
                EntityRegistrar.SPIDER_UNIT.get() // Spider Jockey
        ));
        MONSTER_UNITS.put(4, List.of(
                EntityRegistrar.ZOGLIN_UNIT.get(),
                EntityRegistrar.ENDERMAN_UNIT.get(),
                EntityRegistrar.POISON_SPIDER_UNIT.get() // Poison Spider Jockey
        ));
        MONSTER_UNITS.put(5, List.of(
                EntityRegistrar.WARDEN_UNIT.get()
                // + Charged creepers
        ));
    }

    public static EntityType<? extends Mob> getRandomUnitOfTier(int tier) {
        List<EntityType<? extends Mob>> units = MONSTER_UNITS.get(tier);
        return units.get(random.nextInt(units.size()));
    }

    // spawn monsters evenly spread out across from all directions
    public static void spawnMonsterWave(ServerLevel level, Wave wave) {
        final int pop = wave.population * PlayerServerEvents.rtsPlayers.size();
        int remainingPop = wave.population * PlayerServerEvents.rtsPlayers.size();

        List<BlockPos> bps = getValidSpawnPoints(remainingPop, level, true);

        for (BlockPos bp : bps) {
            BlockState bs = level.getBlockState(bp);

            int tier = random.nextInt(wave.highestUnitTier) + 1;
            EntityType<? extends Mob> mobType = MonsterWaveSpawner.getRandomUnitOfTier(tier);

            bp = bp.offset(0,2,0);

            Entity entity = UnitServerEvents.spawnMob(mobType, level,
                    mobType.getDescription().getString().contains("spider") ? bp.above(): bp,
                    ENEMY_OWNER_NAME);

            if (wave.highestUnitTier >= 3 && entity instanceof SpiderUnit spiderUnit) {
                Entity entityPassenger = UnitServerEvents.spawnMob(EntityRegistrar.SKELETON_UNIT.get(),
                        level, bp.above(), ENEMY_OWNER_NAME);
                if (entityPassenger instanceof Unit unit) {
                    entityPassenger.startRiding(spiderUnit);
                    remainingPop -= getModifiedPopCost(unit);
                }
            }

            if (wave.highestUnitTier >= 4 && entity instanceof PoisonSpiderUnit poisonSpiderUnit) {
                Entity entityPassenger = UnitServerEvents.spawnMob(EntityRegistrar.SKELETON_UNIT.get(),
                        level, bp.above(), ENEMY_OWNER_NAME);
                if (entityPassenger instanceof Unit unit) {
                    entityPassenger.startRiding(poisonSpiderUnit);
                    remainingPop -= getModifiedPopCost(unit);
                }
            }

            if (wave.highestUnitTier >= 5 && entity instanceof CreeperUnit creeperUnit && random.nextBoolean())
                creeperUnit.getEntityData().set(DATA_IS_POWERED, true);

            if (entity instanceof Unit unit) {
                placeIceOrMagma(bp, level);
                remainingPop -= getModifiedPopCost(unit);
            }

            if (remainingPop <= 0)
                break;
        }
        if (remainingPop > 0) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to spawn " + remainingPop + "/" + pop + " population worth of monster units");
        }
        lastFaction = Faction.MONSTERS;
    }
}
