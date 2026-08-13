package com.estrativarus.amasmas;

import com.estrativarus.amasmas.creative.ModCreativeTabs;
import com.estrativarus.amasmas.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Amasmas.MOD_ID)
public class Amasmas {

    public static final String MOD_ID = "amasmas";

    public Amasmas(IEventBus modEventBus) {

        // Registramos los objetos.
        ModItems.register(modEventBus);

        // Registramos las pestañas creativas.
        ModCreativeTabs.register(modEventBus);
    }
}