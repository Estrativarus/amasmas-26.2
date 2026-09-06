package com.estrativarus.amasmas.component;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    public static final DeferredRegister.DataComponents
            DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    Amasmas.MOD_ID
            );

    public static final DeferredHolder<
            DataComponentType<?>,
            DataComponentType<ItemContainerContents>
            > CONTENIDO_BOLSA =
            DATA_COMPONENTS.registerComponentType(
                    "contenido_bolsa",
                    builder ->
                            builder
                                    .persistent(
                                            ItemContainerContents.CODEC
                                    )
                                    .networkSynchronized(
                                            ItemContainerContents.STREAM_CODEC
                                    )
            );

    public static void register(
            IEventBus modEventBus
    ) {

        DATA_COMPONENTS.register(
                modEventBus
        );
    }

    private ModDataComponents() {
    }
}