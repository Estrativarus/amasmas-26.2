package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class FinalizerSkeletonEvents {

    public static final String TAG_FINALIZADOR =
            "amasmas_esqueleto_finalizador";

    private static final String TAG_EQUIPO_APLICADO =
            "amasmas_esqueleto_finalizador_equipo_aplicado";

    private static final String MOD_REPURPOSED_STRUCTURES =
            "repurposed_structures";

    private static final int DIA_INICIO =
            21;

    private static final double VIDA_MAXIMA =
            200.0D;

    private static final int NIVEL_PODER =
            200;

    private static final float PROBABILIDAD_FRAGMENTO =
            0.50F;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final ResourceKey<Structure>
            STRONGHOLD_END =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            "repurposed_structures",
                            "stronghold_end"
                    )
            );

    @SubscribeEvent
    public static void onSkeletonJoin(
            EntityJoinLevelEvent event
    ) {

        if (!estaRepurposedStructuresInstalado()) {
            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (!(event.getEntity()
                instanceof Mob skeleton)) {

            return;
        }

        boolean esSkeletonNormal =
                skeleton.getType()
                        == EntityTypes.SKELETON;

        boolean esFinalizadorBogged =
                skeleton.getType()
                        == EntityTypes.BOGGED
                        && esFinalizador(
                        skeleton
                );

        if (!esSkeletonNormal
                && !esFinalizadorBogged) {

            return;
        }

        level.getServer().execute(() -> {

            if (!skeleton.isAlive()
                    || skeleton.isRemoved()) {

                return;
            }

            convertirSiCorresponde(
                    level,
                    skeleton
            );
        });
    }

    @SubscribeEvent
    public static void onSkeletonTick(
            EntityTickEvent.Post event
    ) {

        if (!estaRepurposedStructuresInstalado()) {
            return;
        }

        if (!(event.getEntity()
                instanceof Mob skeleton)) {

            return;
        }

        if (skeleton.getType()
                != EntityTypes.SKELETON) {

            return;
        }

        if (!(skeleton.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((skeleton.tickCount
                + skeleton.getId())
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        convertirSiCorresponde(
                level,
                skeleton
        );
    }

    private static boolean estaRepurposedStructuresInstalado() {

        return ModList.get().isLoaded(
                MOD_REPURPOSED_STRUCTURES
        );
    }

    public static boolean convertirSiCorresponde(
            ServerLevel level,
            Mob skeleton
    ) {

        if (!estaRepurposedStructuresInstalado()) {
            return false;
        }

        if (esFinalizador(skeleton)) {

            asegurarConfiguracion(
                    level,
                    skeleton
            );

            return true;
        }

        if (skeleton.getType()
                != EntityTypes.SKELETON) {

            return false;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return false;
        }

        if (!estaDentroDelStrongholdEnd(
                level,
                skeleton
        )) {

            return false;
        }

        convertirEnFinalizador(
                level,
                skeleton
        );

        return true;
    }

    private static boolean estaDentroDelStrongholdEnd(
            ServerLevel level,
            Mob skeleton
    ) {

        return level
                .structureManager()
                .getStructureWithPieceAt(
                        skeleton.blockPosition(),
                        holder ->
                                holder.is(
                                        STRONGHOLD_END
                                )
                )
                .isValid();
    }

    private static void eliminarEntidadOriginal(
            Entity entidadOriginal
    ) {

        entidadOriginal.stopRiding();
        entidadOriginal.ejectPassengers();
        entidadOriginal.discard();
    }

    private static void convertirEnFinalizador(
            ServerLevel level,
            Mob skeleton
    ) {

        double x =
                skeleton.getX();

        double y =
                skeleton.getY();

        double z =
                skeleton.getZ();

        float rotacionHorizontal =
                skeleton.getYRot();

        float rotacionVertical =
                skeleton.getXRot();

        Mob finalizador =
                EntityTypes.BOGGED.create(
                        level,
                        EntitySpawnReason.CONVERSION
                );

        if (finalizador == null) {
            return;
        }

        finalizador.setPos(
                x,
                y,
                z
        );

        finalizador.setYRot(
                rotacionHorizontal
        );

        finalizador.setXRot(
                rotacionVertical
        );

        finalizador
                .getPersistentData()
                .putBoolean(
                        TAG_FINALIZADOR,
                        true
                );

        finalizador.setCustomName(
                Component.literal(
                        "Esqueleto Finalizador"
                ).withStyle(
                        ChatFormatting.GRAY,
                        ChatFormatting.BOLD
                )
        );

        finalizador.setCustomNameVisible(
                false
        );

        finalizador.setPersistenceRequired();

        establecerVidaInicial(
                finalizador
        );

        aplicarEquipo(
                level,
                finalizador
        );

        boolean anadido =
                level.addFreshEntity(
                        finalizador
                );

        if (!anadido) {
            finalizador.discard();
            return;
        }

        eliminarEntidadOriginal(
                skeleton
        );
    }

    private static void limpiarEquipoAnterior(
            Mob skeleton
    ) {

        skeleton.setItemSlot(
                EquipmentSlot.HEAD,
                ItemStack.EMPTY
        );

        skeleton.setItemSlot(
                EquipmentSlot.CHEST,
                ItemStack.EMPTY
        );

        skeleton.setItemSlot(
                EquipmentSlot.LEGS,
                ItemStack.EMPTY
        );

        skeleton.setItemSlot(
                EquipmentSlot.FEET,
                ItemStack.EMPTY
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                ItemStack.EMPTY
        );

        skeleton
                .getPersistentData()
                .remove(
                        TAG_EQUIPO_APLICADO
                );
    }

    private static void asegurarConfiguracion(
            ServerLevel level,
            Mob skeleton
    ) {

        asegurarVidaMaxima(
                skeleton
        );

        skeleton.setCustomNameVisible(
                false
        );

        aplicarEquipo(
                level,
                skeleton
        );
    }

    private static void establecerVidaInicial(
            Mob skeleton
    ) {

        AttributeInstance atributoVida =
                skeleton.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        atributoVida.setBaseValue(
                VIDA_MAXIMA
        );

        skeleton.setHealth(
                (float) VIDA_MAXIMA
        );
    }

    private static void asegurarVidaMaxima(
            Mob skeleton
    ) {

        AttributeInstance atributoVida =
                skeleton.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        if (atributoVida.getBaseValue()
                == VIDA_MAXIMA) {

            return;
        }

        double vidaMaximaAnterior =
                atributoVida.getBaseValue();

        float vidaAnterior =
                skeleton.getHealth();

        float proporcionVida;

        if (vidaMaximaAnterior <= 0.0D) {

            proporcionVida =
                    1.0F;

        } else {

            proporcionVida =
                    (float) (
                            vidaAnterior
                                    / vidaMaximaAnterior
                    );
        }

        proporcionVida =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                proporcionVida
                        )
                );

        atributoVida.setBaseValue(
                VIDA_MAXIMA
        );

        skeleton.setHealth(
                (float) VIDA_MAXIMA
                        * proporcionVida
        );
    }

    private static void aplicarEquipo(
            ServerLevel level,
            Mob skeleton
    ) {

        ItemStack armaActual =
                skeleton.getItemBySlot(
                        EquipmentSlot.MAINHAND
                );

        boolean tieneArcoCorrecto =
                !armaActual.isEmpty()
                        && armaActual.is(
                        ModItems.ARCO_RESONANTITA.get()
                );

        if (tieneArcoCorrecto
                && skeleton
                .getPersistentData()
                .contains(
                        TAG_EQUIPO_APLICADO
                )) {

            skeleton.setDropChance(
                    EquipmentSlot.MAINHAND,
                    0.0F
            );

            return;
        }

        ItemStack arco =
                new ItemStack(
                        ModItems.ARCO_RESONANTITA.get()
                );

        anadirEncantamiento(
                level,
                arco,
                Enchantments.POWER,
                NIVEL_PODER
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );

        if (skeleton instanceof AbstractSkeleton abstractSkeleton) {

            abstractSkeleton.reassessWeaponGoal();
        }

        skeleton.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );

        skeleton
                .getPersistentData()
                .putBoolean(
                        TAG_EQUIPO_APLICADO,
                        true
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

        stack.enchant(
                holder,
                nivel
        );
    }

    @SubscribeEvent
    public static void onFinalizerInvulnerability(
            EntityInvulnerabilityCheckEvent event
    ) {

        if (!(event.getEntity()
                instanceof LivingEntity skeleton)) {

            return;
        }

        if (!esFinalizador(skeleton)) {
            return;
        }

        if (!event.getSource().is(
                DamageTypeTags.IS_PROJECTILE
        )) {

            return;
        }

        event.setInvulnerable(
                true
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizerDrops(
            LivingDropsEvent event
    ) {

        LivingEntity skeleton =
                event.getEntity();

        if (!esFinalizador(skeleton)) {
            return;
        }

        if (!(skeleton.level()
                instanceof ServerLevel level)) {

            return;
        }

        if (skeleton
                .getRandom()
                .nextFloat()
                >= PROBABILIDAD_FRAGMENTO) {

            return;
        }

        ItemStack fragmento =
                new ItemStack(
                        ModItems.FRAGMENTO_RESONANTITA.get()
                );

        ItemEntity drop =
                new ItemEntity(
                        level,
                        skeleton.getX(),
                        skeleton.getY(),
                        skeleton.getZ(),
                        fragmento
                );

        event.getDrops().add(
                drop
        );
    }

    public static boolean esFinalizador(
            LivingEntity entity
    ) {

        return entity.getType()
                == EntityTypes.BOGGED

                && entity
                .getPersistentData()
                .contains(
                        TAG_FINALIZADOR
                );
    }

    private FinalizerSkeletonEvents() {
    }
}