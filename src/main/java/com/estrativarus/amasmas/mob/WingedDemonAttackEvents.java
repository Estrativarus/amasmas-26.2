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
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import com.estrativarus.amasmas.mob.MiniWitherEvents;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private static final int[] ATAQUES_SELECCIONABLES = {
            1,
            2,
            3,
            4,
            5,
            7,
            8
    };
    private static final int CANTIDAD_ESQUELETOS_ATAQUE_3 =
            2;

    private static final int RADIO_ATAQUE_3 =
            20;

    private static final int INTENTOS_POSICION_ATAQUE_3 =
            20;


    private static final Map<UUID, Long>
            PROXIMO_ATAQUE_POR_DRAGON =
            new HashMap<>();

    private static final Map<UUID, AtaqueSeisEstado>
            ATAQUES_SEIS_ACTIVOS =
            new HashMap<>();

    private static final Map<UUID, Long>
            ATAQUES_SEIS_PENDIENTES =
            new HashMap<>();

    private static final int JUGADORES_ATAQUE_5 =
            3;

    private static final int INTERVALO_DISPAROS_ATAQUE_5 =
            15;

    private static final double ALTURA_SOBRE_PORTAL_ATAQUE_5 =
            12.0D;

    private static final float VELOCIDAD_BOLA_ATAQUE_5 =
            1.2F;

    private static final int POTENCIA_BOLA_ATAQUE_5 =
            4;

    private static final Map<UUID, AtaqueBolasEstado>
            ATAQUES_BOLAS_ACTIVOS =
            new HashMap<>();

    private static final int ESPERA_ATAQUE_6 =
            40;

    private static final int DURACION_MAXIMA_EMBESTIDA =
            80;

    private static final double VELOCIDAD_EMBESTIDA =
            3.2D;

    private static final double DISTANCIA_FINAL_EMBESTIDA =
            4.0D;

    private static final double DISTANCIA_MAXIMA_OBJETIVO =
            192.0D;

    public static final String TAG_ENDERMAN_ATAQUE_7 =
            "amasmas_enderman_ataque_demonio_alado";

    private static final int CANTIDAD_ENDERMANS_ATAQUE_7 =
            2;

    private static final double DISTANCIA_MAXIMA_OBJETIVO_ATAQUE_7 =
            192.0D;

    private static final int CANTIDAD_MINI_WITHERS_ATAQUE_8 =
            3;

    private static final double RADIO_MINI_WITHERS_ATAQUE_8 =
            4.0D;

    private static final double ALTURA_MINI_WITHERS_ATAQUE_8 =
            8.0D;

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

            ATAQUES_SEIS_PENDIENTES.remove(
                    dragon.getUUID()
            );

            ATAQUES_SEIS_ACTIVOS.remove(
                    dragon.getUUID()
            );

            return;
        }

        if (procesarAtaqueSeis(
                level,
                dragon
        )) {

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

        int indiceAtaque =
                dragon
                        .getRandom()
                        .nextInt(
                                ATAQUES_SELECCIONABLES.length
                        );

        int[] ataquesDisponibles = {
                1,
                2,
                3,
                4,
                5,
                8
        };

        int ataqueSeleccionado =
                ataquesDisponibles[
                        level.getRandom().nextInt(
                                ataquesDisponibles.length
                        )
                        ];

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

            case 4 ->
                    ataqueRegenerarCristal(
                            level,
                            dragon
                    );

            case 5 ->
                    ataqueBolasDeFuego(
                            level,
                            dragon
                    );

            case 7 ->
                    ataqueInvocarEndermans(level,dragon);

            case 8 ->
                ataqueInvocarMiniWithers(level,dragon);

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

    private static void ataqueRegenerarCristal(
            ServerLevel level,
            EnderDragon dragon
    ) {

        List<EndSpikeFeature.EndSpike> torres =
                EndSpikeFeature.getSpikesForLevel(
                        level
                );

        if (torres.isEmpty()) {
            return;
        }

        List<EndSpikeFeature.EndSpike> torresSinCristal =
                new ArrayList<>();

        for (EndSpikeFeature.EndSpike torre :
                torres) {

            if (!torreTieneCristal(
                    level,
                    torre
            )) {

                torresSinCristal.add(
                        torre
                );
            }
        }

        if (torresSinCristal.isEmpty()) {
            return;
        }

        EndSpikeFeature.EndSpike torreElegida =
                torresSinCristal.get(
                        level.getRandom().nextInt(
                                torresSinCristal.size()
                        )
                );

        crearCristalEnTorre(
                level,
                torreElegida
        );
    }

    private static boolean torreTieneCristal(
            ServerLevel level,
            EndSpikeFeature.EndSpike torre
    ) {

        double x =
                torre.getCenterX() + 0.5D;

        double y =
                torre.getHeight() + 1.0D;

        double z =
                torre.getCenterZ() + 0.5D;

        AABB zonaBusqueda =
                new AABB(
                        x - 2.0D,
                        y - 2.0D,
                        z - 2.0D,
                        x + 2.0D,
                        y + 3.0D,
                        z + 2.0D
                );

        List<EndCrystal> cristales =
                level.getEntitiesOfClass(
                        EndCrystal.class,
                        zonaBusqueda,
                        cristal ->
                                cristal.isAlive()
                                        && !cristal.isRemoved()
                );

        return !cristales.isEmpty();
    }

    private static void crearCristalEnTorre(
            ServerLevel level,
            EndSpikeFeature.EndSpike torre
    ) {

        EndCrystal cristal =
                EntityTypes.END_CRYSTAL.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (cristal == null) {
            return;
        }

        double x =
                torre.getCenterX() + 0.5D;

        double y =
                torre.getHeight() + 1.0D;

        double z =
                torre.getCenterZ() + 0.5D;

        cristal.setPos(
                x,
                y,
                z
        );

        cristal.setShowBottom(
                false
        );

        level.addFreshEntity(
                cristal
        );
    }

    private static final class AtaqueBolasEstado {

        private final List<UUID> objetivos;

        private int objetivoActual;

        private int ticksHastaDisparo;

        private AtaqueBolasEstado(
                List<UUID> objetivos
        ) {

            this.objetivos =
                    objetivos;

            this.objetivoActual =
                    0;

            this.ticksHastaDisparo =
                    10;
        }
    }

    private static void ataqueCinco(
            ServerLevel level,
            EnderDragon dragon
    ) {
        List<ServerPlayer> jugadoresValidos =
                new ArrayList<>();

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

            jugadoresValidos.add(
                    player
            );

            programarAtaqueSeis(
                    level,
                    dragon
            );
        }

        if (jugadoresValidos.isEmpty()) {
            return;
        }

        Collections.shuffle(
                jugadoresValidos,
                new java.util.Random(
                        level.getRandom().nextLong()
                )
        );

        int cantidadObjetivos =
                Math.min(
                        JUGADORES_ATAQUE_5,
                        jugadoresValidos.size()
                );

        List<UUID> objetivos =
                new ArrayList<>();

        for (int i = 0;
             i < cantidadObjetivos;
             i++) {

            objetivos.add(
                    jugadoresValidos
                            .get(i)
                            .getUUID()
            );
        }

        BlockPos centroPortal =
                buscarCentroPortal(
                        level
                );

        dragon.setPos(
                centroPortal.getX() + 0.5D,
                centroPortal.getY()
                        + ALTURA_SOBRE_PORTAL_ATAQUE_5,
                centroPortal.getZ() + 0.5D
        );

        dragon.setDeltaMovement(
                Vec3.ZERO
        );

        ATAQUES_BOLAS_ACTIVOS.put(
                dragon.getUUID(),
                new AtaqueBolasEstado(
                        objetivos
                )
        );
    }

    @SubscribeEvent
    public static void onAtaqueBolasTick(
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

        AtaqueBolasEstado estado =
                ATAQUES_BOLAS_ACTIVOS.get(
                        dragon.getUUID()
                );

        if (estado == null) {
            return;
        }

        if (!dragon.isAlive()
                || dragon.isRemoved()) {

            ATAQUES_BOLAS_ACTIVOS.remove(
                    dragon.getUUID()
            );

            return;
        }

        BlockPos centroPortal =
                buscarCentroPortal(
                        level
                );

        dragon.setPos(
                centroPortal.getX() + 0.5D,
                centroPortal.getY()
                        + ALTURA_SOBRE_PORTAL_ATAQUE_5,
                centroPortal.getZ() + 0.5D
        );

        dragon.setDeltaMovement(
                Vec3.ZERO
        );

        if (estado.ticksHastaDisparo > 0) {

            estado.ticksHastaDisparo--;

            return;
        }

        if (estado.objetivoActual
                >= estado.objetivos.size()) {

            ATAQUES_BOLAS_ACTIVOS.remove(
                    dragon.getUUID()
            );

            return;
        }

        UUID uuidObjetivo =
                estado.objetivos.get(
                        estado.objetivoActual
                );

        ServerPlayer objetivo =
                level.getServer()
                        .getPlayerList()
                        .getPlayer(
                                uuidObjetivo
                        );

        estado.objetivoActual++;

        estado.ticksHastaDisparo =
                INTERVALO_DISPAROS_ATAQUE_5;

        if (objetivo == null
                || !objetivo.isAlive()
                || objetivo.isSpectator()
                || objetivo.level() != level) {

            return;
        }

        girarDragonHaciaJugador(
                dragon,
                objetivo
        );

        lanzarBolaDeFuego(
                level,
                dragon,
                objetivo
        );
    }

    private static void girarDragonHaciaJugador(
            EnderDragon dragon,
            ServerPlayer objetivo
    ) {

        double diferenciaX =
                objetivo.getX()
                        - dragon.getX();

        double diferenciaY =
                objetivo.getEyeY()
                        - dragon.getEyeY();

        double diferenciaZ =
                objetivo.getZ()
                        - dragon.getZ();

        double distanciaHorizontal =
                Math.sqrt(
                        diferenciaX * diferenciaX
                                + diferenciaZ * diferenciaZ
                );

        float rotacionHorizontal =
                (float) (
                        Math.toDegrees(
                                Math.atan2(
                                        diferenciaZ,
                                        diferenciaX
                                )
                        ) - 90.0D
                );

        float rotacionVertical =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(
                                        diferenciaY,
                                        distanciaHorizontal
                                )
                        )
                );

        dragon.setYRot(
                rotacionHorizontal
        );

        dragon.setXRot(
                rotacionVertical
        );

        dragon.yBodyRot =
                rotacionHorizontal;
    }

    private static void lanzarBolaDeFuego(
            ServerLevel level,
            EnderDragon dragon,
            ServerPlayer objetivo
    ) {

        Projectile bola =
                EntityTypes.FIREBALL.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (bola == null) {
            return;
        }

        double origenX =
                dragon.getX();

        double origenY =
                dragon.getEyeY();

        double origenZ =
                dragon.getZ();

        double objetivoX =
                objetivo.getX();

        double objetivoY =
                objetivo.getEyeY();

        double objetivoZ =
                objetivo.getZ();

        Vec3 direccion =
                new Vec3(
                        objetivoX - origenX,
                        objetivoY - origenY,
                        objetivoZ - origenZ
                ).normalize();

        bola.setPos(
                origenX
                        + direccion.x * 4.0D,
                origenY
                        + direccion.y * 2.0D,
                origenZ
                        + direccion.z * 4.0D
        );

        bola.setOwner(
                dragon
        );

        bola.shoot(
                direccion.x,
                direccion.y,
                direccion.z,
                VELOCIDAD_BOLA_ATAQUE_5,
                0.0F
        );

        boolean anadida =
                level.addFreshEntity(
                        bola
                );

        if (!anadida) {
            return;
        }

        establecerPotenciaBola(
                level,
                bola
        );
    }

    private static void establecerPotenciaBola(
            ServerLevel level,
            Projectile bola
    ) {

        String comando =
                "data merge entity "
                        + bola.getUUID()
                        + " {ExplosionPower:"
                        + POTENCIA_BOLA_ATAQUE_5
                        + "}";

        level.getServer()
                .getCommands()
                .performPrefixedCommand(
                        level.getServer()
                                .createCommandSourceStack()
                                .withSuppressedOutput(),
                        comando
                );
    }

    private static void programarAtaqueSeis(
            ServerLevel level,
            EnderDragon dragon
    ) {

        UUID dragonUuid =
                dragon.getUUID();

        ATAQUES_SEIS_PENDIENTES.put(
                dragonUuid,
                level.getGameTime()
                        + ESPERA_ATAQUE_6
        );
    }

    private static boolean procesarAtaqueSeis(
            ServerLevel level,
            EnderDragon dragon
    ) {

        UUID dragonUuid =
                dragon.getUUID();

        AtaqueSeisEstado estadoActivo =
                ATAQUES_SEIS_ACTIVOS.get(
                        dragonUuid
                );

        if (estadoActivo != null) {

            continuarEmbestida(
                    level,
                    dragon,
                    estadoActivo
            );

            return true;
        }

        Long momentoEjecucion =
                ATAQUES_SEIS_PENDIENTES.get(
                        dragonUuid
                );

        if (momentoEjecucion == null) {
            return false;
        }

        if (level.getGameTime()
                < momentoEjecucion) {

            return true;
        }

        ATAQUES_SEIS_PENDIENTES.remove(
                dragonUuid
        );

        ServerPlayer objetivo =
                elegirJugadorAleatorio(
                        level,
                        dragon
                );

        if (objetivo == null) {
            return false;
        }

        iniciarEmbestida(
                level,
                dragon,
                objetivo
        );

        return true;
    }

    private static ServerPlayer elegirJugadorAleatorio(
            ServerLevel level,
            EnderDragon dragon
    ) {

        double distanciaMaximaCuadrada =
                DISTANCIA_MAXIMA_OBJETIVO
                        * DISTANCIA_MAXIMA_OBJETIVO;

        java.util.List<ServerPlayer> candidatos =
                new java.util.ArrayList<>();

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

            candidatos.add(
                    player
            );
        }

        if (candidatos.isEmpty()) {
            return null;
        }

        int indice =
                dragon
                        .getRandom()
                        .nextInt(
                                candidatos.size()
                        );

        return candidatos.get(
                indice
        );
    }

    private static void iniciarEmbestida(
            ServerLevel level,
            EnderDragon dragon,
            ServerPlayer objetivo
    ) {

        Vec3 direccion =
                obtenerDireccionEmbestida(
                        dragon,
                        objetivo
                );

        dragon.setDeltaMovement(
                direccion.scale(
                        VELOCIDAD_EMBESTIDA
                )
        );

        dragon.hurtMarked =
                true;

        ATAQUES_SEIS_ACTIVOS.put(
                dragon.getUUID(),
                new AtaqueSeisEstado(
                        objetivo.getUUID(),
                        DURACION_MAXIMA_EMBESTIDA
                )
        );

        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                dragon.getX(),
                dragon.getY() + 2.0D,
                dragon.getZ(),
                100,
                4.0D,
                2.0D,
                4.0D,
                0.15D
        );

        level.playSound(
                null,
                dragon.getX(),
                dragon.getY(),
                dragon.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE,
                4.0F,
                0.75F
        );
    }

    private static void continuarEmbestida(
            ServerLevel level,
            EnderDragon dragon,
            AtaqueSeisEstado estado
    ) {

        ServerPlayer objetivo =
                level.getServer()
                        .getPlayerList()
                        .getPlayer(
                                estado.objetivo
                        );

        if (objetivo == null
                || !objetivo.isAlive()
                || objetivo.isSpectator()
                || objetivo.level() != dragon.level()) {

            terminarEmbestida(
                    dragon
            );

            return;
        }

        double distanciaCuadrada =
                dragon.distanceToSqr(
                        objetivo
                );

        double distanciaFinalCuadrada =
                DISTANCIA_FINAL_EMBESTIDA
                        * DISTANCIA_FINAL_EMBESTIDA;

        if (distanciaCuadrada
                <= distanciaFinalCuadrada) {

            terminarEmbestida(
                    dragon
            );

            return;
        }

        estado.ticksRestantes--;

        if (estado.ticksRestantes <= 0) {

            terminarEmbestida(
                    dragon
            );

            return;
        }

        Vec3 direccion =
                obtenerDireccionEmbestida(
                        dragon,
                        objetivo
                );

        dragon.setDeltaMovement(
                direccion.scale(
                        VELOCIDAD_EMBESTIDA
                )
        );

        dragon.hurtMarked =
                true;

        level.sendParticles(
                ParticleTypes.SMOKE,
                dragon.getX(),
                dragon.getY() + 1.5D,
                dragon.getZ(),
                12,
                1.5D,
                1.0D,
                1.5D,
                0.05D
        );
    }

    private static Vec3 obtenerDireccionEmbestida(
            EnderDragon dragon,
            ServerPlayer objetivo
    ) {

        double diferenciaX =
                objetivo.getX()
                        - dragon.getX();

        double diferenciaY =
                objetivo.getEyeY()
                        - dragon.getY();

        double diferenciaZ =
                objetivo.getZ()
                        - dragon.getZ();

        Vec3 direccion =
                new Vec3(
                        diferenciaX,
                        diferenciaY,
                        diferenciaZ
                );

        if (direccion.lengthSqr()
                < 0.0001D) {

            return new Vec3(
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        return direccion.normalize();
    }

    private static void terminarEmbestida(
            EnderDragon dragon
    ) {

        ATAQUES_SEIS_ACTIVOS.remove(
                dragon.getUUID()
        );

        dragon.setDeltaMovement(
                dragon
                        .getDeltaMovement()
                        .scale(
                                0.25D
                        ));
    }

    private static final class AtaqueSeisEstado {

        private final UUID objetivo;

        private int ticksRestantes;

        private AtaqueSeisEstado(
                UUID objetivo,
                int ticksRestantes
        ) {

            this.objetivo =
                    objetivo;

            this.ticksRestantes =
                    ticksRestantes;
        }
    }

    private static void ataqueBolasDeFuego(
            ServerLevel level,
            EnderDragon dragon
    ) {

        List<ServerPlayer> jugadoresValidos =
                new ArrayList<>();

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

            jugadoresValidos.add(
                    player
            );
        }

        if (jugadoresValidos.isEmpty()) {
            return;
        }

        Collections.shuffle(
                jugadoresValidos,
                new java.util.Random(
                        level.getRandom().nextLong()
                )
        );

        int cantidadObjetivos =
                Math.min(
                        JUGADORES_ATAQUE_5,
                        jugadoresValidos.size()
                );

        List<UUID> objetivos =
                new ArrayList<>();

        for (int i = 0;
             i < cantidadObjetivos;
             i++) {

            objetivos.add(
                    jugadoresValidos
                            .get(i)
                            .getUUID()
            );
        }

        BlockPos centroPortal =
                buscarCentroPortal(
                        level
                );

        dragon.setPos(
                centroPortal.getX() + 0.5D,
                centroPortal.getY()
                        + ALTURA_SOBRE_PORTAL_ATAQUE_5,
                centroPortal.getZ() + 0.5D
        );

        dragon.setDeltaMovement(
                Vec3.ZERO
        );

        ATAQUES_BOLAS_ACTIVOS.put(
                dragon.getUUID(),
                new AtaqueBolasEstado(
                        objetivos
                )
        );
    }

    private static void ataqueInvocarEndermans(
            ServerLevel level,
            EnderDragon dragon
    ) {

        ServerPlayer primerObjetivo =
                elegirJugadorAleatorioAtaqueSiete(
                        level,
                        dragon
                );

        if (primerObjetivo == null) {
            return;
        }

        BlockPos posicionSuelo =
                buscarSueloBajoDragon(
                        level,
                        dragon
                );

        for (int numero = 0;
             numero < CANTIDAD_ENDERMANS_ATAQUE_7;
             numero++) {

            ServerPlayer objetivo =
                    elegirJugadorAleatorioAtaqueSiete(
                            level,
                            dragon
                    );

            if (objetivo == null) {

                objetivo =
                        primerObjetivo;
            }

            generarEndermanEnfadado(
                    level,
                    posicionSuelo,
                    objetivo,
                    numero
            );
        }

        level.sendParticles(
                ParticleTypes.PORTAL,
                posicionSuelo.getX() + 0.5D,
                posicionSuelo.getY() + 1.0D,
                posicionSuelo.getZ() + 0.5D,
                100,
                1.0D,
                1.5D,
                1.0D,
                0.25D
        );

        level.playSound(
                null,
                posicionSuelo.getX() + 0.5D,
                posicionSuelo.getY() + 1.0D,
                posicionSuelo.getZ() + 0.5D,
                SoundEvents.ENDERMAN_SCREAM,
                SoundSource.HOSTILE,
                3.0F,
                0.7F
        );
    }

    private static BlockPos buscarSueloBajoDragon(
            ServerLevel level,
            EnderDragon dragon
    ) {

        int x =
                (int) Math.floor(
                        dragon.getX()
                );

        int z =
                (int) Math.floor(
                        dragon.getZ()
                );

        int alturaSuperficie =
                level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x,
                        z
                );

        BlockPos superficie =
                new BlockPos(
                        x,
                        alturaSuperficie,
                        z
                );

        for (int y = alturaSuperficie;
             y >= level.getMinY();
             y--) {

            BlockPos suelo =
                    new BlockPos(
                            x,
                            y,
                            z
                    );

            BlockPos espacioEncima =
                    suelo.above();

            boolean sueloSolido =
                    !level
                            .getBlockState(suelo)
                            .isAir();

            boolean espacioLibre =
                    level
                            .getBlockState(
                                    espacioEncima
                            )
                            .isAir();

            boolean segundoEspacioLibre =
                    level
                            .getBlockState(
                                    espacioEncima.above()
                            )
                            .isAir();

            if (sueloSolido
                    && espacioLibre
                    && segundoEspacioLibre) {

                return espacioEncima;
            }
        }

        return superficie;
    }

    private static ServerPlayer elegirJugadorAleatorioAtaqueSiete(
            ServerLevel level,
            EnderDragon dragon
    ) {

        double distanciaMaximaCuadrada =
                DISTANCIA_MAXIMA_OBJETIVO_ATAQUE_7
                        * DISTANCIA_MAXIMA_OBJETIVO_ATAQUE_7;

        java.util.List<ServerPlayer> candidatos =
                new java.util.ArrayList<>();

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

            candidatos.add(
                    player
            );
        }

        if (candidatos.isEmpty()) {
            return null;
        }

        int indice =
                dragon
                        .getRandom()
                        .nextInt(
                                candidatos.size()
                        );

        return candidatos.get(
                indice
        );
    }

    private static void generarEndermanEnfadado(
            ServerLevel level,
            BlockPos posicionSuelo,
            ServerPlayer objetivo,
            int numero
    ) {

        Mob enderman =
                EntityTypes.ENDERMAN.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (enderman == null) {
            return;
        }

        double desplazamientoX;

        if (numero == 0) {

            desplazamientoX =
                    -0.75D;

        } else {

            desplazamientoX =
                    0.75D;
        }

        enderman.setPos(
                posicionSuelo.getX()
                        + 0.5D
                        + desplazamientoX,
                posicionSuelo.getY(),
                posicionSuelo.getZ()
                        + 0.5D
        );

        enderman.setYRot(
                objetivo.getYRot()
        );

        enderman.setXRot(
                0.0F
        );

        enderman.setPersistenceRequired();

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_ENDERMAN_ATAQUE_7,
                        true
                );

        enderman.setTarget(
                objetivo
        );

        boolean anadido =
                level.addFreshEntity(
                        enderman
                );

        if (!anadido) {
            return;
        }

        level.getServer().execute(() -> {

            if (!enderman.isAlive()
                    || enderman.isRemoved()) {

                return;
            }

            enderman.setTarget(
                    objetivo
            );
        });
    }

    private static void ataqueInvocarMiniWithers(
            ServerLevel level,
            EnderDragon dragon
    ) {

        BlockPos centroPortal =
                buscarCentroPortal(
                        level
                );

        double centroX =
                centroPortal.getX() + 0.5D;

        double centroY =
                centroPortal.getY()
                        + ALTURA_MINI_WITHERS_ATAQUE_8;

        double centroZ =
                centroPortal.getZ() + 0.5D;

        for (int indice = 0;
             indice < CANTIDAD_MINI_WITHERS_ATAQUE_8;
             indice++) {

            double angulo =
                    indice
                            * (
                            Math.PI
                                    * 2.0D
                                    / CANTIDAD_MINI_WITHERS_ATAQUE_8
                    );

            double x =
                    centroX
                            + Math.cos(angulo)
                            * RADIO_MINI_WITHERS_ATAQUE_8;

            double z =
                    centroZ
                            + Math.sin(angulo)
                            * RADIO_MINI_WITHERS_ATAQUE_8;

            WitherBoss miniWither =
                    MiniWitherEvents
                            .crearMiniWitherExterno(
                                    level,
                                    x,
                                    centroY,
                                    z
                            );

            if (miniWither == null) {
                continue;
            }

            boolean anadido =
                    level.addFreshEntity(
                            miniWither
                    );

            if (!anadido) {
                miniWither.discard();
            }
        }
    }

    private WingedDemonAttackEvents() {
    }
}
