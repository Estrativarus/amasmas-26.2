package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.health.PlayerHealthSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
                    serverLevel,
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
                serverLevel,
                player,
                resultado
        );
    }

    private static ItemStack devolverCuenco(
            ServerLevel level,
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
                .add(
                        cuenco
                )) {

            soltarJuntoAlJugador(
                    level,
                    player,
                    cuenco
            );
        }

        return resultado;
    }

    private static void soltarJuntoAlJugador(
            ServerLevel level,
            ServerPlayer player,
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return;
        }

        ItemEntity itemEntity =
                new ItemEntity(
                        level,
                        player.getX(),
                        player.getY() + 0.5D,
                        player.getZ(),
                        stack.copy()
                );

        itemEntity.setDefaultPickUpDelay();

        double movimientoX =
                level.getRandom()
                        .nextGaussian()
                        * 0.05D;

        double movimientoZ =
                level.getRandom()
                        .nextGaussian()
                        * 0.05D;

        itemEntity.setDeltaMovement(
                movimientoX,
                0.2D,
                movimientoZ
        );

        level.addFreshEntity(
                itemEntity
        );
    }
}