package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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

    private static final String TAG_TRANSFORMACION_PENDIENTE =
            "amasmas_transformacion_arana_pendiente";

    private static final int DURACION_RESISTENCIA =
            20 * 15;

    private static final int AMPLIFICADOR_RESISTENCIA =
            2;

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob arana)) {

            return;
        }

        if (arana.getType() != EntityTypes.SPIDER
                && arana.getType()
                != EntityTypes.CAVE_SPIDER) {

            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        if (arana.getType()
                == EntityTypes.CAVE_SPIDER) {

            aplicarProgresion(
                    arana,
                    diaActual
            );

            return;
        }

        programarTransformacion(
                level,
                arana,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onSpiderTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob arana)) {

            return;
        }

        if (arana.getType() != EntityTypes.SPIDER
                && arana.getType()
                != EntityTypes.CAVE_SPIDER) {

            return;
        }

        if (!(arana.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (arana.tickCount % 20 != 0) {
            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        if (arana.getType()
                == EntityTypes.SPIDER) {

            programarTransformacion(
                    level,
                    arana,
                    diaActual
            );

            return;
        }

        aplicarProgresion(
                arana,
                diaActual
        );
    }

    private static void programarTransformacion(
            ServerLevel level,
            Mob arana,
            int diaActual
    ) {

        if (arana
                .getPersistentData()
                .contains(
                        TAG_TRANSFORMACION_PENDIENTE
                )) {

            return;
        }

        arana
                .getPersistentData()
                .putBoolean(
                        TAG_TRANSFORMACION_PENDIENTE,
                        true
                );

        double x =
                arana.getX();

        double y =
                arana.getY();

        double z =
                arana.getZ();

        float rotacionHorizontal =
                arana.getYRot();

        float rotacionVertical =
                arana.getXRot();

        boolean tieneNombre =
                arana.hasCustomName();

        var nombreAnterior =
                arana.getCustomName();

        boolean nombreVisible =
                arana.isCustomNameVisible();

        boolean persistente =
                arana.isPersistenceRequired();

        level.getServer().execute(() -> {

            if (!arana.isAlive()
                    || arana.isRemoved()) {

                return;
            }

            Mob aranaDeCueva =
                    EntityTypes.CAVE_SPIDER.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            if (aranaDeCueva == null) {
                return;
            }

            aranaDeCueva.setPos(
                    x,
                    y,
                    z
            );

            aranaDeCueva.setYRot(
                    rotacionHorizontal
            );

            aranaDeCueva.setXRot(
                    rotacionVertical
            );

            if (tieneNombre
                    && nombreAnterior != null) {

                aranaDeCueva.setCustomName(
                        nombreAnterior.copy()
                );

                aranaDeCueva.setCustomNameVisible(
                        nombreVisible
                );
            }

            if (persistente) {
                aranaDeCueva.setPersistenceRequired();
            }

            aplicarProgresion(
                    aranaDeCueva,
                    diaActual
            );

            arana.discard();

            level.addFreshEntity(
                    aranaDeCueva
            );
        });
    }

    private static void aplicarProgresion(
            Mob arana,
            int diaActual
    ) {

        if (diaActual < 7) {
            return;
        }

        arana.addEffect(
                new MobEffectInstance(
                        MobEffects.RESISTANCE,
                        DURACION_RESISTENCIA,
                        AMPLIFICADOR_RESISTENCIA,
                        false,
                        false,
                        false
                )
        );
    }

    private CaveSpiderProgressionEvents() {
    }
}