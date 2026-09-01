package com.estrativarus.amasmas.creative;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab>
            CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    Amasmas.MOD_ID
            );

    public static final DeferredHolder<
            CreativeModeTab,
            CreativeModeTab
            > AMASMAS_TAB =
            CREATIVE_MODE_TABS.register(
                    "amasmas_tab",
                    () -> CreativeModeTab.builder()

                            .title(
                                    Component.translatable(
                                            "creativetab.amasmas.amasmas_tab"
                                    )
                            )

                            .icon(
                                    () -> new ItemStack(
                                            ModItems.BOTAS_LANUDAS.get()
                                    )
                            )

                            .displayItems(
                                    (parameters, output) -> {

                                        output.accept(
                                                ModItems.BOTAS_LANUDAS.get()
                                        );

                                        output.accept(
                                                ModItems.MANZANA_NETHERITA.get()
                                        );

                                        output.accept(
                                                ModItems.FRAGMENTO_RESONANTITA.get()
                                        );

                                        output.accept(
                                                ModItems.ARCO_RESONANTITA.get()
                                        );
                                    }
                            )

                            .build()
            );

    public static void register(
            IEventBus modEventBus
    ) {

        CREATIVE_MODE_TABS.register(
                modEventBus
        );
    }

    private ModCreativeTabs() {
    }
}