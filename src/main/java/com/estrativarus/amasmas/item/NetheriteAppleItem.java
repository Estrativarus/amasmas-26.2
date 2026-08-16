package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.health.PlayerHealthSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NetheriteAppleItem extends Item {

    /*
     * La manzana aumenta la vida desde:
     *
     * 20 puntos = 10 corazones
     * 28 puntos = 14 corazones
     *
     * Por tanto, añade ocho puntos de salud.
     */
    public static final int BONIFICACION_SALUD =
            8;

    public NetheriteAppleItem(
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

        /*
         * Dejamos que Minecraft procese primero el consumo:
         *
         * - disminución de la pila;
         * - hambre;
         * - animación;
         * - sonido.
         */
        ItemStack resultado =
                super.finishUsingItem(
                        stack,
                        level,
                        entity
                );

        /*
         * La mejora solo se aplica a jugadores del servidor.
         */
        if (!(entity instanceof ServerPlayer player)) {
            return resultado;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return resultado;
        }

        PlayerHealthSavedData datos =
                PlayerHealthSavedData.get(
                        serverLevel.getServer()
                );

        /*
         * La misma mejora solamente puede obtenerse una vez.
         */
        if (datos.tieneManzanaNetherita(
                player.getUUID()
        )) {

            player.sendSystemMessage(
                    Component.literal(
                            "Ya habías obtenido la mejora de la Manzana de Netherita."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            /*
             * Aunque ya tuviera la mejora, la manzana ya fue
             * consumida. Más adelante podemos impedir incluso
             * que empiece a comerla si lo prefieres.
             */
            return resultado;
        }

        /*
         * Registramos permanentemente la mejora.
         */
        datos.concederManzanaNetherita(
                player.getUUID()
        );

        /*
         * Aplicamos inmediatamente la nueva vida máxima.
         */
        PlayerHealthSavedData.aplicarSaludGuardada(
                player
        );

        /*
         * Curamos los ocho puntos nuevos para que los cuatro
         * corazones adicionales aparezcan llenos.
         */
        player.heal(
                BONIFICACION_SALUD
        );

        /*
         * Lentitud V durante 10 segundos:
         *
         * 10 segundos = 200 ticks.
         * Amplificador 4 = nivel V.
         */
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        200,
                        4,
                        false,
                        true,
                        true
                )
        );

        player.sendSystemMessage(
                Component.empty()

                        .append(
                                Component.literal(
                                        "Tu salud máxima ha aumentado a "
                                ).withStyle(
                                        ChatFormatting.GREEN
                                )
                        )

                        .append(
                                Component.literal(
                                        "14 corazones"
                                ).withStyle(
                                        ChatFormatting.DARK_RED,
                                        ChatFormatting.BOLD
                                )
                        )

                        .append(
                                Component.literal(".")
                                        .withStyle(
                                                ChatFormatting.GREEN
                                        )
                        )
        );

        return resultado;
    }
}