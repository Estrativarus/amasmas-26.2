package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class TotemUseEvents {

    private static final int DIA_DOS_TOTEMS =
            14;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerUseTotem(
            LivingUseTotemEvent event
    ) {

        if (event.isCanceled()) {
            return;
        }

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (!event.getTotem()
                .is(Items.TOTEM_OF_UNDYING)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        boolean usaDosTotems =
                diaActual >= DIA_DOS_TOTEMS;

        if (usaDosTotems
                && !consumirSegundoTotem(
                player,
                event.getTotem()
        )) {

            event.setCanceled(true);

            player.sendSystemMessage(
                    Component.literal(
                            "Necesitas dos Tótems para sobrevivir."
                    ).withStyle(
                            ChatFormatting.RED
                    )
            );

            return;
        }

        Component mensaje =
                crearMensaje(
                        player,
                        usaDosTotems
                );

        level.getServer()
                .getPlayerList()
                .broadcastSystemMessage(
                        mensaje,
                        false
                );
    }

    private static boolean consumirSegundoTotem(
            ServerPlayer player,
            ItemStack totemActivado
    ) {

        if (totemActivado.getCount() >= 2) {

            totemActivado.shrink(
                    1
            );

            return true;
        }

        ItemStack manoPrincipal =
                player.getMainHandItem();

        if (manoPrincipal != totemActivado
                && manoPrincipal.is(
                Items.TOTEM_OF_UNDYING
        )) {

            manoPrincipal.shrink(
                    1
            );

            return true;
        }

        ItemStack manoSecundaria =
                player.getOffhandItem();

        if (manoSecundaria != totemActivado
                && manoSecundaria.is(
                Items.TOTEM_OF_UNDYING
        )) {

            manoSecundaria.shrink(
                    1
            );

            return true;
        }

        int espaciosInventario =
                player
                        .getInventory()
                        .getContainerSize();

        for (int slot = 0;
             slot < espaciosInventario;
             slot++) {

            ItemStack stack =
                    player
                            .getInventory()
                            .getItem(slot);

            if (stack == totemActivado) {
                continue;
            }

            if (!stack.is(
                    Items.TOTEM_OF_UNDYING
            )) {

                continue;
            }

            stack.shrink(
                    1
            );

            return true;
        }

        return false;
    }

    private static Component crearMensaje(
            ServerPlayer player,
            boolean usaDosTotems
    ) {

        String texto;

        if (usaDosTotems) {

            texto =
                    " ha activado dos Tótems!";

        } else {

            texto =
                    " ha activado un Tótem!";
        }

        return Component.empty()
                .append(
                        Component.literal(
                                player
                                        .getName()
                                        .getString()
                        ).withStyle(
                                ChatFormatting.GREEN
                        )
                )
                .append(
                        Component.literal(
                                texto
                        ).withStyle(
                                ChatFormatting.YELLOW
                        )
                );
    }

    private TotemUseEvents() {
    }
}