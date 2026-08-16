package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class PigAiFixEvents {

    /*
     * Comprobamos una vez por segundo.
     */
    private static final int INTERVALO_COMPROBACION =
            20;

    @SubscribeEvent
    public static void onPigTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity() instanceof Mob pig)) {
            return;
        }

        /*
         * Únicamente cerdos vanilla.
         *
         * No afecta a Piglins, Hoglins, Piglin Brutes
         * ni Piglins zombificados.
         */
        if (pig.getType() != EntityTypes.PIG) {
            return;
        }

        if (!(pig.level() instanceof ServerLevel)) {
            return;
        }

        if (pig.tickCount
                % INTERVALO_COMPROBACION != 0) {
            return;
        }

        /*
         * Si alguna mecánica dejó al cerdo con NoAI,
         * volvemos a activar su comportamiento.
         */
        if (pig.isNoAi()) {
            pig.setNoAi(false);
        }
    }

    private PigAiFixEvents() {
    }
}