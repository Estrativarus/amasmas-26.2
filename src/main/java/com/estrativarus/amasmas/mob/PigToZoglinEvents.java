package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Zoglin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class PigToZoglinEvents {

    private static final int DIA_TRANSFORMACION =
            7;

    private static final int INTERVALO_COMPROBACION =
            20;

    @SubscribeEvent
    public static void onPigJoinLevel(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Pig pig)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_TRANSFORMACION) {
            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        pig
                )) {

            return;
        }

        PigReplacementData datos =
                PigReplacementData.from(
                        pig
                );

        /*
         * Impide que el cerdo entre en el mundo.
         *
         * Así nunca se envía al cliente y no puede
         * quedar una representación fantasma inmóvil.
         */
        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearZoglinDesdeDatos(
                        level,
                        datos
                )
        );
    }

    @SubscribeEvent
    public static void onPigTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Pig pig)) {

            return;
        }

        if (!(pig.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((pig.tickCount + pig.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_TRANSFORMACION) {
            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        pig
                )) {

            return;
        }

        convertirCerdoYaCargado(
                level,
                pig
        );
    }

    private static void crearZoglinDesdeDatos(
            ServerLevel level,
            PigReplacementData datos
    ) {

        Zoglin zoglin =
                EntityTypes.ZOGLIN.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (zoglin == null) {
            return;
        }

        datos.applyTo(
                zoglin
        );

        boolean anadido =
                level.addFreshEntity(
                        zoglin
                );

        if (!anadido) {

            zoglin.discard();
        }
    }

    private static void convertirCerdoYaCargado(
            ServerLevel level,
            Pig pig
    ) {

        if (!pig.isAlive()
                || pig.isRemoved()) {

            return;
        }

        Zoglin zoglin =
                EntityTypes.ZOGLIN.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (zoglin == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            pig
                    );

            return;
        }

        zoglin.setBaby(
                pig.isBaby()
        );

        if (pig.isPersistenceRequired()) {

            zoglin.setPersistenceRequired();
        }

        boolean reemplazado =
                EntityReplacementHelper.reemplazar(
                        level,
                        pig,
                        zoglin
                );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            pig
                    );
        }
    }

    private record PigReplacementData(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            float yHeadRot,
            double movimientoX,
            double movimientoY,
            double movimientoZ,
            boolean bebe,
            boolean persistente,
            net.minecraft.network.chat.Component nombre,
            boolean nombreVisible
    ) {

        private static PigReplacementData from(
                Pig pig
        ) {

            return new PigReplacementData(
                    pig.getX(),
                    pig.getY(),
                    pig.getZ(),
                    pig.getYRot(),
                    pig.getXRot(),
                    pig.getYHeadRot(),
                    pig.getDeltaMovement().x,
                    pig.getDeltaMovement().y,
                    pig.getDeltaMovement().z,
                    pig.isBaby(),
                    pig.isPersistenceRequired(),
                    pig.getCustomName() == null
                            ? null
                            : pig.getCustomName().copy(),
                    pig.isCustomNameVisible()
            );
        }

        private void applyTo(
                Zoglin zoglin
        ) {

            zoglin.setPos(
                    x,
                    y,
                    z
            );

            zoglin.setYRot(
                    yRot
            );

            zoglin.setXRot(
                    xRot
            );

            zoglin.setYHeadRot(
                    yHeadRot
            );

            zoglin.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            zoglin.setBaby(
                    bebe
            );

            if (persistente) {

                zoglin.setPersistenceRequired();
            }

            if (nombre != null) {

                zoglin.setCustomName(
                        nombre
                );

                zoglin.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private PigToZoglinEvents() {
    }
}