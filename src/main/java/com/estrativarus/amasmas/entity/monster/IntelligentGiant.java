package com.estrativarus.amasmas.entity.monster;

import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class IntelligentGiant
        extends Giant {

    private static final double VIDA_MAXIMA =
            600.0D;

    private static final double DANO_ATAQUE =
            1000.0D;

    private static final double VELOCIDAD =
            0.23D;

    private static final double DISTANCIA_SEGUIMIENTO =
            64.0D;

    public IntelligentGiant(
            EntityType<? extends Giant> entityType,
            Level level
    ) {

        super(
                entityType,
                level
        );

        this.setPersistenceRequired();
        this.xpReward =
                50;
    }

    public static AttributeSupplier.Builder crearAtributos() {

        return Giant
                .createAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        VIDA_MAXIMA
                )
                .add(
                        Attributes.ATTACK_DAMAGE,
                        DANO_ATAQUE
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        VELOCIDAD
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        DISTANCIA_SEGUIMIENTO
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.85D
                );
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                0,
                new FloatGoal(
                        this
                )
        );

        this.goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.0D,
                        false
                )
        );

        this.goalSelector.addGoal(
                5,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        0.8D
                )
        );

        this.goalSelector.addGoal(
                7,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        32.0F
                )
        );

        this.goalSelector.addGoal(
                8,
                new RandomLookAroundGoal(
                        this
                )
        );

        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(
                        this
                )
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
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

        this.spawnAtLocation(
                level,
                new ItemStack(
                        ModItems.ROPAJE_GIGANTE.get()
                )
        );
    }
}