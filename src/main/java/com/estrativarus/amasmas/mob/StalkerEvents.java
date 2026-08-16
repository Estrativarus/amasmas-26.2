package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.estrativarus.amasmas.specialbook.SpecialEnchantedBooks;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class StalkerEvents {

    /*
     * Etiqueta persistente que identifica a los Stalkers.
     *
     * Las scoreboard tags se guardan con la entidad.
     */
    public static final String TAG_STALKER =
            "amasmas_stalker";

    /*
     * Etiqueta administrativa para convertir manualmente
     * un Creaking en Stalker durante las pruebas.
     */
    public static final String TAG_FORZAR_STALKER =
            "amasmas_forzar_stalker";

    /*
     * Evita generar más de una vez el libro especial.
     */
    private static final String TAG_RECOMPENSA_PROCESADA =
            "amasmas_stalker_recompensa_procesada";

    /*
     * Uno de cada cinco Creakings será Stalker.
     *
     * 1 / 5 = 20 %.
     */
    private static final int PROBABILIDAD_STALKER =
            5;
    private static final float PROBABILIDAD_LIBRO =
            1F;
    /*
     * Daño base del Stalker.
     *
     * 20 puntos de daño equivalen a diez corazones
     * antes de aplicar armadura, efectos y reducciones.
     */
    private static final double DANO_STALKER =
            20.0D;

    /*
     * Nivel del libro especial que soltará.
     */
    private static final int NIVEL_EFICIENCIA =
            7;

    /*
     * Cantidad de partículas generadas en cada actualización.
     */
    private static final int CANTIDAD_PARTICULAS =
            4;

    /*
     * Cada cuántos ticks mostramos partículas.
     *
     * 4 ticks = cinco actualizaciones por segundo.
     */
    private static final int INTERVALO_PARTICULAS =
            4;

    /*
     * Cada cuántos ticks reforzamos el atributo de daño.
     *
     * 20 ticks = una vez por segundo.
     */
    private static final int INTERVALO_ATRIBUTO =
            20;

    /*
     * Se ejecuta cuando una entidad entra en un nivel.
     *
     * Aquí realizamos la tirada de uno entre cinco para
     * los Creakings nuevos.
     */
    @SubscribeEvent
    public static void onCreakingJoinLevel(
            EntityJoinLevelEvent event
    ) {

        /*
         * Solo trabajamos en el servidor.
         */
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Solo procesamos entidades vivas.
         */
        if (!(event.getEntity() instanceof LivingEntity creaking)) {
            return;
        }

        /*
         * Solo procesamos Creakings.
         */
        if (creaking.getType() != EntityTypes.CREAKING) {
            return;
        }

        /*
         * Si ya es Stalker, simplemente restauramos su daño.
         *
         * Esto permite que siga funcionando después de cerrar
         * y volver a abrir el mundo.
         */
        if (esStalker(creaking)) {

            level.getServer().execute(() -> {

                if (creaking.isAlive()
                        && !creaking.isRemoved()) {

                    configurarStalker(creaking);
                }
            });

            return;
        }

        /*
         * No repetimos la tirada aleatoria cuando un Creaking
         * antiguo vuelve a cargarse desde el disco.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        /*
         * Antes del día 7 no aparecen Stalkers.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * Uno de cada cinco Creakings.
         */
        boolean debeSerStalker =
                creaking
                        .getRandom()
                        .nextInt(PROBABILIDAD_STALKER)
                        == 0;

        if (!debeSerStalker) {
            return;
        }

        /*
         * Esperamos hasta el siguiente ciclo del servidor
         * para que Minecraft termine de inicializar el Creaking.
         */
        level.getServer().execute(() -> {

            if (!creaking.isAlive()
                    || creaking.isRemoved()) {
                return;
            }

            convertirEnStalker(creaking);
        });
    }

    /*
     * Controla:
     *
     * - conversiones administrativas;
     * - partículas rojas;
     * - mantenimiento del daño base.
     */
    @SubscribeEvent
    public static void onStalkerTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity() instanceof LivingEntity creaking)) {
            return;
        }

        if (creaking.getType() != EntityTypes.CREAKING) {
            return;
        }

        if (!(creaking.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Herramienta administrativa:
         *
         * si el Creaking contiene esta etiqueta, se convierte
         * inmediatamente en Stalker.
         */
        /*
         * Herramienta administrativa:
         *
         * Si el Creaking se llama exactamente "Forzar Stalker",
         * lo convertimos inmediatamente.
         */
        if (creaking.hasCustomName()
                && creaking.getCustomName() != null
                && creaking
                .getCustomName()
                .getString()
                .equals("Forzar Stalker")) {

            convertirEnStalker(creaking);

            return;
        }

        /*
         * Los Creakings normales no necesitan ejecutar
         * ninguna lógica adicional.
         */
        if (!esStalker(creaking)) {
            return;
        }

        /*
         * Mantenemos el daño base una vez por segundo.
         *
         * Esto evita que otra mecánica o una recarga del mundo
         * deje al Stalker con el daño vanilla.
         */
        if (creaking.tickCount % INTERVALO_ATRIBUTO == 0) {
            establecerDanoStalker(creaking);
        }

        /*
         * Mostramos las partículas rojas cinco veces
         * por segundo.
         */
        if (creaking.tickCount % INTERVALO_PARTICULAS == 0) {

            mostrarParticulasRojas(
                    level,
                    creaking
            );
        }
    }

    /*
     * Convierte un Creaking normal en Stalker.
     */
    public static void convertirEnStalker(
            LivingEntity creaking
    ) {

        /*
         * Evitamos repetir la conversión.
         */
        if (esStalker(creaking)) {
            configurarStalker(creaking);
            return;
        }

        creaking.addTag(TAG_STALKER);

        configurarStalker(creaking);
    }

    /*
     * Aplica las propiedades que deben mantenerse
     * durante toda la vida del Stalker.
     */
    private static void configurarStalker(
            LivingEntity stalker
    ) {

        establecerDanoStalker(stalker);

        /*
         * Nombre visible para identificarlo.
         */
        stalker.setCustomName(
                Component.literal("Stalker")
                        .withStyle(
                                ChatFormatting.DARK_RED,
                                ChatFormatting.BOLD
                        )
        );

        stalker.setCustomNameVisible(true);

        /*
         * El Stalker es una criatura especial y rara.
         * Evitamos que desaparezca por distancia.
         */
        if (stalker instanceof net.minecraft.world.entity.Mob mob) {
            mob.setPersistenceRequired();
        }
    }

    /*
     * Establece veinte puntos de daño base.
     */
    private static void establecerDanoStalker(
            LivingEntity stalker
    ) {

        AttributeInstance atributoDano =
                stalker.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        /*
         * Algunos tipos de entidades podrían no tener
         * este atributo. En ese caso evitamos un NullPointer.
         */
        if (atributoDano == null) {
            return;
        }

        atributoDano.setBaseValue(
                DANO_STALKER
        );
    }

    /*
     * Genera partículas de indicador de daño.
     *
     * Estas partículas tienen un aspecto rojizo y no requieren
     * parámetros adicionales de color, evitando problemas de
     * compatibilidad entre mappings.
     */
    private static void mostrarParticulasRojas(
            ServerLevel level,
            LivingEntity stalker
    ) {

        level.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,

                /*
                 * Posición central de las partículas.
                 */
                stalker.getX(),
                stalker.getY()
                        + stalker.getBbHeight() * 0.55D,
                stalker.getZ(),

                /*
                 * Cantidad.
                 */
                CANTIDAD_PARTICULAS,

                /*
                 * Dispersión horizontal y vertical.
                 */
                stalker.getBbWidth() * 0.6D,
                stalker.getBbHeight() * 0.45D,
                stalker.getBbWidth() * 0.6D,

                /*
                 * Velocidad de las partículas.
                 */
                0.02D
        );
    }

    /*
     * Añade el libro de Eficiencia VII al morir.
     *
     * No cancelamos el botín vanilla del Creaking.
     */
    @SubscribeEvent
    public static void onStalkerDamaged(
            LivingDamageEvent.Post event
    ) {

        LivingEntity stalker =
                event.getEntity();

        if (stalker.getType()
                != EntityTypes.CREAKING) {

            return;
        }

        if (!esStalker(stalker)) {
            return;
        }

        if (recompensaProcesada(stalker)) {
            return;
        }

        if (stalker.getHealth() > 0.0F) {
            return;
        }

        if (!(stalker.level()
                instanceof ServerLevel level)) {

            return;
        }

        marcarRecompensaProcesada(stalker);

        if (stalker
                .getRandom()
                .nextFloat()
                >= PROBABILIDAD_LIBRO) {

            return;
        }

        ItemStack libro =
                SpecialEnchantedBooks
                        .crearLibroEficiencia(
                                level
                        );

        ItemEntity libroDrop =
                new ItemEntity(
                        level,
                        stalker.getX(),
                        stalker.getY(),
                        stalker.getZ(),
                        libro
                );

        level.addFreshEntity(
                libroDrop
        );
    }

    /*
     * Comprueba si una entidad es un Stalker.
     */
    public static boolean esStalker(
            LivingEntity entity
    ) {

        return entity
                .getPersistentData()
                .contains(
                        TAG_STALKER
                );
    }

    /*
     * Comprueba si el libro ya fue generado.
     */
    private static boolean recompensaProcesada(
            LivingEntity stalker
    ) {

        return stalker
                .getPersistentData()
                .contains(
                        TAG_RECOMPENSA_PROCESADA
                );
    }

    /*
     * Marca la recompensa como procesada.
     */
    private static void marcarRecompensaProcesada(
            LivingEntity stalker
    ) {

        stalker
                .getPersistentData()
                .putBoolean(
                        TAG_RECOMPENSA_PROCESADA,
                        true
                );
    }

    /*
     * Crea un libro encantado con Eficiencia VII.
     */


    private StalkerEvents() {
    }
}