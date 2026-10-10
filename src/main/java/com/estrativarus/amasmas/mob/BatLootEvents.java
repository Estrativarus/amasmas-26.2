package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class BatLootEvents {

    private static final float PROBABILIDAD_ALA =
            0.90F;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBatDrops(
            LivingDropsEvent event
    ) {

        LivingEntity entidad =
                event.getEntity();

        if (entidad.getType()
                != EntityTypes.BAT) {

            return;
        }

        if (!(entidad.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (entidad
                .getRandom()
                .nextFloat()
                >= PROBABILIDAD_ALA) {

            return;
        }

        ItemStack ala =
                new ItemStack(
                        ModItems.ALA_MURCIELAGO.get(),
                        1
                );

        ItemEntity drop =
                new ItemEntity(
                        level,
                        entidad.getX(),
                        entidad.getY(),
                        entidad.getZ(),
                        ala
                );

        drop.setDefaultPickUpDelay();

        event.getDrops().add(
                drop
        );
    }

    private BatLootEvents() {
    }
}
