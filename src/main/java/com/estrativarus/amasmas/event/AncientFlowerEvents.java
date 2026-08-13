package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class AncientFlowerEvents {

    /*
     * Los efectos duran tres segundos.
     *
     * Mientras el jugador permanezca dentro de la flor,
     * se renuevan constantemente.
     *
     * Cuando salga, desaparecerán al cabo de pocos segundos.
     */
    private static final int DURACION_EFECTO = 60;

    @SubscribeEvent
    public static void onPlayerTick(
            EntityTickEvent.Post event
    ) {

        /*
         * Solo nos interesan los jugadores.
         */
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        /*
         * Solo aplicamos los efectos desde el servidor.
         */
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * No necesitamos comprobarlo veinte veces por segundo.
         *
         * Cada cinco ticks equivale a cuatro comprobaciones
         * por segundo y sigue respondiendo rápidamente.
         */
        if (player.tickCount % 5 != 0) {
            return;
        }

        /*
         * Posición de los pies del jugador.
         */
        BlockPos posicionPies =
              player.blockPosition();

        BlockState bloqueEnLosPies =
                level.getBlockState(posicionPies);

        BlockState bloqueDebajo =
                level.getBlockState(
                        posicionPies.below()
                );

        /*
         * Comprobamos si el jugador está caminando dentro
         * de una Torchflower o una Pitcher Plant.
         */
        boolean estaEnFlorAntigua =
                esTorchflower(bloqueEnLosPies)
                        || esTorchflower(bloqueDebajo)
                        || esPitcherPlant(bloqueEnLosPies)
                        || esPitcherPlant(bloqueDebajo);

        /*
         * Ambas flores proporcionan los dos efectos:
         *
         * - Regeneración I
         * - Lentitud I
         */
        if (estaEnFlorAntigua) {
            aplicarRegeneracion(player);
            aplicarLentitud(player);
        }
    }

    private static boolean esTorchflower(
            BlockState state
    ) {
        return state.is(Blocks.TORCHFLOWER);
    }

    private static boolean esPitcherPlant(
            BlockState state
    ) {

        /*
         * PITCHER_PLANT es la planta completamente desarrollada.
         *
         * PITCHER_CROP representa sus distintas fases de
         * crecimiento en la tierra de cultivo.
         *
         * Incluimos ambas para que el efecto funcione también
         * mientras está creciendo.
         */
        return state.is(Blocks.PITCHER_PLANT)
                || state.is(Blocks.PITCHER_CROP);
    }

    private static void aplicarRegeneracion(
            Player player
    ) {

        /*
         * Los amplificadores empiezan desde cero:
         *
         * 0 = Regeneración I
         * 1 = Regeneración II
         */
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.REGENERATION,
                        DURACION_EFECTO,
                        0,
                        false,
                        false,
                        true
                )
        );
    }

    private static void aplicarLentitud(
            Player player
    ) {

        /*
         * Amplificador 0 = Lentitud I.
         */
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        DURACION_EFECTO,
                        0,
                        false,
                        false,
                        true
                )
        );
    }

    private AncientFlowerEvents() {
    }
}