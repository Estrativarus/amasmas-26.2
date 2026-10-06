package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ShadowlandsStalkerEvents {

    private static final String MOD_NULLSCAPE =
            "nullscape";

    private static final int DIA_INICIO =
            14;

    private static final int PROBABILIDAD_STALKER =
            8;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_stalker_shadowlands";

    private static final ResourceKey<Biome>
            SHADOWLANDS =
            ResourceKey.create(
                    Registries.BIOME,
                    Identifier.fromNamespaceAndPath(
                            "nullscape",
                            "shadowlands"
                    )
            );

    @SubscribeEvent
    public static void onEndermanJoin(
            EntityJoinLevelEvent event
    ) {

        if (!ModList.get().isLoaded(
                MOD_NULLSCAPE
        )) {

            return;
        }

        if (event.isCanceled()) {
            return;
        }

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

        if (!puedeRealizarTirada(
                level,
                enderman,
                diaActual
        )) {

            return;
        }

        marcarTiradaRealizada(
                enderman
        );

        if (!superaTirada(
                enderman
        )) {

            return;
        }

        procesarEndermanNuevo(
                event,
                level,
                enderman
        );
    }

    @SubscribeEvent
    public static void onEndermanTick(
            EntityTickEvent.Post event
    ) {

        if (!ModList.get().isLoaded(
                MOD_NULLSCAPE
        )) {

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

        if (!puedeRealizarTirada(
                level,
                enderman,
                diaActual
        )) {

            return;
        }

        marcarTiradaRealizada(
                enderman
        );

        if (!superaTirada(
                enderman
        )) {

            return;
        }

        convertirEndermanYaCargado(
                level,
                enderman
        );
    }

    private static boolean puedeRealizarTirada(
            ServerLevel level,
            Mob enderman,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return false;
        }

        if (!level
                .getBiome(
                        enderman.blockPosition()
                )
                .is(
                        SHADOWLANDS
                )) {

            return false;
        }

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_TIRADA_REALIZADA
                )) {

            return false;
        }

        return EntityReplacementHelper
                .puedeSerReemplazada(
                        enderman
                );
    }

    private static void marcarTiradaRealizada(
            Mob enderman
    ) {

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_REALIZADA,
                        true
                );
    }

    private static boolean superaTirada(
            Mob enderman
    ) {

        return enderman
                .getRandom()
                .nextInt(
                        PROBABILIDAD_STALKER
                )
                == 0;
    }

    private static void procesarEndermanNuevo(
            EntityJoinLevelEvent event,
            ServerLevel level,
            Mob enderman
    ) {

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        enderman
                )) {

            return;
        }

        StalkerReplacementData datos =
                StalkerReplacementData.from(
                        enderman
                );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearStalkerDesdeDatos(
                        level,
                        datos
                )
        );
    }

    private static void crearStalkerDesdeDatos(
            ServerLevel level,
            StalkerReplacementData datos
    ) {

        Mob stalker =
                EntityTypes.CREAKING.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (stalker == null) {
            return;
        }

        datos.aplicarA(
                stalker
        );

        StalkerEvents.configurarStalkerExterno(
                level,
                stalker
        );

        boolean anadido =
                level.addFreshEntity(
                        stalker
                );

        if (!anadido) {

            stalker.discard();
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

            return;
        }

        Mob stalker =
                EntityTypes.CREAKING.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (stalker == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );

            return;
        }

        StalkerEvents.configurarStalkerExterno(
                level,
                stalker
        );

        boolean reemplazado =
                EntityReplacementHelper.reemplazar(
                        level,
                        enderman,
                        stalker
                );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );
        }
    }

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
    }

    private record StalkerReplacementData(
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

        private static StalkerReplacementData from(
                Mob enderman
        ) {

            return new StalkerReplacementData(
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
                Mob stalker
        ) {

            stalker.setPos(
                    x,
                    y,
                    z
            );

            stalker.setYRot(
                    yRot
            );

            stalker.setXRot(
                    xRot
            );

            stalker.setYHeadRot(
                    yHeadRot
            );

            stalker.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                stalker.setPersistenceRequired();
            }

            if (nombre != null) {

                stalker.setCustomName(
                        nombre
                );

                stalker.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private ShadowlandsStalkerEvents() {
    }
}