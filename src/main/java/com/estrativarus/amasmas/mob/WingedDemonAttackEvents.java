package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.estrativarus.amasmas.mob.SkeletonClassEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class WingedDemonAttackEvents {

    private static final int TICKS_ENTRE_ATAQUES_NORMAL =
            20 * 30;

    private static final int TICKS_ENTRE_ATAQUES_CABREO =
            20 * 15;

    private static final double DISTANCIA_MAXIMA_JUGADORES =
            192.0D;

    private static final double DISTANCIA_INVOCACION =
            16.0D;

    private static final int MICRO_DEMONIOS_POR_JUGADOR =
            2;

    private static final int ATAQUES_IMPLEMENTADOS =
            3;

    private static final int CANTIDAD_ESQUELETOS_ATAQUE_3 =
            2;

    private static final int RADIO_ATAQUE_3 =
            20;

    private static final int INTENTOS_POSICION_ATAQUE_3 =
            20;


    private static final Map<UUID, Long>
            PROXIMO_ATAQUE_POR_DRAGON =
            new HashMap<>();

    @SubscribeEvent
    public static void onWingedDemonTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof EnderDragon dragon)) {

            return;
        }

        if (!(dragon.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (!WingedDemonEvents
                .esDemonioAlado(
                        dragon
                )) {

            return;
        }

        if (!dragon.isAlive()
                || dragon.isRemoved()) {

            PROXIMO_ATAQUE_POR_DRAGON.remove(
                    dragon.getUUID()
            );

            return;
        }

        long tiempoActual =
                level.getGameTime();

        UUID dragonUuid =
                dragon.getUUID();

        Long siguienteAtaque =
                PROXIMO_ATAQUE_POR_DRAGON.get(
                        dragonUuid
                );

        if (siguienteAtaque == null) {

            programarSiguienteAtaque(
                    level,
                    dragon
            );

            return;
        }

        if (tiempoActual < siguienteAtaque) {
            return;
        }

        ejecutarAtaqueAleatorio(
                level,
                dragon
        );

        programarSiguienteAtaque(
                level,
                dragon
        );
    }

    private static void ataqueBombardeoTnt(
            ServerLevel level,
            EnderDragon dragon
    ) {

        for (int numero = 0;
             numero < 4;
             numero++) {

            double angulo =
                    dragon
                            .getRandom()
                            .nextDouble()
                            * Math.PI
                            * 2.0D;

            double distancia =
                    1.5D
                            + dragon
                            .getRandom()
                            .nextDouble()
                            * 2.5D;

            double x =
                    dragon.getX()
                            + Math.cos(angulo)
                            * distancia;

            double y =
                    dragon.getY()
                            - 1.0D
                            + dragon
                            .getRandom()
                            .nextDouble()
                            * 2.0D;

            double z =
                    dragon.getZ()
                            + Math.sin(angulo)
                            * distancia;

            FulminantShulkerEvents
                    .crearTntFulminanteDeAtaque(
                            level,
                            x,
                            y,
                            z,
                            dragon
                    );
        }

        level.sendParticles(
                ParticleTypes.SMOKE,
                dragon.getX(),
                dragon.getY(),
                dragon.getZ(),
                80,
                4.0D,
                2.0D,
                4.0D,
                0.08D
        );

        level.playSound(
                null,
                dragon.getX(),
                dragon.getY(),
                dragon.getZ(),
                SoundEvents.TNT_PRIMED,
                SoundSource.HOSTILE,
                3.0F,
                0.65F
        );
    }

    private static void programarSiguienteAtaque(
            ServerLevel level,
            EnderDragon dragon
    ) {

        int intervalo;

        if (WingedDemonEvents
                .estaEnModoCabreo(
                        dragon
                )) {

            intervalo =
                    TICKS_ENTRE_ATAQUES_CABREO;

        } else {

            intervalo =
                    TICKS_ENTRE_ATAQUES_NORMAL;
        }

        PROXIMO_ATAQUE_POR_DRAGON.put(
                dragon.getUUID(),
                level.getGameTime()
                        + intervalo
        );
    }

    private static void ejecutarAtaqueAleatorio(
            ServerLevel level,
            EnderDragon dragon
    ) {

        int ataqueSeleccionado =
                dragon
                        .getRandom()
                        .nextInt(
                                ATAQUES_IMPLEMENTADOS
                        )
                        + 1;

        switch (ataqueSeleccionado) {

            case 1 ->
                    ataqueInvocarMicroDemonios(
                            level,
                            dragon
                    );

            case 2 ->
                    ataqueBombardeoTnt(
                            level,
                            dragon
                    );

            case 3 ->
                    ataqueInvocarEsqueletos(
                            level,
                            dragon
                    );

            default -> {
            }
        }
    }

    private static void ataqueInvocarMicroDemonios(
            ServerLevel level,
            EnderDragon dragon
    ) {

        double distanciaMaximaCuadrada =
                DISTANCIA_MAXIMA_JUGADORES
                        * DISTANCIA_MAXIMA_JUGADORES;

        for (ServerPlayer player :
                level.players()) {

            if (!player.isAlive()
                    || player.isSpectator()) {

                continue;
            }

            if (dragon.distanceToSqr(player)
                    > distanciaMaximaCuadrada) {

                continue;
            }

            for (int numero = 0;
                 numero < MICRO_DEMONIOS_POR_JUGADOR;
                 numero++) {

                generarMicroDemonioCercaDelJugador(
                        level,
                        dragon,
                        player,
                        numero
                );
            }

            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    40,
                    3.0D,
                    1.0D,
                    3.0D,
                    0.08D
            );

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.VEX_CHARGE,
                    SoundSource.HOSTILE,
                    1.5F,
                    0.7F
            );
        }
    }

    private static void generarMicroDemonioCercaDelJugador(
            ServerLevel level,
            EnderDragon dragon,
            ServerPlayer player,
            int numero
    ) {

        double anguloBase =
                dragon
                        .getRandom()
                        .nextDouble()
                        * Math.PI
                        * 2.0D;

        double separacion =
                Math.PI
                        * numero;

        double angulo =
                anguloBase
                        + separacion;

        double x =
                player.getX()
                        + Math.cos(angulo)
                        * DISTANCIA_INVOCACION;

        double y =
                player.getY()
                        + 1.0D;

        double z =
                player.getZ()
                        + Math.sin(angulo)
                        * DISTANCIA_INVOCACION;

        Mob vex =
                EntityTypes.VEX.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (vex == null) {
            return;
        }

        vex.setPos(
                x,
                y,
                z
        );

        vex.setYRot(
                player.getYRot()
        );

        vex.setXRot(
                0.0F
        );

        vex.setPersistenceRequired();

        level.addFreshEntity(
                vex
        );
    }

    private static void ataqueInvocarEsqueletos(
            ServerLevel level,
            EnderDragon dragon
    ) {

        BlockPos centroBatalla =
                buscarCentroPortal(
                        level
                );

        RandomSource random =
                level.getRandom();

        for (int i = 0;
             i < CANTIDAD_ESQUELETOS_ATAQUE_3;
             i++) {

            BlockPos posicionAparicion =
                    buscarPosicionAtaqueTres(
                            level,
                            centroBatalla,
                            random
                    );

            if (posicionAparicion == null) {
                continue;
            }

            Mob witherSkeleton =
                    EntityTypes.WITHER_SKELETON.create(
                            level,
                            EntitySpawnReason.TRIGGERED
                    );

            if (witherSkeleton == null) {
                continue;
            }

            witherSkeleton.setPos(
                    posicionAparicion.getX() + 0.5D,
                    posicionAparicion.getY(),
                    posicionAparicion.getZ() + 0.5D
            );

            witherSkeleton.setYRot(
                    random.nextFloat() * 360.0F
            );

            witherSkeleton.setXRot(
                    0.0F
            );

            SkeletonClassEvents
                    .configurarClaseCincoExterna(
                            level,
                            witherSkeleton
                    );

            level.addFreshEntity(
                    witherSkeleton
            );
        }
    }

    private static BlockPos buscarCentroPortal(
            ServerLevel level
    ) {

        int alturaInicial =
                level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        0,
                        0
                );

        for (int y = alturaInicial + 10;
             y >= level.getMinY();
             y--) {

            BlockPos posicion =
                    new BlockPos(
                            0,
                            y,
                            0
                    );

            if (level
                    .getBlockState(
                            posicion
                    )
                    .is(
                            Blocks.BEDROCK
                    )) {

                return posicion;
            }
        }

        return new BlockPos(
                0,
                alturaInicial,
                0
        );
    }

    private static BlockPos buscarPosicionAtaqueTres(
            ServerLevel level,
            BlockPos centroBatalla,
            RandomSource random
    ) {

        for (int intento = 0;
             intento < INTENTOS_POSICION_ATAQUE_3;
             intento++) {

            double angulo =
                    random.nextDouble()
                            * Math.PI
                            * 2.0D;

            double distancia =
                    Math.sqrt(
                            random.nextDouble()
                    ) * RADIO_ATAQUE_3;

            int desplazamientoX =
                    (int) Math.round(
                            Math.cos(angulo)
                                    * distancia
                    );

            int desplazamientoZ =
                    (int) Math.round(
                            Math.sin(angulo)
                                    * distancia
                    );

            int x =
                    centroBatalla.getX()
                            + desplazamientoX;

            int z =
                    centroBatalla.getZ()
                            + desplazamientoZ;

            int ySuelo =
                    level.getHeight(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

            BlockPos posicion =
                    new BlockPos(
                            x,
                            ySuelo,
                            z
                    );

            BlockPos bloqueInferior =
                    posicion.below();

            if (level
                    .getBlockState(
                            bloqueInferior
                    )
                    .isAir()) {

                continue;
            }

            if (!level
                    .getBlockState(
                            posicion
                    )
                    .isAir()) {

                continue;
            }

            if (!level
                    .getBlockState(
                            posicion.above()
                    )
                    .isAir()) {

                continue;
            }

            return posicion;
        }

        return null;
    }

    private WingedDemonAttackEvents() {
    }
}
