package com.estrativarus.amasmas.lives;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class PlayerLivesEvents {

    /*
     * Inventarios pendientes de restaurar.
     *
     * Solo existen durante el proceso:
     *
     * muerte -> pantalla de muerte -> reaparición
     *
     * No necesitan guardarse en disco porque normalmente este proceso
     * dura unos segundos y el servidor continúa encendido.
     */
    private static final Map<UUID, List<ItemStack>>
            INVENTARIOS_PENDIENTES = new HashMap<>();

    /*
     * Marca qué jugadores deben conservar el inventario.
     *
     * LivingDeathEvent decide si deben conservarlo.
     * LivingDropsEvent cancela sus objetos soltados.
     */
    private static final Map<UUID, Boolean>
            CONSERVAR_INVENTARIO = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerDeath(
            LivingDeathEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        MinecraftServer server = level.getServer();

        PlayerLivesSavedData datos =
                PlayerLivesSavedData.get(server);

        /*
         * Restamos la vida primero.
         *
         * 3 -> 2: conserva
         * 2 -> 1: conserva
         * 1 -> 0: pierde
         */
        int vidasRestantes =
                datos.registrarMuerte(player.getUUID());

        boolean debeConservar =
                vidasRestantes > 0;

        CONSERVAR_INVENTARIO.put(
                player.getUUID(),
                debeConservar
        );

        if (debeConservar) {

            /*
             * Guardamos una copia de cada posición del inventario.
             *
             * Incluye inventario principal, barra rápida,
             * armadura y mano secundaria según el contenedor
             * de inventario del jugador.
             */
            List<ItemStack> copiaInventario =
                    new ArrayList<>();

            for (
                    int slot = 0;
                    slot < player.getInventory().getContainerSize();
                    slot++
            ) {
                copiaInventario.add(
                        player
                                .getInventory()
                                .getItem(slot)
                                .copy()
                );
            }

            INVENTARIOS_PENDIENTES.put(
                    player.getUUID(),
                    copiaInventario
            );
        }

        enviarMensajeVidas(
                player,
                vidasRestantes,
                debeConservar
        );
    }

    /*
     * Evitamos que aparezcan objetos en el suelo durante
     * las dos primeras muertes.
     */
    @SubscribeEvent
    public static void onPlayerDrops(
            LivingDropsEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean debeConservar =
                CONSERVAR_INVENTARIO.getOrDefault(
                        player.getUUID(),
                        false
                );

        if (debeConservar) {
            event.setCanceled(true);
        }
    }

    /*
     * Restauramos el inventario en el nuevo objeto jugador
     * que Minecraft crea al reaparecer.
     */
    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {

        if (!event.isWasDeath()) {
            return;
        }

        UUID uuid = event.getEntity().getUUID();

        boolean debeConservar =
                CONSERVAR_INVENTARIO.getOrDefault(
                        uuid,
                        false
                );

        if (!debeConservar) {
            limpiarDatosTemporales(uuid);
            return;
        }

        List<ItemStack> copiaInventario =
                INVENTARIOS_PENDIENTES.get(uuid);

        if (copiaInventario == null) {
            limpiarDatosTemporales(uuid);
            return;
        }

        /*
         * Evitamos superar el número de slots que tenga
         * el inventario del jugador nuevo.
         */
        int cantidadSlots =
                Math.min(
                        copiaInventario.size(),
                        event
                                .getEntity()
                                .getInventory()
                                .getContainerSize()
                );

        for (int slot = 0; slot < cantidadSlots; slot++) {

            event
                    .getEntity()
                    .getInventory()
                    .setItem(
                            slot,
                            copiaInventario
                                    .get(slot)
                                    .copy()
                    );
        }

        event.getEntity().getInventory().setChanged();

        limpiarDatosTemporales(uuid);
    }

    private static void limpiarDatosTemporales(
            UUID uuid
    ) {
        INVENTARIOS_PENDIENTES.remove(uuid);
        CONSERVAR_INVENTARIO.remove(uuid);
    }

    private static void enviarMensajeVidas(
            ServerPlayer player,
            int vidasRestantes,
            boolean conservaInventario
    ) {

        Component mensaje = Component.empty()

                .append(
                        Component.literal("Has perdido una vida. ")
                                .withStyle(ChatFormatting.RED)
                )

                .append(
                        Component.literal("Vidas restantes: ")
                                .withStyle(ChatFormatting.GOLD)
                )

                .append(
                        Component.literal(
                                String.valueOf(vidasRestantes)
                        ).withStyle(
                                colorSegunVidas(vidasRestantes),
                                ChatFormatting.BOLD
                        )
                );

        player.sendSystemMessage(mensaje);

        if (conservaInventario) {

            player.sendSystemMessage(
                    Component.literal(
                            "Has conservado tu inventario."
                    ).withStyle(ChatFormatting.GREEN)
            );

        } else {

            player.sendSystemMessage(
                    Component.literal(
                            "No te quedan vidas: tus objetos se han soltado."
                    ).withStyle(
                            ChatFormatting.DARK_RED,
                            ChatFormatting.BOLD
                    )
            );
        }
    }

    private static ChatFormatting colorSegunVidas(
            int vidas
    ) {
        return switch (vidas) {
            case 3 -> ChatFormatting.GREEN;
            case 2 -> ChatFormatting.YELLOW;
            case 1 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    private PlayerLivesEvents() {
    }
}