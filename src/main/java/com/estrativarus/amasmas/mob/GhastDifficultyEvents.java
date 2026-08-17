package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class GhastDifficultyEvents {

    /*
     * Vida vanilla del Ghast:
     *
     * 10 puntos = 5 corazones.
     *
     * Vida mejorada:
     *
     * 20 puntos = 10 corazones.
     */
    private static final double VIDA_GHAST_MEJORADO =
            20.0D;

    /*
     * Radio/potencia de explosión.
     *
     * Vanilla = 1.
     * Mejorado = 2.
     */
    private static final int POTENCIA_EXPLOSION =
            6;

    /*
     * Comprobamos a los Ghasts una vez por segundo.
     *
     * Esto permite modificar también los que ya estuvieran
     * generados antes de alcanzar el día 7.
     */
    private static final int INTERVALO_ACTUALIZACION =
            20;

    @SubscribeEvent
    public static void onGhastTick(
            EntityTickEvent.Post event
    ) {

        /*
         * Solo procesamos entidades vivas.
         */
        if (!(event.getEntity()
                instanceof LivingEntity ghast)) {
            return;
        }

        /*
         * Solo Ghasts hostiles.
         *
         * Los Happy Ghasts no se modifican.
         */
        if (ghast.getType() != EntityTypes.GHAST) {
            return;
        }

        /*
         * Solo ejecutamos la lógica en el servidor.
         */
        if (!(ghast.level()
                instanceof ServerLevel level)) {
            return;
        }

        /*
         * Una comprobación por segundo.
         */
        if (ghast.tickCount
                % INTERVALO_ACTUALIZACION != 0) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        /*
         * Antes del día 7 se mantiene el Ghast vanilla.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * Aplicamos la vida y el radio de explosión.
         */
        aplicarDobleVida(ghast);
        aplicarExplosionMayor(level, ghast);
    }

    private static void aplicarDobleVida(
            LivingEntity ghast
    ) {

        AttributeInstance atributoVida =
                ghast.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        /*
         * Solo hacemos la conversión una vez.
         *
         * Esto impide curar al Ghast cada segundo.
         */
        if (atributoVida.getBaseValue()
                >= VIDA_GHAST_MEJORADO) {
            return;
        }

        float vidaAnterior =
                ghast.getHealth();

        double vidaMaximaAnterior =
                atributoVida.getBaseValue();

        atributoVida.setBaseValue(
                VIDA_GHAST_MEJORADO
        );

        /*
         * Conservamos el porcentaje de salud.
         *
         * Ejemplos:
         *
         * 10/10 -> 20/20
         *  5/10 -> 10/20
         *  2/10 ->  4/20
         */
        float porcentajeVida =
                vidaMaximaAnterior <= 0.0D
                        ? 1.0F
                        : (float) (
                        vidaAnterior
                                / vidaMaximaAnterior
                );

        porcentajeVida =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                porcentajeVida
                        )
                );

        ghast.setHealth(
                (float) VIDA_GHAST_MEJORADO
                        * porcentajeVida
        );
    }

    private static void aplicarExplosionMayor(
            ServerLevel level,
            LivingEntity ghast
    ) {

        /*
         * En lugar de depender de métodos internos del Ghast
         * que pueden cambiar entre mappings, utilizamos el
         * comando data para establecer ExplosionPower.
         *
         * La entidad queda seleccionada mediante su UUID,
         * por lo que únicamente se modifica este Ghast.
         */
        String uuid =
                ghast.getUUID().toString();

        level
                .getServer()
                .getCommands()
                .performPrefixedCommand(
                        level
                                .getServer()
                                .createCommandSourceStack()
                                .withSuppressedOutput(),

                        "data merge entity "
                                + uuid
                                + " {ExplosionPower:"
                                + POTENCIA_EXPLOSION
                                + "}"
                );
    }

    private GhastDifficultyEvents() {
    }
}
