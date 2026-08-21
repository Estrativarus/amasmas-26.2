package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class WingedDemonEvents {

    public static final String TAG_DEMONIO_ALADO =
            "amasmas_demonio_alado";

    public static final String TAG_MODO_CABREO =
            "amasmas_demonio_alado_modo_cabreo";

    private static final String TAG_CONFIGURADO =
            "amasmas_demonio_alado_configurado";

    private static final int DIA_INICIO =
            14;

    private static final double VIDA_MAXIMA =
            1600.0D;

    private static final float UMBRAL_MODO_CABREO =
            600.0F;

    private static final int INTERVALO_COMPROBACION =
            10;

    @SubscribeEvent
    public static void onDragonJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof EnderDragon dragon)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        level.getServer().execute(() -> {

            if (!dragon.isAlive()
                    || dragon.isRemoved()) {

                return;
            }

            configurarDemonioAlado(
                    dragon
            );
        });
    }

    @SubscribeEvent
    public static void onDragonTick(
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

        if ((dragon.tickCount + dragon.getId())
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

        configurarDemonioAlado(
                dragon
        );

        comprobarModoCabreo(
                level,
                dragon
        );
    }

    private static void configurarDemonioAlado(
            EnderDragon dragon
    ) {

        dragon
                .getPersistentData()
                .putBoolean(
                        TAG_DEMONIO_ALADO,
                        true
                );

        dragon.setCustomName(
                Component.literal(
                        "Demonio Alado"
                ).withStyle(
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.BOLD
                )
        );

        dragon.setCustomNameVisible(
                false
        );

        AttributeInstance atributoVida =
                dragon.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        boolean configuradoAnteriormente =
                dragon
                        .getPersistentData()
                        .contains(
                                TAG_CONFIGURADO
                        );

        if (!configuradoAnteriormente) {

            atributoVida.setBaseValue(
                    VIDA_MAXIMA
            );

            dragon.setHealth(
                    (float) VIDA_MAXIMA
            );

            dragon
                    .getPersistentData()
                    .putBoolean(
                            TAG_CONFIGURADO,
                            true
                    );

            return;
        }

        if (atributoVida.getBaseValue()
                != VIDA_MAXIMA) {

            float vidaActual =
                    Math.min(
                            dragon.getHealth(),
                            (float) VIDA_MAXIMA
                    );

            atributoVida.setBaseValue(
                    VIDA_MAXIMA
            );

            dragon.setHealth(
                    vidaActual
            );
        }
    }

    private static void comprobarModoCabreo(
            ServerLevel level,
            EnderDragon dragon
    ) {

        if (!esDemonioAlado(dragon)) {
            return;
        }

        if (estaEnModoCabreo(dragon)) {
            return;
        }

        if (dragon.getHealth()
                > UMBRAL_MODO_CABREO) {

            return;
        }

        activarModoCabreo(
                level,
                dragon
        );
    }

    private static void activarModoCabreo(
            ServerLevel level,
            EnderDragon dragon
    ) {

        dragon
                .getPersistentData()
                .putBoolean(
                        TAG_MODO_CABREO,
                        true
                );

        Component mensaje =
                Component.literal(
                        "El Demonio Alado ha entrado en Modo Cabreau"
                ).withStyle(
                        ChatFormatting.DARK_RED,
                        ChatFormatting.BOLD
                );

        level.getServer()
                .getPlayerList()
                .broadcastSystemMessage(
                        mensaje,
                        false
                );

        level.playSound(
                null,
                dragon.getX(),
                dragon.getY(),
                dragon.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE,
                5.0F,
                0.6F
        );

        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                dragon.getX(),
                dragon.getY() + 2.0D,
                dragon.getZ(),
                150,
                5.0D,
                3.0D,
                5.0D,
                0.15D
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                dragon.getX(),
                dragon.getY() + 2.0D,
                dragon.getZ(),
                100,
                4.0D,
                2.0D,
                4.0D,
                0.1D
        );
    }

    public static boolean esDemonioAlado(
            EnderDragon dragon
    ) {

        return dragon.getType()
                == EntityTypes.ENDER_DRAGON

                && dragon
                .getPersistentData()
                .contains(
                        TAG_DEMONIO_ALADO
                );
    }

    public static boolean estaEnModoCabreo(
            EnderDragon dragon
    ) {

        return esDemonioAlado(dragon)

                && dragon
                .getPersistentData()
                .contains(
                        TAG_MODO_CABREO
                );
    }

    public static float getUmbralModoCabreo() {

        return UMBRAL_MODO_CABREO;
    }

    private WingedDemonEvents() {
    }
}