package com.estrativarus.amasmas.mixin.client;

import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoggedRenderer.class)
public abstract class BoggedFinalizerRendererMixin {

    private static final String NOMBRE_FINALIZADOR =
            "Esqueleto Finalizador";

    private static final Identifier TEXTURA_FINALIZADOR =
            Identifier.fromNamespaceAndPath(
                    "amasmas",
                    "textures/entity/skeleton/esqueleto_finalizador.png"
            );

    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void amasmas$guardarNombreFinalizador(
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
                        NOMBRE_FINALIZADOR
                )) {

            return;
        }

        state.nameTag =
                Component.literal(
                        NOMBRE_FINALIZADOR
                );
    }

    @Inject(
            method = "getTextureLocation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void amasmas$usarTexturaFinalizador(
            BoggedRenderState state,
            CallbackInfoReturnable<Identifier> cir
    ) {

        if (state.nameTag == null) {
            return;
        }

        if (!state
                .nameTag
                .getString()
                .equals(
                        NOMBRE_FINALIZADOR
                )) {

            return;
        }

        cir.setReturnValue(
                TEXTURA_FINALIZADOR
        );
    }
}