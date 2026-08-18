package com.estrativarus.amasmas.mixin;

import com.estrativarus.amasmas.mob.MiniWitherEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class MiniWitherBlockDestructionMixin {

    @Inject(
            method = "customServerAiStep",
            at = @At("HEAD")
    )
    private void amasmas$desactivarDestruccionMiniWither(
            ServerLevel level,
            CallbackInfo ci
    ) {

        WitherBoss wither =
                (WitherBoss) (Object) this;

        if (!MiniWitherEvents.esMiniWither(
                wither
        )) {

            return;
        }

        /*
         * El contador interno de destrucción corporal
         * no se expone directamente.
         *
         * Al mantener al Mini Wither sin invulnerabilidad
         * y limpiar las explosiones, la principal destrucción
         * quedará deshabilitada por el evento de explosión.
         *
         * El siguiente paso será añadir un accessor al contador
         * si tu mapping permite identificarlo.
         */
    }
}