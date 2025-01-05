package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.*;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.ability.abilities.CallLightning;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.*;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Laboratory extends ProductionBuilding implements RangeIndicator {

    public final static String buildingName = "Laboratory";
    public final static String structureName = "laboratory";
    public final static String upgradedStructureName = "laboratory_lightning";
    public final static ResourceCost cost = ResourceCosts.LABORATORY;

    // distance you can move away from a town centre before being turned back into a villager
    private final Set<BlockPos> lightningBorderBps = new HashSet<>();

    public Laboratory(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.BREWING_STAND;
        this.icon = new ResourceLocation("minecraft", "textures/block/brewing_stand.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.85f;

        this.canSetRallyPoint = false;

        this.startingBlockTypes.add(Blocks.SPRUCE_PLANKS);
        this.startingBlockTypes.add(Blocks.BLACKSTONE);

        Ability callLightning = new CallLightning(this);
        this.abilities.add(callLightning);

        if (level.isClientSide()) {
            this.productionButtons = Arrays.asList(
                ResearchHusks.getStartButton(this, Keybindings.keyQ),
                ResearchDrowned.getStartButton(this, Keybindings.keyW),
                ResearchStrays.getStartButton(this, Keybindings.keyE),
                ResearchSpiderJockeys.getStartButton(this, Keybindings.keyR),
                ResearchPoisonSpiders.getStartButton(this, Keybindings.keyT),
                ResearchSpiderWebs.getStartButton(this, Keybindings.keyY),
                ResearchSlimeConversion.getStartButton(this, Keybindings.keyU),
                ResearchLabLightningRod.getStartButton(this, Keybindings.keyI),
                ResearchSilverfish.getStartButton(this, Keybindings.keyO),
                ResearchSculkAmplifiers.getStartButton(this, Keybindings.keyP)
            );
            this.abilityButtons.add(callLightning.getButton(Keybindings.keyL));
        }
    }

    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAgeAfterBuilt > 0 && tickAgeAfterBuilt % 100 == 0)
            updateBorderBps();
    }

    private int getLightningRange() {
        return isUpgraded() && isBuilt ? CallLightning.RANGE : 0;
    }

    @Override
    public void updateBorderBps() {
        if (!level.isClientSide())
            return;
        this.lightningBorderBps.clear();
        this.lightningBorderBps.addAll(MiscUtil.getRangeIndicatorCircleBlocks(centrePos,
            getLightningRange() - TimeClientEvents.VISIBLE_BORDER_ADJ, level));
    }

    @Override
    public Set<BlockPos> getBorderBps() { return lightningBorderBps; }

    @Override
    public boolean showOnlyWhenSelected() {
        return true;
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    // return the lightning rod is built based on existing placed blocks
    // returns null if it is not build or is damaged
    // also will return null if outside of render range, but shouldn't matter since it'd be out of ability range anyway
    public BlockPos getLightningRodPos() {
        for (BuildingBlock block : blocks) {
            if (this.getLevel().getBlockState(block.getBlockPos()).getBlock() == Blocks.LIGHTNING_ROD &&
                this.getLevel().getBlockState(block.getBlockPos().below()).getBlock() == Blocks.WAXED_COPPER_BLOCK)
                return block.getBlockPos();
        }
        return null;
    }

    // check that the lightning rod is built based on existing placed blocks
    @Override
    public boolean isUpgraded() {
        for (BuildingBlock block : blocks)
            if (block.getBlockState().getBlock() == Blocks.LIGHTNING_ROD)
                return true;
        return false;
    }

    public void changeStructure(String newStructureName) {
        ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
        this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);
        super.refreshBlocks();
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            Laboratory.buildingName,
            new ResourceLocation("minecraft", "textures/block/brewing_stand.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == Laboratory.class,
            () -> false,
            () -> (BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) &&
                    BuildingClientEvents.hasFinishedBuilding(Graveyard.buildingName)) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(Laboratory.class),
            null,
            List.of(
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.laboratory"), Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.laboratory.tooltip1"), Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.laboratory.tooltip2"), Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.laboratory.tooltip3"), Style.EMPTY)
            ),
            null
        );
    }
}
