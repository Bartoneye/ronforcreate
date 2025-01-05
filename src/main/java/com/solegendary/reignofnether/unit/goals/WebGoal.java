package com.solegendary.reignofnether.unit.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;

public class WebGoal extends AbstractCastTargetedSpellGoal {

    public WebGoal(Mob mob, int channelTicks, int range, Consumer<LivingEntity> onCastEntity, Consumer<BlockPos> onCastGround) {
        super(mob, channelTicks, range, onCastEntity, onCastGround, null);
    }
}
