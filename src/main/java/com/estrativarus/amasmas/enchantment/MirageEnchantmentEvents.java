package com.estrativarus.amasmas.enchantment;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class MirageEnchantmentEvents {

    private static final double MULTIPLICADOR_DISTANCIA =
            0.75D;

    private static final int INTERVALO_COMPROBACION =
            10;

    @SubscribeEvent
    public static void onLivingChangeTarget(
            LivingChangeTargetEvent event
    ) {

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        LivingEntity nuevoObjetivo =
                event.getNewAboutToBeSetTarget();

        if (!(nuevoObjetivo
                instanceof ServerPlayer player)) {

            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (!tieneEspejismo(
                level,
                player
        )) {

            return;
        }

        if (estaDentroDeDistanciaReducida(
                mob,
                player
        )) {

            return;
        }

        event.setNewAboutToBeSetTarget(
                null
        );
    }

    @SubscribeEvent
    public static void onMobTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((mob.tickCount + mob.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        LivingEntity objetivo =
                mob.getTarget();

        if (!(objetivo
                instanceof ServerPlayer player)) {

            return;
        }

        if (!tieneEspejismo(
                level,
                player
        )) {

            return;
        }

        if (estaDentroDeDistanciaReducida(
                mob,
                player
        )) {

            return;
        }

        mob.setTarget(
                null
        );

        mob.getNavigation()
                .stop();
    }

    private static boolean tieneEspejismo(
            ServerLevel level,
            ServerPlayer player
    ) {

        ItemStack pechera =
                player.getItemBySlot(
                        EquipmentSlot.CHEST
                );

        if (pechera.isEmpty()) {
            return false;
        }

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

        int nivel =
                EnchantmentHelper
                        .getItemEnchantmentLevel(
                                espejismo,
                                pechera
                        );

        return nivel > 0;
    }

    private static boolean estaDentroDeDistanciaReducida(
            Mob mob,
            ServerPlayer player
    ) {

        AttributeInstance atributoSeguimiento =
                mob.getAttribute(
                        Attributes.FOLLOW_RANGE
                );

        if (atributoSeguimiento == null) {

            return true;
        }

        double distanciaOriginal =
                atributoSeguimiento.getValue();

        double distanciaReducida =
                distanciaOriginal
                        * MULTIPLICADOR_DISTANCIA;

        double distanciaReducidaCuadrada =
                distanciaReducida
                        * distanciaReducida;

        return mob.distanceToSqr(
                player
        ) <= distanciaReducidaCuadrada;
    }

    private MirageEnchantmentEvents() {
    }
}