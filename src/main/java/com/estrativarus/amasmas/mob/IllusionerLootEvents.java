package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class IllusionerLootEvents {

    private static final float PROBABILIDAD_LIBRO =
            0.50F;

    private static final int NIVEL_SAQUEO =
            5;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIllusionerDrops(
            LivingDropsEvent event
    ) {

        LivingEntity illusioner =
                event.getEntity();

        if (illusioner.getType()
                != EntityTypes.ILLUSIONER) {

            return;
        }

        if (!(illusioner.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (illusioner
                .getRandom()
                .nextFloat()
                >= PROBABILIDAD_LIBRO) {

            return;
        }

        ItemStack libro =
                crearLibroSaqueo(
                        level
                );

        ItemEntity drop =
                new ItemEntity(
                        level,
                        illusioner.getX(),
                        illusioner.getY(),
                        illusioner.getZ(),
                        libro
                );

        event.getDrops().add(
                drop
        );
    }

    private static ItemStack crearLibroSaqueo(
            ServerLevel level
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> saqueo =
                registro.getOrThrow(
                        Enchantments.LOOTING
                );

        ItemStack libro =
                new ItemStack(
                        Items.ENCHANTED_BOOK
                );

        EnchantmentHelper.updateEnchantments(
                libro,
                encantamientos ->
                        encantamientos.set(
                                saqueo,
                                NIVEL_SAQUEO
                        )
        );

        return libro;
    }

    private IllusionerLootEvents() {
    }
}