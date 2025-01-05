package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.monsters.SlimeProd;
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
import static com.solegendary.reignofnether.building.BuildingUtils.getMinCorner;

public class SlimePit extends ProductionBuilding {

    public final static String buildingName = "Slime Pit";
    public final static String structureName = "slime_pit";
    public final static ResourceCost cost = ResourceCosts.SLIME_PIT;

    public SlimePit(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.SLIME_BLOCK;
        this.icon = new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/slime.png");

        this.canSetRallyPoint = true;

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.POLISHED_DEEPSLATE);
        this.startingBlockTypes.add(Blocks.COBBLED_DEEPSLATE);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                SlimeProd.getStartButton(this, Keybindings.keyQ)
            );
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            SlimePit.buildingName,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/slime.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == SlimePit.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Graveyard.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(SlimePit.class),
            null,
            List.of(
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.slime_pit"), Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.slime_pit.tooltip1"), Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.slime_pit.tooltip3"), Style.EMPTY)
            ),
            null
        );
    }

    public BlockPos getDefaultOutdoorSpawnPoint() {
        return getMinCorner(this.blocks).offset(-spawnRadiusOffset + 4, 0, -spawnRadiusOffset + 9);
    }
}
