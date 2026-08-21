package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class Day14NoLootEvents {

    private static final int DIA_INICIO =
            14;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRestrictedMobDrops(
            LivingDropsEvent event
    ) {

        LivingEntity entity =
                event.getEntity();

        if (!(entity.level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (!esMobSinLoot(
                entity.getType()
        )) {

            return;
        }

        event.setCanceled(
                true
        );
    }

    private static boolean esMobSinLoot(
            EntityType<?> type
    ) {

        return type == EntityTypes.BLAZE
                || type == EntityTypes.IRON_GOLEM
                || type == EntityTypes.ZOMBIFIED_PIGLIN
                || type == EntityTypes.GHAST
                || type == EntityTypes.GUARDIAN
                || type == EntityTypes.ELDER_GUARDIAN
                || type == EntityTypes.MAGMA_CUBE
                || type == EntityTypes.ENDERMAN
                || type == EntityTypes.WITCH
                || type == EntityTypes.WITHER_SKELETON
                || type == EntityTypes.EVOKER
                || type == EntityTypes.BREEZE;
    }

    private Day14NoLootEvents() {
    }
}