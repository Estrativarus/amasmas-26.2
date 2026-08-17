package com.estrativarus.amasmas.deathtrain;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class MobDifficultyEvents {

    /*
     * Duración de 60 ticks = 3 segundos.
     *
     * Se renueva una vez por segundo durante el Death Train.
     * Al terminar, los efectos desaparecen rápidamente por sí solos.
     */
    private static final int DURACION_EFECTOS =
            20 * 30;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {

        /*
         * Mob incluye:
         *
         * - mobs hostiles;
         * - mobs neutrales;
         * - mobs pasivos.
         *
         * No incluye jugadores.
         */
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        /*
         * Solo trabajamos en el servidor.
         */
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Cada mob renueva los efectos una vez por segundo,
         * no veinte veces por segundo.
         */
        if (mob.tickCount % 200 != 0) {
            return;
        }

        DeathTrainSavedData deathTrain =
                DeathTrainSavedData.get(
                        level.getServer()
                );

        /*
         * Fuera del Death Train no renovamos nada.
         *
         * Los efectos anteriores caducarán en un máximo de
         * tres segundos.
         */
        if (!deathTrain.estaActivo()) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        int nivel =
                calcularNivelMobs(diaActual);

        /*
         * Días 1-20:
         *
         * el Death Train puede estar activo, pero los mobs
         * todavía no reciben efectos.
         */
        if (nivel <= 0) {
            return;
        }

        /*
         * Minecraft utiliza amplificadores desde cero:
         *
         * 0 = nivel I
         * 1 = nivel II
         * 2 = nivel III
         */
        int amplificador = nivel - 1;

        aplicarEfectos(mob, amplificador);
    }

    public static int calcularNivelMobs(int dia) {

        if (dia < 21) {
            return 0;
        }

        if (dia < 42) {
            return 1;
        }

        if (dia < 63) {
            return 2;
        }

        return 3;
    }

    private static void aplicarEfectos(
            Mob mob,
            int amplificador
    ) {

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.STRENGTH,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.RESISTANCE,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }

    private MobDifficultyEvents() {
    }
}