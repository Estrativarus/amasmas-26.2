package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class InfestedMobProgressionEvents {

    private static final int DIA_INICIO =
            14;

    private static final int INTERVALO_ACTUALIZACION =
            100;

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (!esMobPermitido(mob)) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionCompartida(
                mob,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onInfestedMobTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (!esMobPermitido(mob)) {
            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((mob.tickCount + mob.getId())
                % INTERVALO_ACTUALIZACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionCompartida(
                mob,
                diaActual
        );
    }

    private static void aplicarProgresionCompartida(
            Mob mob,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return;
        }

        CaveSpiderProgressionEvents
                .aplicarProgresion(
                        mob,
                        diaActual
                );
    }

    private static boolean esMobPermitido(
            Mob mob
    ) {

        return mob.getType()
                == EntityTypes.SILVERFISH

                || mob.getType()
                == EntityTypes.ENDERMITE;
    }

    private InfestedMobProgressionEvents() {
    }
}