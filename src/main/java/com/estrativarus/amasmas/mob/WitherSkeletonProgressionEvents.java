package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class WitherSkeletonProgressionEvents {

    private static final String TAG_ARMADURA_DIA_7 =
            "amasmas_wither_armadura_dia_7";

    private static final String TAG_EQUIPO_DIA_14 =
            "amasmas_wither_equipo_dia_14";

    private static final int NIVEL_FILO =
            3;


    private static final int NIVEL_PROTECCION =
            4;

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob witherSkeleton)) {

            return;
        }

        if (witherSkeleton.getType()
                != EntityTypes.WITHER_SKELETON) {

            return;
        }

        aplicarProgresionActual(
                level,
                witherSkeleton
        );
    }

    @SubscribeEvent
    public static void onWitherSkeletonTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob witherSkeleton)) {

            return;
        }

        if (witherSkeleton.getType()
                != EntityTypes.WITHER_SKELETON) {

            return;
        }

        if (!(witherSkeleton.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (witherSkeleton.tickCount % 40 != 0) {
            return;
        }

        aplicarProgresionActual(
                level,
                witherSkeleton
        );
    }

    private static void aplicarProgresionActual(
            ServerLevel level,
            Mob witherSkeleton
    ) {

        if (level.dimension() != Level.NETHER) {
            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        if (diaActual >= 14) {

            aplicarEtapaDia14(
                    level,
                    witherSkeleton
            );

        } else {

            aplicarEtapaDia7(
                    level,
                    witherSkeleton
            );
        }

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    level,
                    witherSkeleton
            );
        }
    }

    private static void aplicarEtapaDia7(
            ServerLevel level,
            Mob witherSkeleton
    ) {

        if (witherSkeleton
                .getPersistentData()
                .contains(
                        TAG_ARMADURA_DIA_7
                )) {

            return;
        }

        witherSkeleton
                .getPersistentData()
                .putBoolean(
                        TAG_ARMADURA_DIA_7,
                        true
                );

        equiparPieza(
                level,
                witherSkeleton,
                EquipmentSlot.HEAD,
                new ItemStack(
                        Items.CHAINMAIL_HELMET
                )
        );

        equiparPieza(
                level,
                witherSkeleton,
                EquipmentSlot.CHEST,
                new ItemStack(
                        Items.CHAINMAIL_CHESTPLATE
                )
        );

        equiparPieza(
                level,
                witherSkeleton,
                EquipmentSlot.LEGS,
                new ItemStack(
                        Items.CHAINMAIL_LEGGINGS
                )
        );

        equiparPieza(
                level,
                witherSkeleton,
                EquipmentSlot.FEET,
                new ItemStack(
                        Items.CHAINMAIL_BOOTS
                )
        );

        bloquearDropsArmadura(
                witherSkeleton
        );
    }

    private static void equiparPieza(
            ServerLevel level,
            Mob witherSkeleton,
            EquipmentSlot slot,
            ItemStack pieza
    ) {

        anadirEncantamiento(
                level,
                pieza,
                Enchantments.PROTECTION,
                NIVEL_PROTECCION
        );

        witherSkeleton.setItemSlot(
                slot,
                pieza
        );
    }

    private static void anadirEncantamiento(
            ServerLevel level,
            ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey,
            int nivel
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> holder =
                registro.getOrThrow(
                        enchantmentKey
                );

        EnchantmentHelper.updateEnchantments(
                stack,
                encantamientos ->
                        encantamientos.set(
                                holder,
                                nivel
                        )
        );
    }

    private static void bloquearDropsArmadura(
            Mob witherSkeleton
    ) {

        witherSkeleton.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );
    }

    private static void aplicarEtapaDia14(
            ServerLevel level,
            Mob witherSkeleton
    ) {

        if (witherSkeleton
                .getPersistentData()
                .contains(
                        TAG_EQUIPO_DIA_14
                )) {

            return;
        }

        ItemStack casco =
                new ItemStack(
                        Items.NETHERITE_HELMET
                );

        ItemStack pechera =
                new ItemStack(
                        Items.NETHERITE_CHESTPLATE
                );

        ItemStack pantalones =
                new ItemStack(
                        Items.NETHERITE_LEGGINGS
                );

        ItemStack botas =
                new ItemStack(
                        Items.NETHERITE_BOOTS
                );

        prepararPiezaDia14(
                level,
                casco
        );

        prepararPiezaDia14(
                level,
                pechera
        );

        prepararPiezaDia14(
                level,
                pantalones
        );

        prepararPiezaDia14(
                level,
                botas
        );

        witherSkeleton.setItemSlot(
                EquipmentSlot.HEAD,
                casco
        );

        witherSkeleton.setItemSlot(
                EquipmentSlot.CHEST,
                pechera
        );

        witherSkeleton.setItemSlot(
                EquipmentSlot.LEGS,
                pantalones
        );

        witherSkeleton.setItemSlot(
                EquipmentSlot.FEET,
                botas
        );

        ItemStack espada =
                new ItemStack(
                        Items.NETHERITE_SWORD
                );

        anadirEncantamiento(
                level,
                espada,
                Enchantments.SHARPNESS,
                NIVEL_FILO
        );

        witherSkeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                espada
        );

        bloquearDropsEquipoDia14(
                witherSkeleton
        );

        witherSkeleton
                .getPersistentData()
                .putBoolean(
                        TAG_ARMADURA_DIA_7,
                        true
                );

        witherSkeleton
                .getPersistentData()
                .putBoolean(
                        TAG_EQUIPO_DIA_14,
                        true
                );
    }

    private static void prepararPiezaDia14(
            ServerLevel level,
            ItemStack pieza
    ) {

        anadirEncantamiento(
                level,
                pieza,
                Enchantments.PROTECTION,
                NIVEL_PROTECCION
        );

        aplicarRibeteRibDeCuarzo(
                level,
                pieza
        );
    }

    private static void aplicarRibeteRibDeCuarzo(
            ServerLevel level,
            ItemStack pieza
    ) {

        Registry<TrimPattern> patrones =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.TRIM_PATTERN
                        );

        Registry<TrimMaterial> materiales =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.TRIM_MATERIAL
                        );

        Holder.Reference<TrimPattern> patronRib =
                patrones.getOrThrow(
                        TrimPatterns.RIB
                );

        Holder.Reference<TrimMaterial> materialCuarzo =
                materiales.getOrThrow(
                        TrimMaterials.QUARTZ
                );

        ArmorTrim ribete =
                new ArmorTrim(
                        materialCuarzo,
                        patronRib
                );

        pieza.set(
                DataComponents.TRIM,
                ribete
        );
    }

    private static void bloquearDropsEquipoDia14(
            Mob witherSkeleton
    ) {

        witherSkeleton.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );

        witherSkeleton.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    private static void aplicarEtapaDia21(
            ServerLevel level,
            Mob witherSkeleton
    ) {

    }

    private WitherSkeletonProgressionEvents() {
    }
}