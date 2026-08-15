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

        /*
         * Es posible que la entidad haya desaparecido antes
         * de que se ejecute la tarea aplazada.
         */
        if (!zombie.isAlive()
                || zombie.isRemoved()) {
            return;
        }

        /*
         * Evitamos procesar dos veces la misma entidad.
         */
        if (zombie
                .getPersistentData()
                .contains(TAG_CLASIFICADO)) {

            return;
        }

        /*
         * Marcamos la entidad antes de continuar.
         *
         * De esta forma nunca se reclasificará al cargar
         * chunks o cambiar de día.
         */
        zombie.addTag(TAG_CLASIFICADO);

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Estas cuatro clases solo existen del día 7 al 13.
         */
        if (diaActual < 7 || diaActual > 13) {
            return;
        }

        /*
         * Si el zombi está sobre una montura no muerta,
         * siempre será de la clase Jinete.
         */
        if (estaEnMonturaEspecial(zombie)) {
            configurarJinete(level, zombie);
            return;
        }

        /*
         * Cuatro posibilidades equiprobables:
         *
         * 0 = Guerrero
         * 1 = Jinete
         * 2 = Modificado
         * 3 = Tanque
         */
        int clase =
                zombie.getRandom().nextInt(4);

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

        if (!(event.getEntity() instanceof Mob zombie)) {
            return;
        }

        /*
         * Solo los zombis que recibieron la clase Modificado.
         */
        if (!zombie
                .getPersistentData()
                .contains(TAG_MODIFICADO)) {

            return;
        }

        if (!(zombie.level()
                instanceof ServerLevel)) {
            return;
        }

        /*
         * Si el zombi está chocando horizontalmente con una
         * pared, le damos velocidad vertical.
         *
         * Conservamos su velocidad horizontal actual.
         */
        if (zombie.horizontalCollision) {

            zombie.setDeltaMovement(
                    zombie
                            .getDeltaMovement()
                            .x,
                    VELOCIDAD_ESCALADA,
                    zombie
                            .getDeltaMovement()
                            .z
            );

            /*
             * Marca el movimiento para sincronizarlo
             * correctamente con los clientes.
             */
            zombie.hurtMarked = true;
        }
    }

    private ZombieClassEvents() {
    }
}