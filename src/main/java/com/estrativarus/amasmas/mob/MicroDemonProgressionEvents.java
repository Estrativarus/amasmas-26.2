package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class MicroDemonProgressionEvents {

    private static final int DIA_INICIO =
            14;

    private static final String TAG_TRANSFORMACION_PENDIENTE =
            "amasmas_phantom_transformacion_vex_pendiente";

    private static final String TAG_MICRO_DEMONIO =
            "amasmas_micro_demonio";

    private static final String TAG_EQUIPO_APLICADO =
            "amasmas_micro_demonio_equipo_aplicado";

    private static final int DURACION_EFECTOS =
            20 * 15;

    private static final int AMPLIFICADOR_RESISTENCIA =
            2;

    private static final int AMPLIFICADOR_FUERZA =
            2;

    private static final int NIVEL_FILO =
            20;

    private static final int INTERVALO_ACTUALIZACION =
            20;

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (mob.getType() != EntityTypes.PHANTOM
                && mob.getType() != EntityTypes.VEX) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (mob.getType() == EntityTypes.PHANTOM) {

            programarTransformacion(
                    level,
                    mob
            );

            return;
        }

        convertirEnMicroDemonio(
                level,
                mob,
                diaActual
        );
    }

    @SubscribeEvent
    public static void onPhantomOrVexTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob mob)) {

            return;
        }

        if (mob.getType() != EntityTypes.PHANTOM
                && mob.getType() != EntityTypes.VEX) {

            return;
        }

        if (!(mob.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((mob.tickCount + mob.getId())
                % INTERVALO_ACTUALIZACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        if (mob.getType() == EntityTypes.PHANTOM) {

            programarTransformacion(
                    level,
                    mob
            );

            return;
        }

        convertirEnMicroDemonio(
                level,
                mob,
                diaActual
        );
    }

    private static void programarTransformacion(
            ServerLevel level,
            Mob phantom
    ) {

        if (phantom
                .getPersistentData()
                .contains(
                        TAG_TRANSFORMACION_PENDIENTE
                )) {

            return;
        }

        phantom
                .getPersistentData()
                .putBoolean(
                        TAG_TRANSFORMACION_PENDIENTE,
                        true
                );

        double x =
                phantom.getX();

        double y =
                phantom.getY();

        double z =
                phantom.getZ();

        float rotacionHorizontal =
                phantom.getYRot();

        float rotacionVertical =
                phantom.getXRot();

        boolean eraPersistente =
                phantom.isPersistenceRequired();

        level.getServer().execute(() -> {

            if (!phantom.isAlive()
                    || phantom.isRemoved()) {

                return;
            }

            Mob vex =
                    EntityTypes.VEX.create(
                            level,
                            EntitySpawnReason.CONVERSION
                    );

            if (vex == null) {

                phantom
                        .getPersistentData()
                        .remove(
                                TAG_TRANSFORMACION_PENDIENTE
                        );

                return;
            }

            vex.setPos(
                    x,
                    y,
                    z
            );

            vex.setYRot(
                    rotacionHorizontal
            );

            vex.setXRot(
                    rotacionVertical
            );

            if (eraPersistente) {
                vex.setPersistenceRequired();
            }

            convertirEnMicroDemonio(
                    level,
                    vex,
                    DIA_INICIO
            );

            boolean anadido =
                    level.addFreshEntity(
                            vex
                    );

            if (!anadido) {

                phantom
                        .getPersistentData()
                        .remove(
                                TAG_TRANSFORMACION_PENDIENTE
                        );

                return;
            }

            phantom.discard();
        });
    }

    private static void convertirEnMicroDemonio(
            ServerLevel level,
            Mob vex,
            int diaActual
    ) {

        vex
                .getPersistentData()
                .putBoolean(
                        TAG_MICRO_DEMONIO,
                        true
                );

        aplicarNombre(
                vex
        );

        aplicarEquipoDia14(
                level,
                vex
        );

        aplicarEfectosDia14(
                vex
        );

        if (diaActual >= 21) {

            aplicarEtapaDia21(
                    level,
                    vex
            );
        }

        if (diaActual >= 42) {

            aplicarEtapaDia42(
                    level,
                    vex
            );
        }

        if (diaActual >= 63) {

            aplicarEtapaDia63(
                    level,
                    vex
            );
        }
    }

    private static void aplicarNombre(
            Mob vex
    ) {

        vex.setCustomName(
                Component.literal(
                        "Micro Demonio"
                ).withStyle(
                        ChatFormatting.DARK_RED,
                        ChatFormatting.BOLD
                )
        );

        vex.setCustomNameVisible(false);
    }

    private static void aplicarEquipoDia14(
            ServerLevel level,
            Mob vex
    ) {

        if (vex
                .getPersistentData()
                .contains(
                        TAG_EQUIPO_APLICADO
                )) {

            return;
        }

        ItemStack espada =
                new ItemStack(
                        Items.NETHERITE_SWORD
                );

        anadirEncantamiento(
                level,
                espada,
                Enchantments.SHARPNESS,
                NIVEL_FILO
        );

        vex.setItemSlot(
                EquipmentSlot.MAINHAND,
                espada
        );

        vex.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );

        vex
                .getPersistentData()
                .putBoolean(
                        TAG_EQUIPO_APLICADO,
                        true
                );
    }

    private static void aplicarEfectosDia14(
            Mob vex
    ) {

        renovarEfectoSiNecesario(
                vex,
                MobEffects.RESISTANCE,
                AMPLIFICADOR_RESISTENCIA
        );

        renovarEfectoSiNecesario(
                vex,
                MobEffects.STRENGTH,
                AMPLIFICADOR_FUERZA
        );
    }

    private static void renovarEfectoSiNecesario(
            Mob vex,
            Holder<net.minecraft.world.effect.MobEffect> efecto,
            int amplificador
    ) {

        MobEffectInstance efectoActual =
                vex.getEffect(
                        efecto
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                == amplificador
                && efectoActual.getDuration() > 80) {

            return;
        }

        vex.addEffect(
                new MobEffectInstance(
                        efecto,
                        DURACION_EFECTOS,
                        amplificador,
                        false,
                        false,
                        false
                )
        );
    }

    private static void anadirEncantamiento(
            ServerLevel level,
            ItemStack stack,
            ResourceKey<Enchantment> claveEncantamiento,
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
                        claveEncantamiento
                );

        EnchantmentHelper.updateEnchantments(
                stack,
                encantamientos ->
                        encantamientos.set(
                                holder,
                                nivel
                        )
        );
    }

    private static void aplicarEtapaDia21(
            ServerLevel level,
            Mob vex
    ) {

    }

    private static void aplicarEtapaDia42(
            ServerLevel level,
            Mob vex
    ) {

    }

    private static void aplicarEtapaDia63(
            ServerLevel level,
            Mob vex
    ) {

    }

    public static boolean esMicroDemonio(
            Mob mob
    ) {

        return mob.getType() == EntityTypes.VEX
                && mob
                .getPersistentData()
                .contains(
                        TAG_MICRO_DEMONIO
                );
    }

    private MicroDemonProgressionEvents() {
    }
}