package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class ModGameEvents {

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {

        // Solo procesamos eventos causados por jugadores.
        if (!(event.getCause() instanceof Player player)) {
            return;
        }

        // Comprobamos las botas que lleva el jugador.
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        // Si no lleva las botas lanudas, dejamos pasar la vibración.
        if (!boots.is(ModItems.BOTAS_LANUDAS.get())) {
            return;
        }

        // Los pasos siempre son silenciosos con las botas.
        if (event.getVanillaEvent().is(GameEvent.STEP)) {
            event.setCanceled(true);
            return;
        }

        // Los aterrizajes desde poca altura también son silenciosos.
        if (event.getVanillaEvent().is(GameEvent.HIT_GROUND)
                && player.fallDistance <= 4.0F) {

            event.setCanceled(true);
        }
    }

    private ModGameEvents() {
    }
}