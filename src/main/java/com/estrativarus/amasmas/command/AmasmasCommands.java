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