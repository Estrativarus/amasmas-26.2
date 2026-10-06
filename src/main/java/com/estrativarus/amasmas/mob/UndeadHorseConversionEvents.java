package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class UndeadHorseConversionEvents {

    public static final String TAG_JINETE_NO_CLASIFICAR =
            "amasmas_jinete_caballo_no_clasificar";

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onHorseJoin(
            EntityJoinLevelEvent event
    ) {

        if (event.isCanceled()) {
            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof AbstractHorse horse)) {

            return;
        }

        if (!esEquinoConvertible(
                horse
        )) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (diaActual < DIA_INICIO) {
            return;
        }

        procesarEquinoNuevo(
                event,
                level,
                horse
        );
    }

    @SubscribeEvent
    public static void onHorseTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof AbstractHorse horse)) {

            return;
        }

        if (!esEquinoConvertible(
                horse
        )) {

            return;
        }

        if (!(horse.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((horse.tickCount + horse.getId())
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

        convertirEquinoYaCargado(
                level,
                horse
        );
    }

    private static void procesarEquinoNuevo(
            EntityJoinLevelEvent event,
            ServerLevel level,
            AbstractHorse horse
    ) {

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        horse
                )) {

            return;
        }

        HorseReplacementData datos =
                HorseReplacementData.from(
                        horse
                );

        boolean convertirEnEsqueleto =
                horse
                        .getRandom()
                        .nextBoolean();

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearMonturaDesdeDatos(
                        level,
                        datos,
                        convertirEnEsqueleto
                )
        );
    }

    private static void crearMonturaDesdeDatos(
            ServerLevel level,
            HorseReplacementData datos,
            boolean convertirEnEsqueleto
    ) {

        Mob montura;
        Mob jinete;

        if (convertirEnEsqueleto) {

            montura =
                    EntityTypes.SKELETON_HORSE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            jinete =
                    EntityTypes.SKELETON.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

        } else {

            montura =
                    EntityTypes.ZOMBIE_HORSE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            jinete =
                    EntityTypes.ZOMBIE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );
        }

        if (montura == null
                || jinete == null) {

            eliminarEntidadNueva(
                    montura
            );

            eliminarEntidadNueva(
                    jinete
            );

            return;
        }

        datos.aplicarAMontura(
                montura
        );

        datos.aplicarAJinete(
                jinete
        );

        configurarMontura(
                montura
        );

        configurarJinete(
                jinete
        );

        boolean monturaAnadida =
                level.addFreshEntity(
                        montura
                );

        if (!monturaAnadida) {

            eliminarEntidadNueva(
                    montura
            );

            eliminarEntidadNueva(
                    jinete
            );

            return;
        }

        boolean jineteAnadido =
                level.addFreshEntity(
                        jinete
                );

        if (!jineteAnadido) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            montura
                    );

            eliminarEntidadNueva(
                    jinete
            );

            return;
        }

        boolean montado =
                jinete.startRiding(
                        montura
                );

        if (!montado) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            jinete
                    );

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            montura
                    );
        }
    }

    private static void convertirEquinoYaCargado(
            ServerLevel level,
            AbstractHorse horse
    ) {

        if (!horse.isAlive()
                || horse.isRemoved()) {

            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        horse
                )) {

            return;
        }

        boolean convertirEnEsqueleto =
                horse
                        .getRandom()
                        .nextBoolean();

        Mob montura;
        Mob jinete;

        if (convertirEnEsqueleto) {

            montura =
                    EntityTypes.SKELETON_HORSE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            jinete =
                    EntityTypes.SKELETON.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

        } else {

            montura =
                    EntityTypes.ZOMBIE_HORSE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            jinete =
                    EntityTypes.ZOMBIE.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );
        }

        if (montura == null
                || jinete == null) {

            eliminarEntidadNueva(
                    montura
            );

            eliminarEntidadNueva(
                    jinete
            );

            EntityReplacementHelper
                    .cancelarReemplazo(
                            horse
                    );

            return;
        }

        configurarMontura(
                montura
        );

        configurarJinete(
                jinete
        );

        boolean reemplazado =
                EntityReplacementHelper
                        .reemplazarConJinete(
                                level,
                                horse,
                                montura,
                                jinete
                        );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            horse
                    );
        }
    }

    private static void configurarMontura(
            Mob montura
    ) {

        montura.setPersistenceRequired();
    }

    private static void configurarJinete(
            Mob jinete
    ) {

        jinete.setPersistenceRequired();

        jinete
                .getPersistentData()
                .putBoolean(
                        TAG_JINETE_NO_CLASIFICAR,
                        true
                );
    }

    private static void eliminarEntidadNueva(
            Mob entity
    ) {

        if (entity == null) {
            return;
        }

        EntityReplacementHelper
                .eliminarDefinitivamente(
                        entity
                );
    }

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
    }

    private static boolean esEquinoConvertible(
            AbstractHorse horse
    ) {

        return horse.getType()
                == EntityTypes.HORSE

                || horse.getType()
                == EntityTypes.DONKEY

                || horse.getType()
                == EntityTypes.MULE;
    }

    private record HorseReplacementData(
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

        private static HorseReplacementData from(
                AbstractHorse horse
        ) {

            return new HorseReplacementData(
                    horse.getX(),
                    horse.getY(),
                    horse.getZ(),
                    horse.getYRot(),
                    horse.getXRot(),
                    horse.getYHeadRot(),
                    horse.getDeltaMovement().x,
                    horse.getDeltaMovement().y,
                    horse.getDeltaMovement().z,
                    horse.isPersistenceRequired(),
                    horse.getCustomName() == null
                            ? null
                            : horse
                            .getCustomName()
                            .copy(),
                    horse.isCustomNameVisible()
            );
        }

        private void aplicarAMontura(
                Mob montura
        ) {

            montura.setPos(
                    x,
                    y,
                    z
            );

            montura.setYRot(
                    yRot
            );

            montura.setXRot(
                    xRot
            );

            montura.setYHeadRot(
                    yHeadRot
            );

            montura.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                montura.setPersistenceRequired();
            }

            if (nombre != null) {

                montura.setCustomName(
                        nombre
                );

                montura.setCustomNameVisible(
                        nombreVisible
                );
            }
        }

        private void aplicarAJinete(
                Mob jinete
        ) {

            jinete.setPos(
                    x,
                    y + 1.0D,
                    z
            );

            jinete.setYRot(
                    yRot
            );

            jinete.setXRot(
                    0.0F
            );
        }
    }

    private UndeadHorseConversionEvents() {
    }
}