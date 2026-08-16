package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.specialbook.SpecialEnchantedBooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class WiseGuardianEvents {

    /*
     * Marca almacenada en los datos persistentes
     * del Guardián Anciano.
     *
     * El Mixin del siguiente paso utilizará esta misma
     * marca para identificar al Guardián Sabio.
     */
    public static final String TAG_GUARDIAN_SABIO =
            "amasmas_guardian_sabio";

    /*
     * Vida máxima:
     *
     * 100 puntos = 50 corazones.
     */
    private static final double VIDA_MAXIMA =
            100.0D;

    /*
     * Regeneración durante 15 segundos.
     *
     * El efecto se renueva una vez por segundo, así que
     * en la práctica permanece activo continuamente.
     *
     * Evitamos utilizar una duración infinita para que
     * el sistema sea más sencillo de actualizar o retirar
     * en etapas futuras.
     */
    private static final int DURACION_REGENERACION =
            20 * 15;

    /*
     * 0 = Regeneración I.
     */
    private static final int AMPLIFICADOR_REGENERACION =
            0;

    /*
     * 50 % de probabilidad de soltar Respiración V.
     */
    private static final float PROBABILIDAD_LIBRO =
            0.50F;

    /*
     * Detecta Guardianes Ancianos nuevos al entrar
     * en el mundo.
     */
    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity guardian)) {
            return;
        }

        if (guardian.getType()
                != EntityTypes.ELDER_GUARDIAN) {

            return;
        }

        /*
         * No ignoramos loadedFromDisk().
         *
         * También queremos actualizar Guardianes Ancianos
         * antiguos cuando se carguen desde el disco.
         */
        intentarConvertirEnSabio(
                level,
                guardian
        );
    }

    /*
     * Este evento permite detectar también a los Guardianes
     * Ancianos que ya estaban cargados cuando el día pasó a 7.
     */
    @SubscribeEvent
    public static void onGuardianTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof LivingEntity guardian)) {

            return;
        }

        if (guardian.getType()
                != EntityTypes.ELDER_GUARDIAN) {

            return;
        }

        if (!(guardian.level()
                instanceof ServerLevel level)) {

            return;
        }

        /*
         * Una comprobación por segundo.
         */
        if (guardian.tickCount % 20 != 0) {
            return;
        }

        /*
         * Convierte al Guardián si todavía no está marcado
         * y el servidor ya ha llegado al día 7.
         */
        intentarConvertirEnSabio(
                level,
                guardian
        );

        /*
         * Si ya es Guardián Sabio, renovamos continuamente
         * la Regeneración I.
         */
        if (esGuardianSabio(guardian)) {

            aplicarRegeneracion(
                    guardian
            );
        }
    }

    private static void intentarConvertirEnSabio(
            ServerLevel level,
            LivingEntity guardian
    ) {

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Antes del día 7 se mantiene completamente vanilla.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * Si ya está convertido, no volvemos a modificar
         * su salud ni su nombre.
         */
        if (esGuardianSabio(guardian)) {
            return;
        }

        /*
         * Guardamos la marca persistente.
         *
         * Esta información sobrevivirá al cierre del servidor
         * y a la descarga del chunk.
         */
        guardian
                .getPersistentData()
                .putBoolean(
                        TAG_GUARDIAN_SABIO,
                        true
                );

        /*
         * Modificamos el atributo de vida máxima.
         *
         * Los atributos controlan propiedades básicas
         * sincronizadas de las entidades, incluida MAX_HEALTH.
         */
        AttributeInstance atributoVida =
                guardian.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida != null) {

            atributoVida.setBaseValue(
                    VIDA_MAXIMA
            );

            /*
             * Cuando se convierte por primera vez, queda
             * completamente curado a 100 puntos.
             */
            guardian.setHealth(
                    guardian.getMaxHealth()
            );
        }

        /*
         * Nombre visible sobre la entidad.
         */
        guardian.setCustomName(
                Component.literal(
                        "Guardián Sabio"
                ).withStyle(
                        ChatFormatting.AQUA,
                        ChatFormatting.BOLD
                )
        );

        guardian.setCustomNameVisible(true);

        aplicarRegeneracion(guardian);

        /*
         * Partículas para indicar visualmente que la
         * conversión se ha realizado.
         */
        level.sendParticles(
                ParticleTypes.ENCHANT,
                guardian.getX(),
                guardian.getY() + 1.0D,
                guardian.getZ(),
                40,
                1.0D,
                1.0D,
                1.0D,
                0.15D
        );
    }

    private static void aplicarRegeneracion(
            LivingEntity guardian
    ) {

        guardian.addEffect(
                new MobEffectInstance(
                        MobEffects.REGENERATION,
                        DURACION_REGENERACION,
                        AMPLIFICADOR_REGENERACION,
                        false,
                        false,
                        false
                )
        );
    }

    /*
     * Comprueba la marca persistente.
     *
     * Este método es público porque el Mixin del punto 3
     * también necesitará comprobar si el Guardián es Sabio.
     */
    public static boolean esGuardianSabio(
            LivingEntity entity
    ) {

        return entity
                .getPersistentData()
                .contains(
                        TAG_GUARDIAN_SABIO
                );
    }

    /*
     * Añade el libro de Respiración V con una
     * probabilidad del 50 %.
     */
    @SubscribeEvent
    public static void onGuardianDrops(
            LivingDropsEvent event
    ) {

        LivingEntity guardian =
                event.getEntity();

        if (guardian.getType()
                != EntityTypes.ELDER_GUARDIAN) {

            return;
        }

        /*
         * Solo los Guardianes convertidos en Sabios
         * pueden soltar el libro especial.
         */
        if (!esGuardianSabio(guardian)) {
            return;
        }

        if (!(guardian.level()
                instanceof ServerLevel level)) {

            return;
        }

        /*
         * Tirada independiente del 50 %.
         */
        if (guardian.getRandom().nextFloat()
                >= PROBABILIDAD_LIBRO) {

            return;
        }

        ItemStack libro =
                SpecialEnchantedBooks
                        .crearLibroRespiracion(
                                level
                        );

        /*
         * Creamos el objeto exactamente en la posición
         * donde murió el Guardián Sabio.
         */
        ItemEntity libroDrop =
                new ItemEntity(
                        level,
                        guardian.getX(),
                        guardian.getY(),
                        guardian.getZ(),
                        libro
                );

        /*
         * LivingDropsEvent proporciona la colección de
         * ItemEntity generados por la muerte. Añadimos
         * nuestro libro sin cancelar los drops vanilla.
         */
        event.getDrops().add(
                libroDrop
        );
    }

    private WiseGuardianEvents() {
    }
}
