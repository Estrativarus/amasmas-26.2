package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.Amasmas;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Moditems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Amasmas.MOD_ID);

    public static final




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
