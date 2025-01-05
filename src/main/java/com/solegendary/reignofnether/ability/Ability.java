package com.solegendary.reignofnether.ability;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.tps.TPSClientEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Ability {
    public final UnitAction action; // null for worker building production items (handled specially in BuildingClientEvents)
    public final Level level;
    public final float cooldownMax;
    private float cooldown = 0;
    public final float range; // if <= 0, is melee
    public final float radius; // if <= 0, is single target
    public final boolean canTargetEntities;
    public boolean oneClickOneUse; // if true, a group of units/buildings will use their abilities one by one
    public boolean canAutocast = false;
    public boolean autocast = false;

    public Ability(UnitAction action, Level level, int cooldownMax, float range, float radius, boolean canTargetEntities) {
        this.action = action;
        this.level = level;
        this.cooldownMax = cooldownMax;
        this.range = range;
        this.radius = radius;
        this.canTargetEntities = canTargetEntities;
        this.oneClickOneUse = false;
    }

    public Ability(UnitAction action, Level level, int cooldownMax, float range, float radius, boolean canTargetEntities, boolean oneClickOneUse) {
        this.action = action;
        this.level = level;
        this.cooldownMax = cooldownMax;
        this.range = range;
        this.radius = radius;
        this.canTargetEntities = canTargetEntities;
        this.oneClickOneUse = oneClickOneUse;
    }

    public void tickCooldown() {
        if (this.cooldown > 0) {
            if (this.level.isClientSide())
                this.cooldown -= (TPSClientEvents.getCappedTPS() / 20D);
            else
                this.cooldown -= 1;
        }
    }

    public boolean isChanneling() { return false; }

    public float getCooldown() { return this.cooldown; }

    public boolean isOffCooldown() { return this.cooldown <= 0; }

    public void setToMaxCooldown() {
        this.cooldown = cooldownMax;
    }

    public void setCooldown(float cooldown) {
        if (this.level.isClientSide() && cooldown == cooldownMax) {
            HudClientEvents.setLowestCdHudEntity();
        }
        this.cooldown = Math.min(cooldown, cooldownMax);
    }

    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) { }

    public void use(Level level, Building buildingUsing, LivingEntity targetEntity) { }

    public void use(Level level, Unit unitUsing, BlockPos targetBp) { }

    public void use(Level level, Building buildingUsing, BlockPos targetBp) { }

    public AbilityButton getButton(Keybinding hotkey) {
        return null;
    }

    public boolean canBypassCooldown() { return false; }

    public boolean shouldResetBehaviours() { return true; }
}
