package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class EndermanProgressionEvents {

    private static final int DIA_INICIO =
            14;

    private static final double DANO_DIA_14 =
            30.0D;

    private static final int PROBABILIDAD_CREEPER =
            30;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final String TAG_TIRADA_CREEPER =
            "amasmas_enderman_tirada_creeper";

    @SubscribeEvent
    public static void onEndermanJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob enderman)) {

            return;
        }

        if (enderman.getType()
                != EntityTypes.ENDERMAN) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (debeIntentarConversion(
                enderman
        )) {

            marcarTiradaRealizada(
                    enderman
            );

            boolean debeConvertirse =
                    realizarTiradaConversion(
                            enderman
                    );

            if (debeConvertirse) {

                procesarEndermanNuevoComoCreeper(
                        event,
                        level,
                        enderman
                );

                return;
            }
        }

        aplicarProgresionSinConversion(
                enderman,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onEndermanTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob enderman)) {

            return;
        }

        if (enderman.getType()
                != EntityTypes.ENDERMAN) {

            return;
        }

        if (!(enderman.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((enderman.tickCount + enderman.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (debeIntentarConversion(
                enderman
        )) {

            marcarTiradaRealizada(
                    enderman
            );

            boolean debeConvertirse =
                    realizarTiradaConversion(
                            enderman
                    );

            if (debeConvertirse) {

                convertirEndermanYaCargado(
                        level,
                        enderman
                );

                return;
            }
        }

        aplicarProgresionSinConversion(
                enderman,
                diaActual
        );
    }

    private static boolean debeIntentarConversion(
            Mob enderman
    ) {

        return !enderman
                .getPersistentData()
                .contains(
                        TAG_TIRADA_CREEPER
                );
    }

    private static void marcarTiradaRealizada(
            Mob enderman
    ) {

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_CREEPER,
                        true
                );
    }

    private static boolean realizarTiradaConversion(
            Mob enderman
    ) {

        return enderman
                .getRandom()
                .nextInt(
                        PROBABILIDAD_CREEPER
                )
                == 0;
    }

    private static void procesarEndermanNuevoComoCreeper(
            EntityJoinLevelEvent event,
            ServerLevel level,
            Mob enderman
    ) {

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        enderman
                )) {

            aplicarEtapaDia14(
                    enderman
            );

            return;
        }

        EndermanReplacementData datos =
                EndermanReplacementData.from(
                        enderman
                );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearCreeperDesdeDatos(
                        level,
                        datos
                )
        );
    }

    private static void crearCreeperDesdeDatos(
            ServerLevel level,
            EndermanReplacementData datos
    ) {

        Mob creeper =
                EntityTypes.CREEPER.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (creeper == null) {
            return;
        }

        datos.aplicarA(
                creeper
        );

        boolean anadido =
                level.addFreshEntity(
                        creeper
                );

        if (!anadido) {

            creeper.discard();
        }
    }

    private static void convertirEndermanYaCargado(
            ServerLevel level,
            Mob enderman
    ) {

        if (!enderman.isAlive()
                || enderman.isRemoved()) {

            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        enderman
                )) {

            aplicarEtapaDia14(
                    enderman
            );

            return;
        }

        Mob creeper =
                EntityTypes.CREEPER.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (creeper == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );

            return;
        }

        if (enderman.isPersistenceRequired()) {

            creeper.setPersistenceRequired();
        }

        boolean reemplazado =
                EntityReplacementHelper.reemplazar(
                        level,
                        enderman,
                        creeper
                );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );

            aplicarEtapaDia14(
                    enderman
            );
        }
    }

    private static void aplicarProgresionSinConversion(
            Mob enderman,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return;
        }

        aplicarEtapaDia14(
                enderman
        );

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    enderman
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    enderman
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    enderman
            );
        }
    }

    private static void aplicarEtapaDia14(
            Mob enderman
    ) {

        AttributeInstance atributoDano =
                enderman.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (atributoDano == null) {
            return;
        }

        if (atributoDano.getBaseValue()
                == DANO_DIA_14) {

            return;
        }

        atributoDano.setBaseValue(
                DANO_DIA_14
        );
    }

    private static void aplicarEtapaDia21(
            Mob enderman
    ) {
    }

    private static void aplicarEtapaDia42(
            Mob enderman
    ) {
    }

    private static void aplicarEtapaDia63(
            Mob enderman
    ) {
    }

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
    }

    private record EndermanReplacementData(
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

        private static EndermanReplacementData from(
                Mob enderman
        ) {

            return new EndermanReplacementData(
                    enderman.getX(),
                    enderman.getY(),
                    enderman.getZ(),
                    enderman.getYRot(),
                    enderman.getXRot(),
                    enderman.getYHeadRot(),
                    enderman.getDeltaMovement().x,
                    enderman.getDeltaMovement().y,
                    enderman.getDeltaMovement().z,
                    enderman.isPersistenceRequired(),
                    enderman.getCustomName() == null
                            ? null
                            : enderman
                            .getCustomName()
                            .copy(),
                    enderman.isCustomNameVisible()
            );
        }

        private void aplicarA(
                Mob creeper
        ) {

            creeper.setPos(
                    x,
                    y,
                    z
            );

            creeper.setYRot(
                    yRot
            );

            creeper.setXRot(
                    xRot
            );

            creeper.setYHeadRot(
                    yHeadRot
            );

            creeper.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                creeper.setPersistenceRequired();
            }

            if (nombre != null) {

                creeper.setCustomName(
                        nombre
                );

                creeper.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private EndermanProgressionEvents() {
    }
}