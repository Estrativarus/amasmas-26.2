package com.estrativarus.amasmas.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class TorchflowerLightMixin {

    /*
     * Interceptamos la consulta del nivel de luz.
     *
     * Si el estado consultado pertenece a una Torchflower,
     * devolvemos nivel 10, igual que una antorcha de almas.
     */
    @Inject(
            method = "getLightEmission",
            at = @At("HEAD"),
            cancellable = true
    )
    private void amasmas$torchflowerLight(
            CallbackInfoReturnable<Integer> cir
    ) {

        /*
         * El Mixin se aplica sobre BlockStateBase.
         * En tiempo de ejecución, la instancia también es
         * un BlockState.
         */
        BlockBehaviour.BlockStateBase state =
                (BlockBehaviour.BlockStateBase) (Object) this;

        if (state.is(Blocks.TORCHFLOWER)) {
            cir.setReturnValue(10);
        }
    }
}