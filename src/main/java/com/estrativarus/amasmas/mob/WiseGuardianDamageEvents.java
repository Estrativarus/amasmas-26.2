package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class WiseGuardianDamageEvents {

    /*
     * Daño base del láser del Guardián Sabio.
     *
     * 16 puntos equivalen a ocho corazones
     * antes de aplicar reducciones.
     */
    private static final float DANO_LASER =
            16.0F;

    /*
     * Daño de las espinas.
     *
     * 6 puntos equivalen a tres corazones
     * antes de aplicar reducciones.
     */
    private static final float DANO_ESPINAS =
            6.0F;

    @SubscribeEvent
    public static void onIncomingDamage(
            LivingIncomingDamageEvent event
    ) {

        DamageSource source =
                event.getSource();

        /*
         * Obtenemos la entidad responsable del daño.
         *
         * Para el láser y las espinas del Guardián,
         * la entidad responsable será el propio Guardián.
         */
        Entity atacante =
                source.getEntity();

        if (!(atacante instanceof LivingEntity guardian)) {
            return;
        }

        /*
         * Esta comprobación evita afectar a:
         *
         * - Guardianes normales;
         * - otras criaturas con daño mágico;
         * - encantamiento Espinas de jugadores;
         * - cualquier otra fuente de daño.
         */
        if (guardian.getType()
                != EntityTypes.ELDER_GUARDIAN) {

            return;
        }

        /*
         * Solo modificamos Guardianes Ancianos que hayan
         * sido transformados en Guardianes Sabios.
         */
        if (!WiseGuardianEvents
                .esGuardianSabio(guardian)) {

            return;
        }

        /*
         * DAÑO DE ESPINAS
         *
         * Se produce cuando una entidad golpea cuerpo a cuerpo
         * al Guardián mientras sus espinas están extendidas.
         */
        if (source.is(DamageTypes.THORNS)) {

            event
                    .getContainer()
                    .setNewDamage(
                            DANO_ESPINAS
                    );

            return;
        }

        /*
         * DAÑO DEL LÁSER
         *
         * El láser del Guardián utiliza daño mágico indirecto.
         *
         * Comprobamos también que la entidad directa del daño
         * sea el mismo Guardián. Esto evita modificar otros
         * daños mágicos indirectos que pudieran atribuirse
         * accidentalmente a la entidad.
         */
        if (source.is(DamageTypes.INDIRECT_MAGIC)
                && source.getDirectEntity() == guardian) {

            event
                    .getContainer()
                    .setNewDamage(
                            DANO_LASER
                    );
        }
    }

    private WiseGuardianDamageEvents() {
    }
}