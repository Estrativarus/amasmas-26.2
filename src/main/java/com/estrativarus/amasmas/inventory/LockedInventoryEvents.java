package com.estrativarus.amasmas.inventory;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class LockedInventoryEvents {

    private static final int DIA_BLOQUEO =
            21;

    private static final int PRIMER_SLOT_BLOQUEADO =
            9;

    private static final int ULTIMO_SLOT_BLOQUEADO =
            17;

    private static final int INTERVALO_COMPROBACION =
            5;

    private static final String TAG_BLOQUEO_NOTIFICADO =
            "amasmas_inventario_reducido_notificado";

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        actualizarBloqueo(
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        actualizarBloqueo(
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        actualizarBloqueo(
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        if ((player.tickCount + player.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        actualizarBloqueo(
                player
        );
    }

    private static void actualizarBloqueo(
            ServerPlayer player
    ) {

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_BLOQUEO) {

            eliminarMarcadores(
                    player
            );

            player
                    .getPersistentData()
                    .remove(
                            TAG_BLOQUEO_NOTIFICADO
                    );

            return;
        }

        limpiarMarcadoresFueraDeZona(
                player
        );

        limpiarMarcadorDelCursor(
                player
        );

        bloquearPrimeraFila(
                player
        );

        notificarBloqueo(
                player
        );
    }

    private static void bloquearPrimeraFila(
            ServerPlayer player
    ) {

        Inventory inventario =
                player.getInventory();

        for (int slot = PRIMER_SLOT_BLOQUEADO;
             slot <= ULTIMO_SLOT_BLOQUEADO;
             slot++) {

            ItemStack stackActual =
                    inventario.getItem(
                            slot
                    );

            if (esMarcador(stackActual)) {
                continue;
            }

            if (!stackActual.isEmpty()) {

                ItemStack objetoDesplazado =
                        stackActual.copy();

                inventario.setItem(
                        slot,
                        ItemStack.EMPTY
                );

                moverObjetoFueraDeZonaBloqueada(
                        player,
                        objetoDesplazado
                );
            }

            inventario.setItem(
                    slot,
                    crearMarcador()
            );
        }

        inventario.setChanged();
    }

    private static void moverObjetoFueraDeZonaBloqueada(
            ServerPlayer player,
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return;
        }

        Inventory inventario =
                player.getInventory();

        combinarConPilasExistentes(
                inventario,
                stack
        );

        if (stack.isEmpty()) {
            return;
        }

        colocarEnSlotsLibres(
                inventario,
                stack
        );

        if (stack.isEmpty()) {
            return;
        }

        player.drop(
                stack,
                false
        );
    }

    private static void combinarConPilasExistentes(
            Inventory inventario,
            ItemStack stack
    ) {

        for (int slot = 0;
             slot < inventario.getContainerSize();
             slot++) {

            if (esSlotBloqueado(slot)) {
                continue;
            }

            ItemStack stackDestino =
                    inventario.getItem(
                            slot
                    );

            if (stackDestino.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(
                    stackDestino,
                    stack
            )) {

                continue;
            }

            int espacioDisponible =
                    stackDestino.getMaxStackSize()
                            - stackDestino.getCount();

            if (espacioDisponible <= 0) {
                continue;
            }

            int cantidadMovida =
                    Math.min(
                            espacioDisponible,
                            stack.getCount()
                    );

            stackDestino.grow(
                    cantidadMovida
            );

            stack.shrink(
                    cantidadMovida
            );

            if (stack.isEmpty()) {
                return;
            }
        }
    }

    private static void colocarEnSlotsLibres(
            Inventory inventario,
            ItemStack stack
    ) {

        for (int slot = 0;
             slot < inventario.getContainerSize();
             slot++) {

            if (esSlotBloqueado(slot)) {
                continue;
            }

            if (!inventario
                    .getItem(slot)
                    .isEmpty()) {

                continue;
            }

            int cantidadColocada =
                    Math.min(
                            stack.getCount(),
                            stack.getMaxStackSize()
                    );

            ItemStack nuevaPila =
                    stack.copy();

            nuevaPila.setCount(
                    cantidadColocada
            );

            inventario.setItem(
                    slot,
                    nuevaPila
            );

            stack.shrink(
                    cantidadColocada
            );

            if (stack.isEmpty()) {
                return;
            }
        }
    }

    private static void limpiarMarcadoresFueraDeZona(
            ServerPlayer player
    ) {

        Inventory inventario =
                player.getInventory();

        for (int slot = 0;
             slot < inventario.getContainerSize();
             slot++) {

            if (esSlotBloqueado(slot)) {
                continue;
            }

            ItemStack stack =
                    inventario.getItem(
                            slot
                    );

            if (!esMarcador(stack)) {
                continue;
            }

            inventario.setItem(
                    slot,
                    ItemStack.EMPTY
            );
        }

        ItemStack manoSecundaria =
                player.getOffhandItem();

        if (esMarcador(manoSecundaria)) {

            player.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                    ItemStack.EMPTY
            );
        }

        inventario.setChanged();
    }

    private static void limpiarMarcadorDelCursor(
            ServerPlayer player
    ) {

        ItemStack stackCursor =
                player.containerMenu
                        .getCarried();

        if (!esMarcador(stackCursor)) {
            return;
        }

        player.containerMenu.setCarried(
                ItemStack.EMPTY
        );
    }

    private static void eliminarMarcadores(
            ServerPlayer player
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

            if (!esMarcador(stack)) {
                continue;
            }

            inventario.setItem(
                    slot,
                    ItemStack.EMPTY
            );
        }

        if (esMarcador(
                player.getOffhandItem()
        )) {

            player.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                    ItemStack.EMPTY
            );
        }

        limpiarMarcadorDelCursor(
                player
        );

        inventario.setChanged();
    }

    private static void notificarBloqueo(
            ServerPlayer player
    ) {

        if (player
                .getPersistentData()
                .contains(
                        TAG_BLOQUEO_NOTIFICADO
                )) {

            return;
        }

        player
                .getPersistentData()
                .putBoolean(
                        TAG_BLOQUEO_NOTIFICADO,
                        true
                );

        player.sendSystemMessage(
                Component.literal(
                        "El espacio de tu inventario se ha reducido."
                ).withStyle(
                        ChatFormatting.DARK_RED
                )
        );
    }

    private static ItemStack crearMarcador() {

        return new ItemStack(
                ModItems.SLOT_BLOQUEADO.get()
        );
    }

    private static boolean esMarcador(
            ItemStack stack
    ) {

        return !stack.isEmpty()
                && stack.is(
                ModItems.SLOT_BLOQUEADO.get()
        );
    }

    private static boolean esSlotBloqueado(
            int slot
    ) {

        return slot >= PRIMER_SLOT_BLOQUEADO
                && slot <= ULTIMO_SLOT_BLOQUEADO;
    }

    private LockedInventoryEvents() {
    }
}