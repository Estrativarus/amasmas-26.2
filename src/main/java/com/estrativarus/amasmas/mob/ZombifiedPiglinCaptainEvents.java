package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.enchantment.ModEnchantments;
import com.estrativarus.amasmas.specialbook.SpecialEnchantedBooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class ZombifiedPiglinCaptainEvents {

    /*
     * Marca persistente utilizada para identificar
     * al Capitán Piglin Zombificado.
     */
    public static final String TAG_CAPITAN =
            "amasmas_capitan_piglin_zombificado";

    /*
     * Marca que indica que este Piglin ya participó
     * en la tirada de Capitán.
     *
     * De este modo nunca podrá repetir la tirada al
     * descargar y volver a cargar el chunk.
     */
    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_capitan_realizada";

    /*
     * Uno de cada 1000:
     *
     * 1 / 1000 = 0,001 = 0,1 %
     */
    private static final int PROBABILIDAD_CAPITAN =
            200;

    /*
     * Fuerza II:
     *
     * amplificador 0 = Fuerza I
     * amplificador 1 = Fuerza II
     */
    private static final int AMPLIFICADOR_FUERZA =
            1;

    /*
     * El efecto dura 15 segundos y se renueva
     * una vez por segundo.
     */
    private static final int DURACION_FUERZA =
            20 * 15;

    /*
     * Detecta exclusivamente Piglins zombificados nuevos.
     */
    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof Mob piglin)) {
            return;
        }

        if (piglin.getType()
                != EntityTypes.ZOMBIFIED_PIGLIN) {

            return;
        }

        /*
         * No realizamos la tirada sobre Piglins antiguos
         * que vuelven a cargarse desde el disco.
         *
         * Los Capitanes que ya estén marcados conservarán
         * sus propiedades gracias al evento de tick.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        /*
         * Esperamos al siguiente ciclo del servidor para
         * permitir que Minecraft termine de configurar
         * el equipamiento vanilla de la entidad.
         */
        level.getServer().execute(() -> {

            if (!piglin.isAlive()
                    || piglin.isRemoved()) {

                return;
            }

            intentarConvertirEnCapitan(
                    level,
                    piglin
            );
        });
    }

    private static void intentarConvertirEnCapitan(
            ServerLevel level,
            Mob piglin
    ) {

        /*
         * Evitamos repetir la tirada.
         */
        if (piglin
                .getPersistentData()
                .contains(TAG_TIRADA_REALIZADA)) {

            return;
        }

        /*
         * Marcamos la tirada antes de continuar.
         */
        piglin
                .getPersistentData()
                .putBoolean(
                        TAG_TIRADA_REALIZADA,
                        true
                );

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Antes del día 7 no existen Capitanes.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * nextInt(1000) genera un número entre
         * 0 y 999.
         *
         * Solo el resultado 0 produce un Capitán.
         */
        if (piglin.getRandom().nextInt(
                PROBABILIDAD_CAPITAN
        ) != 0) {

            return;
        }

        convertirEnCapitan(
                level,
                piglin
        );
    }

    private static void convertirEnCapitan(
            ServerLevel level,
            Mob piglin
    ) {

        /*
         * Marca persistente de la variante.
         */
        piglin
                .getPersistentData()
                .putBoolean(
                        TAG_CAPITAN,
                        true
                );

        /*
         * Nombre visible.
         */
        SpecialMobNames.asignar(
                piglin,
                "Capitán Piglin Zombificado",
                ChatFormatting.DARK_RED
        );

        /*
         * Armadura completa de netherita
         * con Protección I.
         */
        equiparEncantado(
                level,
                piglin,
                EquipmentSlot.HEAD,
                new ItemStack(
                        Items.NETHERITE_HELMET
                ),
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                piglin,
                EquipmentSlot.CHEST,
                new ItemStack(
                        Items.NETHERITE_CHESTPLATE
                ),
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                piglin,
                EquipmentSlot.LEGS,
                new ItemStack(
                        Items.NETHERITE_LEGGINGS
                ),
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                piglin,
                EquipmentSlot.FEET,
                new ItemStack(
                        Items.NETHERITE_BOOTS
                ),
                Enchantments.PROTECTION,
                1
        );

        /*
         * Espada de netherita.
         */
        ItemStack espada =
                new ItemStack(
                        Items.NETHERITE_SWORD
                );

        /*
         * Filo III.
         */
        anadirEncantamiento(
                level,
                espada,
                Enchantments.SHARPNESS,
                3
        );

        /*
         * Drenaje III.
         */
        anadirEncantamiento(
                level,
                espada,
                ModEnchantments.DRENAJE,
                3
        );

        piglin.setItemSlot(
                EquipmentSlot.MAINHAND,
                espada
        );

        impedirDropsEquipamiento(
                piglin
        );

        aplicarFuerza(piglin);
    }

    /*
     * Equipa un objeto con un encantamiento.
     */
    private static void equiparEncantado(
            ServerLevel level,
            Mob piglin,
            EquipmentSlot slot,
            ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey,
            int nivel
    ) {

        anadirEncantamiento(
                level,
                stack,
                enchantmentKey,
                nivel
        );

        piglin.setItemSlot(
                slot,
                stack
        );
    }

    /*
     * Añade un encantamiento a un ItemStack.
     *
     * Se usa tanto para encantamientos vanilla como
     * para nuestro encantamiento Drenaje.
     */
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

        /*
         * Buscamos el encantamiento dentro del registro.
         *
         * Si el recurso no estuviera cargado, simplemente
         * no añadimos ese encantamiento.
         */
        registro.get(enchantmentKey)
                .ifPresent(holder ->

                        EnchantmentHelper
                                .updateEnchantments(
                                        stack,
                                        encantamientos ->
                                                encantamientos.set(
                                                        holder,
                                                        nivel
                                                )
                                )
                );
    }

    /*
     * Impide que caigan:
     *
     * - casco;
     * - pechera;
     * - pantalones;
     * - botas;
     * - espada.
     *
     * Los drops vanilla de carne podrida y oro no
     * se modifican.
     */
    private static void impedirDropsEquipamiento(
            Mob piglin
    ) {

        piglin.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        piglin.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        piglin.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        piglin.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );

        piglin.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    /*
     * Renueva Fuerza II permanentemente.
     */
    @SubscribeEvent
    public static void onCaptainTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof LivingEntity piglin)) {

            return;
        }

        if (piglin.getType()
                != EntityTypes.ZOMBIFIED_PIGLIN) {

            return;
        }

        if (!(piglin.level()
                instanceof ServerLevel)) {

            return;
        }

        if (!esCapitan(piglin)) {
            return;
        }

        /*
         * Renovamos Fuerza II una vez por segundo.
         */
        if (piglin.tickCount % 20 != 0) {
            return;
        }

        aplicarFuerza(piglin);
    }

    private static void aplicarFuerza(
            LivingEntity piglin
    ) {

        piglin.addEffect(
                new MobEffectInstance(
                        MobEffects.STRENGTH,
                        DURACION_FUERZA,
                        AMPLIFICADOR_FUERZA,
                        false,
                        false,
                        false
                )
        );
    }

    /*
     * Drop garantizado de Filo VI.
     */
    @SubscribeEvent
    public static void onCaptainDrops(
            LivingDropsEvent event
    ) {

        LivingEntity piglin =
                event.getEntity();

        if (piglin.getType()
                != EntityTypes.ZOMBIFIED_PIGLIN) {

            return;
        }

        if (!esCapitan(piglin)) {
            return;
        }

        if (!(piglin.level()
                instanceof ServerLevel level)) {

            return;
        }

        /*
         * El libro se genera mediante el sistema central
         * de libros especiales.
         */
        ItemStack libro =
                SpecialEnchantedBooks
                        .crearLibroFilo(
                                level
                        );

        ItemEntity libroDrop =
                new ItemEntity(
                        level,
                        piglin.getX(),
                        piglin.getY(),
                        piglin.getZ(),
                        libro
                );

        /*
         * Añadimos el libro a los drops existentes.
         *
         * No cancelamos el evento, por lo que siguen
         * apareciendo la carne podrida y los drops de oro.
         */
        event.getDrops().add(
                libroDrop
        );
    }

    public static boolean esCapitan(
            LivingEntity entity
    ) {

        return entity
                .getPersistentData()
                .contains(
                        TAG_CAPITAN
                );
    }

    private ZombifiedPiglinCaptainEvents() {
    }
}