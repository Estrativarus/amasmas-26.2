package com.estrativarus.amasmas.mixin.client;

import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoggedRenderer.class)
public abstract class BoggedOuterLayerMixin {

    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void amasmas$ocultarCapaExteriorFinalizador(
            Bogged bogged,
            BoggedRenderState state,
            float partialTick,
            CallbackInfo ci
    ) {

        if (!bogged.hasCustomName()
                || bogged.getCustomName() == null) {

            return;
        }

        if (!bogged
                .getCustomName()
                .getString()
                .equals(
                        "Esqueleto Finalizador"
                )) {

            return;
        }

        state.isSheared =
                true;
    }
}