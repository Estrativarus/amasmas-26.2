package com.estrativarus.amasmas.event;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.ResonantiteArrowItem;
import com.estrativarus.amasmas.mixin.ServerLevelGameEventMixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ResonantiteArrowImpactEvents {

    @SubscribeEvent
    public static void onProjectileImpact(
            ProjectileImpactEvent event
    ) {

        Projectile projectile =
                event.getProjectile();

        if (!projectile
                .getPersistentData()
                .contains(
                        ResonantiteArrowItem
                                .TAG_FLECHA_RESONANTITA
                )) {

            return;
        }

        if (!(projectile.level()
                instanceof ServerLevel level)) {

            return;
        }

        HitResult hitResult =
                event.getRayTraceResult();

        if (!(hitResult
                instanceof EntityHitResult entityHitResult)) {

            return;
        }

        Entity objetivo =
                entityHitResult.getEntity();

        objetivo
                .getPersistentData()
                .putBoolean(
                        ResonantiteArrowItem
                                .TAG_IMPACTO_RESONANTITA,
                        true
                );

        level.getServer().execute(() -> {

            if (objetivo.isRemoved()) {
                return;
            }

            objetivo
                    .getPersistentData()
                    .remove(
                            ResonantiteArrowItem
                                    .TAG_IMPACTO_RESONANTITA

                    );
        });
    }

    private ResonantiteArrowImpactEvents() {
    }
}