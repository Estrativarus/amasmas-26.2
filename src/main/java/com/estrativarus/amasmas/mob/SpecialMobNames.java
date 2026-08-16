package com.estrativarus.amasmas.mob;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class SpecialMobNames {

    public static void asignar(
            Entity entity,
            String nombre,
            ChatFormatting color
    ) {

        entity.setCustomName(
                Component.literal(nombre)
                        .withStyle(
                                color,
                                ChatFormatting.BOLD
                        )
        );

        entity.setCustomNameVisible(false);
    }

    private SpecialMobNames() {
    }
}