package com.estrativarus.amasmas.entity;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.entity.monster.FinalizerSkeleton;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ModEntityEvents {

    @SubscribeEvent
    public static void onCreateAttributes(
            EntityAttributeCreationEvent event
    ) {

        event.put(
                ModEntities.ESQUELETO_FINALIZADOR.get(),
                FinalizerSkeleton
                        .crearAtributos()
                        .build()
        );
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(
            RegisterSpawnPlacementsEvent event
    ) {

        event.register(
                ModEntities.ESQUELETO_FINALIZADOR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (
                        entityType,
                        level,
                        spawnReason,
                        pos,
                        random
                ) -> true,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    private ModEntityEvents() {
    }
}