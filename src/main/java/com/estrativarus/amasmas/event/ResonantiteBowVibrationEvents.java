package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ResonantiteBowVibrationEvents {

    @SubscribeEvent
    public static void onVanillaGameEvent(
            VanillaGameEvent event
    ) {

        if (!event
                .getVanillaEvent()
                .equals(
                        GameEvent.PROJECTILE_SHOOT
                )) {

            return;
        }

        if (!(event.getCause()
                instanceof ServerPlayer player)) {

            return;
        }

        if (!tieneArcoResonantitaEnLaMano(
                player
        )) {

            return;
        }

        event.setCanceled(
                true
        );
    }

    private static boolean tieneArcoResonantitaEnLaMano(
            ServerPlayer player
    ) {

        ItemStack manoPrincipal =
                player.getMainHandItem();

        if (manoPrincipal.is(
                ModItems.ARCO_RESONANTITA.get()
        )) {

            return true;
        }

        ItemStack manoSecundaria =
                player.getOffhandItem();

        return manoSecundaria.is(
                ModItems.ARCO_RESONANTITA.get()
        );
    }

    private ResonantiteBowVibrationEvents() {
    }
}