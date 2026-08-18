package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.ModList;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class MiniWitherEvents {

    public static final String TAG_MINI_WITHER =
            "amasmas_mini_wither";

    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_mini_wither_realizada";

    private static final String TAG_TRANSFORMACION_PENDIENTE =
            "amasmas_transformacion_mini_wither_pendiente";

    private static final int DIA_INICIO =
            14;

    private static final int PROBABILIDAD_APARICION =
            3;

    private static final double VIDA_MAXIMA =
            80.0D;

    private static final double ESCALA =
            0.35D;

    private static final float DANO_CABEZA =
            2.0F;

    private static final double EMPUJE_HORIZONTAL =
            2.5D;

    private static final double EMPUJE_VERTICAL =
            0.75D;

    private static final int DURACION_WITHER =
            20 * 10;

    private static final int AMPLIFICADOR_WITHER =
            1;

    private static final String MOD_REPURPOSED_STRUCTURES =
            "repurposed_structures";

    private static final ResourceKey<Structure>
            END_CITY =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "end_city"
                    )
            );


    private static final ResourceKey<Structure>
            ANCIENT_CITY_END =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            "repurposed_structures",
                            "ancient_city_end"
                    )
            );

    @SubscribeEvent
    public static void onEndermanJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof LivingEntity enderman)) {

            return;
        }

        if (enderman.getType()
                != EntityTypes.ENDERMAN) {

            return;
        }

        if (event.loadedFromDisk()) {
            return;
        }

        level.getServer().execute(() -> {

            if (!enderman.isAlive()
                    || enderman.isRemoved()) {

                return;
            }

            intentarCrearMiniWither(
                    level,
                    enderman
            );
        });
    }

    private static void intentarCrearMiniWither(
            ServerLevel level,
            LivingEntity enderman
    ) {

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_TIRADA_REALIZADA
                )) {

            return;
        }

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_REALIZADA,
                        true
                );

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (!estaDentroDeEstructuraPermitida(
                level,
                enderman
        )) {

            return;
        }

        if (enderman
                .getRandom()
                .nextInt(
                        PROBABILIDAD_APARICION
                )
                != 0) {

            return;
        }

        programarTransformacion(
                level,
                enderman
        );
    }

    private static boolean estaDentroDeEstructuraPermitida(
            ServerLevel level,
            LivingEntity enderman
    ) {

        boolean dentroDeEndCity =
                level
                        .structureManager()
                        .getStructureWithPieceAt(
                                enderman.blockPosition(),
                                holder ->
                                        holder.is(
                                                END_CITY
                                        )
                        )
                        .isValid();

        if (dentroDeEndCity) {
            return true;
        }

        if (!ModList.get().isLoaded(
                MOD_REPURPOSED_STRUCTURES
        )) {

            return false;
        }

        return level
                .structureManager()
                .getStructureWithPieceAt(
                        enderman.blockPosition(),
                        holder ->
                                holder.is(
                                        ANCIENT_CITY_END
                                )
                )
                .isValid();
    }

    private static void programarTransformacion(
            ServerLevel level,
            LivingEntity enderman
    ) {

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_TRANSFORMACION_PENDIENTE
                )) {

            return;
        }

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TRANSFORMACION_PENDIENTE,
                        true
                );

        double x =
                enderman.getX();

        double y =
                enderman.getY();

        double z =
                enderman.getZ();

        float yRot =
                enderman.getYRot();

        float xRot =
                enderman.getXRot();

        level.getServer().execute(() -> {

            if (!enderman.isAlive()
                    || enderman.isRemoved()) {

                return;
            }

            WitherBoss miniWither =
                    EntityTypes.WITHER.create(
                            level,
                            EntitySpawnReason.TRIGGERED
                    );

            if (miniWither == null) {

                enderman
                        .getPersistentData()
                        .remove(
                                TAG_TRANSFORMACION_PENDIENTE
                        );

                return;
            }

            miniWither.setPos(
                    x,
                    y,
                    z
            );

            miniWither.setYRot(
                    yRot
            );

            miniWither.setXRot(
                    xRot
            );

            configurarMiniWither(
                    miniWither
            );

            boolean anadido =
                    level.addFreshEntity(
                            miniWither
                    );

            if (!anadido) {

                enderman
                        .getPersistentData()
                        .remove(
                                TAG_TRANSFORMACION_PENDIENTE
                        );

                return;
            }

            enderman.discard();
        });
    }

    private static void configurarMiniWither(
            WitherBoss miniWither
    ) {

        miniWither
                .getPersistentData()
                .putBoolean(
                        TAG_MINI_WITHER,
                        true
                );

        miniWither.setCustomName(
                Component.literal(
                        "Mini Wither"
                ).withStyle(
                        ChatFormatting.GRAY,
                        ChatFormatting.BOLD
                )
        );

        miniWither.setCustomNameVisible(
                false
        );

        miniWither.setPersistenceRequired();

        AttributeInstance atributoVida =
                miniWither.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida != null) {

            atributoVida.setBaseValue(
                    VIDA_MAXIMA
            );

            miniWither.setHealth(
                    (float) VIDA_MAXIMA
            );
        }

        AttributeInstance atributoEscala =
                miniWither.getAttribute(
                        Attributes.SCALE
                );

        if (atributoEscala != null) {

            atributoEscala.setBaseValue(
                    ESCALA
            );
        }

        miniWither.setInvulnerableTicks(
                0
        );
        ((com.estrativarus.amasmas.mixin.WitherBossAccessor)
                (Object) miniWither)
                .amasmas$getBossEvent()
                .setVisible(false);
    }

    @SubscribeEvent
    public static void onMiniWitherTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof WitherBoss miniWither)) {

            return;
        }

        if (!esMiniWither(miniWither)) {
            return;
        }

        if (miniWither.tickCount % 40 != 0) {
            return;
        }

        if (miniWither.getInvulnerableTicks() > 0) {

            miniWither.setInvulnerableTicks(
                    0
            );
        }
    }

    @SubscribeEvent
    public static void onMiniWitherHeadDamage(
            LivingIncomingDamageEvent event
    ) {

        if (!(event.getEntity().level()
                instanceof ServerLevel)) {

            return;
        }

        Entity entidadDirecta =
                event.getSource()
                        .getDirectEntity();

        Entity propietario =
                obtenerPropietario(
                        entidadDirecta
                );

        if (!(propietario instanceof WitherBoss miniWither)) {
            return;
        }

        if (!esMiniWither(miniWither)) {
            return;
        }

        LivingEntity victima =
                event.getEntity();

        event.getContainer().setNewDamage(
                DANO_CABEZA
        );

        victima.addEffect(
                new MobEffectInstance(
                        MobEffects.WITHER,
                        DURACION_WITHER,
                        AMPLIFICADOR_WITHER,
                        false,
                        true,
                        true
                )
        );

        aplicarEmpuje(
                miniWither,
                victima
        );
    }

    private static Entity obtenerPropietario(
            Entity entidadDirecta
    ) {

        if (entidadDirecta
                instanceof Projectile projectile) {

            return projectile.getOwner();
        }

        return null;
    }

    private static void aplicarEmpuje(
            WitherBoss miniWither,
            LivingEntity victima
    ) {

        double diferenciaX =
                victima.getX()
                        - miniWither.getX();

        double diferenciaZ =
                victima.getZ()
                        - miniWither.getZ();

        double longitud =
                Math.sqrt(
                        diferenciaX * diferenciaX
                                + diferenciaZ * diferenciaZ
                );

        if (longitud < 0.001D) {

            diferenciaX = 1.0D;
            diferenciaZ = 0.0D;
            longitud = 1.0D;
        }

        double empujeX =
                diferenciaX
                        / longitud
                        * EMPUJE_HORIZONTAL;

        double empujeZ =
                diferenciaZ
                        / longitud
                        * EMPUJE_HORIZONTAL;

        victima.push(
                empujeX,
                EMPUJE_VERTICAL,
                empujeZ
        );

        victima.hurtMarked =
                true;
    }

    @SubscribeEvent
    public static void onMiniWitherExplosion(
            ExplosionEvent.Detonate event
    ) {

        Entity entidadDirecta =
                event.getExplosion()
                        .getDirectSourceEntity();

        Entity propietario =
                obtenerPropietario(
                        entidadDirecta
                );

        if (!(propietario instanceof WitherBoss miniWither)) {
            return;
        }

        if (!esMiniWither(miniWither)) {
            return;
        }

        event.getAffectedBlocks()
                .clear();
    }

    public static boolean esMiniWither(
            LivingEntity entity
    ) {

        return entity.getType()
                == EntityTypes.WITHER

                && entity
                .getPersistentData()
                .contains(
                        TAG_MINI_WITHER
                );
    }

    private MiniWitherEvents() {
    }
}