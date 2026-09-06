package com.estrativarus.amasmas.entity;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.entity.monster.FinalizerSkeleton;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.estrativarus.amasmas.entity.monster.IntelligentGiant;

public final class ModEntities {

    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(
                    Amasmas.MOD_ID
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<FinalizerSkeleton>
            > ESQUELETO_FINALIZADOR =
            ENTITY_TYPES.register(
                    "esqueleto_finalizador",
                    () -> {

                        ResourceKey<EntityType<?>> key =
                                ResourceKey.create(
                                        Registries.ENTITY_TYPE,
                                        Identifier.fromNamespaceAndPath(
                                                Amasmas.MOD_ID,
                                                "esqueleto_finalizador"
                                        )
                                );

                        return EntityType.Builder
                                .of(
                                        FinalizerSkeleton::new,
                                        MobCategory.MONSTER
                                )
                                .sized(
                                        0.6F,
                                        1.99F
                                )
                                .eyeHeight(
                                        1.74F
                                )
                                .clientTrackingRange(
                                        10
                                )
                                .updateInterval(
                                        3
                                )
                                .build(
                                        key
                                );
                    }
            );
    public static final DeferredHolder<
            EntityType<?>,
            EntityType<IntelligentGiant>
            > GIGANTE =
            ENTITY_TYPES.register(
                    "gigante",
                    () -> {

                        ResourceKey<EntityType<?>> key =
                                ResourceKey.create(
                                        Registries.ENTITY_TYPE,
                                        Identifier.fromNamespaceAndPath(
                                                Amasmas.MOD_ID,
                                                "gigante"
                                        )
                                );

                        return EntityType.Builder
                                .of(
                                        IntelligentGiant::new,
                                        MobCategory.MONSTER
                                )
                                .sized(
                                        3.6F,
                                        12.0F
                                )
                                .eyeHeight(
                                        10.44F
                                )
                                .clientTrackingRange(
                                        16
                                )
                                .updateInterval(
                                        3
                                )
                                .build(
                                        key
                                );
                    }
            );

    public static void register(
            IEventBus modEventBus
    ) {

        ENTITY_TYPES.register(
                modEventBus
        );
    }

    private ModEntities() {
    }
}