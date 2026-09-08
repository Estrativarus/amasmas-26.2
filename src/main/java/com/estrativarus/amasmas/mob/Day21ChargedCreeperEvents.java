package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.mixin.CreeperPoweredAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class Day21ChargedCreeperEvents {

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onCreeperJoinLevel(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Creeper creeper)) {

            return;
        }

        level.getServer().execute(() -> {

            if (!creeper.isAlive()
                    || creeper.isRemoved()) {

                return;
            }

            cargarSiCorresponde(
                    level,
                    creeper
            );
        });
    }

    @SubscribeEvent
    public static void onCreeperTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Creeper creeper)) {

            return;
        }

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((creeper.tickCount
                + creeper.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        cargarSiCorresponde(
                level,
                creeper
        );
    }

    private static void cargarSiCorresponde(
            ServerLevel level,
            Creeper creeper
    ) {

        if (creeper.isPowered()) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        creeper
                .getEntityData()
                .set(
                        CreeperPoweredAccessor
                                .amasmas$getPoweredDataAccessor(),
                        true
                );
    }

    private Day21ChargedCreeperEvents() {
    }
}