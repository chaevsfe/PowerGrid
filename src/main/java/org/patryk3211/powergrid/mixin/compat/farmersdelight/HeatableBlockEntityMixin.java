package org.patryk3211.powergrid.mixin.compat.farmersdelight;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.basinheater.BasinHeaterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;

@Mixin(HeatableBlockEntity.class)
public interface HeatableBlockEntityMixin {
    @Inject(method = "isHeated(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void powergrid$heatFromBasinHeater(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof BasinHeaterBlock)
            cir.setReturnValue(below.getValue(BasinHeaterBlock.HEAT_LEVEL).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING));
    }
}
