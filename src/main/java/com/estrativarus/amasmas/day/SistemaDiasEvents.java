package com.estrativarus.amasmas.day;

import com.estrativarus.amasmas.Amasmas;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class SistemaDiasEvents {

    /*
     * Se ejecuta cuando el servidor ha terminado de arrancar.
     *
     * También sucede en un mundo de un jugador, porque internamente
     * Minecraft abre un servidor integrado.
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {

        SistemaDiasSavedData data =
                SistemaDiasSavedData.get(event.getServer());

        System.out.println(
                "[Amasmas] Sistema de días cargado. Día actual: "
                        + data.getDiaActual()
        );
    }

    private SistemaDiasEvents() {
    }
}