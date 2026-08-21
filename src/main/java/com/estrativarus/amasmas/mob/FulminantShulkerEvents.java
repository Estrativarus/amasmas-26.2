package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class FulminantShulkerEvents {

    private static void protegerCaparazon(
            ItemEntity caparazon
    ) {

        caparazon
                .getPersistentData()
                .putBoolean(
                        TAG_CAPARAZON_PROTEGIDO,
                        true
                );
    }

    public static final String TAG_SHULKER_FULMINANTE =
            "amasmas_shulker_fulminante";

    private static final String TAG_PROYECTIL_PROCESADO =
            "amasmas_bala_shulker_procesada";

    private static final String TAG_TNT_FULMINANTE =
            "amasmas_tnt_shulker_fulminante";

    private static final String TAG_TNT_MUERTE_CREADA =
            "amasmas_tnt_muerte_shulker_creada";

    private static final String TAG_CAPARAZON_PROTEGIDO =
            "amasmas_caparazon_shulker_protegido";

    private static final int DIA_INICIO =
            14;

    private static final int MECHA_TNT_IMPACTO =
            20;

    private static final int MECHA_TNT_MUERTE =
            30;

    private static final float MULTIPLICADOR_DANO_TNT =
            3.0F;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onCaparazonInvulnerabilityCheck(
            EntityInvulnerabilityCheckEvent event
    ) {

        if (!(event.getEntity()
                instanceof ItemEntity itemEntity)) {

            return;
        }

        if (!itemEntity
                .getItem()
                .is(
                        Items.SHULKER_SHELL
                )) {

            return;
        }

        if (!itemEntity
                .getPersistentData()
                .contains(
                        TAG_CAPARAZON_PROTEGIDO
                )) {

            return;
        }

        if (!event
                .getSource()
                .is(
                        DamageTypeTags.IS_EXPLOSION
                )) {

            return;
        }

        event.setInvulnerable(
                true
        );
    }

    @SubscribeEvent
    public static void onShulkerTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof LivingEntity shulker)) {

            return;
        }

        if (shulker.getType()
                != EntityTypes.SHULKER) {

            return;
        }

        if (!(shulker.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((shulker.tickCount
                + shulker.getId())
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

        convertirEnFulminante(
                shulker
        );
    }

    private static void convertirEnFulminante(
            LivingEntity shulker
    ) {

        if (esShulkerFulminante(shulker)) {
            return;
        }

        shulker
                .getPersistentData()
                .putBoolean(
                        TAG_SHULKER_FULMINANTE,
                        true
                );

        shulker.setCustomName(
                Component.literal(
                        "Shulker Fulminante"
                ).withStyle(
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.BOLD
                )
        );

        shulker.setCustomNameVisible(
                false
        );

        if (shulker instanceof net.minecraft.world.entity.Mob mob) {
            mob.setPersistenceRequired();
        }
    }

    @SubscribeEvent
    public static void onShulkerBulletImpact(
            ProjectileImpactEvent event
    ) {

        Projectile proyectil =
                event.getProjectile();

        if (!(proyectil.level()
                instanceof ServerLevel level)) {

            return;
        }

        Entity propietario =
                proyectil.getOwner();

        if (!(propietario
                instanceof LivingEntity shulker)) {

            return;
        }

        if (shulker.getType()
                != EntityTypes.SHULKER) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        convertirEnFulminante(
                shulker
        );

        if (!esShulkerFulminante(shulker)) {
            return;
        }

        if (proyectil
                .getPersistentData()
                .contains(
                        TAG_PROYECTIL_PROCESADO
                )) {

            return;
        }

        proyectil
                .getPersistentData()
                .putBoolean(
                        TAG_PROYECTIL_PROCESADO,
                        true
                );

        crearTntFulminante(
                level,
                proyectil.getX(),
                proyectil.getY(),
                proyectil.getZ(),
                shulker,
                MECHA_TNT_IMPACTO
        );
    }

    @SubscribeEvent
    public static void onShulkerDeath(
            LivingDeathEvent event
    ) {

        LivingEntity shulker =
                event.getEntity();

        if (shulker.getType()
                != EntityTypes.SHULKER) {

            return;
        }

        if (!(shulker.level()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        convertirEnFulminante(
                shulker
        );

        if (!esShulkerFulminante(shulker)) {
            return;
        }

        if (shulker
                .getPersistentData()
                .contains(
                        TAG_TNT_MUERTE_CREADA
                )) {

            return;
        }

        shulker
                .getPersistentData()
                .putBoolean(
                        TAG_TNT_MUERTE_CREADA,
                        true
                );

        double x =
                shulker.getX();

        double y =
                shulker.getY();

        double z =
                shulker.getZ();

        level.getServer().execute(() ->
                crearTntFulminante(
                        level,
                        x,
                        y,
                        z,
                        null,
                        MECHA_TNT_MUERTE
                )
        );
    }

    private static void crearTntFulminante(
            ServerLevel level,
            double x,
            double y,
            double z,
            LivingEntity propietario,
            int mecha
    ) {

        PrimedTnt tnt =
                new PrimedTnt(
                        level,
                        x,
                        y,
                        z,
                        propietario
                );

        tnt.setFuse(
                mecha
        );

        tnt
                .getPersistentData()
                .putBoolean(
                        TAG_TNT_FULMINANTE,
                        true
                );

        level.addFreshEntity(
                tnt
        );
    }

    @SubscribeEvent
    public static void onFulminantTntDamage(
            LivingIncomingDamageEvent event
    ) {

        Entity entidadDirecta =
                event.getSource()
                        .getDirectEntity();

        if (!(entidadDirecta
                instanceof PrimedTnt tnt)) {

            return;
        }

        if (!tnt
                .getPersistentData()
                .contains(
                        TAG_TNT_FULMINANTE
                )) {

            return;
        }

        float danoOriginal =
                event.getAmount();

        event
                .getContainer()
                .setNewDamage(
                        danoOriginal
                                * MULTIPLICADOR_DANO_TNT
                );
    }

    @SubscribeEvent
    public static void onFulminantShulkerDrops(
            LivingDropsEvent event
    ) {

        LivingEntity shulker =
                event.getEntity();

        if (shulker.getType()
                != EntityTypes.SHULKER) {

            return;
        }

        if (!esShulkerFulminante(shulker)) {
            return;
        }

        if (!(shulker.level()
                instanceof ServerLevel level)) {

            return;
        }

        boolean yaTieneCaparazon =
                false;

        for (ItemEntity itemEntity :
                event.getDrops()) {

            if (!itemEntity
                    .getItem()
                    .is(
                            Items.SHULKER_SHELL
                    )) {

                continue;
            }

            yaTieneCaparazon =
                    true;

            protegerCaparazon(
                    itemEntity
            );
        }

        if (yaTieneCaparazon) {
            return;
        }

        ItemEntity caparazon =
                new ItemEntity(
                        level,
                        shulker.getX(),
                        shulker.getY(),
                        shulker.getZ(),
                        new ItemStack(
                                Items.SHULKER_SHELL
                        )
                );

        protegerCaparazon(
                caparazon
        );

        event.getDrops().add(
                caparazon
        );
    }

    public static boolean esShulkerFulminante(
            LivingEntity entity
    ) {

        return entity.getType()
                == EntityTypes.SHULKER

                && entity
                .getPersistentData()
                .contains(
                        TAG_SHULKER_FULMINANTE
                );
    }

    public static void crearTntFulminanteDeAtaque(
            ServerLevel level,
            double x,
            double y,
            double z,
            LivingEntity propietario
    ) {

        crearTntFulminante(
                level,
                x,
                y,
                z,
                propietario,
                50
        );
    }

    private FulminantShulkerEvents() {
    }
}