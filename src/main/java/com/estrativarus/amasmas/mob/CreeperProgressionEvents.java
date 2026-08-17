package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class CreeperProgressionEvents {

    private static final int DIA_INICIO =
            7;

    private static final int DURACION_EFECTOS =
            20 * 15;

    private static final int AMPLIFICADOR_VELOCIDAD =
            0;

    @SubscribeEvent
    public static void onCreeperJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob creeper)) {

            return;
        }

        if (creeper.getType()
                != EntityTypes.CREEPER) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                creeper,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onCreeperTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob creeper)) {

            return;
        }

        if (creeper.getType()
                != EntityTypes.CREEPER) {

            return;
        }

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (creeper.tickCount % 20 != 0) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                creeper,
                diaActual
        );
    }

    private static void aplicarProgresionActual(
            Mob creeper,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return;
        }

        aplicarEtapaDia7(
                creeper
        );

        if (diaActual >= 14) {

            aplicarEtapaDia14(
                    creeper
            );
        }

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    creeper
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    creeper
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    creeper
            );
        }
    }

    private static void aplicarEtapaDia7(
            Mob creeper
    ) {

        creeper.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        DURACION_EFECTOS,
                        AMPLIFICADOR_VELOCIDAD,
                        false,
                        false,
                        false
                )
        );
    }

    private static void aplicarEtapaDia14(
            Mob creeper
    ) {

    }

    private static void aplicarEtapaDia21(
            Mob creeper
    ) {

    }

    private static void aplicarEtapaDia42(
            Mob creeper
    ) {

    }

    private static void aplicarEtapaDia63(
            Mob creeper
    ) {

    }

    private CreeperProgressionEvents() {
    }
}