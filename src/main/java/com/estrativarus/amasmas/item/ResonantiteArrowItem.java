package com.estrativarus.amasmas.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ResonantiteArrowItem
        extends ArrowItem {

    public static final String TAG_FLECHA_RESONANTITA =
            "amasmas_flecha_resonantita";

    public static final String TAG_IMPACTO_RESONANTITA =
            "amasmas_impacto_resonantita_silencioso";

    private static final int DURACION_FUEGO_SEGUNDOS =
            0;

    public ResonantiteArrowItem(
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

        flecha.setSilent(
                true
        );

        flecha.igniteForSeconds(
                DURACION_FUEGO_SEGUNDOS
        );

        flecha
                .getPersistentData()
                .putBoolean(
                        TAG_FLECHA_RESONANTITA,
                        true
                );

        return flecha;
    }
}