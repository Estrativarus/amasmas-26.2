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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class DrownedEquipmentEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(
            EntityJoinLevelEvent event
    ) {

        /*
         * Solo trabajamos en el servidor.
         */
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Solo procesamos ahogados.
         */
        if (!(event.getEntity() instanceof Mob drowned)) {
            return;
        }

        if (drowned.getType() != EntityTypes.DROWNED) {
            return;
        }

        /*
         * No volvemos a clasificar ahogados cargados desde disco.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        MinecraftServer server = level.getServer();

        /*
         * Esperamos hasta el siguiente ciclo del servidor.
         *
         * Esto permite que Minecraft haya terminado de asignar
         * cualquier montura al ahogado.
         */
        server.execute(() -> {

            if (!drowned.isAlive()
                    || drowned.isRemoved()) {
                return;
            }

            clasificarAhogado(
                    level,
                    drowned
            );
        });
    }

    private static void clasificarAhogado(
            ServerLevel level,
            Mob drowned
    ) {

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        /*
         * Estas clases solamente aparecen entre
         * los días 7 y 13, ambos incluidos.
         */
        if (diaActual < 7 || diaActual > 13) {
            return;
        }

        /*
         * Si ya está montado en un Nautilo o Nautilo zombi,
         * obligatoriamente será Jinete ahogado.
         */
        if (estaMontadoEnNautilo(drowned)) {

            equiparJinete(
                    level,
                    drowned
            );

            return;
        }

        /*
         * nextInt(3) genera:
         *
         * 0 -> Guerrero
         * 1 -> Jinete
         * 2 -> Tanque
         *
         * Cada resultado tiene un 33,33 %.
         */
        int clase =
                drowned.getRandom().nextInt(3);

        switch (clase) {

            case 0 ->
                    equiparGuerrero(
                            level,
                            drowned
                    );

            case 1 ->
                    equiparJinete(
                            level,
                            drowned
                    );

            default ->
                    equiparTanque(
                            level,
                            drowned
                    );
        }
    }

    private static boolean estaMontadoEnNautilo(
            Mob drowned
    ) {

        Entity montura =
                drowned.getVehicle();

        if (montura == null) {
            return false;
        }

        return montura.getType() == EntityTypes.NAUTILUS
                || montura.getType()
                == EntityTypes.ZOMBIE_NAUTILUS;
    }

    /*
     * GUERRERO AHOGADO
     *
     * - Armadura completa de cuero.
     * - Protección II.
     * - Tridente con Filo II.
     */
    private static void equiparGuerrero(
            ServerLevel level,
            Mob drowned
    ) {

        equiparArmadura(
                level,
                drowned,
                new ItemStack(Items.LEATHER_HELMET),
                new ItemStack(Items.LEATHER_CHESTPLATE),
                new ItemStack(Items.LEATHER_LEGGINGS),
                new ItemStack(Items.LEATHER_BOOTS),
                Enchantments.PROTECTION,
                2
        );

        ItemStack tridente =
                new ItemStack(Items.TRIDENT);

        encantar(
                level,
                tridente,
                Enchantments.SHARPNESS,
                2
        );

        equiparObjetoPrincipal(
                drowned,
                tridente
        );
    }

    /*
     * JINETE AHOGADO
     *
     * - Armadura completa de cota de malla.
     * - Protección contra proyectiles III.
     * - Tridente con Filo II.
     */
    private static void equiparJinete(
            ServerLevel level,
            Mob drowned
    ) {

        equiparArmadura(
                level,
                drowned,
                new ItemStack(Items.CHAINMAIL_HELMET),
                new ItemStack(Items.CHAINMAIL_CHESTPLATE),
                new ItemStack(Items.CHAINMAIL_LEGGINGS),
                new ItemStack(Items.CHAINMAIL_BOOTS),
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        ItemStack tridente =
                new ItemStack(Items.TRIDENT);

        encantar(
                level,
                tridente,
                Enchantments.SHARPNESS,
                2
        );

        equiparObjetoPrincipal(
                drowned,
                tridente
        );
    }

    /*
     * ZOMBI TANQUE
     *
     * - Armadura completa de diamante.
     * - Protección II.
     * - Caña de pescar con Empuje V.
     */
    private static void equiparTanque(
            ServerLevel level,
            Mob drowned
    ) {

        equiparArmadura(
                level,
                drowned,
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS),
                Enchantments.PROTECTION,
                2
        );

        ItemStack cana =
                new ItemStack(Items.FISHING_ROD);

        encantar(
                level,
                cana,
                Enchantments.KNOCKBACK,
                5
        );

        equiparObjetoPrincipal(
                drowned,
                cana
        );
    }

    private static void equiparArmadura(
            ServerLevel level,
            Mob drowned,
            ItemStack casco,
            ItemStack pechera,
            ItemStack pantalones,
            ItemStack botas,
            net.minecraft.resources.ResourceKey<Enchantment>
                    encantamiento,
            int nivelEncantamiento
    ) {

        encantar(
                level,
                casco,
                encantamiento,
                nivelEncantamiento
        );

        encantar(
                level,
                pechera,
                encantamiento,
                nivelEncantamiento
        );

        encantar(
                level,
                pantalones,
                encantamiento,
                nivelEncantamiento
        );

        encantar(
                level,
                botas,
                encantamiento,
                nivelEncantamiento
        );

        drowned.setItemSlot(
                EquipmentSlot.HEAD,
                casco
        );

        drowned.setItemSlot(
                EquipmentSlot.CHEST,
                pechera
        );

        drowned.setItemSlot(
                EquipmentSlot.LEGS,
                pantalones
        );

        drowned.setItemSlot(
                EquipmentSlot.FEET,
                botas
        );

        /*
         * Ninguna pieza de armadura puede caer.
         */
        drowned.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        drowned.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        drowned.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        drowned.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );
    }

    private static void equiparObjetoPrincipal(
            Mob drowned,
            ItemStack objeto
    ) {

        drowned.setItemSlot(
                EquipmentSlot.MAINHAND,
                objeto
        );

        /*
         * El tridente o la caña tampoco pueden caer.
         */
        drowned.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    private static void encantar(
            ServerLevel level,
            ItemStack stack,
            net.minecraft.resources.ResourceKey<Enchantment>
                    encantamiento,
            int nivel
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        /*
         * Obtenemos el Holder del encantamiento vanilla.
         */
        registro.get(encantamiento)
                .ifPresent(holder ->
                        stack.enchant(
                                holder,
                                nivel
                        )
                );
    }

    private DrownedEquipmentEvents() {
    }
}
