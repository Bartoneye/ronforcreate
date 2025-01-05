package com.solegendary.reignofnether.survival.spawners;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.BlockRegistrar;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.solegendary.reignofnether.survival.SurvivalServerEvents.*;

public class WaveSpawner {

    public static final int MAX_SPAWN_RANGE = 80;
    public static final int MIN_SPAWN_RANGE = 60;
    public static final int SAMPLE_POINTS_PER_BUILDING = 100;
    public static final int MAX_FAILED_BUILDINGS = 10;

    public static final int MIN_VALID_BUILDINGS = 5; // once we sort the buildings by distance to centroid, how many buildings do we pick to spawn around?
    public static final float PERCENT_VALID_BUILDINGS = 0.1f; // % of all buildings added to MIN_VALID_BUILDINGS

    private static final Random random = new Random();

    public static int getModifiedPopCost(Unit unit) {
        return Math.max(1, unit.getPopCost() - 1);
    }

    public static void placeIceOrMagma(BlockPos bp, Level level) {;

        BlockPos bpLiquid = null;
        for (int i = 0; i < 10; i++) {
            if (level.getBlockState(bp.offset(0,5-i,0)).getMaterial().isLiquid()) {
                bpLiquid = bp.offset(0,5-i,0);
                break;
            }
        }
        if (bpLiquid == null)
            return;

        BlockState bs = level.getBlockState(bpLiquid);
        BlockState bsToPlace;

        if (bs.getMaterial() == Material.LAVA)
            bsToPlace = BlockRegistrar.WALKABLE_MAGMA_BLOCK.get().defaultBlockState();
        else if (bs.getMaterial() == Material.WATER)
            bsToPlace = Blocks.FROSTED_ICE.defaultBlockState();
        else
            return;

        level.setBlockAndUpdate(bpLiquid, bsToPlace);

        List<BlockPos> bps = List.of(bpLiquid.north(), bpLiquid.east(), bpLiquid.south(), bpLiquid.west(),
                bpLiquid.north().east(),
                bpLiquid.south().west(),
                bpLiquid.north().east(),
                bpLiquid.south().west());

        // Frostwalker effect provided in LivingEntityMixin, but it only happens on changing block positions on the ground
        for (BlockPos pos : bps) {
            BlockState bsAdj = level.getBlockState(pos);
            if (bsAdj.getMaterial().isLiquid() ||
                    bsAdj.getMaterial() == Material.WATER_PLANT ||
                    bsAdj.getMaterial() == Material.REPLACEABLE_WATER_PLANT)
                level.setBlockAndUpdate(pos, bsToPlace);
        }
    }

    public static List<BlockPos> getValidSpawnPoints(int amount, Level level, boolean allowLiquid) {
        List<Building> buildings = BuildingServerEvents.getBuildings()
                .stream().filter(b -> !ENEMY_OWNER_NAME.equals(b.ownerName) && !b.ownerName.isBlank())
                .toList();

        Random random = new Random();
        if (buildings.isEmpty())
            return List.of();

        Vec3 centroid = new Vec3(0,0,0);

        for (Building building : buildings) {
            centroid = centroid.add(Vec3.atCenterOf(building.centrePos));
        }
        double invBs = 1f / buildings.size();
        final Vec3 fCentroid = centroid.multiply(new Vec3(invBs, invBs, invBs));

        // calculate all valid buildings to spawn around based on distance from the centroid
        List<Building> sortedBuildings = buildings.stream().sorted(
            Comparator.comparing((Building b) -> b.centrePos.distToCenterSqr(fCentroid.x, fCentroid.y, fCentroid.z)).reversed()
        ).toList();

        int numValidBuildings = (int) (MIN_VALID_BUILDINGS + (buildings.size() * PERCENT_VALID_BUILDINGS));
        List<Building> validBuildings = sortedBuildings.subList(0, Math.min(sortedBuildings.size(), numValidBuildings));

        int spawnAttemptsThisBuilding = 0;
        BlockState spawnBs;
        BlockPos spawnBp;
        double distSqrToNearestBuilding = 999999;
        double distSqrToNearestPortal = 999999;
        int failedBuildings = 0;
        ArrayList<BlockPos> validSpawns = new ArrayList<>();

        outerloop:
        do {
            do {
                Building building = validBuildings.get(random.nextInt(validBuildings.size()));

                int x = building.centrePos.getX() + random.nextInt(-MAX_SPAWN_RANGE, MAX_SPAWN_RANGE);
                int z = building.centrePos.getZ() + random.nextInt(-MAX_SPAWN_RANGE, MAX_SPAWN_RANGE);
                int y = level.getChunkAt(new BlockPos(x, 0, z)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

                spawnBp = MiscUtil.getHighestNonAirBlock(level, new BlockPos(x, y, z), true);
                spawnBs = level.getBlockState(spawnBp);
                spawnAttemptsThisBuilding += 1;
                if (spawnAttemptsThisBuilding > 100) {
                    ReignOfNether.LOGGER.warn("Gave up trying to find a suitable spawn!");
                    failedBuildings += 1;
                    if (failedBuildings > MAX_FAILED_BUILDINGS)
                        break outerloop;
                    else
                        continue;
                }
                Vec3 vec3 = new Vec3(x, y, z);
                Building b = BuildingUtils.findClosestBuilding(false, vec3, (b1) -> !b1.ownerName.equals(ENEMY_OWNER_NAME));
                Building p = BuildingUtils.findClosestBuilding(false, vec3, (b1) -> b1.ownerName.equals(ENEMY_OWNER_NAME));

                if (b != null)
                    distSqrToNearestBuilding = b.centrePos.distToCenterSqr(vec3);
                if (p != null)
                    distSqrToNearestPortal = p.centrePos.distToCenterSqr(vec3);

            } while (spawnBs.getMaterial() == Material.LEAVES
                    || spawnBs.getMaterial() == Material.WOOD
                    || distSqrToNearestBuilding < (MIN_SPAWN_RANGE * MIN_SPAWN_RANGE)
                    || distSqrToNearestPortal < (10 * 10)
                    || (spawnBs.getMaterial().isLiquid() && !allowLiquid)
                    || BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp)
                    || BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp.above()));

            validSpawns.add(spawnBp);
            amount -= 1;

        } while(amount > 0);

        Collections.shuffle(validSpawns);

        if (validSpawns.isEmpty())
            PlayerServerEvents.sendMessageToAllPlayers("WARNING: could not find any valid spawn locations!");

        return validSpawns;
    }
}
