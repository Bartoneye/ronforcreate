package com.solegendary.reignofnether.unit.units.piglins;

import com.solegendary.reignofnether.blocks.BlockServerEvents;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchSoulFireballs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class GhastUnitFireball extends LargeFireball {

    public static final int SOULSAND_DURATION = 160;

    public GhastUnitFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, int pExplosionPower) {
        super(pLevel, pShooter, pOffsetX, pOffsetY, pOffsetZ, pExplosionPower);
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);

        if (level.isClientSide())
            return;

        if (getOwner() instanceof GhastUnit ghastUnit &&
                ResearchServerEvents.playerHasResearch(ghastUnit.getOwnerName(), ResearchSoulFireballs.itemName)) {

            for (int x = -4; x < 4; x++) {
                for (int y = -4; y < 4; y++) {
                    for (int z = -4; z < 4; z++) {
                        BlockPos bp = getOnPos().offset(x, y, z);
                        BlockState bs = level.getBlockState(bp);
                        if (bs.getBlock() == Blocks.FIRE) {
                            for (BlockPos bpAdj : List.of(bp, bp.north(), bp.south(), bp.east(), bp.west())) {
                                BlockState bsBelow = level.getBlockState(bpAdj.below());
                                if (!bsBelow.isAir())
                                    BlockServerEvents.addTempBlock((ServerLevel) level, bpAdj.below(), Blocks.SOUL_SAND.defaultBlockState(), bsBelow, SOULSAND_DURATION);
                            }
                            for (BlockPos bpAdj : List.of(
                                    bp.north().north(),
                                    bp.south().south(),
                                    bp.east().east(),
                                    bp.west().west(),
                                    bp.north().west(),
                                    bp.north().east(),
                                    bp.south().west(),
                                    bp.south().east()
                                )) {
                                if (random.nextBoolean()) {
                                    BlockState bsBelow = level.getBlockState(bpAdj.below());
                                    if (!bsBelow.isAir())
                                        BlockServerEvents.addTempBlock((ServerLevel) level, bpAdj.below(), Blocks.SOUL_SAND.defaultBlockState(), bsBelow, SOULSAND_DURATION);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
