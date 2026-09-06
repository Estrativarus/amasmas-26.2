package com.estrativarus.amasmas.entity;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class GiantSpawnEvents {

    private static final int DIA_INICIO =
            21;

    @SubscribeEvent
    public static void onGiantSpawnPlacement(
            MobSpawnEvent.SpawnPlacementCheck event
    ) {

        if (event.getEntityType()
                != ModEntities.GIGANTE.get()) {

            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {

            event.setResult(
                    MobSpawnEvent
                            .SpawnPlacementCheck
                            .Result
                            .FAIL
            );

            return;
        }

        event.setResult(
                MobSpawnEvent
                        .SpawnPlacementCheck
                        .Result
                        .SUCCEED
        );
    }

    @SubscribeEvent
    public static void onGiantPositionCheck(
            MobSpawnEvent.PositionCheck event
    ) {

        if (event.getEntity()
                .getType()
                != ModEntities.GIGANTE.get()) {

            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {

            event.setResult(
                    MobSpawnEvent
                            .PositionCheck
                            .Result
                            .FAIL
            );

            return;
        }

        event.setResult(
                MobSpawnEvent
                        .PositionCheck
                        .Result
                        .SUCCEED
        );
    }

    private GiantSpawnEvents() {
    }
}