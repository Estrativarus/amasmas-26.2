package com.estrativarus.amasmas.entity;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.entity.monster.FinalizerSkeleton;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class FinalizerSkeletonRuntimeEvents {

    @SubscribeEvent
    public static void onFinalizerJoinLevel(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof FinalizerSkeleton finalizador)) {

            return;
        }

        level.getServer().execute(() -> {

            if (!finalizador.isAlive()
                    || finalizador.isRemoved()) {

                return;
            }

            finalizador.aplicarEquipoInicial(
                    level
            );
        });
    }

    private FinalizerSkeletonRuntimeEvents() {
    }
}