package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchCubeMagma;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class BasaltSprings extends ProductionBuilding {

    public final static String buildingName = "Basalt Springs";
    public final static String structureName = "basalt_springs";
    public final static ResourceCost cost = ResourceCosts.BASALT_SPRINGS;

    public BasaltSprings(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.POLISHED_BASALT;
        this.icon = new ResourceLocation("minecraft", "textures/block/polished_basalt_top.png");

        this.canSetRallyPoint = false;

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.BASALT);
        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                    ResearchCubeMagma.getStartButton(this, Keybindings.keyQ)
            );
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            BasaltSprings.buildingName,
            new ResourceLocation("minecraft", "textures/block/polished_basalt_top.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == BasaltSprings.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Bastion.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(BasaltSprings.class),
            null,
            List.of(
                FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.basalt_springs"), Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.basalt_springs.tooltip1"), Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.basalt_springs.tooltip2"), Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.piglins.reignofnether.basalt_springs.tooltip3"), Style.EMPTY)
            ),
            null
        );
    }
}
