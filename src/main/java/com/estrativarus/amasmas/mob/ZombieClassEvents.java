package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class ZombieClassEvents {

    /*
     * Estas etiquetas se guardan dentro de la propia entidad.
     *
     * Impiden clasificar al mismo zombi varias veces y
     * permiten identificar al zombi modificado.
     */
    private static final String TAG_CLASIFICADO =
            "amasmas_zombie_clasificado";

    private static final String TAG_GUERRERO =
            "amasmas_zombie_guerrero";

    private static final String TAG_JINETE =
            "amasmas_zombie_jinete";

    private static final String TAG_MODIFICADO =
            "amasmas_zombie_modificado";

    private static final String TAG_TANQUE =
            "amasmas_zombie_tanque";

    private static final String TAG_EVOLUCION_DIA_14 =
            "amasmas_zombie_evolucion_dia_14";

    /*
     * Velocidad vertical utilizada por el zombi modificado
     * cuando choca contra una pared.
     */
    private static final double VELOCIDAD_ESCALADA =
            0.20D;

    /*
     * Se ejecuta cuando una entidad entra en el mundo.
     */
    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        /*
         * Solamente trabajamos desde el servidor.
         */
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        /*
         * No repetimos la clasificación cuando un zombi
         * guardado vuelve a cargarse desde el disco.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        /*
         * Trabajamos con Mob para no depender de la ubicación
         * concreta de las clases Zombie y Husk en 26.2.
         */
        if (!(event.getEntity() instanceof Mob zombie)) {
            return;
        }

        /*
         * Solo zombis normales y husks.
         */
        if (zombie.getType() != EntityTypes.ZOMBIE
                && zombie.getType() != EntityTypes.HUSK) {
            return;
        }

        /*
         * Esperamos hasta el siguiente tick.
         *
         * Esto permite que Minecraft termine de configurar
         * el equipamiento y la posible montura.
         */
        level.getServer().execute(() ->
                clasificarZombie(level, zombie)
        );
    }

    private static void clasificarZombie(
            ServerLevel level,
            Mob zombie
    ) {

        if (!zombie.isAlive()
                || zombie.isRemoved()) {

            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        boolean clasificado =
                zombie
                        .getPersistentData()
                        .contains(
                                TAG_CLASIFICADO
                        );

        if (!clasificado) {

            zombie
                    .getPersistentData()
                    .putBoolean(
                            TAG_CLASIFICADO,
                            true
                    );

            if (estaEnMonturaEspecial(zombie)) {

                configurarJinete(
                        level,
                        zombie
                );

            } else {

                int clase =
                        zombie
                                .getRandom()
                                .nextInt(4);

                switch (clase) {

                    case 0 ->
                            configurarGuerrero(
                                    level,
                                    zombie
                            );

                    case 1 ->
                            configurarJinete(
                                    level,
                                    zombie
                            );

                    case 2 ->
                            configurarModificado(
                                    level,
                                    zombie
                            );

                    default ->
                            configurarTanque(
                                    level,
                                    zombie
                            );
                }
            }
        }

        if (diaActual >= 14) {

            aplicarEvolucionDia14(
                    level,
                    zombie
            );
        }
    }

    private static boolean estaEnMonturaEspecial(
            Mob zombie
    ) {

        Entity vehiculo =
                zombie.getVehicle();

        if (vehiculo == null) {
            return false;
        }

        return vehiculo.getType()
                == EntityTypes.ZOMBIE_HORSE

                || vehiculo.getType()
                == EntityTypes.CAMEL_HUSK;
    }

    /*
     * ZOMBI GUERRERO
     *
     * Armadura completa de cuero con Protección II.
     * Espada de piedra con Filo I.
     */
    private static void aplicarEvolucionDia14(
            ServerLevel level,
            Mob zombie
    ) {

        if (zombie
                .getPersistentData()
                .contains(
                        TAG_EVOLUCION_DIA_14
                )) {

            return;
        }

        if (estaEnMonturaEspecial(zombie)) {

            limpiarMarcasDeClase(
                    zombie
            );

            zombie
                    .getPersistentData()
                    .putBoolean(
                            TAG_JINETE,
                            true
                    );

            evolucionarJineteDia14(
                    level,
                    zombie
            );

        } else if (zombie
                .getPersistentData()
                .contains(TAG_GUERRERO)) {

            evolucionarGuerreroDia14(
                    level,
                    zombie
            );

        } else if (zombie
                .getPersistentData()
                .contains(TAG_JINETE)) {

            evolucionarJineteDia14(
                    level,
                    zombie
            );

        } else if (zombie
                .getPersistentData()
                .contains(TAG_MODIFICADO)) {

            evolucionarModificadoDia14(
                    level,
                    zombie
            );

        } else if (zombie
                .getPersistentData()
                .contains(TAG_TANQUE)) {

            evolucionarTanqueDia14(
                    level,
                    zombie
            );

        } else {

            return;
        }

        zombie
                .getPersistentData()
                .putBoolean(
                        TAG_EVOLUCION_DIA_14,
                        true
                );
    }

    private static void evolucionarGuerreroDia14(
            ServerLevel level,
            Mob zombie
    ) {

        ItemStack casco =
                crearPiezaEncantada(
                        level,
                        Items.IRON_HELMET,
                        Enchantments.PROTECTION,
                        2
                );

        ItemStack pechera =
                crearPiezaEncantada(
                        level,
                        Items.IRON_CHESTPLATE,
                        Enchantments.PROTECTION,
                        2
                );

        ItemStack pantalones =
                crearPiezaEncantada(
                        level,
                        Items.IRON_LEGGINGS,
                        Enchantments.PROTECTION,
                        2
                );

        ItemStack botas =
                crearPiezaEncantada(
                        level,
                        Items.IRON_BOOTS,
                        Enchantments.PROTECTION,
                        2
                );

        aplicarRibeteSilenceNetherite(
                level,
                casco
        );

        aplicarRibeteSilenceNetherite(
                level,
                pechera
        );

        aplicarRibeteSilenceNetherite(
                level,
                pantalones
        );

        aplicarRibeteSilenceNetherite(
                level,
                botas
        );

        equiparArmadura(
                zombie,
                casco,
                pechera,
                pantalones,
                botas
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.IRON_SWORD,
                Enchantments.SHARPNESS,
                2
        );

        impedirDropsDeEquipamiento(
                zombie
        );
    }

    private static void evolucionarJineteDia14(
            ServerLevel level,
            Mob zombie
    ) {

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.DIAMOND_HELMET,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.DIAMOND_CHESTPLATE,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.DIAMOND_LEGGINGS,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.DIAMOND_BOOTS,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.IRON_SPEAR,
                Enchantments.SHARPNESS,
                2
        );

        impedirDropsDeEquipamiento(
                zombie
        );
    }

    private static void evolucionarModificadoDia14(
            ServerLevel level,
            Mob zombie
    ) {

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.DIAMOND_HELMET,
                Enchantments.PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.IRON_CHESTPLATE,
                Enchantments.PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.DIAMOND_LEGGINGS,
                Enchantments.PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.IRON_BOOTS,
                Enchantments.PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.DIAMOND_SWORD,
                Enchantments.SHARPNESS,
                3
        );

        impedirDropsDeEquipamiento(
                zombie
        );
    }

    private static void evolucionarTanqueDia14(
            ServerLevel level,
            Mob zombie
    ) {

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.DIAMOND_HELMET,
                Enchantments.PROTECTION,
                4
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.DIAMOND_CHESTPLATE,
                Enchantments.PROTECTION,
                4
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.DIAMOND_LEGGINGS,
                Enchantments.PROTECTION,
                4
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.DIAMOND_BOOTS,
                Enchantments.PROTECTION,
                4
        );

        ItemStack hacha =
                new ItemStack(
                        Items.DIAMOND_AXE
                );

        anadirEncantamiento(
                level,
                hacha,
                Enchantments.KNOCKBACK,
                10
        );

        anadirEncantamiento(
                level,
                hacha,
                Enchantments.SHARPNESS,
                3
        );

        zombie.setItemSlot(
                EquipmentSlot.MAINHAND,
                hacha
        );

        impedirDropsDeEquipamiento(
                zombie
        );
    }

    private static ItemStack crearPiezaEncantada(
            ServerLevel level,
            Item item,
            net.minecraft.resources.ResourceKey<Enchantment>
                    enchantmentKey,
            int nivel
    ) {

        ItemStack stack =
                new ItemStack(
                        item
                );

        anadirEncantamiento(
                level,
                stack,
                enchantmentKey,
                nivel
        );

        return stack;
    }

    private static void anadirEncantamiento(
            ServerLevel level,
            ItemStack stack,
            net.minecraft.resources.ResourceKey<Enchantment>
                    enchantmentKey,
            int nivel
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> encantamiento =
                registro.getOrThrow(
                        enchantmentKey
                );

        stack.enchant(
                encantamiento,
                nivel
        );
    }

    private static void equiparArmadura(
            Mob zombie,
            ItemStack casco,
            ItemStack pechera,
            ItemStack pantalones,
            ItemStack botas
    ) {

        zombie.setItemSlot(
                EquipmentSlot.HEAD,
                casco
        );

        zombie.setItemSlot(
                EquipmentSlot.CHEST,
                pechera
        );

        zombie.setItemSlot(
                EquipmentSlot.LEGS,
                pantalones
        );

        zombie.setItemSlot(
                EquipmentSlot.FEET,
                botas
        );
    }

    private static void limpiarMarcasDeClase(
            Mob zombie
    ) {

        zombie
                .getPersistentData()
                .remove(TAG_GUERRERO);

        zombie
                .getPersistentData()
                .remove(TAG_JINETE);

        zombie
                .getPersistentData()
                .remove(TAG_MODIFICADO);

        zombie
                .getPersistentData()
                .remove(TAG_TANQUE);
    }

    private static void aplicarRibeteSilenceNetherite(
            ServerLevel level,
            ItemStack pieza
    ) {

        Registry<TrimPattern> patrones =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.TRIM_PATTERN
                        );

        Registry<TrimMaterial> materiales =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.TRIM_MATERIAL
                        );

        Holder.Reference<TrimPattern> patron =
                patrones.getOrThrow(
                        TrimPatterns.SILENCE
                );

        Holder.Reference<TrimMaterial> material =
                materiales.getOrThrow(
                        TrimMaterials.NETHERITE
                );

        pieza.set(
                DataComponents.TRIM,
                new ArmorTrim(
                        material,
                        patron
                )
        );
    }

    private static void configurarGuerrero(
            ServerLevel level,
            Mob zombie
    ) {

        zombie
                .getPersistentData()
                .putBoolean(
                        TAG_GUERRERO,
                        true
                );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.LEATHER_HELMET,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.LEATHER_CHESTPLATE,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.LEATHER_LEGGINGS,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.LEATHER_BOOTS,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.STONE_SWORD,
                Enchantments.SHARPNESS,
                1
        );

        impedirDropsDeEquipamiento(zombie);
    }

    /*
     * ZOMBI JINETE
     *
     * Armadura completa de cota de malla con
     * Protección contra proyectiles III.
     *
     * Lanza de hierro con Filo II.
     */
    private static void configurarJinete(
            ServerLevel level,
            Mob zombie
    ) {

        zombie
                .getPersistentData()
                .putBoolean(
                        TAG_JINETE,
                        true
                );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.CHAINMAIL_HELMET,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.CHAINMAIL_CHESTPLATE,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.CHAINMAIL_LEGGINGS,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.CHAINMAIL_BOOTS,
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.IRON_SPEAR,
                Enchantments.SHARPNESS,
                2
        );

        impedirDropsDeEquipamiento(zombie);
    }

    /*
     * ZOMBI MODIFICADO
     *
     * Mezcla de hierro y oro con Protección I.
     * Espada de hierro con Filo II.
     *
     * La lógica de escalada se ejecuta en onZombieTick.
     */
    private static void configurarModificado(
            ServerLevel level,
            Mob zombie
    ) {

        zombie
                .getPersistentData()
                .putBoolean(
                        TAG_MODIFICADO,
                        true
                );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.IRON_HELMET,
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.GOLDEN_CHESTPLATE,
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.IRON_LEGGINGS,
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.GOLDEN_BOOTS,
                Enchantments.PROTECTION,
                1
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.IRON_SWORD,
                Enchantments.SHARPNESS,
                2
        );

        impedirDropsDeEquipamiento(zombie);
    }

    /*
     * ZOMBI TANQUE
     *
     * Armadura completa de diamante con Protección II.
     * Hacha de diamante con Empuje V.
     */
    private static void configurarTanque(
            ServerLevel level,
            Mob zombie
    ) {

        zombie
                .getPersistentData()
                .putBoolean(
                        TAG_TANQUE,
                        true
                );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.HEAD,
                Items.DIAMOND_HELMET,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.CHEST,
                Items.DIAMOND_CHESTPLATE,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.LEGS,
                Items.DIAMOND_LEGGINGS,
                Enchantments.PROTECTION,
                2
        );

        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.FEET,
                Items.DIAMOND_BOOTS,
                Enchantments.PROTECTION,
                2
        );

        /*
         * Empuje V supera el nivel vanilla habitual,
         * pero es válido como nivel directo en un objeto.
         */
        equiparEncantado(
                level,
                zombie,
                EquipmentSlot.MAINHAND,
                Items.DIAMOND_AXE,
                Enchantments.KNOCKBACK,
                5
        );

        impedirDropsDeEquipamiento(zombie);
    }

    /*
     * Crea un objeto, le añade el encantamiento indicado
     * y lo coloca en el slot correspondiente.
     */
    private static void equiparEncantado(
            ServerLevel level,
            Mob zombie,
            EquipmentSlot slot,
            Item item,
            net.minecraft.resources.ResourceKey<Enchantment>
                    enchantmentKey,
            int nivel
    ) {

        ItemStack objeto =
                new ItemStack(item);

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> encantamiento =
                registro.getOrThrow(
                        enchantmentKey
                );

        /*
         * Añadimos directamente el encantamiento al ItemStack.
         */
        objeto.enchant(
                encantamiento,
                nivel
        );

        zombie.setItemSlot(
                slot,
                objeto
        );
    }

    /*
     * Las piezas y el arma no pueden caer.
     *
     * Esto no modifica el loot normal del zombi:
     * la carne podrida sigue pudiendo caer.
     */
    private static void impedirDropsDeEquipamiento(
            Mob zombie
    ) {

        zombie.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        zombie.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        zombie.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        zombie.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );

        zombie.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    /*
     * Escalada del zombi modificado.
     */
    @SubscribeEvent
    public static void onZombieTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob zombie)) {

            return;
        }

        if (zombie.getType()
                != EntityTypes.ZOMBIE
                && zombie.getType()
                != EntityTypes.HUSK) {

            return;
        }

        if (!(zombie.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((zombie.tickCount
                + zombie.getId()) % 100 == 0) {

            clasificarZombie(
                    level,
                    zombie
            );

            if (estaEnMonturaEspecial(zombie)
                    && !zombie
                    .getPersistentData()
                    .contains(TAG_JINETE)) {

                configurarJinete(
                        level,
                        zombie
                );

                zombie
                        .getPersistentData()
                        .putBoolean(
                                TAG_EVOLUCION_DIA_14,
                                false
                        );

                int diaActual =
                        SistemaDiasSavedData
                                .get(level.getServer())
                                .getDiaActual();

                if (diaActual >= 14) {

                    aplicarEvolucionDia14(
                            level,
                            zombie
                    );
                }
            }
        }

        if (!zombie
                .getPersistentData()
                .contains(TAG_MODIFICADO)) {

            return;
        }

        if (!zombie.horizontalCollision) {
            return;
        }

        zombie.setDeltaMovement(
                zombie
                        .getDeltaMovement()
                        .x,
                VELOCIDAD_ESCALADA,
                zombie
                        .getDeltaMovement()
                        .z
        );

        zombie.fallDistance =
                0.0F;

        zombie.hurtMarked =
                true;
    }

    private ZombieClassEvents() {
    }
}