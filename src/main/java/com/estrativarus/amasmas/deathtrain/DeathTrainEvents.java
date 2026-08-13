package com.estrativarus.amasmas.deathtrain;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class DeathTrainEvents {

    /*
     * Evita comprobar el clima veinte veces por segundo.
     */
    private static int contadorTicks = 0;

    /*
     * Guarda el estado anterior para detectar:
     *
     * inactivo -> activo
     * activo -> inactivo
     */
    private static boolean estabaActivo = false;

    /*
     * Guarda el último estado aplicado de UHC para no ejecutar
     * el comando innecesariamente cada segundo.
     */
    private static boolean uhcEstabaActivo = false;

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {

        // El Death Train solamente se activa por la muerte de jugadores.
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        /*
         * En 26.2 obtenemos el servidor a través del ServerLevel.
         */
        if (!(player.level() instanceof ServerLevel playerLevel)) {
            return;
        }

        MinecraftServer server = playerLevel.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        int horas =
                calcularHorasPorMuerte(diaActual);

        DeathTrainSavedData datos =
                DeathTrainSavedData.get(server);

        datos.anadirHoras(horas);

        /*
         * Aplicamos inmediatamente el estado del Death Train.
         *
         * Así no tenemos que esperar hasta la siguiente comprobación
         * de un segundo.
         */
        actualizarEstadoDeathTrain(server);

        Component mensaje = Component.empty()

                .append(
                        Component.literal("☠ ")
                                .withStyle(
                                        ChatFormatting.DARK_RED,
                                        ChatFormatting.BOLD
                                )
                )

                .append(
                        Component.literal(
                                player.getName().getString()
                                        + " ha muerto. "
                        ).withStyle(ChatFormatting.RED)
                )

                .append(
                        Component.literal("Death Train activado: ")
                                .withStyle(ChatFormatting.GOLD)
                )

                .append(
                        Component.literal("+" + horas + " horas")
                                .withStyle(
                                        ChatFormatting.YELLOW,
                                        ChatFormatting.BOLD
                                )
                )

                .append(
                        Component.literal(
                                " | Tiempo restante: "
                                        + datos.getHorasRestantesRedondeadas()
                                        + " h"
                        ).withStyle(ChatFormatting.AQUA)
                );

        server.getPlayerList().broadcastSystemMessage(
                mensaje,
                false
        );
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        contadorTicks++;

        /*
         * Hacemos la comprobación una vez por segundo.
         */
        if (contadorTicks < 20) {
            return;
        }

        contadorTicks = 0;

        actualizarEstadoDeathTrain(event.getServer());
    }

    /*
     * Este método centraliza todo el estado global del Death Train:
     *
     * - tormenta;
     * - finalización;
     * - UHC;
     * - mensajes.
     *
     * También podremos llamarlo desde los comandos administrativos.
     */
    public static void actualizarEstadoDeathTrain(
            MinecraftServer server
    ) {

        DeathTrainSavedData datos =
                DeathTrainSavedData.get(server);

        boolean activo = datos.estaActivo();

        if (activo) {

            /*
             * Tormenta eléctrica obligatoria.
             *
             * clearTime = 0
             * rainTime = 1200 ticks
             * raining = true
             * thundering = true
             *
             * Como se renueva una vez por segundo, dormir o utilizar
             * /weather clear no puede terminarla permanentemente.
             */
            server.setWeatherParameters(
                    0,
                    1200,
                    true,
                    true
            );

            /*
             * UHC solo estará activado durante el Death Train
             * y a partir del día 42.
             */
            int diaActual =
                    SistemaDiasSavedData
                            .get(server)
                            .getDiaActual();

            boolean debeEstarUhcActivo =
                    diaActual >= 42;

            actualizarUhc(
                    server,
                    debeEstarUhcActivo
            );

        } else {

            /*
             * Hay que llamar a comprobarFinalizacion para que
             * finTormenta se reinicie a cero en SavedData.
             */
            boolean acabaDeFinalizar =
                    datos.comprobarFinalizacion();

            /*
             * Si el Death Train estaba funcionando y ahora no,
             * restauramos el clima y desactivamos UHC.
             */
            if (estabaActivo || acabaDeFinalizar) {

                server.setWeatherParameters(
                        6000,
                        0,
                        false,
                        false
                );

                actualizarUhc(server, false);

                server.getPlayerList().broadcastSystemMessage(
                        Component.literal(
                                "El Death Train ha terminado."
                        ).withStyle(
                                ChatFormatting.GREEN,
                                ChatFormatting.BOLD
                        ),
                        false
                );
            }
        }

        estabaActivo = activo;
    }

    /*
     * Activa o desactiva la regeneración natural.
     *
     * Utilizamos el comando interno porque en Minecraft 26.2
     * las reglas de juego utilizan un nuevo sistema de registro.
     */
    private static void actualizarUhc(
            MinecraftServer server,
            boolean activarUhc
    ) {

        if (uhcEstabaActivo == activarUhc) {
            return;
        }

        String comando;

        if (activarUhc) {
            comando =
                    "gamerule minecraft:natural_health_regeneration false";
        } else {
            comando =
                    "gamerule minecraft:natural_health_regeneration true";
        }

        CommandSourceStack source =
                server
                        .createCommandSourceStack()
                        .withSuppressedOutput();

        server.getCommands().performPrefixedCommand(
                source,
                comando
        );

        uhcEstabaActivo = activarUhc;

        if (activarUhc) {

            server.getPlayerList().broadcastSystemMessage(
                    Component.literal(
                            "Modo UHC activado durante el Death Train."
                    ).withStyle(
                            ChatFormatting.DARK_RED,
                            ChatFormatting.BOLD
                    ),
                    false
            );

        } else {

            server.getPlayerList().broadcastSystemMessage(
                    Component.literal(
                            "Modo UHC desactivado: vuelve la regeneración natural."
                    ).withStyle(ChatFormatting.GREEN),
                    false
            );
        }
    }

    /*
     * Duración añadida por muerte.
     *
     * Día 1  -> 1 hora
     * Día 20 -> 20 horas
     * Día 21 -> 1 hora
     * Día 22 -> 2 horas
     * Día 42 -> 1 hora
     * Día 63 -> 1 hora
     */
    public static int calcularHorasPorMuerte(int dia) {

        if (dia < 1) {
            dia = 1;
        }

        /*
         * La resta de uno hace que cada bloque tenga exactamente
         * 21 días:
         *
         * 1-20, 21-41, 42-62, 63...
         */
        return ((dia - 1) % 21) + 1;
    }

    private DeathTrainEvents() {
    }
}