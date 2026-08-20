package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class GhastDay14Events {

    private static final int DIA_INICIO =
            14;

    private static final double VIDA_MAXIMA =
            240.0D;

    private static final int INTERVALO_COMPROBACION =
            100;

    @SubscribeEvent
    public static void onGhastTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob ghast)) {

            return;
        }

        if (ghast.getType()
                != EntityTypes.GHAST) {

            return;
        }

        if (!(ghast.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((ghast.tickCount + ghast.getId())
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

        aplicarVidaDia14(
                ghast
        );
    }

    @SubscribeEvent
    public static void onEndCrystalExplosion(
            ExplosionEvent.Start event
    ) {

        if (!(event.getLevel()
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

        Entity origenExplosion =
                event.getExplosion()
                        .getDirectSourceEntity();

        if (origenExplosion == null
                || origenExplosion.getType()
                != EntityTypes.END_CRYSTAL) {

            return;
        }

        double x =
                origenExplosion.getX();

        double y =
                origenExplosion.getY();

        double z =
                origenExplosion.getZ();

        level.getServer().execute(() ->
                crearGhast(
                        level,
                        x,
                        y,
                        z
                )
        );
    }

    private static void crearGhast(
            ServerLevel level,
            double x,
            double y,
            double z
    ) {

        Mob ghast =
                EntityTypes.GHAST.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (ghast == null) {
            return;
        }

        ghast.setPos(
                x,
                y + 2.0D,
                z
        );

        aplicarVidaDia14(
                ghast
        );

        ghast.setPersistenceRequired();

        level.addFreshEntity(
                ghast
        );
    }

    private static void aplicarVidaDia14(
            Mob ghast
    ) {

        AttributeInstance atributoVida =
                ghast.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        if (atributoVida.getBaseValue()
                >= VIDA_MAXIMA) {

            return;
        }

        double vidaMaximaAnterior =
                atributoVida.getBaseValue();

        float vidaAnterior =
                ghast.getHealth();

        float proporcionVida;

        if (vidaMaximaAnterior <= 0.0D) {

            proporcionVida =
                    1.0F;

        } else {

            proporcionVida =
                    (float) (
                            vidaAnterior
                                    / vidaMaximaAnterior
                    );
        }

        proporcionVida =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                proporcionVida
                        )
                );

        atributoVida.setBaseValue(
                VIDA_MAXIMA
        );

        ghast.setHealth(
                (float) VIDA_MAXIMA
                        * proporcionVida
        );
    }

    private GhastDay14Events() {
    }
}