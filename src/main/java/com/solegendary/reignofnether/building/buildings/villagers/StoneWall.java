package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
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

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class StoneWall extends Building implements GarrisonableBuilding {

    public final static String buildingName = "Stone Wall";
    public final static String structureName = "stone_wall";

    public final static ResourceCost cost = ResourceCosts.STONEWALL;

    private final static int MAX_OCCUPANTS = 4;

    public StoneWall(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.CHISELED_STONE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/chiseled_stone_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.0f;

        this.startingBlockTypes.add(Blocks.STONE_BRICKS);
        this.startingBlockTypes.add(Blocks.STONE_BRICK_SLAB);

    }

    public Faction getFaction() {
        return Faction.VILLAGERS;
    }

    // don't use this for abilities as it may not be balanced
    public int getAttackRange() {
        return 16;
    }

    // bonus for units attacking garrisoned units
    public int getExternalAttackRangeBonus() {
        return 10;
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public boolean canDestroyBlock(BlockPos relativeBp) {
        return relativeBp.getY() != 10 &&
                relativeBp.getY() != 11;
    }

    @Override
    public BlockPos getEntryPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2, 6, 2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2, 6, 2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2, 6, -2);
        } else {
            return new BlockPos(2, 6, -2);
        }
    }

    @Override
    public BlockPos getExitPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2, 1, 2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2, 1, 2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2, 1, -2);
        } else {
            return new BlockPos(2, 1, -2);
        }
    }

    @Override
    public boolean isFull() {
        return GarrisonableBuilding.getNumOccupants(this) >= MAX_OCCUPANTS;
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                StoneWall.buildingName,
                new ResourceLocation("minecraft", "textures/block/chiseled_stone_bricks.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == StoneWall.class,
                TutorialClientEvents::isEnabled,
                () -> BuildingClientEvents.hasFinishedBuilding(TownCentre.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(StoneWall.class),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.stonewall"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.stonewall.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.stonewall.tooltip2"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.stonewall.tooltip3", MAX_OCCUPANTS), Style.EMPTY)
                ),
                null
        );
    }
}