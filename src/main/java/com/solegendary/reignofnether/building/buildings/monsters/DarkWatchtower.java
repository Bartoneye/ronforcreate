package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class DarkWatchtower extends Building implements GarrisonableBuilding {

    public final static String buildingName = "Dark Watchtower";
    public final static String structureName = "dark_watchtower";
    public final static ResourceCost cost = ResourceCosts.DARK_WATCHTOWER;

    //Right End Watchtower
    public final static String structureNameER = "watchtower_end1";

    //Left End Watchtower
    public final static String structureNameEL = "watchtower_end2";

    //Middle of wall Watchtower
    public final static String structureNameMid = "watchtower_2w";

    //Watchtower corner
    public final static String structureNameCorner = "watchtower_corner";

    private final static int MAX_OCCUPANTS = 3;

    public DarkWatchtower(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.DEEPSLATE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/deepslate_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.0f;

        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICKS);
        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICK_SLAB);
        this.startingBlockTypes.add(Blocks.CRACKED_DEEPSLATE_BRICKS);
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    // don't use this for abilities as it may not be balanced
    public int getAttackRange() { return 24; }
    // bonus for units attacking garrisoned units
    public int getExternalAttackRangeBonus() { return 10; }

    public boolean canDestroyBlock(BlockPos relativeBp) {
        return relativeBp.getY() != 10 &&
                relativeBp.getY() != 11;
    }

    private void adjustStructureBasedOnProximity(LevelAccessor level, BlockPos originPos) {
        if (!level.isClientSide() || level.isClientSide()) {
            String newStructureName = "";
            // Define the proximity radius (e.g., 50 blocks)
            int proximityRadius = 12;

// Get all StoneWalls within the defined range
            List<Building> nearbyWalls = BuildingClientEvents.getBuildingsWithinRange(
                    new Vec3(originPos.getX(), originPos.getY(), originPos.getZ()),
                    proximityRadius, // Search distance
                    HauntedWall.buildingName // Search for HauntedWall structures
            );

// Get all Open StoneGates within the defined range
            List<Building> nearbyGatesOpen = BuildingClientEvents.getBuildingsWithinRange(
                    new Vec3(originPos.getX(), originPos.getY(), originPos.getZ()),
                    proximityRadius, // Search distance
                    HauntedGate.buildingName // Search for HauntedGate structures
            );
//Get all Shut StoneGates within the defined range
            List<Building> nearbyGatesShut = BuildingClientEvents.getBuildingsWithinRange(
                    new Vec3(originPos.getX(), originPos.getY(), originPos.getZ()),
                    proximityRadius, // Search distance
                    HauntedGate.buildingNameShut // Search for HauntedGate structures
            );

// Combine the lists
            List<Building> nearbyBuildings = new ArrayList<>();
            nearbyBuildings.addAll(nearbyWalls);
            nearbyBuildings.addAll(nearbyGatesOpen);
            nearbyBuildings.addAll(nearbyGatesShut);

            boolean stoneBuildingNearby = nearbyBuildings.stream().anyMatch(building -> building instanceof HauntedWall || building instanceof HauntedGate);
            int numBuildings = (int) nearbyBuildings.stream()
                    .filter(building -> building instanceof HauntedWall || building instanceof HauntedGate)
                    .count();
            Building nearestBuilding = nearbyBuildings.stream()
                    .findFirst()
                    .orElse(null); // Identify the nearest HauntedWall or HauntedGate

            // Adjust structure name based on proximity to HauntedWall
            if (stoneBuildingNearby && nearestBuilding != null) {
                boolean leftSide = false; // Flag to check if there is a building on the left
                boolean rightSide = false; // Flag to check if there is a building on the right

                BuildingUtils.Position watchtowerPos = new BuildingUtils.Position(centrePos.getX(), centrePos.getY(), centrePos.getZ());

                // Iterate through all nearby buildings
                for (Building building : nearbyBuildings) {
                    BuildingUtils.Position buildingPos = new BuildingUtils.Position(
                            building.centrePos.getX(),
                            building.centrePos.getY(),
                            building.centrePos.getZ()
                    );

                    // Determine the relative position of the building
                    String relativePosition = BuildingUtils.getRelativePosition(watchtowerPos, buildingPos);

                    // Check if the building is on the left or right side
                    if (this.rotation == Rotation.CLOCKWISE_90) {
                        if (relativePosition.contains("X Position: -x")) {
                            leftSide = true;
                        } else if (relativePosition.contains("X Position: +x")) {
                            rightSide = true;
                        }
                    } else if (this.rotation == Rotation.COUNTERCLOCKWISE_90) {
                        if (relativePosition.contains("X Position: +x")) {
                            leftSide = true;
                        } else if (relativePosition.contains("X Position: -x")) {
                            rightSide = true;
                        }
                    } else if (this.rotation == Rotation.NONE) {
                        if (relativePosition.contains("Z Position: +z")) {
                            leftSide = true;
                        } else if (relativePosition.contains("Z Position: -z")) {
                            rightSide = true;
                        }
                    } else if (this.rotation == Rotation.CLOCKWISE_180) {
                        if (relativePosition.contains("Z Position: -z")) {
                            leftSide = true;
                        } else if (relativePosition.contains("Z Position: +z")) {
                            rightSide = true;
                        }
                    }

                    // Exit loop early if both conditions are met
                    if (leftSide && rightSide) {
                        break;
                    }
                }

                // Check for towers on both sides
                if (leftSide && rightSide) {
                    newStructureName = DarkWatchtower.structureNameMid; // Build "Mid" tower if both sides are occupied
                    System.out.println("Building 'Mid' tower: Structures exist on both left and right sides.");
                } else if (numBuildings == 1) {
                    // For exactly one nearby building
                    BuildingUtils.Position stoneBuildingPos = new BuildingUtils.Position(
                            nearestBuilding.centrePos.getX(),
                            nearestBuilding.centrePos.getY(),
                            nearestBuilding.centrePos.getZ()
                    );

                    String relativePosition = BuildingUtils.getRelativePosition(watchtowerPos, stoneBuildingPos);

                    if (this.rotation == Rotation.CLOCKWISE_90) {
                        if (relativePosition.contains("X Position: -x")) {
                            newStructureName = DarkWatchtower.structureNameER;
                        } else if (relativePosition.contains("X Position: +x")) {
                            newStructureName = DarkWatchtower.structureNameEL;
                        }
                    } else if (this.rotation == Rotation.COUNTERCLOCKWISE_90) {
                        if (relativePosition.contains("X Position: +x")) {
                            newStructureName = DarkWatchtower.structureNameER;
                        } else if (relativePosition.contains("X Position: -x")) {
                            newStructureName = DarkWatchtower.structureNameEL;
                        }
                    } else if (this.rotation == Rotation.NONE) {
                        if (relativePosition.contains("Z Position: +z")) {
                            newStructureName = DarkWatchtower.structureNameER;
                        } else if (relativePosition.contains("Z Position: -z")) {
                            newStructureName = DarkWatchtower.structureNameEL;
                        }
                    } else if (this.rotation == Rotation.CLOCKWISE_180) {
                        if (relativePosition.contains("Z Position: -z")) {
                            newStructureName = DarkWatchtower.structureNameER;
                        } else if (relativePosition.contains("Z Position: +z")) {
                            newStructureName = DarkWatchtower.structureNameEL;
                        }
                    }

                } else if (numBuildings == 2) {
                    if (this.rotation == Rotation.NONE && leftSide && nearestBuilding.rotation == Rotation.CLOCKWISE_90) {
                        newStructureName = DarkWatchtower.structureNameCorner;
                        System.out.println("Orientation is: " + this.rotation.name());
                    } else if (this.rotation == Rotation.CLOCKWISE_180 && leftSide && nearestBuilding.rotation == Rotation.NONE) {
                        newStructureName = DarkWatchtower.structureNameCorner;
                        System.out.println("Orientation is: " + this.rotation.name());
                    } else if (this.rotation == Rotation.CLOCKWISE_90 && leftSide && nearestBuilding.rotation == Rotation.CLOCKWISE_180) {
                        newStructureName = DarkWatchtower.structureNameCorner;
                        System.out.println("Orientation is: " + this.rotation.name());
                    } else if (this.rotation == Rotation.COUNTERCLOCKWISE_90 && leftSide) {
                        newStructureName = DarkWatchtower.structureNameCorner;
                        System.out.println("Orientation is: " + this.rotation.name());
                    } else {
                        System.out.println("Orientation of the tower does not meet placement requirements.");
                        HudClientEvents.showTemporaryMessage(I18n.get("buildings.villagers.reignofnether.dark_watchtower.corner"));
                    }

                } else {
                    newStructureName = DarkWatchtower.structureName; // Default structure
                    System.out.println("No nearby structure detected. Using default watchtower.");
                }

                // Refresh block data to utilize the new structure
                ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
                this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);
            }
        }
    }

    public void onNearbyBuildingUpdate(LevelAccessor level, BlockPos originPos, Building nearbyBuilding) {
        if (nearbyBuilding instanceof HauntedWall || nearbyBuilding instanceof HauntedGate) {
            // Call adjustStructureBasedOnProximity to react to proximity changes
            adjustStructureBasedOnProximity(level, originPos);

            // Refresh blocks after adjustments
            super.refreshBlocks();
        }
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            DarkWatchtower.buildingName,
            new ResourceLocation("minecraft", "textures/block/deepslate_bricks.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == DarkWatchtower.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(DarkWatchtower.class),
            null,
            List.of(
                    FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.dark_watchtower"), Style.EMPTY.withBold(true)),
                    ResourceCosts.getFormattedCost(cost),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.dark_watchtower.tooltip1"), Style.EMPTY),
                    FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.dark_watchtower.tooltip2"), Style.EMPTY),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.dark_watchtower.tooltip3", MAX_OCCUPANTS), Style.EMPTY)
            ),
            null
        );
    }

    @Override
    public BlockPos getEntryPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2,11,2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2,11,2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2,11,-2);
        } else {
            return new BlockPos(2,11,-2);
        }
    }

    @Override
    public BlockPos getExitPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2,1,2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2,1,2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2,1,-2);
        } else {
            return new BlockPos(2,1,-2);
        }
    }

    @Override
    public boolean isFull() { return GarrisonableBuilding.getNumOccupants(this) >= MAX_OCCUPANTS; }
}
