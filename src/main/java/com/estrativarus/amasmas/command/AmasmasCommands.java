package com.estrativarus.amasmas.command;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.ChatFormatting;
import com.estrativarus.amasmas.deathtrain.DeathTrainEvents;
import com.estrativarus.amasmas.deathtrain.DeathTrainSavedData;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class AmasmasCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        event.getDispatcher().register(

                /*
                 * Comando principal:
                 *
                 * /amasmas
                 */
                Commands.literal("amasmas")

                        /*
                         * Comando accesible para todos:
                         *
                         * /amasmas dia
                         */
                        .then(


                                Commands.literal("dia")
                                        .executes(context -> {

                                            SistemaDiasSavedData data =
                                                    SistemaDiasSavedData.get(
                                                            context
                                                                    .getSource()
                                                                    .getServer()
                                                    );

                                            int diaActual =
                                                    data.getDiaActual();

                                            int siguienteCambio =
                                                    calcularSiguienteCambio(
                                                            diaActual
                                                    );

                                            Component mensajeDia = Component.empty()

                                                    // Texto inicial en rojo.
                                                    .append(
                                                            Component.literal("Día actual del servidor: ")
                                                                    .withStyle(ChatFormatting.RED)
                                                    )

                                                    // Número del día en amarillo.
                                                    .append(
                                                            Component.literal(String.valueOf(diaActual))
                                                                    .withStyle(ChatFormatting.YELLOW)
                                                    );

                                            context.getSource().sendSuccess(
                                                    () -> mensajeDia,
                                                    false
                                            );

                                            /*
                                             * Mostramos el próximo cambio
                                             * solamente hasta el día 70.
                                             */
                                            if (diaActual < 70) {

                                                int diasRestantes =
                                                        siguienteCambio
                                                                - diaActual;

                                                Component mensajeSiguienteCambio = Component.empty()

                                                        // Primera parte en rojo.
                                                        .append(
                                                                Component.literal("Próximo cambio importante: ")
                                                                        .withStyle(ChatFormatting.RED)
                                                        )

                                                        // Día del próximo cambio en verde brillante.
                                                        .append(
                                                                Component.literal("día " + siguienteCambio)
                                                                        .withStyle(ChatFormatting.GREEN)
                                                        )

                                                        // Días restantes en azul claro brillante.
                                                        .append(
                                                                Component.literal(
                                                                        " (faltan " + diasRestantes + " días)"
                                                                ).withStyle(ChatFormatting.AQUA)
                                                        );

                                                context.getSource().sendSuccess(
                                                        () -> mensajeSiguienteCambio,
                                                        false
                                                );

                                            } else {

                                                context.getSource().sendSuccess(
                                                        () -> Component.literal(
                                                                "Se ha alcanzado la etapa final del día 70."
                                                        ),
                                                        false
                                                );
                                            }

                                            return diaActual;
                                        })
                        )

                        /*
                         * Comando solo para administradores:
                         *
                         * /amasmas cambiardia <número>
                         */
                        .then(
                                Commands.literal("cambiardia")

                                        /*
                                         * Nivel 2 permite normalmente:
                                         * operadores y consola del servidor.
                                         */
                                        .requires(source ->
                                                source.permissions().hasPermission(
                                                        Permissions.COMMANDS_GAMEMASTER
                                                )
                                        )

                                        /*
                                         * Solo aceptamos días desde el 1.
                                         */
                                        .then(
                                                Commands.argument(
                                                                "numero",
                                                                IntegerArgumentType.integer(
                                                                        1
                                                                )
                                                        )

                                                        .executes(context -> {

                                                            int nuevoDia =
                                                                    IntegerArgumentType
                                                                            .getInteger(
                                                                                    context,
                                                                                    "numero"
                                                                            );

                                                            SistemaDiasSavedData data =
                                                                    SistemaDiasSavedData.get(
                                                                            context
                                                                                    .getSource()
                                                                                    .getServer()
                                                                    );

                                                            data.establecerDia(
                                                                    nuevoDia
                                                            );

                                                            context
                                                                    .getSource()
                                                                    .sendSuccess(
                                                                            () ->
                                                                                    Component.literal(
                                                                                            "El día del servidor se ha cambiado al día "
                                                                                                    + nuevoDia
                                                                                    ),
                                                                            true
                                                                    );

                                                            return nuevoDia;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("tormenta")

                                        /*
                                         * COMANDO:
                                         *
                                         * /amasmas tormenta anadirhoras <cantidad>
                                         */
                                        .then(
                                                Commands.literal("anadirhoras")

                                                        /*
                                                         * Solo los administradores pueden
                                                         * añadir horas manualmente.
                                                         */
                                                        .requires(source ->
                                                                source.permissions().hasPermission(
                                                                        Permissions.COMMANDS_GAMEMASTER
                                                                )
                                                        )

                                                        /*
                                                         * Añadimos el argumento <cantidad>.
                                                         *
                                                         * El número mínimo permitido es 1.
                                                         */
                                                        .then(
                                                                Commands.argument(
                                                                                "cantidad",
                                                                                IntegerArgumentType.integer(1)
                                                                        )

                                                                        .executes(context -> {

                                                                            /*
                                                                             * Obtenemos el número escrito
                                                                             * por el administrador.
                                                                             */
                                                                            int cantidad =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "cantidad"
                                                                                    );

                                                                            /*
                                                                             * Obtenemos los datos persistentes
                                                                             * del Death Train.
                                                                             */
                                                                            DeathTrainSavedData datos =
                                                                                    DeathTrainSavedData.get(
                                                                                            context
                                                                                                    .getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            /*
                                                                             * Añadimos las horas.
                                                                             */
                                                                            datos.anadirHoras(cantidad);

                                                                            /*
                                                                             * Hacemos que los cambios se apliquen
                                                                             * inmediatamente:
                                                                             *
                                                                             * - comienza la tormenta;
                                                                             * - se actualiza UHC;
                                                                             * - se actualiza el estado global.
                                                                             */
                                                                            DeathTrainEvents
                                                                                    .actualizarEstadoDeathTrain(
                                                                                            context
                                                                                                    .getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            /*
                                                                             * Creamos el mensaje de confirmación.
                                                                             */
                                                                            Component mensaje =
                                                                                    Component.empty()

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            "Se han añadido "
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.GREEN
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            cantidad
                                                                                                                    + " horas"
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.YELLOW,
                                                                                                            ChatFormatting.BOLD
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            " al Death Train. "
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.GREEN
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            "Tiempo restante: "
                                                                                                                    + datos
                                                                                                                    .getHorasRestantesRedondeadas()
                                                                                                                    + " h"
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.AQUA
                                                                                                    )
                                                                                            );

                                                                            context
                                                                                    .getSource()
                                                                                    .sendSuccess(
                                                                                            () -> mensaje,
                                                                                            true
                                                                                    );

                                                                            return cantidad;
                                                                        })
                                                        )
                                        )

                                        /*
                                         * COMANDO:
                                         *
                                         * /amasmas tormenta quitarhoras <cantidad>
                                         */
                                        .then(
                                                Commands.literal("quitarhoras")

                                                        /*
                                                         * Solo los administradores pueden
                                                         * quitar horas manualmente.
                                                         */
                                                        .requires(source ->
                                                                source.permissions().hasPermission(
                                                                        Permissions.COMMANDS_GAMEMASTER
                                                                )
                                                        )

                                                        /*
                                                         * Añadimos el argumento <cantidad>.
                                                         */
                                                        .then(
                                                                Commands.argument(
                                                                                "cantidad",
                                                                                IntegerArgumentType.integer(1)
                                                                        )

                                                                        .executes(context -> {

                                                                            /*
                                                                             * Obtenemos el número escrito
                                                                             * por el administrador.
                                                                             */
                                                                            int cantidad =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "cantidad"
                                                                                    );

                                                                            /*
                                                                             * Obtenemos los datos del Death Train.
                                                                             */
                                                                            DeathTrainSavedData datos =
                                                                                    DeathTrainSavedData.get(
                                                                                            context
                                                                                                    .getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            /*
                                                                             * Quitamos las horas.
                                                                             */
                                                                            datos.quitarHoras(cantidad);

                                                                            /*
                                                                             * Actualizamos inmediatamente el estado.
                                                                             *
                                                                             * Si ya no queda tiempo:
                                                                             *
                                                                             * - termina la tormenta;
                                                                             * - se desactiva UHC;
                                                                             * - los mobs dejan de renovar efectos.
                                                                             */
                                                                            DeathTrainEvents
                                                                                    .actualizarEstadoDeathTrain(
                                                                                            context
                                                                                                    .getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            /*
                                                                             * Creamos el mensaje de confirmación.
                                                                             */
                                                                            Component mensaje =
                                                                                    Component.empty()

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            "Se han quitado "
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.RED
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            cantidad
                                                                                                                    + " horas"
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.YELLOW,
                                                                                                            ChatFormatting.BOLD
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            " del Death Train. "
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.RED
                                                                                                    )
                                                                                            )

                                                                                            .append(
                                                                                                    Component.literal(
                                                                                                            "Tiempo restante: "
                                                                                                                    + datos
                                                                                                                    .getHorasRestantesRedondeadas()
                                                                                                                    + " h"
                                                                                                    ).withStyle(
                                                                                                            ChatFormatting.AQUA
                                                                                                    )
                                                                                            );

                                                                            context
                                                                                    .getSource()
                                                                                    .sendSuccess(
                                                                                            () -> mensaje,
                                                                                            true
                                                                                    );

                                                                            return cantidad;
                                                                        })
                                                        )
                                        )

                                        /*
                                         * COMANDO:
                                         *
                                         * /amasmas tormenta estado
                                         *
                                         * Puede utilizarlo cualquier jugador.
                                         */
                                        .then(
                                                Commands.literal("estado")

                                                        .executes(context -> {

                                                            /*
                                                             * Obtenemos la información guardada
                                                             * del Death Train.
                                                             */
                                                            DeathTrainSavedData datos =
                                                                    DeathTrainSavedData.get(
                                                                            context
                                                                                    .getSource()
                                                                                    .getServer()
                                                                    );

                                                            Component mensaje;

                                                            /*
                                                             * Construimos un mensaje diferente
                                                             * dependiendo de si está activo.
                                                             */
                                                            if (datos.estaActivo()) {

                                                                mensaje = Component.empty()

                                                                        .append(
                                                                                Component.literal(
                                                                                        "Death Train activo: "
                                                                                ).withStyle(
                                                                                        ChatFormatting.RED,
                                                                                        ChatFormatting.BOLD
                                                                                )
                                                                        )

                                                                        .append(
                                                                                Component.literal(
                                                                                        datos
                                                                                                .getHorasRestantesRedondeadas()
                                                                                                + " horas restantes"
                                                                                ).withStyle(
                                                                                        ChatFormatting.YELLOW
                                                                                )
                                                                        );

                                                            } else {

                                                                mensaje = Component.literal(
                                                                        "El Death Train no está activo."
                                                                ).withStyle(
                                                                        ChatFormatting.GREEN
                                                                );
                                                            }

                                                            /*
                                                             * Enviamos el mensaje únicamente
                                                             * a quien utilizó el comando.
                                                             */
                                                            context
                                                                    .getSource()
                                                                    .sendSuccess(
                                                                            () -> mensaje,
                                                                            false
                                                                    );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    /*
     * Calcula el siguiente múltiplo de siete.
     *
     * Día 1  → próximo cambio: 7
     * Día 7  → próximo cambio: 14
     * Día 13 → próximo cambio: 14
     * Día 69 → próximo cambio: 70
     */
    private static int calcularSiguienteCambio(int diaActual) {

        int siguiente =
                ((diaActual / 7) + 1) * 7;

        return Math.min(siguiente, 70);
    }

    private AmasmasCommands() {
    }
}