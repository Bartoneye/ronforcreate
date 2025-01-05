package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.survival.SurvivalServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(
            method = "onChangedBlock",
            at = @At("TAIL"),
            cancellable = true
    )
    protected void onChangedBlock(BlockPos pPos, CallbackInfo ci) {
        Entity entity = this.getLevel().getEntity(this.getId());

        if (!this.getLevel().isClientSide() && entity instanceof Unit unit)
            if (SurvivalServerEvents.isEnabled() && SurvivalServerEvents.ENEMY_OWNER_NAME.equals(unit.getOwnerName())) {
                ci.cancel();
                FrostWalkerOnEntityMoved((LivingEntity) entity, this.level, pPos, 1);
            }
    }

    // copied from FrostWalkerEnchantment.onEntityMoved
    private void FrostWalkerOnEntityMoved(LivingEntity pLiving, Level pLevel, BlockPos pPos, int pLevelConflicting) {
        if (pLiving.isOnGround()) {

            float f = (float)Math.min(16, 2 + pLevelConflicting);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            Iterator var7 = BlockPos.betweenClosed(pPos.offset(-f, -1.0, -f), pPos.offset(f, -1.0, f)).iterator();

            while(true) {
                BlockPos blockpos;
                BlockState blockstate1;
                do {
                    do {
                        if (!var7.hasNext()) {
                            return;
                        }
                        blockpos = (BlockPos)var7.next();
                    } while(!blockpos.closerToCenterThan(pLiving.position(), f));

                    blockpos$mutableblockpos.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
                } while(!blockstate1.isAir());

                BlockState blockstate2 = pLevel.getBlockState(blockpos);
                boolean isFull = blockstate2.getBlock() == Blocks.WATER && blockstate2.getValue(LiquidBlock.LEVEL) == 0;

                BlockState iceState = Blocks.FROSTED_ICE.defaultBlockState();
                if (blockstate2.getMaterial() == Material.WATER && isFull && iceState.canSurvive(pLevel, blockpos) &&
                        pLevel.isUnobstructed(iceState, blockpos, CollisionContext.empty()) &&
                        !ForgeEventFactory.onBlockPlace(pLiving, BlockSnapshot.create(pLevel.dimension(), pLevel, blockpos), Direction.UP)) {

                    pLevel.setBlockAndUpdate(blockpos, iceState);
                    pLevel.scheduleTick(blockpos, Blocks.FROSTED_ICE, Mth.nextInt(pLiving.getRandom(), 60, 120));
                }

                isFull = blockstate2.getBlock() == Blocks.LAVA && blockstate2.getValue(LiquidBlock.LEVEL) == 0;
                BlockState magmaState = Blocks.NETHERRACK.defaultBlockState();
                if (blockstate2.getMaterial() == Material.LAVA && isFull && magmaState.canSurvive(pLevel, blockpos) &&
                        pLevel.isUnobstructed(magmaState, blockpos, CollisionContext.empty()) &&
                        !ForgeEventFactory.onBlockPlace(pLiving, BlockSnapshot.create(pLevel.dimension(), pLevel, blockpos), Direction.UP)) {

                    pLevel.setBlockAndUpdate(blockpos, magmaState);
                    pLevel.scheduleTick(blockpos, Blocks.NETHERRACK, Mth.nextInt(pLiving.getRandom(), 60, 120));
                }
            }
        }
    }
}
