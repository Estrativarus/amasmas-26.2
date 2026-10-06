package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class LootingFiveAnvilEvents {

    private static final int NIVEL_SAQUEO =
            5;

    private static final int COSTE_EXPERIENCIA =
            20;

    @SubscribeEvent
    public static void onAnvilUpdate(
            AnvilUpdateEvent event
    ) {

        ItemStack espada =
                event.getLeft();

        ItemStack libro =
                event.getRight();

        if (espada.isEmpty()
                || libro.isEmpty()) {

            return;
        }

        if (!espada.is(
                ItemTags.SWORDS
        )) {

            return;
        }

        if (!libro.is(
                Items.ENCHANTED_BOOK
        )) {

            return;
        }

        HolderLookup.RegistryLookup<Enchantment> registro =
                event
                        .getPlayer()
                        .level()
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> saqueo =
                registro.getOrThrow(
                        Enchantments.LOOTING
                );

        ItemEnchantments encantamientosLibro =
                EnchantmentHelper
                        .getEnchantmentsForCrafting(
                                libro
                        );

        int nivelLibro =
                encantamientosLibro.getLevel(
                        saqueo
                );

        if (nivelLibro != NIVEL_SAQUEO) {
            return;
        }

        ItemStack resultado =
                espada.copy();

        resultado.setCount(
                1
        );

        EnchantmentHelper.updateEnchantments(
                resultado,
                encantamientos ->
                        encantamientos.set(
                                saqueo,
                                NIVEL_SAQUEO
                        )
        );

        event.setOutput(
                resultado
        );

        event.setXpCost(
                COSTE_EXPERIENCIA
        );

        event.setMaterialCost(
                1
        );
    }

    private LootingFiveAnvilEvents() {
    }
}