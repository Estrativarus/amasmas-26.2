package com.estrativarus.amasmas.health;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class PlayerHealthEvents {

    /*
     * Al entrar en el servidor restauramos la salud máxima.
     */
    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        PlayerHealthSavedData
                .aplicarSaludGuardada(player);
    }

    /*
     * Al reaparecer después de morir se crea una nueva
     * instancia del jugador, así que reaplicamos la mejora.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        PlayerHealthSavedData
                .aplicarSaludGuardada(player);
    }

    /*
     * También la reaplicamos al cambiar de dimensión para
     * cubrir recreaciones o sincronizaciones de la entidad.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        PlayerHealthSavedData
                .aplicarSaludGuardada(player);
    }

    private PlayerHealthEvents() {
    }
}