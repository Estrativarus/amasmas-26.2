package com.estrativarus.amasmas.client;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.client.screen.BolsaGiganteScreen;
import com.estrativarus.amasmas.menu.ModMenus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = Amasmas.MOD_ID
)
public final class ClientModEvents {

    @SubscribeEvent
    public static void registerMenuScreens(
            RegisterMenuScreensEvent event
    ) {

        event.register(ModMenus.BOLSA_GIGANTE_MENU.get(), BolsaGiganteScreen::new);
    }

    private ClientModEvents() {
    }
}