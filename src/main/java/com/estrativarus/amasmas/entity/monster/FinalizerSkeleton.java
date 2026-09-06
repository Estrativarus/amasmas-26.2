package com.estrativarus.amasmas.entity.monster;

import com.estrativarus.amasmas.entity.ModEntities;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public final class FinalizerSkeleton extends Bogged {

    private static final double VIDA_MAXIMA =
            200.0D;

    private static final int NIVEL_PODER =
            200;

    private static final float PROBABILIDAD_FRAGMENTO =
            0.50F;

    private static final String TAG_EQUIPO_APLICADO =
            "amasmas_finalizador_equipo_aplicado";

    public FinalizerSkeleton(
            EntityType<? extends Bogged> entityType,
            Level level
    ) {

        super(
                entityType,
                level
        );

        setCustomName(
                Component.literal(
                        "Esqueleto Finalizador"
                ).withStyle(
                        ChatFormatting.GRAY,
                        ChatFormatting.BOLD
                )
        );

        setCustomNameVisible(
                false
        );

        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder crearAtributos() {

        return Mob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        VIDA_MAXIMA
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.25D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        48.0D
                )
                .add(
                        Attributes.ATTACK_DAMAGE,
                        2.0D
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.15D
                );
    }

    public void aplicarEquipoInicial(
            ServerLevel level
    ) {

        if (getPersistentData()
                .contains(
                        TAG_EQUIPO_APLICADO
                )) {

            setDropChance(
                    EquipmentSlot.MAINHAND,
                    0.0F
            );

            return;
        }

        ItemStack arco =
                new ItemStack(
                        ModItems.ARCO_RESONANTITA.get()
                );

        anadirEncantamiento(
                level,
                arco,
                Enchantments.POWER,
                NIVEL_PODER
        );

        setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );

        reassessWeaponGoal();

        setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );

        setHealth(
                getMaxHealth()
        );

        setPersistenceRequired();

        getPersistentData()
                .putBoolean(
                        TAG_EQUIPO_APLICADO,
                        true
                );
    }

    private static void anadirEncantamiento(
            ServerLevel level,
            ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey,
            int nivel
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> holder =
                registro.getOrThrow(
                        enchantmentKey
                );

        stack.enchant(
                holder,
                nivel
        );
    }

    @Override
    public boolean isInvulnerableTo(
            ServerLevel level,
            DamageSource source
    ) {

        if (source.is(
                DamageTypeTags.IS_PROJECTILE
        )) {

            return true;
        }

        return super.isInvulnerableTo(
                level,
                source
        );
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level,
            DamageSource source,
            boolean recentlyHit
    ) {

        super.dropCustomDeathLoot(
                level,
                source,
                recentlyHit
        );

        if (getRandom().nextFloat()
                >= PROBABILIDAD_FRAGMENTO) {

            return;
        }

        ItemEntity fragmento =
                new ItemEntity(
                        level,
                        getX(),
                        getY(),
                        getZ(),
                        new ItemStack(
                                ModItems.FRAGMENTO_RESONANTITA.get()
                        )
                );

        level.addFreshEntity(
                fragmento
        );
    }
}