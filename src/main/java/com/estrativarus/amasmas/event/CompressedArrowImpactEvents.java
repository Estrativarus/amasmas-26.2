package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.CompressedArrowItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import java.util.List;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class CompressedArrowImpactEvents {

    private static final float DANO_EXPLOSION =
            3.0F;

    private static final double RADIO_EXPLOSION =
            4.0D;

    private static final double FUERZA_HORIZONTAL =
            1.6D;

    private static final double FUERZA_VERTICAL =
            0.9D;

    @SubscribeEvent
    public static void onCompressedArrowImpact(
            ProjectileImpactEvent event
    ) {

        Projectile projectile =
                event.getProjectile();

        if (!esFlechaComprimida(
                projectile
        )) {

            return;
        }

        if (!(projectile.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (projectile
                .getPersistentData()
                .contains(
                        CompressedArrowItem
                                .TAG_EXPLOSION_REALIZADA
                )) {

            return;
        }

        projectile
                .getPersistentData()
                .putBoolean(
                        CompressedArrowItem
                                .TAG_EXPLOSION_REALIZADA,
                        true
                );

        HitResult impacto =
                event.getRayTraceResult();

        Vec3 posicionImpacto =
                impacto.getLocation();

        Entity tirador =
                projectile.getOwner();

        level.getServer().execute(() ->
                crearExplosionDeAire(
                        level,
                        projectile,
                        tirador,
                        posicionImpacto
                )
        );
    }

    private static boolean esFlechaComprimida(
            Projectile projectile
    ) {

        return projectile
                .getPersistentData()
                .contains(
                        CompressedArrowItem
                                .TAG_FLECHA_COMPRIMIDA
                );
    }

    private static void crearExplosionDeAire(
            ServerLevel level,
            Projectile projectile,
            Entity tirador,
            Vec3 centro
    ) {

        crearEfectosVisuales(
                level,
                centro
        );

        AABB zona =
                new AABB(
                        centro.x - RADIO_EXPLOSION,
                        centro.y - RADIO_EXPLOSION,
                        centro.z - RADIO_EXPLOSION,
                        centro.x + RADIO_EXPLOSION,
                        centro.y + RADIO_EXPLOSION,
                        centro.z + RADIO_EXPLOSION
                );

        List<LivingEntity> entidades =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        zona,
                        entidad ->
                                entidad.isAlive()
                                        && !entidad.isSpectator()
                );

        boolean disparadaPorJugador =
                tirador instanceof Player;

        for (LivingEntity entidad :
                entidades) {

            if (entidad == tirador) {
                continue;
            }

            double distancia =
                    entidad.position()
                            .distanceTo(
                                    centro
                            );

            if (distancia > RADIO_EXPLOSION) {
                continue;
            }

            aplicarEmpuje(
                    entidad,
                    centro,
                    distancia
            );

            boolean objetivoEsJugador =
                    entidad instanceof Player;

            boolean debeRecibirDano =
                    !disparadaPorJugador
                            || !objetivoEsJugador;

            if (!debeRecibirDano) {
                continue;
            }

            entidad.hurtServer(
                    level,
                    level
                            .damageSources()
                            .explosion(
                                    projectile,
                                    tirador
                            ),
                    DANO_EXPLOSION
            );
        }
    }

    private static void aplicarEmpuje(
            LivingEntity entidad,
            Vec3 centro,
            double distancia
    ) {

        Vec3 direccion =
                entidad.position()
                        .subtract(
                                centro
                        );

        double longitudHorizontal =
                Math.sqrt(
                        direccion.x * direccion.x
                                + direccion.z * direccion.z
                );

        if (longitudHorizontal < 0.001D) {

            direccion =
                    new Vec3(
                            0.1D,
                            0.0D,
                            0.0D
                    );

            longitudHorizontal =
                    0.1D;
        }

        double intensidad =
                1.0D
                        - Math.min(
                        distancia / RADIO_EXPLOSION,
                        1.0D
                );

        double empujeHorizontal =
                FUERZA_HORIZONTAL
                        * intensidad;

        double empujeVertical =
                0.2D
                        + FUERZA_VERTICAL
                        * intensidad;

        entidad.push(
                direccion.x
                        / longitudHorizontal
                        * empujeHorizontal,
                empujeVertical,
                direccion.z
                        / longitudHorizontal
                        * empujeHorizontal
        );
    }

    private static void crearEfectosVisuales(
            ServerLevel level,
            Vec3 centro
    ) {

        level.sendParticles(
                ParticleTypes.GUST_EMITTER_SMALL,
                centro.x,
                centro.y,
                centro.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.GUST,
                centro.x,
                centro.y,
                centro.z,
                20,
                1.0D,
                1.0D,
                1.0D,
                0.1D
        );

        level.playSound(
                null,
                centro.x,
                centro.y,
                centro.z,
                SoundEvents.BREEZE_WIND_CHARGE_BURST,
                SoundSource.PLAYERS,
                1.2F,
                1.0F
        );
    }

    private CompressedArrowImpactEvents() {
    }
}