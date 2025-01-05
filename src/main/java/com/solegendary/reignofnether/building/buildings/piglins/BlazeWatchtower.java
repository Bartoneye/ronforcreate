package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.piglins.BlazeUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class BlazeWatchtower extends Building implements GarrisonableBuilding/*, NetherConvertingBuilding*/ {

    //Disabled, for now
    //public final static float NON_NETHER_BUILD_TIME_MODIFIER = 2.0f;

    public final static String buildingName = "Blaze Watchtower";
    public final static String structureName = "blaze_watchtower";
    public final static ResourceCost cost = ResourceCosts.BLAZE_WATCHTOWER;

    private final static int MAX_OCCUPANTS = 1;

    //Netherzone ranges
    /*public NetherZone netherConversionZone = null;

    @Override
    public double getMaxRange() {
        return 6;
    }

    @Override
    public double getStartingRange() {
        return 3;
    }

    @Override
    public NetherZone getZone() {
        return netherConversionZone;
    }*/

    public BlazeWatchtower(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.CHISELED_NETHER_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/chiseled_nether_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.2f;

        this.startingBlockTypes.add(Blocks.RED_NETHER_BRICK_STAIRS);
        this.startingBlockTypes.add(Blocks.RED_NETHER_BRICKS);
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    // don't use this for abilities as it may not be balanced
    public int getAttackRange() { return 24; }
    // bonus for units attacking garrisoned units
    public int getExternalAttackRangeBonus() { return 10; }


    public boolean canDestroyBlock(BlockPos relativeBp) {
        return relativeBp.getY() != 10 &&
                relativeBp.getY() != 11;
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    //Netherzone setup config
    /*@Override
    public void setNetherZone(NetherZone nz) {
        if (netherConversionZone == null) {
            netherConversionZone = nz;
            if (!level.isClientSide()) {
                BuildingServerEvents.netherZones.add(netherConversionZone);
                BuildingServerEvents.saveNetherZones((ServerLevel) level);
            }
        }
    }

    @Override
    public void onBuilt() {
        super.onBuilt();
        setNetherZone(new NetherZone(centrePos.offset(0, -2, 0), getMaxRange(), getStartingRange()));
    }*/

    @Override
    public BlockPos getEntryPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2,6,2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2,6,2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2,6,-2);
        } else {
            return new BlockPos(2,6,-2);
        }
    }

    @Override
    public BlockPos getExitPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(4,1,4);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-4,1,4);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-4,1,-4);
        } else {
            return new BlockPos(4,1,-4);
        }
    }

    @Override
    public boolean isFull() { return GarrisonableBuilding.getNumOccupants(this) >= MAX_OCCUPANTS; }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                BlazeWatchtower.buildingName,
                new ResourceLocation("minecraft", "textures/block/lava_flow.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == BlazeWatchtower.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(CentralPortal.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(BlazeWatchtower.class),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.blaze_watchtower"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.blaze_watchtower.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.blaze_watchtower.tooltip2"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.blaze_watchtower.tooltip3", MAX_OCCUPANTS), Style.EMPTY)
                ),
                null
        );
    }

}
