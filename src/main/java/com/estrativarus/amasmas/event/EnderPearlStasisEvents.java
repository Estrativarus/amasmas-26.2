package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class EnderPearlStasisEvents {

    private static final int DIA_DESACTIVACION =
            14;

    @SubscribeEvent
    public static void onEnderPearlTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof ThrownEnderpearl pearl)) {

            return;
        }

        if (!(pearl.level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_DESACTIVACION) {
            return;
        }

        if (!estaEnColumnaDeBurbujas(
                level,
                pearl.blockPosition()
        )) {

            return;
        }

        level.sendParticles(
                ParticleTypes.PORTAL,
                pearl.getX(),
                pearl.getY(),
                pearl.getZ(),
                20,
                0.2D,
                0.2D,
                0.2D,
                0.1D
        );

        pearl.discard();
    }

    private static boolean estaEnColumnaDeBurbujas(
            ServerLevel level,
            BlockPos posicion
    ) {

        if (level
                .getBlockState(posicion)
                .is(Blocks.BUBBLE_COLUMN)) {

            return true;
        }

        if (level
                .getBlockState(
                        posicion.below()
                )
                .is(Blocks.BUBBLE_COLUMN)) {

            return true;
        }

        return level
                .getBlockState(
                        posicion.above()
                )
                .is(Blocks.BUBBLE_COLUMN);
    }

    private EnderPearlStasisEvents() {
    }
}