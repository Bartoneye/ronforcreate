package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WaveEnemy {

    //private static final int PERIODIC_COMMAND_INTERVAL = 200;
    private static final int IDLE_COMMAND_INTERVAL = 100;

    public final Unit unit;
    private long idleTicks = 0;
    private long ticks = 0;

    private BlockPos lastOnPos;

    public WaveEnemy(Unit unit) {
        this.unit = unit;
        this.lastOnPos = getEntity().getOnPos();
    }

    public LivingEntity getEntity() {
        return ((LivingEntity) unit);
    }

    public void tick(long ticksToAdd) {
        if (getEntity().isPassenger())
            return;

        ticks += ticksToAdd;

        boolean isAttacking = unit.getTargetGoal().getTarget() != null;
        if (!isAttacking &&
            unit instanceof AttackerUnit aUnit &&
            aUnit.getAttackBuildingGoal() instanceof MeleeAttackBuildingGoal mabg &&
            mabg.isAttacking())
            isAttacking = true;

        BlockPos onPos = getEntity().getOnPos();
        if (onPos.equals(lastOnPos) && !isAttacking)
            idleTicks += ticksToAdd;
        else
            idleTicks = 0;

        lastOnPos = onPos;

        if (ticks > 0 && ticks == ticksToAdd * 10)
            startingCommand();

        //if (ticks > 0 && ticks % PERIODIC_COMMAND_INTERVAL == 0)
        //    periodicCommand();

        if (idleTicks > 0 && idleTicks % IDLE_COMMAND_INTERVAL == 0)
            idleCommand();

        if (unit instanceof CreeperUnit creeperUnit) {
            Building nearestBuilding = getNearestAttackableBuilding();
            if (nearestBuilding != null) {
                BlockPos bpTarget = nearestBuilding.getClosestGroundPos(((Entity) unit).getOnPos(), 1);
                if (creeperUnit.distanceToSqr(Vec3.atCenterOf(bpTarget)) < 4)
                    creeperUnit.startToExplode();
            }
        }
    }

    // done shortly after spawn
    public void startingCommand() {
        attackMoveNearestBuilding();
    }

    // done every X ticks
    //public void periodicCommand() {
    //    attackMoveNearestBuilding();
    //}

    // done if the unit didn't change position in X ticks
    public void idleCommand() {
        if (unit instanceof CreeperUnit ||
            (unit instanceof AttackerUnit aUnit && aUnit.canAttackBuildings()))
            attackMoveNearestBuilding();
        else
            attackMoveNearestUnit();
    }

    // done when attacked
    public void retaliateCommand() { }

    private Building getNearestAttackableBuilding() {
        List<Building> buildings = BuildingServerEvents.getBuildings().stream()
                .filter(b -> !SurvivalServerEvents.ENEMY_OWNER_NAME.equals(b.ownerName) && !b.ownerName.isBlank())
                .sorted(Comparator.comparing(b -> b.centrePos.distToCenterSqr(((Entity) unit).position())))
                .toList();

        BlockPos targetBp = null;
        if (!buildings.isEmpty())
            return buildings.get(0);

        return null;
    }

    private LivingEntity getNearestAttackableUnit() {
        List<LivingEntity> entities = UnitServerEvents.getAllUnits().stream()
                .filter(le -> le instanceof Unit u && !SurvivalServerEvents.ENEMY_OWNER_NAME.equals(u.getOwnerName()) && !u.getOwnerName().isBlank())
                .sorted(Comparator.comparing(le -> le.position().distanceToSqr(((Entity) unit).position())))
                .toList();

        BlockPos targetBp = null;
        if (!entities.isEmpty())
            return entities.get(0);

        return null;
    }

    private void attackMoveNearestBuilding() {
        unit.resetBehaviours();

        Entity entity = (Entity) unit;
        Building nearestBuilding = getNearestAttackableBuilding();

        BlockPos targetBp = null;
        if (nearestBuilding != null)
            targetBp = nearestBuilding.getClosestGroundPos(((Entity) unit).getOnPos(), 1);

        if (targetBp != null) {
            if (unit instanceof AttackerUnit)
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.ATTACK_MOVE, -1,
                        new int[]{entity.getId()},  targetBp, new BlockPos(0,0,0));
            else
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.MOVE, -1,
                        new int[]{entity.getId()},  targetBp, new BlockPos(0,0,0));
        }
    }

    private void attackMoveRandomBuilding() {
        unit.resetBehaviours();

        ArrayList<Building> buildings = BuildingServerEvents.getBuildings();
        Collections.shuffle(buildings);

        List<Building> playerBuildings = buildings.stream()
                .filter(b -> !SurvivalServerEvents.ENEMY_OWNER_NAME.equals(b.ownerName) && !b.ownerName.isBlank())
                .toList();

        BlockPos targetBp = null;
        if (!playerBuildings.isEmpty())
            targetBp = buildings.get(0).getClosestGroundPos(((Entity) unit).getOnPos(), 1);


        if (targetBp != null) {
            if (unit instanceof AttackerUnit)
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.ATTACK_MOVE, -1,
                        new int[]{((Entity) unit).getId()},  targetBp, new BlockPos(0,0,0));
            else
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.MOVE, -1,
                        new int[]{((Entity) unit).getId()},  targetBp, new BlockPos(0,0,0));
        }
    }

    private void attackMoveNearestUnit() {
        unit.resetBehaviours();

        Entity entity = (Entity) unit;
        LivingEntity nearestUnit = getNearestAttackableUnit();

        BlockPos targetBp = null;
        if (nearestUnit != null)
            targetBp = nearestUnit.getOnPos();

        if (targetBp != null) {
            if (unit instanceof AttackerUnit)
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.ATTACK_MOVE, -1,
                        new int[]{((Entity) unit).getId()},  targetBp, new BlockPos(0,0,0));
            else
                UnitServerEvents.addActionItem(unit.getOwnerName(), UnitAction.MOVE, -1,
                        new int[]{((Entity) unit).getId()},  targetBp, new BlockPos(0,0,0));
        }
    }

    private void attackNearestWorker() {

    }
}
