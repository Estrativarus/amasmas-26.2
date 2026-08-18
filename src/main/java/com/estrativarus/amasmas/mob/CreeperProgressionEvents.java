package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;


@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class CreeperProgressionEvents {

    private static final int DIA_INICIO =
            7;

    private static final int DURACION_EFECTOS =
            20 * 8;

    private static final int AMPLIFICADOR_VELOCIDAD_1 =
            0;

    private static final int AMPLIFICADOR_VELOCIDAD_2 =
            1;

    private static final int DIA_EXPLOSION_MEJORADA =
            14;

    private static final float MULTIPLICADOR_DANO_EXPLOSION =
            2.0F;

    private static final int RADIO_EXPLOSION_DIA_14 =
            6;

    private static final String TAG_EXPLOSION_DIA_14 =
            "amasmas_creeper_explosion_dia_14";
    @SubscribeEvent
    public static void onCreeperExplosionDamage(
            LivingIncomingDamageEvent event
    ) {

        if (!(event.getEntity().level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_EXPLOSION_MEJORADA) {
            return;
        }

        if (!event.getSource().is(
                DamageTypeTags.IS_EXPLOSION
        )) {

            return;
        }

        Entity responsable =
                event.getSource().getEntity();

        if (responsable == null
                || responsable.getType()
                != EntityTypes.CREEPER) {

            return;
        }

        float danoActual =
                event.getAmount();

        event.getContainer().setNewDamage(
                danoActual
                        * MULTIPLICADOR_DANO_EXPLOSION
        );
    }

    private static void aplicarExplosionMejorada(
            Mob creeper
    ) {

        if (creeper
                .getPersistentData()
                .contains(
                        TAG_EXPLOSION_DIA_14
                )) {

            return;
        }

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        String comando =
                "data merge entity "
                        + creeper.getUUID()
                        + " {ExplosionRadius:"
                        + RADIO_EXPLOSION_DIA_14
                        + "b}";

        level.getServer()
                .getCommands()
                .performPrefixedCommand(
                        level.getServer()
                                .createCommandSourceStack()
                                .withSuppressedOutput(),
                        comando
                );

        creeper
                .getPersistentData()
                .putBoolean(
                        TAG_EXPLOSION_DIA_14,
                        true
                );
    }
    @SubscribeEvent
    public static void onCreeperJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob creeper)) {

            return;
        }

        if (creeper.getType()
                != EntityTypes.CREEPER) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                creeper,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onCreeperTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob creeper)) {

            return;
        }

        if (creeper.getType()
                != EntityTypes.CREEPER) {

            return;
        }

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((creeper.tickCount
                + creeper.getId()) % 100 != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                creeper,
                diaActual
        );
    }

    private static void aplicarProgresionActual(
            Mob creeper,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return;
        }

        int amplificadorVelocidad;

        if (diaActual >= 14) {

            amplificadorVelocidad =
                    AMPLIFICADOR_VELOCIDAD_2;

        } else {

            amplificadorVelocidad =
                    AMPLIFICADOR_VELOCIDAD_1;
        }

        aplicarVelocidad(
                creeper,
                amplificadorVelocidad
        );

        if (diaActual >= DIA_EXPLOSION_MEJORADA) {

            aplicarExplosionMejorada(
                    creeper
            );

        }

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    creeper
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    creeper
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    creeper
            );
        }
    }

    private static void aplicarExplosionDia14(
            Mob creeper
    ) {

        if (creeper
                .getPersistentData()
                .contains(
                        TAG_EXPLOSION_DIA_14
                )) {

            return;
        }

        creeper
                .getPersistentData()
                .putBoolean(
                        TAG_EXPLOSION_DIA_14,
                        true
                );

        if (!(creeper.level()
                instanceof ServerLevel level)) {

            return;
        }

        String uuid =
                creeper.getUUID().toString();

        String comando =
                "data merge entity "
                        + uuid
                        + " {ExplosionRadius:"
                        + RADIO_EXPLOSION_DIA_14
                        + "b}";

        level.getServer()
                .getCommands()
                .performPrefixedCommand(
                        level.getServer()
                                .createCommandSourceStack()
                                .withSuppressedOutput(),
                        comando
                );
    }
    private static void aplicarVelocidad(
            Mob creeper,
            int amplificador
    ) {

        MobEffectInstance efectoActual =
                creeper.getEffect(
                        MobEffects.SPEED
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                == amplificador
                && efectoActual.getDuration() > 80) {

            return;
        }

        creeper.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }

    private static void aplicarEtapaDia21(
            Mob creeper
    ) {

    }

    private static void aplicarEtapaDia42(
            Mob creeper
    ) {

    }

    private static void aplicarEtapaDia63(
            Mob creeper
    ) {

    }

    private CreeperProgressionEvents() {
    }
}