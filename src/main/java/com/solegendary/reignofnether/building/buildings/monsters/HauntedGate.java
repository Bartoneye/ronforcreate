package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.ToggleGate;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class HauntedGate extends Building implements GarrisonableBuilding {

    public final static String buildingName = "Open Haunted Gate";
    public final static String structureName = "old_stone_gate_open";

    public final static String buildingNameShut = "Shut Haunted Gate";
    public final static String structureNameShut = "old_stone_gate_closed";

    public final static ResourceCost cost = ResourceCosts.HAUNTED_GATE;

    private final static int MAX_OCCUPANTS = 4;

    private HauntedGate.GateType gateType = GateType.GATE_OPEN;


    public HauntedGate(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.CRACKED_DEEPSLATE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/cracked_deepslate_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.0f;

        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICKS);
        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICK_STAIRS);

        Ability toggleGate = new ToggleGate(this);
        this.abilities.add(toggleGate);

        if (level.isClientSide()) {
            this.abilityButtons.add(toggleGate.getButton(Keybindings.keyQ));
        }

    }

    public Faction getFaction() {
        return Faction.MONSTERS;
    }

    // don't use this for abilities as it may not be balanced
    public int getAttackRange() {
        return 16;
    }

    // bonus for units attacking garrisoned units
    public int getExternalAttackRangeBonus() {
        return 10;
    }

    public boolean canDestroyBlock(BlockPos relativeBp) {
        return relativeBp.getY() != 10 &&
                relativeBp.getY() != 11;
    }

    public HauntedGate.GateType getGateType() {
        return this.gateType;
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


    public void changeStructure(GateType gateType) {
        if (!this.getLevel().isClientSide() || (this.getLevel().isClientSide())) {
            String newStructureName = "";
            switch (gateType) {
                case GATE_OPEN -> {
                    //This is the OPEN gate case
                    HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.toggle_gate.gateopen"));
                    this.name = buildingName;
                    this.gateType = Building.GateType.GATE_OPEN;
                    newStructureName = HauntedGate.structureName;
                }
                case GATE_SHUT -> {
                    //This is the CLOSED gate case
                    HudClientEvents.showTemporaryMessage(I18n.get("abilities.reignofnether.toggle_gate.gateshut"));
                    this.name = buildingNameShut;
                    this.gateType = Building.GateType.GATE_SHUT;
                    newStructureName = HauntedGate.structureNameShut;
                }
            }


            if (!newStructureName.isEmpty()) {
                ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
                this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);

                Level level = this.getLevel(); // Get the current world
                for (BuildingBlock block : this.blocks) {
                    BlockPos position = block.getBlockPos(); // Get the block's position
                    BlockState state = block.getBlockState(); // Get the block's state
                    if (position != null && state != null) {
                        // Get the block's name from its Block object
                        String blockName = state.getBlock().getDescriptionId(); // Description ID typically gives the unlocalized name

                        // Check if the block's name contains "fence" (case-insensitive)
                        if (blockName.toLowerCase().contains("fence") || blockName.toLowerCase().contains("air") || blockName.toLowerCase().contains("pulley")) {
                            level.setBlockAndUpdate(position, state); // Update block in the world
                        }
                    }
                }

                super.refreshBlocks();

            }
        }
    }


    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                HauntedGate.buildingName,
                new ResourceLocation("minecraft", "textures/block/cracked_deepslate_bricks.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == HauntedGate.class,
                TutorialClientEvents::isEnabled,
                () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(HauntedGate.class),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.haunted_gate"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.haunted_gate.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.haunted_gate.tooltip2"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("buildings.monsters.reignofnether.haunted_gate.tooltip3", MAX_OCCUPANTS), Style.EMPTY)
                ),
                null
        );
    }

}

