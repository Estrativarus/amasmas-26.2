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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class DrownedEquipmentEvents {

    private static final String TAG_CLASIFICADO =
            "amasmas_drowned_clasificado";

    private static final String TAG_GUERRERO =
            "amasmas_drowned_guerrero";

    private static final String TAG_JINETE =
            "amasmas_drowned_jinete";

    private static final String TAG_TANQUE =
            "amasmas_drowned_tanque";

    private static final String TAG_EVOLUCION_DIA_14 =
            "amasmas_drowned_evolucion_dia_14";

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

    private static void limpiarMarcasDeClase(
            Mob drowned
    ) {

        drowned
                .getPersistentData()
                .remove(
                        TAG_GUERRERO
                );

        drowned
                .getPersistentData()
                .remove(
                        TAG_JINETE
                );

        drowned
                .getPersistentData()
                .remove(
                        TAG_TANQUE
                );
    }

    @SubscribeEvent
    public static void onDrownedTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob drowned)) {

            return;
        }

        if (drowned.getType()
                != EntityTypes.DROWNED) {

            return;
        }

        if (!(drowned.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((drowned.tickCount
                + drowned.getId()) % 100 != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < 7) {
            return;
        }

        clasificarAhogado(
                level,
                drowned
        );

        if (diaActual >= 14
                && estaMontadoEnNautilo(drowned)
                && !esClase(
                drowned,
                TAG_JINETE
        )) {

            convertirEnJineteDia14(
                    level,
                    drowned
            );
        }
    }

    private static void clasificarAhogado(
            ServerLevel level,
            Mob drowned
    ) {

        if (!drowned.isAlive()
                || drowned.isRemoved()) {

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

        boolean yaClasificado =
                drowned
                        .getPersistentData()
                        .contains(
                                TAG_CLASIFICADO
                        );

        if (!yaClasificado) {

            drowned
                    .getPersistentData()
                    .putBoolean(
                            TAG_CLASIFICADO,
                            true
                    );

            if (estaMontadoEnNautilo(
                    drowned
            )) {

                marcarClase(
                        drowned,
                        TAG_JINETE
                );

                equiparJinete(
                        level,
                        drowned
                );

            } else {

                int clase =
                        drowned
                                .getRandom()
                                .nextInt(3);

                switch (clase) {

                    case 0 -> {

                        marcarClase(
                                drowned,
                                TAG_GUERRERO
                        );

                        equiparGuerrero(
                                level,
                                drowned
                        );
                    }

                    case 1 -> {

                        marcarClase(
                                drowned,
                                TAG_JINETE
                        );

                        equiparJinete(
                                level,
                                drowned
                        );
                    }

                    default -> {

                        marcarClase(
                                drowned,
                                TAG_TANQUE
                        );

                        equiparTanque(
                                level,
                                drowned
                        );
                    }
                }
            }
        }

        if (diaActual >= 14) {

            aplicarEvolucionDia14(
                    level,
                    drowned
            );
        }
    }

    private static void evolucionarGuerreroDia14(
            ServerLevel level,
            Mob drowned
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

        colocarArmadura(
                drowned,
                casco,
                pechera,
                pantalones,
                botas
        );

        ItemStack tridente =
                new ItemStack(
                        Items.TRIDENT
                );

        encantar(
                level,
                tridente,
                Enchantments.SHARPNESS,
                4
        );

        equiparObjetoPrincipal(
                drowned,
                tridente
        );

        impedirDropsDeEquipamiento(
                drowned
        );
    }

    private static void evolucionarJineteDia14(
            ServerLevel level,
            Mob drowned
    ) {

        equiparArmadura(
                level,
                drowned,
                new ItemStack(
                        Items.DIAMOND_HELMET
                ),
                new ItemStack(
                        Items.DIAMOND_CHESTPLATE
                ),
                new ItemStack(
                        Items.DIAMOND_LEGGINGS
                ),
                new ItemStack(
                        Items.DIAMOND_BOOTS
                ),
                Enchantments.PROJECTILE_PROTECTION,
                3
        );

        ItemStack tridente =
                new ItemStack(
                        Items.TRIDENT
                );

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

        impedirDropsDeEquipamiento(
                drowned
        );
    }

    private static void evolucionarTanqueDia14(
            ServerLevel level,
            Mob drowned
    ) {

        equiparArmadura(
                level,
                drowned,
                new ItemStack(
                        Items.DIAMOND_HELMET
                ),
                new ItemStack(
                        Items.DIAMOND_CHESTPLATE
                ),
                new ItemStack(
                        Items.DIAMOND_LEGGINGS
                ),
                new ItemStack(
                        Items.DIAMOND_BOOTS
                ),
                Enchantments.PROTECTION,
                4
        );

        ItemStack cana =
                new ItemStack(
                        Items.FISHING_ROD
                );

        encantar(
                level,
                cana,
                Enchantments.KNOCKBACK,
                20
        );

        encantar(
                level,
                cana,
                Enchantments.SHARPNESS,
                20
        );

        equiparObjetoPrincipal(
                drowned,
                cana
        );

        impedirDropsDeEquipamiento(
                drowned
        );
    }

    private static ItemStack crearPiezaEncantada(
            ServerLevel level,
            net.minecraft.world.item.Item item,
            net.minecraft.resources.ResourceKey<Enchantment>
                    encantamiento,
            int nivel
    ) {

        ItemStack stack =
                new ItemStack(
                        item
                );

        encantar(
                level,
                stack,
                encantamiento,
                nivel
        );

        return stack;
    }

    private static void colocarArmadura(
            Mob drowned,
            ItemStack casco,
            ItemStack pechera,
            ItemStack pantalones,
            ItemStack botas
    ) {

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
    }

    private static void impedirDropsDeEquipamiento(
            Mob drowned
    ) {

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

        drowned.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
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

        ArmorTrim ribete =
                new ArmorTrim(
                        material,
                        patron
                );

        pieza.set(
                DataComponents.TRIM,
                ribete
        );
    }

    private static void marcarClase(
            Mob drowned,
            String tagClase
    ) {

        drowned
                .getPersistentData()
                .putBoolean(
                        tagClase,
                        true
                );
    }

    private static boolean esClase(
            Mob drowned,
            String tagClase
    ) {

        return drowned
                .getPersistentData()
                .contains(
                        tagClase
                );
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
    private static void aplicarEvolucionDia14(
            ServerLevel level,
            Mob drowned
    ) {

        if (drowned
                .getPersistentData()
                .contains(
                        TAG_EVOLUCION_DIA_14
                )) {

            return;
        }

        if (estaMontadoEnNautilo(drowned)) {

            convertirEnJineteDia14(
                    level,
                    drowned
            );

            return;
        }

        if (esClase(
                drowned,
                TAG_GUERRERO
        )) {

            evolucionarGuerreroDia14(
                    level,
                    drowned
            );

        } else if (esClase(
                drowned,
                TAG_JINETE
        )) {

            evolucionarJineteDia14(
                    level,
                    drowned
            );

        } else if (esClase(
                drowned,
                TAG_TANQUE
        )) {

            evolucionarTanqueDia14(
                    level,
                    drowned
            );

        } else {

            return;
        }

        drowned
                .getPersistentData()
                .putBoolean(
                        TAG_EVOLUCION_DIA_14,
                        true
                );
    }

    private static void convertirEnJineteDia14(
            ServerLevel level,
            Mob drowned
    ) {

        limpiarMarcasDeClase(
                drowned
        );

        marcarClase(
                drowned,
                TAG_JINETE
        );

        evolucionarJineteDia14(
                level,
                drowned
        );

        drowned
                .getPersistentData()
                .putBoolean(
                        TAG_CLASIFICADO,
                        true
                );

        drowned
                .getPersistentData()
                .putBoolean(
                        TAG_EVOLUCION_DIA_14,
                        true
                );
    }

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
