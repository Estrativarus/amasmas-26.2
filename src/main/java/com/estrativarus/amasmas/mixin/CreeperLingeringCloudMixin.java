package com.estrativarus.amasmas.mixin;

import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperLingeringCloudMixin {

    @Inject(
            method = "spawnLingeringCloud",
            at = @At("HEAD"),
            cancellable = true
    )
    private void amasmas$cancelarNubeCreeper(
            CallbackInfo ci
    ) {

        Creeper creeper =
                (Creeper) (Object) this;

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        ci.cancel();
    }
}