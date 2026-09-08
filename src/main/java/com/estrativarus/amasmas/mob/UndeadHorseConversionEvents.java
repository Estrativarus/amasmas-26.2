package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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

    private static final String TAG_CONVERSION_PROGRAMADA =
            "amasmas_equino_conversion_programada";

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onHorseJoin(
            EntityJoinLevelEvent event
    ) {

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

        programarConversion(
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

        intentarConvertir(
                level,
                horse
        );
    }

    private static void programarConversion(
            ServerLevel level,
            AbstractHorse horse
    ) {

        if (horse
                .getPersistentData()
                .contains(
                        TAG_CONVERSION_PROGRAMADA
                )) {

            return;
        }

        horse
                .getPersistentData()
                .putBoolean(
                        TAG_CONVERSION_PROGRAMADA,
                        true
                );

        level.getServer().execute(() -> {

            if (!horse.isAlive()
                    || horse.isRemoved()) {

                return;
            }

            horse
                    .getPersistentData()
                    .remove(
                            TAG_CONVERSION_PROGRAMADA
                    );

            intentarConvertir(
                    level,
                    horse
            );
        });
    }

    private static void intentarConvertir(
            ServerLevel level,
            AbstractHorse horse
    ) {

        if (!horse.isAlive()
                || horse.isRemoved()) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (!esEquinoConvertible(
                horse
        )) {

            return;
        }

        boolean convertirEnEsqueleto =
                horse
                        .getRandom()
                        .nextBoolean();

        if (convertirEnEsqueleto) {

            crearCaballoEsqueletoConJinete(
                    level,
                    horse
            );

        } else {

            crearCaballoZombieConJinete(
                    level,
                    horse
            );
        }
    }

    private static void crearCaballoZombieConJinete(
            ServerLevel level,
            AbstractHorse original
    ) {

        Mob montura =
                EntityTypes.ZOMBIE_HORSE.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        Mob jinete =
                EntityTypes.ZOMBIE.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (montura == null
                || jinete == null) {

            descartarSiExiste(
                    montura
            );

            descartarSiExiste(
                    jinete
            );

            return;
        }

        prepararMontura(
                original,
                montura
        );

        prepararJinete(
                original,
                jinete
        );

        boolean monturaAnadida =
                level.addFreshEntity(
                        montura
                );

        if (!monturaAnadida) {

            montura.discard();
            jinete.discard();

            return;
        }

        boolean jineteAnadido =
                level.addFreshEntity(
                        jinete
                );

        if (!jineteAnadido) {

            montura.discard();
            jinete.discard();

            return;
        }

        boolean montado =
                jinete.startRiding(
                        montura
                );

        if (!montado) {

            montura.discard();
            jinete.discard();

            return;
        }

        eliminarOriginal(
                original
        );
    }

    private static void crearCaballoEsqueletoConJinete(
            ServerLevel level,
            AbstractHorse original
    ) {

        Mob montura =
                EntityTypes.SKELETON_HORSE.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        Mob jinete =
                EntityTypes.SKELETON.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (montura == null
                || jinete == null) {

            descartarSiExiste(
                    montura
            );

            descartarSiExiste(
                    jinete
            );

            return;
        }

        prepararMontura(
                original,
                montura
        );

        prepararJinete(
                original,
                jinete
        );

        boolean monturaAnadida =
                level.addFreshEntity(
                        montura
                );

        if (!monturaAnadida) {

            montura.discard();
            jinete.discard();

            return;
        }

        boolean jineteAnadido =
                level.addFreshEntity(
                        jinete
                );

        if (!jineteAnadido) {

            montura.discard();
            jinete.discard();

            return;
        }

        boolean montado =
                jinete.startRiding(
                        montura
                );

        if (!montado) {

            montura.discard();
            jinete.discard();

            return;
        }

        eliminarOriginal(
                original
        );
    }

    private static void prepararMontura(
            AbstractHorse original,
            Mob montura
    ) {

        montura.setPos(
                original.getX(),
                original.getY(),
                original.getZ()
        );

        montura.setYRot(
                original.getYRot()
        );

        montura.setXRot(
                original.getXRot()
        );

        montura.setDeltaMovement(
                original.getDeltaMovement()
        );

        montura.setPersistenceRequired();

        if (original.hasCustomName()) {

            montura.setCustomName(
                    original.getCustomName()
            );

            montura.setCustomNameVisible(
                    original.isCustomNameVisible()
            );
        }
    }

    private static void prepararJinete(
            AbstractHorse original,
            Mob jinete
    ) {

        jinete.setPos(
                original.getX(),
                original.getY() + 1.0D,
                original.getZ()
        );

        jinete.setYRot(
                original.getYRot()
        );

        jinete.setXRot(
                0.0F
        );

        jinete.setPersistenceRequired();

        jinete
                .getPersistentData()
                .putBoolean(
                        TAG_JINETE_NO_CLASIFICAR,
                        true
                );
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

    private static void eliminarOriginal(
            AbstractHorse original
    ) {

        original.stopRiding();
        original.ejectPassengers();
        original.discard();
    }

    private static void descartarSiExiste(
            Entity entity
    ) {

        if (entity != null) {
            entity.discard();
        }
    }

    private UndeadHorseConversionEvents() {
    }
}