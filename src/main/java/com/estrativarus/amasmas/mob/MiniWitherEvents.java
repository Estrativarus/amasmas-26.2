package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;
import com.estrativarus.amasmas.mixin.WitherBossAccessor;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class MiniWitherEvents {

    public static final String TAG_MINI_WITHER =
            "amasmas_mini_wither";

    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_mini_wither_realizada";

    private static final int DIA_INICIO =
            14;

    private static final int PROBABILIDAD_APARICION =
            3;

    private static final int INTERVALO_COMPROBACION =
            100;

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

        if (event.isCanceled()) {
            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob enderman)) {

            return;
        }

        if (enderman.getType()
                != EntityTypes.ENDERMAN) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (!puedeRealizarTirada(
                level,
                enderman,
                diaActual
        )) {

            return;
        }

        marcarTiradaRealizada(
                enderman
        );

        if (!superaTirada(
                enderman
        )) {

            return;
        }

        procesarEndermanNuevo(
                event,
                level,
                enderman
        );
    }

    @SubscribeEvent
    public static void onEndermanTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob enderman)) {

            return;
        }

        if (enderman.getType()
                != EntityTypes.ENDERMAN) {

            return;
        }

        if (!(enderman.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((enderman.tickCount + enderman.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (!puedeRealizarTirada(
                level,
                enderman,
                diaActual
        )) {

            return;
        }

        marcarTiradaRealizada(
                enderman
        );

        if (!superaTirada(
                enderman
        )) {

            return;
        }

        convertirEndermanYaCargado(
                level,
                enderman
        );
    }

    private static boolean puedeRealizarTirada(
            ServerLevel level,
            Mob enderman,
            int diaActual
    ) {

        if (diaActual < DIA_INICIO) {
            return false;
        }

        if (enderman
                .getPersistentData()
                .contains(
                        TAG_TIRADA_REALIZADA
                )) {

            return false;
        }

        if (!estaDentroDeEstructuraPermitida(
                level,
                enderman
        )) {

            return false;
        }

        return EntityReplacementHelper
                .puedeSerReemplazada(
                        enderman
                );
    }

    private static void marcarTiradaRealizada(
            Mob enderman
    ) {

        enderman
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_REALIZADA,
                        true
                );
    }

    private static boolean superaTirada(
            Mob enderman
    ) {

        return enderman
                .getRandom()
                .nextInt(
                        PROBABILIDAD_APARICION
                )
                == 0;
    }

    private static void procesarEndermanNuevo(
            EntityJoinLevelEvent event,
            ServerLevel level,
            Mob enderman
    ) {

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        enderman
                )) {

            return;
        }

        MiniWitherReplacementData datos =
                MiniWitherReplacementData.from(
                        enderman
                );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearMiniWitherDesdeDatos(
                        level,
                        datos
                )
        );
    }

    private static void crearMiniWitherDesdeDatos(
            ServerLevel level,
            MiniWitherReplacementData datos
    ) {

        WitherBoss miniWither =
                EntityTypes.WITHER.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (miniWither == null) {
            return;
        }

        datos.aplicarA(
                miniWither
        );

        configurarMiniWither(
                miniWither
        );

        boolean anadido =
                level.addFreshEntity(
                        miniWither
                );

        if (!anadido) {

            miniWither.discard();
        }
    }

    private static void convertirEndermanYaCargado(
            ServerLevel level,
            Mob enderman
    ) {

        if (!enderman.isAlive()
                || enderman.isRemoved()) {

            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        enderman
                )) {

            return;
        }

        WitherBoss miniWither =
                EntityTypes.WITHER.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (miniWither == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );

            return;
        }

        configurarMiniWither(
                miniWither
        );

        boolean reemplazado =
                EntityReplacementHelper.reemplazar(
                        level,
                        enderman,
                        miniWither
                );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            enderman
                    );
        }
    }

    private static boolean estaDentroDeEstructuraPermitida(
            ServerLevel level,
            Mob enderman
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

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
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

        ((WitherBossAccessor)
                (Object) miniWither)
                .amasmas$getBossEvent()
                .setVisible(
                        false
                );
    }

    @SubscribeEvent
    public static void onMiniWitherTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof WitherBoss miniWither)) {

            return;
        }

        if (!esMiniWither(
                miniWither
        )) {

            return;
        }

        if (miniWither.tickCount
                % 40 != 0) {

            return;
        }

        if (miniWither.getInvulnerableTicks()
                > 0) {

            miniWither.setInvulnerableTicks(
                    0
            );
        }

        ((WitherBossAccessor)
                (Object) miniWither)
                .amasmas$getBossEvent()
                .setVisible(
                        false
                );
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

        if (!(propietario
                instanceof WitherBoss miniWither)) {

            return;
        }

        if (!esMiniWither(
                miniWither
        )) {

            return;
        }

        LivingEntity victima =
                event.getEntity();

        event.getContainer()
                .setNewDamage(
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

            diferenciaX =
                    1.0D;

            diferenciaZ =
                    0.0D;

            longitud =
                    1.0D;
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

        if (!(propietario
                instanceof WitherBoss miniWither)) {

            return;
        }

        if (!esMiniWither(
                miniWither
        )) {

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

    public static WitherBoss crearMiniWitherExterno(
            ServerLevel level,
            double x,
            double y,
            double z
    ) {

        WitherBoss miniWither =
                EntityTypes.WITHER.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (miniWither == null) {
            return null;
        }

        miniWither.setPos(
                x,
                y,
                z
        );

        miniWither.setYRot(
                level.getRandom().nextFloat()
                        * 360.0F
        );

        miniWither.setXRot(
                0.0F
        );

        configurarMiniWither(
                miniWither
        );

        return miniWither;
    }

    private record MiniWitherReplacementData(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            float yHeadRot,
            double movimientoX,
            double movimientoY,
            double movimientoZ,
            Component nombre,
            boolean nombreVisible
    ) {

        private static MiniWitherReplacementData from(
                Mob enderman
        ) {

            return new MiniWitherReplacementData(
                    enderman.getX(),
                    enderman.getY(),
                    enderman.getZ(),
                    enderman.getYRot(),
                    enderman.getXRot(),
                    enderman.getYHeadRot(),
                    enderman.getDeltaMovement().x,
                    enderman.getDeltaMovement().y,
                    enderman.getDeltaMovement().z,
                    enderman.getCustomName() == null
                            ? null
                            : enderman
                            .getCustomName()
                            .copy(),
                    enderman.isCustomNameVisible()
            );
        }

        private void aplicarA(
                WitherBoss miniWither
        ) {

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

            miniWither.setYHeadRot(
                    yHeadRot
            );

            miniWither.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (nombre != null) {

                miniWither.setCustomName(
                        nombre
                );

                miniWither.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private MiniWitherEvents() {
    }
}