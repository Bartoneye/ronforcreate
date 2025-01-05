package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.BackToWorkBuilding;
import com.solegendary.reignofnether.ability.abilities.CallLightning;
import com.solegendary.reignofnether.ability.abilities.CallToArmsBuilding;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.unit.units.villagers.EngineerProd;
import com.solegendary.reignofnether.unit.units.villagers.VillagerProd;
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

public class TownCentre extends ProductionBuilding implements RangeIndicator {

    public final static String buildingName = "Town Centre";
    public final static String structureName = "town_centre";
    public final static ResourceCost cost = ResourceCosts.TOWN_CENTRE;

    // distance you can move away from a town centre before being turned back into a villager
    public static final int MILITIA_RANGE = 60;
    private final Set<BlockPos> militiaBorderBps = new HashSet<>();

    public TownCentre(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), true);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.POLISHED_GRANITE;
        this.icon = new ResourceLocation("minecraft", "textures/block/polished_granite.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.331f; // 100s total build time with 1 villager
        this.canAcceptResources = true;

        this.startingBlockTypes.add(Blocks.STONE_BRICK_STAIRS);
        this.startingBlockTypes.add(Blocks.GRASS_BLOCK);
        this.startingBlockTypes.add(Blocks.POLISHED_ANDESITE_STAIRS);

        Ability callToArms = new CallToArmsBuilding(level);
        this.abilities.add(callToArms);
        BackToWorkBuilding backToWork = new BackToWorkBuilding(this.level);
        this.abilities.add(backToWork);

        if (level.isClientSide()) {
            this.productionButtons = Arrays.asList(
                    VillagerProd.getStartButton(this, Keybindings.keyQ),
                    EngineerProd.getStartButton(this, Keybindings.keyW)
            );
            this.abilityButtons.add(callToArms.getButton(Keybindings.keyV));
            this.abilityButtons.add(backToWork.getButton(Keybindings.build));
        }
    }

    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAgeAfterBuilt > 0 && tickAgeAfterBuilt % 100 == 0)
            updateBorderBps();
    }

    private int getBorderRange() {
        return isBuilt ? MILITIA_RANGE : 0;
    }

    @Override
    public void updateBorderBps() {
        if (!level.isClientSide())
            return;
        this.militiaBorderBps.clear();
        this.militiaBorderBps.addAll(MiscUtil.getRangeIndicatorCircleBlocks(centrePos,
            getBorderRange() - TimeClientEvents.VISIBLE_BORDER_ADJ, level));
    }

    @Override
    public Set<BlockPos> getBorderBps() {
        return militiaBorderBps;
    }

    @Override
    public boolean showOnlyWhenSelected() {
        return true;
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                TownCentre.buildingName,
                new ResourceLocation("minecraft", "textures/block/polished_granite.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == TownCentre.class,
                () -> false,
                () -> true,
                () -> BuildingClientEvents.setBuildingToPlace(TownCentre.class),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.town_centre"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedPop(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.villagers.reignofnether.town_centre.tooltip1"), Style.EMPTY)
                ),
                null
        );
    }
}
