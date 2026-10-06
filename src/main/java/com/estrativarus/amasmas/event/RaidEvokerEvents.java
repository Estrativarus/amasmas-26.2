package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class RaidEvokerEvents {

    private static final int INTERVALO_COMPROBACION =
            20;

    private static final String TAG_REEMPLAZO_PROGRAMADO =
            "amasmas_evoker_raid_reemplazo_programado";

    @SubscribeEvent
    public static void onEntityJoinLevel(
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
                instanceof Raider evoker)) {

            return;
        }

        if (!esEvoker(
                evoker
        )) {

            return;
        }

        Raid raid =
                evoker.getCurrentRaid();

        if (raid == null) {

            return;
        }

        if (evoker
                .getPersistentData()
                .contains(
                        TAG_REEMPLAZO_PROGRAMADO
                )) {

            return;
        }

        evoker
                .getPersistentData()
                .putBoolean(
                        TAG_REEMPLAZO_PROGRAMADO,
                        true
                );

        int oleada =
                evoker.getWave();

        RaiderReplacementData datos =
                RaiderReplacementData.from(
                        evoker
                );

        raid.removeFromRaid(
                level,
                evoker,
                true
        );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearIllusionerParaRaid(
                        level,
                        raid,
                        oleada,
                        datos
                )
        );
    }

    @SubscribeEvent
    public static void onEvokerTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Raider evoker)) {

            return;
        }

        if (!esEvoker(
                evoker
        )) {

            return;
        }

        if (!(evoker.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((evoker.tickCount + evoker.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        Raid raid =
                evoker.getCurrentRaid();

        if (raid == null) {

            return;
        }

        if (evoker
                .getPersistentData()
                .contains(
                        TAG_REEMPLAZO_PROGRAMADO
                )) {

            return;
        }

        evoker
                .getPersistentData()
                .putBoolean(
                        TAG_REEMPLAZO_PROGRAMADO,
                        true
                );

        reemplazarEvokerYaCargado(
                level,
                raid,
                evoker
        );
    }

    private static void crearIllusionerParaRaid(
            ServerLevel level,
            Raid raid,
            int oleada,
            RaiderReplacementData datos
    ) {

        Mob entidad =
                EntityTypes.ILLUSIONER.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (!(entidad instanceof Raider illusioner)) {

            if (entidad != null) {

                EntityReplacementHelper
                        .eliminarDefinitivamente(
                                entidad
                        );
            }

            return;
        }

        datos.aplicarA(
                illusioner
        );

        prepararIllusioner(
                illusioner
        );

        boolean anadido =
                level.addFreshEntity(
                        illusioner
                );

        if (!anadido) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            illusioner
                    );

            return;
        }

        boolean incorporado =
                raid.addWaveMob(
                        level,
                        oleada,
                        illusioner,
                        true
                );

        if (!incorporado) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            illusioner
                    );
        }
    }

    private static void reemplazarEvokerYaCargado(
            ServerLevel level,
            Raid raid,
            Raider evoker
    ) {

        if (!evoker.isAlive()
                || evoker.isRemoved()) {

            return;
        }

        int oleada =
                evoker.getWave();

        Mob entidad =
                EntityTypes.ILLUSIONER.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (!(entidad instanceof Raider illusioner)) {

            if (entidad != null) {

                EntityReplacementHelper
                        .eliminarDefinitivamente(
                                entidad
                        );
            }

            evoker
                    .getPersistentData()
                    .remove(
                            TAG_REEMPLAZO_PROGRAMADO
                    );

            return;
        }

        copiarDatos(
                evoker,
                illusioner
        );

        prepararIllusioner(
                illusioner
        );

        boolean anadido =
                level.addFreshEntity(
                        illusioner
                );

        if (!anadido) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            illusioner
                    );

            evoker
                    .getPersistentData()
                    .remove(
                            TAG_REEMPLAZO_PROGRAMADO
                    );

            return;
        }

        boolean incorporado =
                raid.addWaveMob(
                        level,
                        oleada,
                        illusioner,
                        true
                );

        if (!incorporado) {

            EntityReplacementHelper
                    .eliminarDefinitivamente(
                            illusioner
                    );

            evoker
                    .getPersistentData()
                    .remove(
                            TAG_REEMPLAZO_PROGRAMADO
                    );

            return;
        }

        raid.removeFromRaid(
                level,
                evoker,
                true
        );

        EntityReplacementHelper
                .eliminarDefinitivamente(
                        evoker
                );
    }

    private static void prepararIllusioner(
            Raider illusioner
    ) {

        illusioner.setPersistenceRequired();

        illusioner.setCanJoinRaid(
                true
        );

        illusioner.setTicksOutsideRaid(
                0
        );
    }

    private static void copiarDatos(
            Raider original,
            Raider reemplazo
    ) {

        reemplazo.setPos(
                original.getX(),
                original.getY(),
                original.getZ()
        );

        reemplazo.setYRot(
                original.getYRot()
        );

        reemplazo.setXRot(
                original.getXRot()
        );

        reemplazo.setYHeadRot(
                original.getYHeadRot()
        );

        reemplazo.setDeltaMovement(
                original.getDeltaMovement()
        );

        reemplazo.setOnGround(
                original.onGround()
        );

        if (original.hasCustomName()
                && original.getCustomName() != null) {

            reemplazo.setCustomName(
                    original
                            .getCustomName()
                            .copy()
            );

            reemplazo.setCustomNameVisible(
                    original.isCustomNameVisible()
            );
        }
    }

    private static boolean esEvoker(
            Raider raider
    ) {

        String entityId =
                raider
                        .getType()
                        .builtInRegistryHolder()
                        .key()
                        .identifier()
                        .toString();

        return entityId.equals(
                "minecraft:evoker"
        );
    }

    private record RaiderReplacementData(
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

        private static RaiderReplacementData from(
                Raider evoker
        ) {

            return new RaiderReplacementData(
                    evoker.getX(),
                    evoker.getY(),
                    evoker.getZ(),
                    evoker.getYRot(),
                    evoker.getXRot(),
                    evoker.getYHeadRot(),
                    evoker.getDeltaMovement().x,
                    evoker.getDeltaMovement().y,
                    evoker.getDeltaMovement().z,
                    evoker.isPersistenceRequired(),
                    evoker.getCustomName() == null
                            ? null
                            : evoker
                            .getCustomName()
                            .copy(),
                    evoker.isCustomNameVisible()
            );
        }

        private void aplicarA(
                Raider illusioner
        ) {

            illusioner.setPos(
                    x,
                    y,
                    z
            );

            illusioner.setYRot(
                    yRot
            );

            illusioner.setXRot(
                    xRot
            );

            illusioner.setYHeadRot(
                    yHeadRot
            );

            illusioner.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                illusioner.setPersistenceRequired();
            }

            if (nombre != null) {

                illusioner.setCustomName(
                        nombre
                );

                illusioner.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private RaidEvokerEvents() {
    }
}