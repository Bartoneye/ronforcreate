package com.solegendary.reignofnether.unit.controls;

import com.solegendary.reignofnether.unit.units.gaia.DragonUnit;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class DragonUnitMoveControl extends MoveControl {
    private final DragonUnit dragon;
    private int floatDuration;

    // 0.05 == 0.25 for movement speed attribute
    private static final float MOVE_SPEED_RATIO = 5;

    public DragonUnitMoveControl(DragonUnit pDragon) {
        super(pDragon);
        this.dragon = pDragon;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            if (this.floatDuration-- <= 0) {
                this.floatDuration += this.dragon.getRandom().nextInt(5) + 2;
                Vec3 $$0 = new Vec3(this.wantedX - this.dragon.getX(), this.wantedY - this.dragon.getY(), this.wantedZ - this.dragon.getZ());
                double $$1 = $$0.length();
                $$0 = $$0.normalize();
                AttributeInstance ms = this.dragon.getAttribute(Attributes.MOVEMENT_SPEED);
                if (ms != null) {
                    this.dragon.setDeltaMovement(this.dragon.getDeltaMovement().add($$0.scale(ms.getValue() / MOVE_SPEED_RATIO)));
                }
            }
        }
    }
}