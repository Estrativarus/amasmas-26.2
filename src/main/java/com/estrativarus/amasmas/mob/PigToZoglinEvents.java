package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.network.chat.Component;
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

    private static final String TAG_TRANSFORMACION_PENDIENTE =
            "amasmas_cerdo_transformacion_zoglin_pendiente";

    private static final int DIA_TRANSFORMACION =
            7;

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

        programarTransformacion(
                level,
                pig
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

        if (pig.tickCount % 20 != 0) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_TRANSFORMACION) {
            return;
        }

        programarTransformacion(
                level,
                pig
        );
    }

    private static void programarTransformacion(
            ServerLevel level,
            Pig pig
    ) {

        if (pig
                .getPersistentData()
                .contains(
                        TAG_TRANSFORMACION_PENDIENTE
                )) {

            return;
        }

        pig
                .getPersistentData()
                .putBoolean(
                        TAG_TRANSFORMACION_PENDIENTE,
                        true
                );

        double x =
                pig.getX();

        double y =
                pig.getY();

        double z =
                pig.getZ();

        float rotacionHorizontal =
                pig.getYRot();

        float rotacionVertical =
                pig.getXRot();

        boolean eraBebe =
                pig.isBaby();

        boolean eraPersistente =
                pig.isPersistenceRequired();

        boolean teniaNombre =
                pig.hasCustomName();

        Component nombreAnterior =
                pig.getCustomName();

        boolean nombreVisible =
                pig.isCustomNameVisible();

        level.getServer().execute(() -> {

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
                limpiarMarcaPendiente(pig);
                return;
            }

            zoglin.setPos(
                    x,
                    y,
                    z
            );

            zoglin.setYRot(
                    rotacionHorizontal
            );

            zoglin.setXRot(
                    rotacionVertical
            );

            zoglin.setBaby(
                    eraBebe
            );

            if (teniaNombre
                    && nombreAnterior != null) {

                zoglin.setCustomName(
                        nombreAnterior.copy()
                );

                zoglin.setCustomNameVisible(
                        nombreVisible
                );
            }

            if (eraPersistente) {
                zoglin.setPersistenceRequired();
            }

            boolean anadido =
                    level.addFreshEntity(
                            zoglin
                    );

            if (!anadido) {
                limpiarMarcaPendiente(pig);
                return;
            }

            pig.discard();
        });
    }

    private static void limpiarMarcaPendiente(
            Pig pig
    ) {

        pig
                .getPersistentData()
                .remove(
                        TAG_TRANSFORMACION_PENDIENTE
                );
    }

    private PigToZoglinEvents() {
    }
}