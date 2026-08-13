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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.HashSet;
import java.util.Set;

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

    /*
     * Jugadores que han consumido su última vida y deben
     * ser procesados cuando reaparezcan.
     */
    private static final Set<UUID> ELIMINACIONES_PENDIENTES =
            new HashSet<>();

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

        mostrarAvisoGlobalDeMuerte(
                server,
                player,
                level
        );

        /*
         * Restamos la vida primero.
         *
         * 3 -> 2: conserva
         * 2 -> 1: conserva
         * 1 -> 0: pierde
         */
        int vidasRestantes =
                datos.registrarMuerte(player.getUUID());

        /*
         * Si acaba de gastar su última vida, lo procesaremos
         * cuando pulse "Reaparecer".
         */
        if (vidasRestantes == 0) {
            ELIMINACIONES_PENDIENTES.add(
                    player.getUUID()
            );
        }

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
    private static void mostrarAvisoGlobalDeMuerte(
            MinecraftServer server,
            ServerPlayer jugadorMuerto,
            ServerLevel nivel
    ) {

        /*
         * Título principal en rojo.
         */
        Component titulo =
                Component.literal("¡MUERTO!")
                        .withStyle(
                                ChatFormatting.RED,
                                ChatFormatting.BOLD
                        );

        /*
         * Subtítulo:
         *
         * Nombre en dorado.
         * "ha muerto" en gris.
         */
        Component subtitulo =
                Component.empty()

                        .append(
                                jugadorMuerto
                                        .getDisplayName()
                                        .copy()
                                        .withStyle(
                                                ChatFormatting.GOLD,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(" ha muerto")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        );

        /*
         * Recorremos todos los jugadores conectados.
         */
        for (
                ServerPlayer jugador :
                server.getPlayerList().getPlayers()
        ) {

            /*
             * Tiempos del título, medidos en ticks:
             *
             * 10 ticks: entrada de 0,5 segundos.
             * 70 ticks: visible durante 3,5 segundos.
             * 20 ticks: salida de 1 segundo.
             */
            jugador.connection.send(
                    new ClientboundSetTitlesAnimationPacket(
                            10,
                            70,
                            20
                    )
            );

            /*
             * Enviamos primero el subtítulo y después el título.
             */
            jugador.connection.send(
                    new ClientboundSetSubtitleTextPacket(
                            subtitulo
                    )
            );

            jugador.connection.send(
                    new ClientboundSetTitleTextPacket(
                            titulo
                    )
            );

        }
        /*
         * Reproducimos el sonido individualmente en la posición
         * de cada jugador conectado.
         *
         * El pitch 0.5 hace que el sonido sea más grave.
         */
        CommandSourceStack source =
                server
                        .createCommandSourceStack()
                        .withSuppressedOutput();

        server.getCommands().performPrefixedCommand(
                source,
                "execute as @a at @s run playsound "
                        + "minecraft:entity.wither.spawn "
                        + "master @s ~ ~ ~ 1 0.5"
        );

        /*
         * Mensaje personalizado del chat.
         */
        Component mensajeChat =
                Component.empty()

                        .append(
                                Component.literal("¡MUERTO! ")
                                        .withStyle(
                                                ChatFormatting.RED,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                jugadorMuerto
                                        .getDisplayName()
                                        .copy()
                                        .withStyle(
                                                ChatFormatting.GOLD,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(" ha muerto.")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        );

        server.getPlayerList().broadcastSystemMessage(
                mensajeChat,
                false
        );

        /*
         * Coordenadas del lugar de la muerte.
         *
         * floor convierte correctamente coordenadas negativas:
         *
         * -3.7 pasa a -4, no a -3.
         */
        int x = (int) Math.floor(jugadorMuerto.getX());
        int y = (int) Math.floor(jugadorMuerto.getY());
        int z = (int) Math.floor(jugadorMuerto.getZ());

        String nombreDimension =
                obtenerNombreDimension(nivel);

        /*
         * Mensaje con dimensión y coordenadas.
         */
        Component mensajeUbicacion =
                Component.empty()

                        .append(
                                Component.literal("Lugar de la muerte: ")
                                        .withStyle(
                                                ChatFormatting.DARK_RED,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(nombreDimension)
                                        .withStyle(
                                                ChatFormatting.LIGHT_PURPLE,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(" | ")
                                        .withStyle(
                                                ChatFormatting.DARK_GRAY
                                        )
                        )

                        .append(
                                Component.literal("X: ")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        )

                        .append(
                                Component.literal(String.valueOf(x))
                                        .withStyle(
                                                ChatFormatting.AQUA
                                        )
                        )

                        .append(
                                Component.literal("  Y: ")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        )

                        .append(
                                Component.literal(String.valueOf(y))
                                        .withStyle(
                                                ChatFormatting.AQUA
                                        )
                        )

                        .append(
                                Component.literal("  Z: ")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        )

                        .append(
                                Component.literal(String.valueOf(z))
                                        .withStyle(
                                                ChatFormatting.AQUA
                                        )
                        );

        server.getPlayerList().broadcastSystemMessage(
                mensajeUbicacion,
                false
        );
    }

    private static String obtenerNombreDimension(
            ServerLevel nivel
    ) {

        /*
         * Dimensiones vanilla.
         */
        if (nivel.dimension().equals(Level.OVERWORLD)) {
            return "Overworld";
        }

        if (nivel.dimension().equals(Level.NETHER)) {
            return "Nether";
        }

        if (nivel.dimension().equals(Level.END)) {
            return "The End";
        }

        /*
         * Si la muerte ocurre en una dimensión de otro mod,
         * mostramos su identificador completo.
         *
         * Ejemplo:
         *
         * otro_mod:dimension_especial
         */
        return nivel.dimension().toString();
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

    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {

        MinecraftServer server = event.getServer();

        /*
         * Desactivamos el mensaje vanilla de muerte.
         *
         * El mod enviará un mensaje coloreado propio.
         */
        CommandSourceStack source =
                server
                        .createCommandSourceStack()
                        .withSuppressedOutput();

        server.getCommands().performPrefixedCommand(
                source,
                "gamerule minecraft:show_death_messages false"
        );
    }
    @SubscribeEvent
    public static void onPlayerRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {

        /*
         * Si el evento se produjo al salir del End,
         * no es una reaparición por muerte.
         */
        if (event.isEndConquered()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();

        /*
         * Si no está pendiente de eliminación,
         * no hacemos nada.
         */
        if (!ELIMINACIONES_PENDIENTES.remove(uuid)) {
            return;
        }

        MinecraftServer server =
                player.level().getServer();

        /*
         * Comprobamos si tiene permisos de operador.
         *
         * COMMANDS_GAMEMASTER representa el nivel habitual
         * necesario para administrar la partida.
         */
        boolean esOperador =
                player
                        .permissions()
                        .hasPermission(
                                Permissions.COMMANDS_GAMEMASTER
                        );

        String nombre =
                player.getGameProfile().name();

        /*
         * Ejecutamos el cambio después de terminar por completo
         * el proceso actual de reaparición.
         */
        server.execute(() -> {

            CommandSourceStack source =
                    server
                            .createCommandSourceStack()
                            .withSuppressedOutput();

            /*
             * Todos los jugadores eliminados pasan primero
             * a modo espectador.
             */
            server.getCommands().performPrefixedCommand(
                    source,
                    "gamemode spectator " + nombre
            );

            if (esOperador) {

                player.sendSystemMessage(
                        Component.literal(
                                "Has agotado todas tus vidas. Permanecerás como espectador porque eres operador."
                        ).withStyle(
                                ChatFormatting.RED,
                                ChatFormatting.BOLD
                        )
                );

            } else {

                /*
                 * El comando ban añade al perfil a la lista de
                 * baneados y expulsa al jugador conectado.
                 */
                server.getCommands().performPrefixedCommand(
                        source,
                        "ban "
                                + nombre
                                + " Has agotado todas tus vidas"
                );
            }
        });
    }


    private PlayerLivesEvents() {
    }
}