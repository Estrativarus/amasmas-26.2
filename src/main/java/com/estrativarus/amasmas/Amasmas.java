package com.estrativarus.amasmas;

import com.estrativarus.amasmas.component.ModDataComponents;
import com.estrativarus.amasmas.creative.ModCreativeTabs;
import com.estrativarus.amasmas.entity.ModEntities;
import com.estrativarus.amasmas.item.ModItems;
import com.estrativarus.amasmas.recipe.ModRecipeSerializers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.estrativarus.amasmas.menu.ModMenus;

@Mod(Amasmas.MOD_ID)
public class Amasmas {

    public static final String MOD_ID =
            "amasmas";

    public Amasmas(
            IEventBus modEventBus
    ) {

        ModDataComponents.register(
                modEventBus
        );

        ModItems.register(
                modEventBus
        );

        ModEntities.register(
                modEventBus
        );

        ModRecipeSerializers.register(
                modEventBus
        );

        ModCreativeTabs.register(
                modEventBus
        );
        ModMenus.register(
                modEventBus
        );
    }
}