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

public class ModCreativeTabs {

    /*
     * Registro que contendrá las pestañas creativas del mod.
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    Amasmas.MOD_ID
            );

    /*
     * Pestaña creativa principal del mod.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AMASMAS_TAB =
            CREATIVE_MODE_TABS.register(
                    "amasmas_tab",
                    () -> CreativeModeTab.builder()

                            /*
                             * Nombre que aparecerá en la parte superior
                             * del inventario creativo.
                             */
                            .title(
                                    Component.translatable(
                                            "itemGroup.amasmas.amasmas_tab"
                                    )
                            )

                            /*
                             * Icono de la pestaña.
                             * Utilizamos las propias botas lanudas.
                             */
                            .icon(
                                    () -> new ItemStack(
                                            ModItems.BOTAS_LANUDAS.get()
                                    )
                            )

                            /*
                             * Objetos que aparecerán dentro de la pestaña.
                             */
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.BOTAS_LANUDAS.get());
                            })

                            .build()
            );

    /*
     * Conecta el registro con el bus del mod.
     */
    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private ModCreativeTabs() {
    }
}