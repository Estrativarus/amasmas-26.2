package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class CaveSpiderProgressionEvents {

    private static final int DIA_CONVERSION_ARANAS =
            7;

    private static final int DIA_RESISTENCIA =
            7;

    private static final int DIA_FUERZA =
            14;

    private static final int DIA_REGENERACION =
            21;

    private static final int INTERVALO_COMPROBACION =
            20;

    private static final int DURACION_EFECTOS_TEMPORALES =
            20 * 15;

    private static final int AMPLIFICADOR_RESISTENCIA =
            2;

    private static final int AMPLIFICADOR_FUERZA =
            4;

    private static final int AMPLIFICADOR_REGENERACION =
            3;

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

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (mob.getType()
                == EntityTypes.SPIDER) {

            procesarAranaNueva(
                    event,
                    level,
                    mob,
                    diaActual
            );

            return;
        }

        if (!tieneProgresionCompartida(
                mob
        )) {

            return;
        }

        aplicarProgresion(
                mob,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onEntityTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (!esEntidadControlada(
                mob
        )) {

            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((mob.tickCount + mob.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (mob.getType()
                == EntityTypes.SPIDER) {

            convertirAranaYaCargada(
                    level,
                    mob,
                    diaActual
            );

            return;
        }

        aplicarProgresion(
                mob,
                diaActual
        );
    }

    private static void procesarAranaNueva(
            EntityJoinLevelEvent event,
            ServerLevel level,
            Mob arana,
            int diaActual
    ) {

        if (diaActual < DIA_CONVERSION_ARANAS) {
            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        arana
                )) {

            return;
        }

        SpiderReplacementData datos =
                SpiderReplacementData.from(
                        arana
                );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearAranaDeCuevaDesdeDatos(
                        level,
                        datos
                )
        );
    }

    private static void crearAranaDeCuevaDesdeDatos(
            ServerLevel level,
            SpiderReplacementData datos
    ) {

        Mob aranaDeCueva =
                EntityTypes.CAVE_SPIDER.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (aranaDeCueva == null) {
            return;
        }

        datos.aplicarA(
                aranaDeCueva
        );

        int diaActual =
                obtenerDiaActual(
                        level
                );

        aplicarProgresion(
                aranaDeCueva,
                diaActual
        );

        boolean anadida =
                level.addFreshEntity(
                        aranaDeCueva
                );

        if (!anadida) {

            aranaDeCueva.discard();
        }
    }

    private static void convertirAranaYaCargada(
            ServerLevel level,
            Mob arana,
            int diaActual
    ) {

        if (diaActual < DIA_CONVERSION_ARANAS) {
            return;
        }

        if (!arana.isAlive()
                || arana.isRemoved()) {

            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        arana
                )) {

            return;
        }

        Mob aranaDeCueva =
                EntityTypes.CAVE_SPIDER.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (aranaDeCueva == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            arana
                    );

            return;
        }

        aplicarProgresion(
                aranaDeCueva,
                diaActual
        );

        boolean reemplazada =
                EntityReplacementHelper.reemplazar(
                        level,
                        arana,
                        aranaDeCueva
                );

        if (!reemplazada) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            arana
                    );
        }
    }

    public static void aplicarProgresion(
            Mob mob,
            int diaActual
    ) {

        if (!tieneProgresionCompartida(
                mob
        )) {

            return;
        }

        if (diaActual >= DIA_RESISTENCIA) {

            renovarEfectoTemporalSiNecesario(
                    mob,
                    MobEffects.RESISTANCE,
                    AMPLIFICADOR_RESISTENCIA
            );
        }

        if (diaActual >= DIA_FUERZA) {

            renovarEfectoTemporalSiNecesario(
                    mob,
                    MobEffects.STRENGTH,
                    AMPLIFICADOR_FUERZA
            );
        }

        if (diaActual >= DIA_REGENERACION) {

            renovarEfectoPermanenteSiNecesario(
                    mob,
                    MobEffects.REGENERATION,
                    AMPLIFICADOR_REGENERACION
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    mob
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    mob
            );
        }
    }

    private static void aplicarEtapaDia42(
            Mob mob
    ) {
    }

    private static void aplicarEtapaDia63(
            Mob mob
    ) {
    }

    private static void renovarEfectoTemporalSiNecesario(
            Mob mob,
            Holder<MobEffect> efecto,
            int amplificador
    ) {

        MobEffectInstance efectoActual =
                mob.getEffect(
                        efecto
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                >= amplificador
                && efectoActual.getDuration()
                > 100) {

            return;
        }

        mob.addEffect(
                new MobEffectInstance(
                        efecto,
                        DURACION_EFECTOS_TEMPORALES,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }

    private static void renovarEfectoPermanenteSiNecesario(
            Mob mob,
            Holder<MobEffect> efecto,
            int amplificador
    ) {

        MobEffectInstance efectoActual =
                mob.getEffect(
                        efecto
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                >= amplificador
                && efectoActual.isInfiniteDuration()) {

            return;
        }

        mob.addEffect(
                new MobEffectInstance(
                        efecto,
                        MobEffectInstance.INFINITE_DURATION,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
    }

    private static boolean esEntidadControlada(
            Mob mob
    ) {

        return mob.getType()
                == EntityTypes.SPIDER

                || tieneProgresionCompartida(
                mob
        );
    }

    private static boolean tieneProgresionCompartida(
            Mob mob
    ) {

        return mob.getType()
                == EntityTypes.CAVE_SPIDER

                || mob.getType()
                == EntityTypes.SILVERFISH

                || mob.getType()
                == EntityTypes.ENDERMITE;
    }

    private record SpiderReplacementData(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            float yHeadRot,
            double movimientoX,
            double movimientoY,
            double movimientoZ,
            boolean persistente,
            Component nombre,
            boolean nombreVisible
    ) {

        private static SpiderReplacementData from(
                Mob arana
        ) {

            return new SpiderReplacementData(
                    arana.getX(),
                    arana.getY(),
                    arana.getZ(),
                    arana.getYRot(),
                    arana.getXRot(),
                    arana.getYHeadRot(),
                    arana.getDeltaMovement().x,
                    arana.getDeltaMovement().y,
                    arana.getDeltaMovement().z,
                    arana.isPersistenceRequired(),
                    arana.getCustomName() == null
                            ? null
                            : arana
                            .getCustomName()
                            .copy(),
                    arana.isCustomNameVisible()
            );
        }

        private void aplicarA(
                Mob aranaDeCueva
        ) {

            aranaDeCueva.setPos(
                    x,
                    y,
                    z
            );

            aranaDeCueva.setYRot(
                    yRot
            );

            aranaDeCueva.setXRot(
                    xRot
            );

            aranaDeCueva.setYHeadRot(
                    yHeadRot
            );

            aranaDeCueva.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                aranaDeCueva.setPersistenceRequired();
            }

            if (nombre != null) {

                aranaDeCueva.setCustomName(
                        nombre
                );

                aranaDeCueva.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private CaveSpiderProgressionEvents() {
    }
}