package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;


@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class LostBoggedEvents {

    public static final String TAG_BOGGED_PERDIDO =
            "amasmas_bogged_perdido";

    private static final String TAG_TIRADA_REALIZADA =
            "amasmas_tirada_bogged_perdido_realizada";

    private static final String MOD_REPURPOSED_STRUCTURES =
            "repurposed_structures";

    private static final int PROBABILIDAD_APARICION =
            1;

    private static final double VIDA_MAXIMA =
            40.0D;
    private static final float PROBABILIDAD_ARCO =
            0.25F;

    private static final ResourceKey<Structure>
            JUNGLE_FORTRESS =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            "repurposed_structures",
                            "fortress_jungle"
                    )
            );

    @SubscribeEvent
    public static void onEntityJoin(
            EntityJoinLevelEvent event
    ) {

        if (!ModList.get().isLoaded(
                MOD_REPURPOSED_STRUCTURES
        )) {

            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob bogged)) {

            return;
        }

        if (bogged.getType()
                != EntityTypes.BOGGED) {

            return;
        }

        if (event.loadedFromDisk()) {
            return;
        }

        level.getServer().execute(() -> {

            if (!bogged.isAlive()
                    || bogged.isRemoved()) {

                return;
            }

            intentarConvertir(
                    level,
                    bogged
            );
        });
    }

    private static void intentarConvertir(
            ServerLevel level,
            Mob bogged
    ) {

        if (bogged
                .getPersistentData()
                .contains(
                        TAG_TIRADA_REALIZADA
                )) {

            return;
        }

        bogged
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

        if (diaActual < 7) {
            return;
        }

        if (!estaDentroDeJungleFortress(
                level,
                bogged
        )) {

            return;
        }

        if (bogged
                .getRandom()
                .nextInt(
                        PROBABILIDAD_APARICION
                )
                != 0) {

            return;
        }

        convertirEnBoggedPerdido(
                level,
                bogged
        );
    }

    private static boolean estaDentroDeJungleFortress(
            ServerLevel level,
            Mob bogged
    ) {

        Registry<Structure> estructuras =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.STRUCTURE
                        );

        Holder.Reference<Structure> estructura =
                estructuras.get(
                        JUNGLE_FORTRESS
                ).orElse(null);

        if (estructura == null) {
            return false;
        }

        return level
                .structureManager()
                .getStructureWithPieceAt(
                        bogged.blockPosition(),
                        holder ->
                                holder.is(
                                        JUNGLE_FORTRESS
                                )
                )
                .isValid();
    }

    private static void convertirEnBoggedPerdido(
            ServerLevel level,
            Mob bogged
    ) {

        bogged
                .getPersistentData()
                .putBoolean(
                        TAG_BOGGED_PERDIDO,
                        true
                );

        bogged.setCustomName(
                Component.literal(
                        "Bogged Perdido"
                ).withStyle(
                        ChatFormatting.DARK_GREEN,
                        ChatFormatting.BOLD
                )
        );

        bogged.setCustomNameVisible(true);

        AttributeInstance atributoVida =
                bogged.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida != null) {

            atributoVida.setBaseValue(
                    VIDA_MAXIMA
            );

            bogged.setHealth(
                    bogged.getMaxHealth()
            );
        }

        ItemStack casco =
                new ItemStack(
                        Items.IRON_HELMET
                );

        ItemStack pechera =
                new ItemStack(
                        Items.IRON_CHESTPLATE
                );

        ItemStack pantalones =
                new ItemStack(
                        Items.NETHERITE_LEGGINGS
                );

        ItemStack botas =
                new ItemStack(
                        Items.NETHERITE_BOOTS
                );

        ItemStack arco =
                new ItemStack(
                        Items.BOW
                );

        anadirEncantamiento(
                level,
                casco,
                Enchantments.PROJECTILE_PROTECTION,
                4
        );

        anadirEncantamiento(
                level,
                pechera,
                Enchantments.PROJECTILE_PROTECTION,
                4
        );

        anadirEncantamiento(
                level,
                pantalones,
                Enchantments.PROJECTILE_PROTECTION,
                4
        );

        anadirEncantamiento(
                level,
                botas,
                Enchantments.PROJECTILE_PROTECTION,
                4
        );

        anadirEncantamiento(
                level,
                arco,
                Enchantments.POWER,
                20
        );

        bogged.setItemSlot(
                EquipmentSlot.HEAD,
                casco
        );

        bogged.setItemSlot(
                EquipmentSlot.CHEST,
                pechera
        );

        bogged.setItemSlot(
                EquipmentSlot.LEGS,
                pantalones
        );

        bogged.setItemSlot(
                EquipmentSlot.FEET,
                botas
        );

        bogged.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );

        impedirDropsEquipamiento(
                bogged
        );
    }

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

        Holder.Reference<Enchantment> holder =
                registro.getOrThrow(
                        enchantmentKey
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

    private static void impedirDropsEquipamiento(
            Mob bogged
    ) {

        bogged.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        bogged.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        bogged.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        bogged.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );

        bogged.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    @SubscribeEvent
    public static void onBoggedPerdidoDrops(
            LivingDropsEvent event
    ) {

        if (!(event.getEntity() instanceof Mob bogged)) {
            return;
        }

        if (bogged.getType() != EntityTypes.BOGGED) {
            return;
        }

        if (!esBoggedPerdido(bogged)) {
            return;
        }

        if (!(bogged.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (bogged
                .getRandom()
                .nextFloat()
                >= PROBABILIDAD_ARCO) {

            return;
        }

        ItemStack arcoRecompensa =
                new ItemStack(
                        Items.BOW
                );

        anadirEncantamiento(
                level,
                arcoRecompensa,
                Enchantments.POWER,
                7
        );

        ItemEntity arcoDrop =
                new ItemEntity(
                        level,
                        bogged.getX(),
                        bogged.getY(),
                        bogged.getZ(),
                        arcoRecompensa
                );

        event.getDrops().add(
                arcoDrop
        );
    }

    public static boolean esBoggedPerdido(
            Mob bogged
    ) {

        return bogged
                .getPersistentData()
                .contains(
                        TAG_BOGGED_PERDIDO
                );
    }

    private LostBoggedEvents() {
    }
}
