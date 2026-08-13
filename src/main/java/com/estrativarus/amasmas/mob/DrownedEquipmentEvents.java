package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class DrownedEquipmentEvents {

    /*
     * Minecraft ya entrega tridente al 6,25 % de los ahogados.
     *
     * Aplicar un 4 % adicional únicamente sobre los ahogados
     * restantes produce una probabilidad total del 10 %:
     *
     * 0,0625 + (0,9375 × 0,04) = 0,10
     */
    private static final float PROBABILIDAD_ADICIONAL_TRIDENTE =
            0.04F;

    @SubscribeEvent
    public static void onEntityJoinLevel(
            EntityJoinLevelEvent event
    ) {

        /*
         * Solo realizamos la operación en el servidor.
         */
        if (event.getLevel().isClientSide()) {
            return;
        }

        /*
         * No volvemos a calcular la probabilidad cuando una
         * entidad guardada se carga nuevamente desde el disco.
         *
         * Sin esta comprobación, un mismo ahogado tendría
         * otra oportunidad cada vez que se carga su chunk.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        /*
         * Comprobamos que la entidad sea un mob.
         */
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        /*
         * Comprobamos su tipo registrado.
         *
         * Así no necesitamos importar directamente la clase
         * Drowned, cuya ubicación cambió en Minecraft 26.2.
         */
        if (mob.getType() != EntityTypes.DROWNED) {
            return;
        }

        /*
         * Si Minecraft ya le ha asignado un tridente mediante
         * su probabilidad vanilla, no modificamos nada.
         */
        if (mob.getMainHandItem().is(Items.TRIDENT)) {
            return;
        }

        /*
         * Aplicamos un 4 % adicional a los ahogados que no
         * recibieron un tridente mediante la probabilidad vanilla.
         */
        if (mob.getRandom().nextFloat()
                < PROBABILIDAD_ADICIONAL_TRIDENTE) {

            mob.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    Items.TRIDENT.getDefaultInstance()
            );
        }
    }

    private DrownedEquipmentEvents() {
    }
}