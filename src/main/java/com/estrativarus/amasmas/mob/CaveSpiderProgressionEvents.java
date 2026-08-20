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

    private static final int DURACION_EFECTOS =
            20 * 15;

    private static final int AMPLIFICADOR_RESISTENCIA =
            2;

    private static final int AMPLIFICADOR_FUERZA =
            4;

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

    private static void aplicarEtapaDia21(
            Mob mob
    ) {

    }

    private static void aplicarEtapaDia42(
            Mob mob
    ) {

    }

    private static void aplicarEtapaDia63(
            Mob mob
    ) {

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

    private static void renovarEfectoSiNecesario(
            Mob mob,
            net.minecraft.core.Holder<
                    net.minecraft.world.effect.MobEffect
                    > efecto,
            int amplificador
    ) {

        MobEffectInstance efectoActual =
                mob.getEffect(
                        efecto
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                == amplificador
                && efectoActual.getDuration() > 100) {

            return;
        }

        mob.addEffect(
                new MobEffectInstance(
                        efecto,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }


    public static void aplicarProgresion(
            Mob mob,
            int diaActual
    ) {

        if (diaActual < 7) {
            return;
        }

        renovarEfectoSiNecesario(
                mob,
                MobEffects.RESISTANCE,
                AMPLIFICADOR_RESISTENCIA
        );

        renovarEfectoSiNecesario(
                mob,
                MobEffects.STRENGTH,
                AMPLIFICADOR_FUERZA
        );

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    mob
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    mob
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    mob
            );
        }
    }


    private CaveSpiderProgressionEvents() {
    }
}