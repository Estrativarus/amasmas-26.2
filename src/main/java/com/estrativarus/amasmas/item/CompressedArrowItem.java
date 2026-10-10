package com.estrativarus.amasmas.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CompressedArrowItem
        extends ArrowItem {

    public static final String TAG_FLECHA_COMPRIMIDA =
            "amasmas_flecha_comprimida";

    public static final String TAG_EXPLOSION_REALIZADA =
            "amasmas_flecha_comprimida_explosion_realizada";

    public CompressedArrowItem(
            Properties properties
    ) {

        super(properties);
    }

    @Override
    public AbstractArrow createArrow(
            Level level,
            ItemStack municion,
            LivingEntity tirador,
            ItemStack arma
    ) {

        AbstractArrow flecha =
                super.createArrow(
                        level,
                        municion,
                        tirador,
                        arma
                );

        flecha.setNoGravity(
                true
        );

        flecha
                .getPersistentData()
                .putBoolean(
                        TAG_FLECHA_COMPRIMIDA,
                        true
                );

        return flecha;
    }
}