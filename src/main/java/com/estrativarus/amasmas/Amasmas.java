package com.estrativarus.amasmas;
//Mulayín
import com.estrativarus.amasmas.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Amasmas.MOD_ID)
public class Amasmas {

    public static final String MOD_ID = "amasmas";

    public Amasmas(IEventBus modEventBus) {
        ModItems.register(modEventBus);
    }
}