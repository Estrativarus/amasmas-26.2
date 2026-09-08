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

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class SpiderRegenerationEvents {

    private static final int DIA_INICIO =
            21;

    private static final int AMPLIFICADOR_REGENERACION =
            3;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onSpiderJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob spider)) {

            return;
        }

        if (!esAranaPermitida(
                spider
        )) {

            return;
        }

        aplicarRegeneracionSiCorresponde(
                level,
                spider
        );
    }

    @SubscribeEvent
    public static void onSpiderTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob spider)) {

            return;
        }

        if (!esAranaPermitida(
                spider
        )) {

            return;
        }

        if (!(spider.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((spider.tickCount + spider.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        aplicarRegeneracionSiCorresponde(
                level,
                spider
        );
    }

    private static void aplicarRegeneracionSiCorresponde(
            ServerLevel level,
            Mob spider
    ) {

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        MobEffectInstance efectoActual =
                spider.getEffect(
                        MobEffects.REGENERATION
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                >= AMPLIFICADOR_REGENERACION
                && efectoActual.isInfiniteDuration()) {

            return;
        }

        spider.addEffect(
                new MobEffectInstance(
                        MobEffects.REGENERATION,
                        MobEffectInstance.INFINITE_DURATION,
                        AMPLIFICADOR_REGENERACION,
                        false,
                        false,
                        false
                )
        );
    }

    private static boolean esAranaPermitida(
            Mob mob
    ) {

        return mob.getType()
                == EntityTypes.SPIDER

                || mob.getType()
                == EntityTypes.CAVE_SPIDER;
    }

    private SpiderRegenerationEvents() {
    }
}