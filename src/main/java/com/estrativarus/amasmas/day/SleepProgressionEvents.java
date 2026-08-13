package com.estrativarus.amasmas.day;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.deathtrain.DeathTrainSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class SleepProgressionEvents {

    /*
     * Evita comprobar la configuración veinte veces por segundo.
     */
    private static int contadorTicks = 0;

    /*
     * Evita ejecutar el mismo gamerule cada segundo
     * si el porcentaje no ha cambiado.
     */
    private static int ultimoPorcentajeAplicado =
            Integer.MIN_VALUE;

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event
    ) {

        contadorTicks++;

        if (contadorTicks < 20) {
            return;
        }

        contadorTicks = 0;

        MinecraftServer server =
                event.getServer();

        int porcentajeNecesario =
                calcularPorcentajeNecesario(server);

        /*
         * Solo ejecutamos el comando si hay un cambio.
         */
        if (porcentajeNecesario
                == ultimoPorcentajeAplicado) {
            return;
        }

        aplicarPorcentajeDeSueno(
                server,
                porcentajeNecesario
        );

        ultimoPorcentajeAplicado =
                porcentajeNecesario;
    }

    private static int calcularPorcentajeNecesario(
            MinecraftServer server
    ) {

        /*
         * Durante un Death Train no se puede pasar
         * la noche durmiendo.
         */
        if (DeathTrainSavedData
                .get(server)
                .estaActivo()) {

            return 101;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Días 1 a 6:
         *
         * Intentamos que sea suficiente una sola persona.
         */
        if (diaActual <= 6) {

            int jugadoresConectados =
                    Math.max(
                            1,
                            server
                                    .getPlayerList()
                                    .getPlayerCount()
                    );

            /*
             * Ejemplos:
             *
             * 2 jugadores -> 50 %
             * 4 jugadores -> 25 %
             * 10 jugadores -> 10 %
             *
             * En todos esos casos basta una persona.
             */
            return Math.max(
                    1,
                    100 / jugadoresConectados
            );
        }

        /*
         * Días 7 a 13:
         *
         * Se necesita la mitad de los jugadores.
         */
        if (diaActual <= 13) {
            return 50;
        }

        /*
         * Día 14 en adelante:
         *
         * 101 % hace imposible alcanzar el porcentaje.
         */
        return 101;
    }

    private static void aplicarPorcentajeDeSueno(
            MinecraftServer server,
            int porcentaje
    ) {

        CommandSourceStack source =
                server
                        .createCommandSourceStack()
                        .withSuppressedOutput();

        server.getCommands().performPrefixedCommand(
                source,
                "gamerule minecraft:players_sleeping_percentage "
                        + porcentaje
        );
    }

    /*
     * Se ejecuta cuando un jugador intenta dormir en una cama.
     */
    @SubscribeEvent
    public static void onPlayerSleepInBed(
            CanPlayerSleepEvent event
    ) {

        ServerPlayer player = event.getEntity();

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Días 1 a 41:
         *
         * El contador siempre se reinicia al utilizar
         * correctamente una cama.
         */
        if (diaActual < 42) {

            reiniciarContadorPhantoms(player);

            /*
             * Mostramos el aviso especial desde el día 7,
             * que es cuando dormir puede no pasar la noche.
             */
            if (diaActual >= 7) {
                enviarMensajeReinicio(player);
            }

            return;
        }

        /*
         * Días 42 a 62:
         *
         * Solo existe un 10 % de probabilidad.
         */
        if (diaActual < 63) {

            boolean haTenidoExito =
                    ThreadLocalRandom
                            .current()
                            .nextDouble()
                            < 0.10D;

            if (haTenidoExito) {

                reiniciarContadorPhantoms(player);
                enviarMensajeReinicio(player);

            } else {

                enviarMensajeFallo(player);
            }

            return;
        }

        /*
         * Día 63 en adelante:
         *
         * Ya no puede reiniciarse.
         */
        enviarMensajeBloqueado(player);
    }

    private static void reiniciarContadorPhantoms(
            ServerPlayer player
    ) {

        player.resetStat(
                Stats.CUSTOM.get(
                        Stats.TIME_SINCE_REST
                )
        );
    }

    private static void enviarMensajeReinicio(
            ServerPlayer player
    ) {

        Component mensaje =
                Component.empty()

                        .append(
                                Component.literal(
                                        "El contador de "
                                ).withStyle(
                                        ChatFormatting.GREEN
                                )
                        )

                        .append(
                                Component.literal("Phantoms")
                                        .withStyle(
                                                ChatFormatting.RED,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(
                                        " se ha reiniciado."
                                ).withStyle(
                                        ChatFormatting.GREEN
                                )
                        );

        player.sendSystemMessage(mensaje);
    }

    private static void enviarMensajeFallo(
            ServerPlayer player
    ) {

        Component mensaje =
                Component.empty()

                        .append(
                                Component.literal(
                                        "No has conseguido reiniciar el contador de "
                                ).withStyle(
                                        ChatFormatting.GRAY
                                )
                        )

                        .append(
                                Component.literal("Phantoms")
                                        .withStyle(
                                                ChatFormatting.RED,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(".")
                                        .withStyle(
                                                ChatFormatting.GRAY
                                        )
                        );

        player.sendSystemMessage(mensaje);
    }

    private static void enviarMensajeBloqueado(
            ServerPlayer player
    ) {

        Component mensaje =
                Component.empty()

                        .append(
                                Component.literal(
                                        "El contador de "
                                ).withStyle(
                                        ChatFormatting.GRAY
                                )
                        )

                        .append(
                                Component.literal("Phantoms")
                                        .withStyle(
                                                ChatFormatting.RED,
                                                ChatFormatting.BOLD
                                        )
                        )

                        .append(
                                Component.literal(
                                        " ya no puede reiniciarse."
                                ).withStyle(
                                        ChatFormatting.GRAY
                                )
                        );

        player.sendSystemMessage(mensaje);
    }

    private SleepProgressionEvents() {
    }
}