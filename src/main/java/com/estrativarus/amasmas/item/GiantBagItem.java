package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.component.ModDataComponents;
import com.estrativarus.amasmas.menu.GiantBagMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class GiantBagItem
        extends Item {

    public static final int NUMERO_SLOTS =
            4;

    public GiantBagItem(
            Properties properties
    ) {

        super(properties);
    }

    public static ItemContainerContents obtenerContenido(
            ItemStack bolsa
    ) {

        return bolsa.getOrDefault(
                ModDataComponents.CONTENIDO_BOLSA.get(),
                ItemContainerContents.EMPTY
        );
    }

    public static void guardarContenido(
            ItemStack bolsa,
            ItemContainerContents contenido
    ) {

        bolsa.set(
                ModDataComponents.CONTENIDO_BOLSA.get(),
                contenido
        );
    }

    public static boolean esBolsaGigante(
            ItemStack stack
    ) {

        return stack.is(
                ModItems.BOLSA_GIGANTE.get()
        );
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack bolsaUsada =
                player.getItemInHand(
                        hand
                );

        if (!esBolsaGigante(
                bolsaUsada
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

        int slotBolsa =
                buscarSlotBolsa(
                        serverPlayer,
                        bolsaUsada,
                        hand
                );

        if (slotBolsa < 0) {

            return InteractionResult.FAIL;
        }

        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId,
                         inventory,
                         menuPlayer) ->
                                new GiantBagMenu(
                                        containerId,
                                        inventory,
                                        slotBolsa
                                ),
                        Component.translatable(
                                "container.amasmas.bolsa_gigante"
                        )
                )
        );

        return InteractionResult.SUCCESS;
    }

    private static int buscarSlotBolsa(
            ServerPlayer player,
            ItemStack bolsaUsada,
            InteractionHand hand
    ) {

        Inventory inventario =
                player.getInventory();

        for (int slot = 0;
             slot < inventario.getContainerSize();
             slot++) {

            ItemStack stack =
                    inventario.getItem(
                            slot
                    );

            if (stack == bolsaUsada) {
                return slot;
            }
        }

        return -1;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay displayComponent,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {

        super.appendHoverText(
                stack,
                context,
                displayComponent,
                textConsumer,
                flag
        );

        ItemContainerContents contenido =
                obtenerContenido(
                        stack
                );

        int espaciosOcupados =
                contarEspaciosOcupados(
                        contenido
                );

        textConsumer.accept(
                Component.translatable(
                        "tooltip.amasmas.bolsa_gigante.capacidad",
                        espaciosOcupados,
                        NUMERO_SLOTS
                )
        );

        textConsumer.accept(
                Component.translatable(
                        "tooltip.amasmas.bolsa_gigante.abrir"
                )
        );
    }

    private static int contarEspaciosOcupados(
            ItemContainerContents contenido
    ) {

        return (int) contenido
                .nonEmptyItemCopyStream()
                .limit(
                        NUMERO_SLOTS
                )
                .count();
    }
}