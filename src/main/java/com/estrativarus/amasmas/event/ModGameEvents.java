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

        // Comprobamos que el causante del evento sea un jugador.
        if (!(event.getCause() instanceof Player player)) {
            return;
        }

        // Solamente queremos silenciar los pasos.
        if (!event.getVanillaEvent().is(GameEvent.STEP)) {
            return;
        }

        // Obtenemos el objeto equipado en los pies.
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        // Si lleva las botas lanudas, cancelamos la vibración del paso.
        if (boots.is(ModItems.BOTAS_LANUDAS.get())) {
            event.setCanceled(true);

           if (event.getVanillaEvent().is(GameEvent.HIT_GROUND)
                   && player.fallDistance <= 4.0f) {
               event.setCanceled(true);
           }
        }
    }

    private ModGameEvents() {
    }
}