package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchWitherClouds;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.piglins.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class BasicPortal extends ProductionBuilding implements NetherConvertingBuilding {

    public final static String buildingName = "Basic Portal";
    public final static String structureName = "portal";
    public final static ResourceCost cost = ResourceCosts.BASIC_PORTAL;

    private final double NETHER_CONVERT_RANGE_MAX = 25;
    private double netherConvertRange = 3;
    private int netherConvertTicksLeft = NETHER_CONVERT_TICKS_MAX;
    private int convertsAfterMaxRange = 0;

    public double getMaxRange() { return NETHER_CONVERT_RANGE_MAX; }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);

        netherConvertTicksLeft -= 1;
        if (netherConvertTicksLeft <= 0 && convertsAfterMaxRange < MAX_CONVERTS_AFTER_MAX_RANGE) {
            netherConvertTick(this, netherConvertRange, NETHER_CONVERT_RANGE_MAX);
            if (netherConvertRange < NETHER_CONVERT_RANGE_MAX)
                netherConvertRange += 0.1f;
            else
                convertsAfterMaxRange += 1;
            netherConvertTicksLeft = NETHER_CONVERT_TICKS_MAX;
        }
    }

    public BasicPortal(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.NETHER_PORTAL;
        this.icon = new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.8f;

        this.startingBlockTypes.add(Blocks.NETHER_BRICKS);

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                    PiglinBruteProd.getStartButton(this, Keybindings.keyQ),
                    PiglinHeadhunterProd.getStartButton(this, Keybindings.keyW),
                    HoglinProd.getStartButton(this, Keybindings.keyE),
                    BlazeProd.getStartButton(this, Keybindings.keyR),
                    WitherSkeletonProd.getStartButton(this, Keybindings.keyT),
                    GhastProd.getStartButton(this, Keybindings.keyY)
            );
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                BasicPortal.buildingName,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == BasicPortal.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(CitadelPortal.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(BasicPortal.class),
                null,
                List.of(
                        FormattedCharSequence.forward(BasicPortal.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("An obsidian portal used to spread nether blocks.", Style.EMPTY),
                        FormattedCharSequence.forward("Can be upgraded for various different functions.", Style.EMPTY)
                ),
                null
        );
    }
}




















