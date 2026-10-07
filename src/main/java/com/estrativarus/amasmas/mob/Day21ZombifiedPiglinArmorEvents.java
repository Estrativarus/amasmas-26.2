package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class Day21ZombifiedPiglinArmorEvents {

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final String TAG_ARMADURA_DIA_21 =
            "amasmas_zombified_piglin_armadura_dia_21";

    @SubscribeEvent
    public static void onZombifiedPiglinJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob piglin)) {

            return;
        }

        if (!esPiglinZombificado(
                piglin
        )) {

            return;
        }

        if (esCapitan(
                piglin
        )) {

            return;
        }

        level.getServer().execute(() -> {

            if (!piglin.isAlive()
                    || piglin.isRemoved()) {

                return;
            }

            aplicarArmaduraSiCorresponde(
                    level,
                    piglin
            );
        });
    }

    @SubscribeEvent
    public static void onZombifiedPiglinTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob piglin)) {

            return;
        }

        if (!esPiglinZombificado(
                piglin
        )) {

            return;
        }

        if (!(piglin.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((piglin.tickCount + piglin.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        aplicarArmaduraSiCorresponde(
                level,
                piglin
        );
    }

    private static void aplicarArmaduraSiCorresponde(
            ServerLevel level,
            Mob piglin
    ) {

        if (!piglin.isAlive()
                || piglin.isRemoved()) {

            return;
        }

        if (!esPiglinZombificado(
                piglin
        )) {

            return;
        }

        if (esCapitan(
                piglin
        )) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (piglin
                .getPersistentData()
                .contains(
                        TAG_ARMADURA_DIA_21
                )) {

            asegurarArmaduraDiamante(
                    piglin
            );

            return;
        }

        equiparArmaduraDiamante(
                piglin
        );

        piglin
                .getPersistentData()
                .putBoolean(
                        TAG_ARMADURA_DIA_21,
                        true
                );
    }

    private static void equiparArmaduraDiamante(
            Mob piglin
    ) {

        piglin.setItemSlot(
                EquipmentSlot.HEAD,
                new ItemStack(
                        Items.DIAMOND_HELMET
                )
        );

        piglin.setItemSlot(
                EquipmentSlot.CHEST,
                new ItemStack(
                        Items.DIAMOND_CHESTPLATE
                )
        );

        piglin.setItemSlot(
                EquipmentSlot.LEGS,
                new ItemStack(
                        Items.DIAMOND_LEGGINGS
                )
        );

        piglin.setItemSlot(
                EquipmentSlot.FEET,
                new ItemStack(
                        Items.DIAMOND_BOOTS
                )
        );
    }

    private static void asegurarArmaduraDiamante(
            Mob piglin
    ) {

        if (!piglin
                .getItemBySlot(
                        EquipmentSlot.HEAD
                )
                .is(
                        Items.DIAMOND_HELMET
                )) {

            piglin.setItemSlot(
                    EquipmentSlot.HEAD,
                    new ItemStack(
                            Items.DIAMOND_HELMET
                    )
            );
        }

        if (!piglin
                .getItemBySlot(
                        EquipmentSlot.CHEST
                )
                .is(
                        Items.DIAMOND_CHESTPLATE
                )) {

            piglin.setItemSlot(
                    EquipmentSlot.CHEST,
                    new ItemStack(
                            Items.DIAMOND_CHESTPLATE
                    )
            );
        }

        if (!piglin
                .getItemBySlot(
                        EquipmentSlot.LEGS
                )
                .is(
                        Items.DIAMOND_LEGGINGS
                )) {

            piglin.setItemSlot(
                    EquipmentSlot.LEGS,
                    new ItemStack(
                            Items.DIAMOND_LEGGINGS
                    )
            );
        }

        if (!piglin
                .getItemBySlot(
                        EquipmentSlot.FEET
                )
                .is(
                        Items.DIAMOND_BOOTS
                )) {

            piglin.setItemSlot(
                    EquipmentSlot.FEET,
                    new ItemStack(
                            Items.DIAMOND_BOOTS
                    )
            );
        }
    }

    private static boolean esPiglinZombificado(
            Mob piglin
    ) {

        return piglin.getType()
                == EntityTypes.ZOMBIFIED_PIGLIN;
    }

    private static boolean esCapitan(
            Mob piglin
    ) {

        return ZombifiedPiglinCaptainEvents
                .esCapitan(
                        piglin
                );
    }

    private Day21ZombifiedPiglinArmorEvents() {
    }
}