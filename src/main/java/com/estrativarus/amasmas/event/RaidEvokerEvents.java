package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.world.entity.raid.Raider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class RaidEvokerEvents {

    private RaidEvokerEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Este cambio solamente debe ejecutarse en el servidor.
        if (event.getLevel().isClientSide()) {
            return;
        }

        /*
         * Comprobamos el ID de la entidad.
         *
         * Usamos el ID "minecraft:evoker" porque EntityType.EVOKER
         * no está disponible en los mappings de tu versión.
         */
        String entityId = event.getEntity()
                .getType()
                .builtInRegistryHolder()
                .key()
                .identifier()
                .toString();

        if (!entityId.equals("minecraft:evoker")) {
            return;
        }

        /*
         * Un invocador es un Raider, es decir, una entidad que puede
         * participar en una invasión.
         */
        if (!(event.getEntity() instanceof Raider raider)) {
            return;
        }

        /*
         * Los invocadores de las mansiones no tienen una raid asignada.
         *
         * Si getCurrentRaid() devuelve null, dejamos que la entidad
         * aparezca normalmente.
         */
        if (raider.getCurrentRaid() == null) {
            return;
        }

        /*
         * Llegados a este punto sabemos que:
         *
         * 1. La entidad es un invocador.
         * 2. El invocador pertenece a una raid.
         *
         * Cancelamos exclusivamente su aparición en la invasión.
         */
        event.setCanceled(true);
    }
}