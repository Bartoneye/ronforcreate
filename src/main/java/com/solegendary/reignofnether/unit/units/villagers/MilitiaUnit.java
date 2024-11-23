package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.ArmSwingingUnit;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitConvertClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MilitiaUnit extends Vindicator implements Unit, AttackerUnit, VillagerDataHolder, ConvertableUnit {
    // region
    private final ArrayList<BlockPos> checkpoints = new ArrayList<>();
    private int checkpointTicksLeft = UnitClientEvents.CHECKPOINT_TICKS_MAX;
    public ArrayList<BlockPos> getCheckpoints() { return checkpoints; };
    public int getCheckpointTicksLeft() { return checkpointTicksLeft; }
    public void setCheckpointTicksLeft(int ticks) { checkpointTicksLeft = ticks; }
    private boolean isCheckpointGreen = true;
    public boolean isCheckpointGreen() { return isCheckpointGreen; };
    public void setIsCheckpointGreen(boolean green) { isCheckpointGreen = green; };
    private int entityCheckpointId = -1;
    public int getEntityCheckpointId() { return entityCheckpointId; };
    public void setEntityCheckpointId(int id) { entityCheckpointId = id; };

    GarrisonGoal garrisonGoal;
    public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
    public boolean canGarrison() { return getGarrisonGoal() != null; }

    UsePortalGoal usePortalGoal;
    public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
    public boolean canUsePortal() { return getUsePortalGoal() != null; }

    public Faction getFaction() {return Faction.VILLAGERS;}
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;};
    public List<Ability> getAbilities() {return abilities;}
    public List<ItemStack> getItems() {return items;};
    public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
    public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
    public BuildRepairGoal getBuildRepairGoal() {return null;}
    public GatherResourcesGoal getGatherResourceGoal() {return null;}
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    private ReturnResourcesGoal returnResourcesGoal;
    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    public LivingEntity getFollowTarget() { return followTarget; }
    public boolean getHoldPosition() { return holdPosition; }
    public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

    // if true causes moveGoal and attackGoal to work together to allow attack moving
    // moves to a block but will chase/attack nearby monsters in range up to a certain distance away
    private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
    private boolean holdPosition = false;
    private BlockPos attackMoveTarget = null;

    // which player owns this unit? this format ensures its synched to client without having to use packets
    public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
    public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
    public static final EntityDataAccessor<String> ownerDataAccessor =
            SynchedEntityData.defineId(MilitiaUnit.class, EntityDataSerializers.STRING);

    // combat stats
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    public int getPopCost() {return popCost;}
    public boolean getWillRetaliate() {return willRetaliate;}
    public int getAttackCooldown() {return (int) (20 / attacksPerSecond);}
    public float getAttacksPerSecond() {return attacksPerSecond;}
    public float getAggroRange() {return aggroRange;}
    public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
    public float getAttackRange() {return attackRange;}
    public float getUnitAttackDamage() {return attackDamage;}
    public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}
    public Goal getAttackGoal() { return attackGoal; }
    public Goal getAttackBuildingGoal() { return attackBuildingGoal; }
    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // ConvertableUnit
    private boolean shouldDiscard = false;
    public boolean shouldDiscard() { return shouldDiscard; }
    public void setShouldDiscard(boolean discard) { this.shouldDiscard = discard; }

    // endregion

    // ticks before turning back into a villager
    final static public float DURATION_TICKS_MAX = 1200;
    public float durationTicksLeft = DURATION_TICKS_MAX;
    public boolean converted = false;

    final static public float attackDamage = 3.0f;
    final static public float attacksPerSecond = 0.5f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 0;
    final static public boolean willRetaliate = false; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = false;

    final static public float maxHealth = 30.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    final static public int popCost = ResourceCosts.MILITIA.population;
    public int maxResources = 100;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public MilitiaUnit(EntityType<? extends Vindicator> entityType, Level level) {
        super(entityType, level);

        if (level.isClientSide()) {
            AbilityButton townCentreButton = TownCentre.getBuildButton(Keybindings.keyQ);
            this.abilityButtons.add(townCentreButton);
            this.abilityButtons.add(OakStockpile.getBuildButton(Keybindings.keyW));
            this.abilityButtons.add(VillagerHouse.getBuildButton(Keybindings.keyE));
            this.abilityButtons.add(WheatFarm.getBuildButton(Keybindings.keyR));
            this.abilityButtons.add(Watchtower.getBuildButton(Keybindings.keyT));
            this.abilityButtons.add(Barracks.getBuildButton(Keybindings.keyY));
            this.abilityButtons.add(Blacksmith.getBuildButton(Keybindings.keyU));
            this.abilityButtons.add(ArcaneTower.getBuildButton(Keybindings.keyI));
            this.abilityButtons.add(Library.getBuildButton(Keybindings.keyO));
            this.abilityButtons.add(Castle.getBuildButton(Keybindings.keyP));
            this.abilityButtons.add(IronGolemBuilding.getBuildButton(Keybindings.keyL));
            this.abilityButtons.add(OakBridge.getBuildButton(Keybindings.keyC));
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, MilitiaUnit.attackDamage)
                .add(Attributes.MOVEMENT_SPEED, MilitiaUnit.movementSpeed)
                .add(Attributes.MAX_HEALTH, MilitiaUnit.maxHealth)
                .add(Attributes.ARMOR, MilitiaUnit.armorValue);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT;}
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }
    @Override
    protected SoundEvent getHurtSound(DamageSource p_34103_) { return SoundEvents.VILLAGER_HURT; }
    @Override
    public boolean isLeftHanded() { return false; }
    @Override // prevent vanilla logic for picking up items
    protected void pickUpItem(ItemEntity pItemEntity) { }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);

        if (durationTicksLeft <= 0 && !converted && !level.isClientSide()) {
            int newId = this.convertToUnit(EntityRegistrar.VILLAGER_UNIT.get());
            UnitConvertClientboundPacket.syncConvertedUnits(getOwnerName(), List.of(getId()), List.of(newId));
            converted = true;
        } else {
            durationTicksLeft -= 1;
        }
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new MeleeAttackUnitGoal(this, true);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void setupEquipmentAndUpgradesClient() {
        ItemStack axeStack = new ItemStack(Items.STONE_SWORD);
        this.setItemSlot(EquipmentSlot.MAINHAND, axeStack);
    }

    @Override
    public void setupEquipmentAndUpgradesServer() {
        ItemStack axeStack = new ItemStack(Items.STONE_SWORD);
        this.setItemSlot(EquipmentSlot.MAINHAND, axeStack);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        return pSpawnData;
    }

    private static final EntityDataAccessor<VillagerData> VILLAGER_DATA;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
        this.entityData.define(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.ARMORER, 1));
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(VILLAGER_DATA);
    }

    @Override
    public void setVillagerData(VillagerData p_35437_) {
        VillagerData villagerdata = this.getVillagerData();
        this.entityData.set(VILLAGER_DATA, p_35437_);
    }

    static {
        VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
    }
}
