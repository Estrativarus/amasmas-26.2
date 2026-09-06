package com.estrativarus.amasmas.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EnderBagItem
        extends Item {

    public EnderBagItem(
            Properties properties
    ) {

        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack stack =
                player.getItemInHand(
                        hand
                );

        if (!stack.is(
                ModItems.BOLSA_ENDER.get()
        )) {

            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {

            return InteractionResult.SUCCESS;
        }

        if (!(player
                instanceof ServerPlayer serverPlayer)) {

            return InteractionResult.PASS;
        }

        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (
                                containerId,
                                inventory,
                                menuPlayer
                        ) ->
                                ChestMenu.threeRows(
                                        containerId,
                                        inventory,
                                        serverPlayer
                                                .getEnderChestInventory()
                                ),
                        Component.translatable(
                                "container.amasmas.bolsa_ender"
                        )
                )
        );

        return InteractionResult.SUCCESS;
    }
}

