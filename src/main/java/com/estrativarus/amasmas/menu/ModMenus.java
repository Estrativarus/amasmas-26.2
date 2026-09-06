package com.estrativarus.amasmas.menu;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>>
            MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    Amasmas.MOD_ID
            );

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<GiantBagMenu>
            > BOLSA_GIGANTE_MENU =
            MENUS.register(
                    "bolsa_gigante",
                    () -> new MenuType<>(
                            GiantBagMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static void register(
            IEventBus modEventBus
    ) {

        MENUS.register(
                modEventBus
        );
    }

    private ModMenus() {
    }
}