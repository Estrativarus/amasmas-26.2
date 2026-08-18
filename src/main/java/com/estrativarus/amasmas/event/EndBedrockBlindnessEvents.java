package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class EndBedrockBlindnessEvents {

    private static final int DIA_INICIO =
            14;

    private static final int INTERVALO_COMPROBACION =
            5;

    private static final int DURACION_CEGUERA =
            100;

    private static final int AMPLIFICADOR_CEGUERA =
            0;

    @SubscribeEvent
    public static void onPlayerTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (level.dimension() != Level.END) {
            return;
        }

        if (player.tickCount
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (!estaTocandoBedrock(
                level,
                player
        )) {

            return;
        }

        aplicarCeguera(
                player
        );
    }

    private static boolean estaTocandoBedrock(
            ServerLevel level,
            Player player
    ) {

        AABB zonaContacto =
                player
                        .getBoundingBox()
                        .inflate(
                                0.05D,
                                0.05D,
                                0.05D
                        );

        int minX =
                (int) Math.floor(
                        zonaContacto.minX
                );

        int minY =
                (int) Math.floor(
                        zonaContacto.minY
                );

        int minZ =
                (int) Math.floor(
                        zonaContacto.minZ
                );

        int maxX =
                (int) Math.floor(
                        zonaContacto.maxX
                );

        int maxY =
                (int) Math.floor(
                        zonaContacto.maxY
                );

        int maxZ =
                (int) Math.floor(
                        zonaContacto.maxZ
                );

        for (BlockPos posicion :
                BlockPos.betweenClosed(
                        minX,
                        minY,
                        minZ,
                        maxX,
                        maxY,
                        maxZ
                )) {

            if (level
                    .getBlockState(posicion)
                    .is(Blocks.BEDROCK)) {

                return true;
            }
        }

        return false;
    }

    private static void aplicarCeguera(
            Player player
    ) {

        MobEffectInstance efectoActual =
                player.getEffect(
                        MobEffects.BLINDNESS
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                == AMPLIFICADOR_CEGUERA
                && efectoActual.getDuration() > 20) {

            return;
        }

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        DURACION_CEGUERA,
                        AMPLIFICADOR_CEGUERA,
                        false,
                        false,
                        true
                )
        );
    }

    private EndBedrockBlindnessEvents() {
    }
}