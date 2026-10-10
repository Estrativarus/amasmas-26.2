package com.estrativarus.amasmas.mixin;

import com.estrativarus.amasmas.item.ResonantiteArrowItem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelGameEventMixin {

    @Inject(
            method = "gameEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void amasmas$cancelarVibracionFlechaResonantita(
            Holder<GameEvent> gameEvent,
            Vec3 posicion,
            GameEvent.Context contexto,
            CallbackInfo ci
    ) {

        Entity fuente =
                contexto.sourceEntity();

        if (fuente == null) {
            return;
        }

        if (fuente
                .getPersistentData()
                .contains(
                        ResonantiteArrowItem
                                .TAG_FLECHA_RESONANTITA
                )) {

            ci.cancel();

            return;
        }

        if (fuente
                .getPersistentData()
                .contains(
                        ResonantiteArrowItem
                                .TAG_IMPACTO_RESONANTITA
                )) {

            ci.cancel();
        }
    }
}