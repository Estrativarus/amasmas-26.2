package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.mixin.MobGoalSelectorAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.PathfinderMob;

import java.util.Set;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class PassiveMobAggressionEvents {

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            40;

    private static final String TAG_IA_AGRESIVA =
            "amasmas_pasivo_agresivo";

    private static final Set<EntityType<?>>
            TIPOS_PASIVOS =
            Set.of(
                    EntityTypes.COW,
                    EntityTypes.MOOSHROOM,
                    EntityTypes.PIG,
                    EntityTypes.SHEEP,
                    EntityTypes.CHICKEN,
                    EntityTypes.RABBIT,
                    EntityTypes.HORSE,
                    EntityTypes.DONKEY,
                    EntityTypes.MULE,
                    EntityTypes.CAMEL,
                    EntityTypes.CAT,
                    EntityTypes.OCELOT,
                    EntityTypes.PARROT,
                    EntityTypes.TURTLE,
                    EntityTypes.ARMADILLO,
                    EntityTypes.SNIFFER
            );

    @SubscribeEvent
    public static void onPassiveMobJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof PathfinderMob mob)) {

            return;
        }

        if (!esPasivoAfectado(mob)) {
            return;
        }

        level.getServer().execute(() -> {

            if (!mob.isAlive()
                    || mob.isRemoved()) {

                return;
            }

            aplicarAgresividadSiCorresponde(
                    level,
                    mob
            );
        });
    }

    @SubscribeEvent
    public static void onPassiveMobTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof PathfinderMob mob)) {

            return;
        }

        if (!esPasivoAfectado(mob)) {
            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((mob.tickCount + mob.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        aplicarAgresividadSiCorresponde(
                level,
                mob
        );
    }

    private static void aplicarAgresividadSiCorresponde(
            ServerLevel level,
            PathfinderMob mob
    ) {

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {

            mob.setTarget(
                    null
            );

            return;
        }

        if (mob
                .getPersistentData()
                .contains(
                        TAG_IA_AGRESIVA
                )) {

            return;
        }

        MobGoalSelectorAccessor accessor =
                (MobGoalSelectorAccessor)
                        (Object) mob;

        accessor
                .amasmas$getGoalSelector()
                .addGoal(
                        1,
                        new MeleeAttackGoal(
                                mob,
                                1.25D,
                                true
                        )
                );

        accessor
                .amasmas$getTargetSelector()
                .addGoal(
                        1,
                        new NearestAttackableTargetGoal<>(
                                mob,
                                Player.class,
                                true
                        )
                );

        mob
                .getPersistentData()
                .putBoolean(
                        TAG_IA_AGRESIVA,
                        true
                );
    }

    private static boolean esPasivoAfectado(
            PathfinderMob mob
    ) {

        return TIPOS_PASIVOS.contains(
                mob.getType()
        );
    }

    private PassiveMobAggressionEvents() {
    }
}