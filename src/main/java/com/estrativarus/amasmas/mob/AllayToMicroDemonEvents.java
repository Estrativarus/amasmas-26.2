package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class AllayToMicroDemonEvents {

    private static final int DIA_INICIO =
            14;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final String TAG_CONVERSION_PENDIENTE =
            "amasmas_allay_conversion_micro_demonio_pendiente";

    @SubscribeEvent
    public static void onAllayJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob allay)) {

            return;
        }

        if (allay.getType()
                != EntityTypes.ALLAY) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        programarConversion(
                level,
                allay
        );
    }

    @SubscribeEvent
    public static void onAllayTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob allay)) {

            return;
        }

        if (allay.getType()
                != EntityTypes.ALLAY) {

            return;
        }

        if (!(allay.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((allay.tickCount + allay.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        programarConversion(
                level,
                allay
        );
    }

    private static void programarConversion(
            ServerLevel level,
            Mob allay
    ) {

        if (allay
                .getPersistentData()
                .contains(
                        TAG_CONVERSION_PENDIENTE
                )) {

            return;
        }

        allay
                .getPersistentData()
                .putBoolean(
                        TAG_CONVERSION_PENDIENTE,
                        true
                );

        double x =
                allay.getX();

        double y =
                allay.getY();

        double z =
                allay.getZ();

        float rotacionHorizontal =
                allay.getYRot();

        float rotacionVertical =
                allay.getXRot();

        ItemStack objetoTransportado =
                allay
                        .getItemBySlot(
                                EquipmentSlot.MAINHAND
                        )
                        .copy();

        level.getServer().execute(() -> {

            if (!allay.isAlive()
                    || allay.isRemoved()) {

                return;
            }

            Mob vex =
                    EntityTypes.VEX.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            if (vex == null) {

                limpiarMarcaPendiente(
                        allay
                );

                return;
            }

            vex.setPos(
                    x,
                    y,
                    z
            );

            vex.setYRot(
                    rotacionHorizontal
            );

            vex.setXRot(
                    rotacionVertical
            );

            vex.setPersistenceRequired();

            boolean vexAnadido =
                    level.addFreshEntity(
                            vex
                    );

            if (!vexAnadido) {

                limpiarMarcaPendiente(
                        allay
                );

                return;
            }

            soltarObjetoTransportado(
                    level,
                    x,
                    y,
                    z,
                    objetoTransportado
            );

            eliminarEntidadOriginal(
                    allay
            );
        });
    }

    private static void soltarObjetoTransportado(
            ServerLevel level,
            double x,
            double y,
            double z,
            ItemStack objeto
    ) {

        if (objeto.isEmpty()) {
            return;
        }

        ItemEntity objetoSoltado =
                new ItemEntity(
                        level,
                        x,
                        y,
                        z,
                        objeto
                );

        level.addFreshEntity(
                objetoSoltado
        );
    }

    private static void limpiarMarcaPendiente(
            Mob allay
    ) {

        allay
                .getPersistentData()
                .remove(
                        TAG_CONVERSION_PENDIENTE
                );
    }

    private static void eliminarEntidadOriginal(
            Entity entidadOriginal
    ) {

        entidadOriginal.stopRiding();
        entidadOriginal.ejectPassengers();
        entidadOriginal.discard();
    }

    private AllayToMicroDemonEvents() {
    }
}