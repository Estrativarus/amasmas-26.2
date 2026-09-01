package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.health.PlayerHealthSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class ChorusFlowerStewItem
        extends Item {

    public ChorusFlowerStewItem(
            Properties properties
    ) {

        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity entity
    ) {

        ItemStack resultado =
                super.finishUsingItem(
                        stack,
                        level,
                        entity
                );

        if (!(entity
                instanceof ServerPlayer player)) {

            return resultado;
        }

        if (!(level
                instanceof ServerLevel serverLevel)) {

            return resultado;
        }

        PlayerHealthSavedData datos =
                PlayerHealthSavedData.get(
                        serverLevel.getServer()
                );

        boolean mejoraConcedida =
                datos.concederChorusFlowerStew(
                        player.getUUID()
                );

        if (!mejoraConcedida) {

            player.sendSystemMessage(
                    Component.literal(
                            "Ya habías obtenido la mejora del Chorus Flower Stew."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return devolverCuenco(
                    player,
                    resultado
            );
        }

        PlayerHealthSavedData
                .aplicarSaludGuardada(
                        player
                );

        player.heal(
                PlayerHealthSavedData
                        .BONIFICACION_CHORUS_FLOWER_STEW
        );

        player.sendSystemMessage(
                Component.literal(
                        "Tu salud máxima ha aumentado en 2 corazones."
                ).withStyle(
                        ChatFormatting.LIGHT_PURPLE
                )
        );

        return devolverCuenco(
                player,
                resultado
        );
    }

    private static ItemStack devolverCuenco(
            ServerPlayer player,
            ItemStack resultado
    ) {

        ItemStack cuenco =
                new ItemStack(
                        Items.BOWL
                );

        if (resultado.isEmpty()) {
            return cuenco;
        }

        if (!player
                .getInventory()
                .add(cuenco)) {

            player.drop(
                    cuenco,
                    false
            );
        }

        return resultado;
    }
}