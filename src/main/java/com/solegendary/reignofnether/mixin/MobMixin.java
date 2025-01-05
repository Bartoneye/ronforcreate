package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.alliance.AllianceSystem;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

// prevent vexes from charging enemies too far from their parent EvokerUnit
@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setTarget(@Nullable LivingEntity pTarget, CallbackInfo ci) {
        if (pTarget == null || !pTarget.isAlive())
            return;

        Entity entity = pTarget.getLevel().getEntity(this.getId());
        if (entity instanceof Vex vex &&
            vex.getOwner() instanceof EvokerUnit eu) {

            boolean outOfRange = eu.distanceTo(pTarget) > eu.getVexTargetRange();
            boolean targetIsAlliedPlayer = pTarget instanceof Player player &&
                    (AllianceSystem.isAllied(player.getName().getString(), eu.getOwnerName()) ||
                    player.getName().getString().equals(eu.getOwnerName()));
            if (outOfRange || targetIsAlliedPlayer)
                ci.cancel();
        }
    }
}
