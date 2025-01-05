package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.villagers.OakStockpile;
import com.solegendary.reignofnether.building.buildings.villagers.TownCentre;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.UnitConvertClientboundPacket;
import com.solegendary.reignofnether.unit.units.villagers.MilitiaUnit;
import com.solegendary.reignofnether.unit.units.villagers.VillagerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Move a villager towards a building to become a militia

public class CallToArmsGoal extends MoveToTargetBlockGoal {

    private Building buildingTarget;

    public CallToArmsGoal(Mob mob) {
        super(mob, true, 0);
    }

    public void tick() {
        if (buildingTarget == null)
            return;
        calcMoveTarget();
        if (this.mob.tickCount % 20 == 0)
            start();

        if (isInRange() && buildingTarget != null && !this.mob.getLevel().isClientSide())
            if (this.mob instanceof VillagerUnit villagerUnit)
                villagerUnit.convertToMilitia();
    }

    private void calcMoveTarget() {
        if (this.buildingTarget != null)
            this.moveTarget = this.buildingTarget.getClosestGroundPos(mob.getOnPos(), 1);
    }

    public boolean isInRange() {
        if (buildingTarget != null && this.moveTarget != null)
            if (BuildingServerEvents.getUnitToBuildingRelationship((Unit) this.mob, buildingTarget) == Relationship.OWNED)
                return MiscUtil.isMobInRangeOfPos(moveTarget, mob, 2);
        return false;
    }

    public void setNearestTownCentreAsTarget() {
        Building building = BuildingUtils.findClosestBuilding(mob.level.isClientSide(), this.mob.getEyePosition(),
                (b) -> b.isBuilt && b.ownerName.equals(((Unit) mob).getOwnerName()) && b instanceof TownCentre);
        if (building instanceof TownCentre townCentre)
            setBuildingTarget(townCentre);
    }

    private void setBuildingTarget(@Nullable Building target) {
        if (target != null) {
            MiscUtil.addUnitCheckpoint((Unit) mob, new BlockPos(
                    target.centrePos.getX(),
                    target.originPos.getY() + 1,
                    target.centrePos.getZ())
            );
            ((Unit) mob).setIsCheckpointGreen(true);
        }
        this.buildingTarget = target;
        calcMoveTarget();
        this.start();
    }

    public Building getBuildingTarget() { return buildingTarget; }

    @Override
    public void stop() {
        buildingTarget = null;
        super.stop();
    }
}
