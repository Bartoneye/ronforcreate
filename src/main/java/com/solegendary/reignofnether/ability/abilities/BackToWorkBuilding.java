package com.solegendary.reignofnether.ability.abilities;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.buildings.villagers.TownCentre;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.sounds.SoundAction;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.ZombieUnit;
import com.solegendary.reignofnether.unit.units.villagers.MilitiaUnit;
import com.solegendary.reignofnether.unit.units.villagers.VillagerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.solegendary.reignofnether.unit.UnitClientEvents.sendUnitCommand;

public class BackToWorkBuilding extends Ability {

    private static final int RANGE = TownCentre.MILITIA_RANGE + 5;

    public BackToWorkBuilding(Level level) {
        super(
                UnitAction.BACK_TO_WORK_BUILDING,
                level,
                0,
                RANGE,
                0,
                false,
                false
        );
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Back to Work (Building)",
                new ResourceLocation("minecraft", "textures/item/iron_pickaxe.png"),
                hotkey,
                () -> false,
                () -> false,
                () -> true,
                () -> sendUnitCommand(UnitAction.BACK_TO_WORK_BUILDING),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.back_to_work_building"), Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.back_to_work_building.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("abilities.reignofnether.back_to_work_building.tooltip2"), Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, BlockPos targetBp) {
        if (!level.isClientSide()) {

            List<VillagerUnit> nearbyVillagers = MiscUtil.getEntitiesWithinRange(
                            new Vector3d(buildingUsing.centrePos.getX(), buildingUsing.centrePos.getY(), buildingUsing.centrePos.getZ()),
                            range, VillagerUnit.class, buildingUsing.getLevel())
                    .stream()
                    .filter(u -> u.getOwnerName().equals(buildingUsing.ownerName))
                    .toList();

            for (VillagerUnit vUnit : nearbyVillagers) {
                vUnit.callToArmsGoal.stop();
                Unit.resetBehaviours(vUnit);
                vUnit.getGatherResourceGoal().saveData = vUnit.getGatherResourceGoal().permSaveData;
                vUnit.getGatherResourceGoal().loadState();
            }

            List<MilitiaUnit> nearbyMilitia = MiscUtil.getEntitiesWithinRange(
                            new Vector3d(buildingUsing.centrePos.getX(), buildingUsing.centrePos.getY(), buildingUsing.centrePos.getZ()),
                            range, MilitiaUnit.class, buildingUsing.getLevel())
                    .stream()
                    .filter(u -> u.getOwnerName().equals(buildingUsing.ownerName))
                    .toList();

            for (MilitiaUnit mUnit : nearbyMilitia)
                mUnit.convertToVillager();
        }
    }
}
