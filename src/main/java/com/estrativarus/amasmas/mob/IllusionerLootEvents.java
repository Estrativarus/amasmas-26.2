package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class IllusionerLootEvents {

    private static final float PROBABILIDAD_LIBRO =
            0.50F;

    private static final int NIVEL_ESPEJISMO =
            1;

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
                crearLibroEspejismo(
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

    private static ItemStack crearLibroEspejismo(
            ServerLevel level
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> espejismo =
                registro.getOrThrow(
                        ModEnchantments.ESPEJISMO
                );

        ItemStack libro =
                new ItemStack(
                        Items.ENCHANTED_BOOK
                );

        EnchantmentHelper.updateEnchantments(
                libro,
                encantamientos ->
                        encantamientos.set(
                                espejismo,
                                NIVEL_ESPEJISMO
                        )
        );

        return libro;
    }

    private IllusionerLootEvents() {
    }
}
