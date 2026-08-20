package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ShadowlandsStalkerEvents {

    private static final String MOD_NULLSCAPE =
            "nullscape";

    private static final int DIA_INICIO =
            14;

    /*
     * 1 entre 8 Endermen se convierte.
     */
    private static final int PROBABILIDAD_STALKER =
            1;

    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_stalker_shadowlands";

    private static final String TAG_CONVERSION_PENDIENTE =
            "amasmas_conversion_stalker_shadowlands_pendiente";

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

        if (event.loadedFromDisk()) {
            return;
        }

        level.getServer().execute(() -> {

            if (!enderman.isAlive()
                    || enderman.isRemoved()) {

                return;
            }

            intentarTransformar(
                    level,
                    enderman
            );
        });
    }

    private static void intentarTransformar(
            ServerLevel level,
            Mob enderman
    ) {

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_TIRADA_REALIZADA
                )) {

            return;
        }

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_REALIZADA,
                        true
                );

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (!level
                .getBiome(
                        enderman.blockPosition()
                )
                .is(
                        SHADOWLANDS
                )) {

            return;
        }

        if (enderman
                .getRandom()
                .nextInt(
                        PROBABILIDAD_STALKER
                )
                != 0) {

            return;
        }

        programarConversion(
                level,
                enderman
        );
    }

    private static void programarConversion(
            ServerLevel level,
            Mob enderman
    ) {

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_CONVERSION_PENDIENTE
                )) {

            return;
        }

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_CONVERSION_PENDIENTE,
                        true
                );

        double x =
                enderman.getX();

        double y =
                enderman.getY();

        double z =
                enderman.getZ();

        float yRot =
                enderman.getYRot();

        float xRot =
                enderman.getXRot();

        level.getServer().execute(() -> {

            if (!enderman.isAlive()
                    || enderman.isRemoved()) {

                return;
            }

            Mob creaking =
                    EntityTypes.CREAKING.create(
                            level,
                            EntitySpawnReason.TRIGGERED
                    );

            if (creaking == null) {

                enderman
                        .getPersistentData()
                        .remove(
                                TAG_CONVERSION_PENDIENTE
                        );

                return;
            }

            creaking.setPos(
                    x,
                    y,
                    z
            );

            creaking.setYRot(
                    yRot
            );

            creaking.setXRot(
                    xRot
            );

            creaking.setPersistenceRequired();

            StalkerEvents.configurarStalkerExterno(
                    level,
                    creaking
            );

            boolean anadido =
                    level.addFreshEntity(
                            creaking
                    );

            if (!anadido) {

                enderman
                        .getPersistentData()
                        .remove(
                                TAG_CONVERSION_PENDIENTE
                        );

                return;
            }

            eliminarEntidadOriginal(
                    enderman
            );
        });
    }

    private static void eliminarEntidadOriginal(
            Entity entidadOriginal
    ) {

        entidadOriginal.stopRiding();
        entidadOriginal.ejectPassengers();
        entidadOriginal.discard();
    }

    private ShadowlandsStalkerEvents() {
    }
}