package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class EndermanProgressionEvents {

    private static final int DIA_DANO_AUMENTADO =
            14;

    private static final double DANO_DIA_14 =
            60.0D;

    @SubscribeEvent
    public static void onEndermanJoin(
            EntityJoinLevelEvent event
    ) {

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
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                enderman,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onEndermanTick(
            EntityTickEvent.Post event
    ) {

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

        if ((enderman.tickCount
                + enderman.getId()) % 100 != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        aplicarProgresionActual(
                enderman,
                diaActual
        );
    }

    private static void aplicarProgresionActual(
            Mob enderman,
            int diaActual
    ) {

        if (diaActual < DIA_DANO_AUMENTADO) {
            return;
        }

        aplicarEtapaDia14(
                enderman
        );

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    enderman
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    enderman
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    enderman
            );
        }
    }

    private static void aplicarEtapaDia14(
            Mob enderman
    ) {

        AttributeInstance atributoDano =
                enderman.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (atributoDano == null) {
            return;
        }

        if (atributoDano.getBaseValue()
                == DANO_DIA_14) {

            return;
        }

        atributoDano.setBaseValue(
                DANO_DIA_14
        );
    }

    private static void aplicarEtapaDia21(
            Mob enderman
    ) {

    }

    private static void aplicarEtapaDia42(
            Mob enderman
    ) {

    }

    private static void aplicarEtapaDia63(
            Mob enderman
    ) {

    }

    private EndermanProgressionEvents() {
    }
}