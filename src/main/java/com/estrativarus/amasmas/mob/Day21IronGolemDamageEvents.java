package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class Day21IronGolemDamageEvents {

    private static final int DIA_INICIO =
            21;

    private static final float MULTIPLICADOR_DANO =
            2.0F;

    @SubscribeEvent
    public static void onLivingIncomingDamage(
            LivingIncomingDamageEvent event
    ) {

        if (!(event.getEntity().level()
                instanceof ServerLevel level)) {

            return;
        }

        Entity atacante =
                event.getSource()
                        .getEntity();

        if (!(atacante instanceof IronGolem)) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        float danoOriginal =
                event.getAmount();

        event.setAmount(
                danoOriginal
                        * MULTIPLICADOR_DANO
        );
    }

    private Day21IronGolemDamageEvents() {
    }
}