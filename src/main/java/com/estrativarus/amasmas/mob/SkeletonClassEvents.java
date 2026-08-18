package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class SkeletonClassEvents {

    /*
     * Identifica a una criatura que ya recibió una clase.
     *
     * En tus mappings no utilizaremos getTags(), por lo
     * que la identificación se realizará mediante el nombre.
     */
    private static final String NOMBRE_CLASE_1 =
            "Acorazado";

    private static final String NOMBRE_CLASE_2 =
            "Badaboing";

    private static final String NOMBRE_CLASE_3 =
            "Pyro";

    private static final String NOMBRE_CLASE_4 =
            "Peón dorado";

    private static final String NOMBRE_CLASE_5 =
            "Mulayin";

    /*
     * Se ejecuta cuando una entidad entra en el mundo.
     */
    @SubscribeEvent
    public static void onSkeletonJoinLevel(
            EntityJoinLevelEvent event
    ) {

        /*
         * Solo trabajamos en el servidor.
         */
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Solo procesamos mobs.
         */
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        /*
         * Solo se aplica a:
         *
         * - Skeleton;
         * - Stray;
         * - Parched.
         */
        boolean esEsqueletoPermitido =
                mob.getType() == EntityTypes.SKELETON
                        || mob.getType() == EntityTypes.STRAY
                        || mob.getType() == EntityTypes.PARCHED;

        if (!esEsqueletoPermitido) {
            return;
        }

        /*
         * No repetimos la selección al cargar la entidad
         * nuevamente desde el disco.
         */
        if (event.loadedFromDisk()) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        /*
         * Antes del día 7 conservan su comportamiento vanilla.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * Seleccionamos del 1 al 5.
         *
         * nextInt(5) produce 0, 1, 2, 3 o 4.
         * Sumamos 1 para obtener 1, 2, 3, 4 o 5.
         *
         * Cada clase tiene exactamente un 20 %.
         */
        int clase =
                mob.getRandom().nextInt(5) + 1;

        /*
         * Esperamos al siguiente ciclo del servidor para
         * sobrescribir correctamente el equipo vanilla.
         */
        level.getServer().execute(() -> {

            if (!mob.isAlive()
                    || mob.isRemoved()) {
                return;
            }

            aplicarClase(
                    level,
                    mob,
                    clase
            );
        });
    }

    private static void aplicarClase(
            ServerLevel level,
            Mob mobOriginal,
            int clase
    ) {

        switch (clase) {

            /*
             * CLASE 1
             *
             * Esqueleto, Stray o Parched original.
             * Armadura completa de diamante.
             * Diez corazones.
             */
            case 1 ->
                    configurarClaseUno(
                            level,
                            mobOriginal
                    );

            /*
             * CLASE 2
             *
             * Se convierte físicamente en Wither Skeleton.
             * Arco con Empuje XX.
             * Armadura completa de cota de malla.
             * Veinte corazones.
             */
            case 2 ->
                    reemplazarPorWitherSkeleton(
                            level,
                            mobOriginal,
                            2
                    );

            /*
             * CLASE 3
             *
             * Conserva su especie original.
             * Hacha de hierro con Aspecto ígneo II.
             * Armadura completa de hierro.
             * Diez corazones.
             */
            case 3 ->
                    configurarClaseTres(
                            level,
                            mobOriginal
                    );

            /*
             * CLASE 4
             *
             * Conserva su especie original.
             * Ballesta con Filo XX.
             * Armadura completa de oro.
             * Veinte corazones.
             */
            case 4 ->
                    configurarClaseCuatro(
                            level,
                            mobOriginal
                    );

            /*
             * CLASE 5
             *
             * Se convierte físicamente en Wither Skeleton.
             * Arco con Poder X.
             * Armadura completa de cuero.
             * Veinte corazones.
             */
            default ->
                    reemplazarPorWitherSkeleton(
                            level,
                            mobOriginal,
                            5
                    );
        }
    }

    /*
     * CLASE 1:
     *
     * Armadura completa de diamante.
     * Diez corazones.
     * Sin arma especial.
     */
    private static void configurarClaseUno(
            ServerLevel level,
            Mob mob
    ) {

        establecerNombre(
                mob,
                NOMBRE_CLASE_1,
                ChatFormatting.AQUA
        );

        establecerVida(
                mob,
                20.0D
        );

        equiparArmadura(
                mob,
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS)
        );

        bloquearDropsEquipamiento(mob);
    }

    /*
     * CLASE 3:
     *
     * Armadura completa de hierro.
     * Hacha de hierro con Aspecto ígneo II.
     * Diez corazones.
     */
    private static void configurarClaseTres(
            ServerLevel level,
            Mob mob
    ) {

        establecerNombre(
                mob,
                NOMBRE_CLASE_3,
                ChatFormatting.RED
        );

        establecerVida(
                mob,
                20.0D
        );

        equiparArmadura(
                mob,
                new ItemStack(Items.IRON_HELMET),
                new ItemStack(Items.IRON_CHESTPLATE),
                new ItemStack(Items.IRON_LEGGINGS),
                new ItemStack(Items.IRON_BOOTS)
        );

        ItemStack hacha =
                new ItemStack(
                        Items.IRON_AXE
                );

        encantar(
                level,
                hacha,
                Enchantments.FIRE_ASPECT,
                2
        );

        mob.setItemSlot(
                EquipmentSlot.MAINHAND,
                hacha
        );

        bloquearDropsEquipamiento(mob);
    }

    /*
     * CLASE 4:
     *
     * Armadura completa de oro.
     * Ballesta con Filo XX.
     * Veinte corazones.
     */
    private static void configurarClaseCuatro(
            ServerLevel level,
            Mob mob
    ) {

        establecerNombre(
                mob,
                NOMBRE_CLASE_4,
                ChatFormatting.GOLD
        );

        establecerVida(
                mob,
                40.0D
        );

        equiparArmadura(
                mob,
                new ItemStack(Items.GOLDEN_HELMET),
                new ItemStack(Items.GOLDEN_CHESTPLATE),
                new ItemStack(Items.GOLDEN_LEGGINGS),
                new ItemStack(Items.GOLDEN_BOOTS)
        );

        ItemStack ballesta =
                new ItemStack(
                        Items.CROSSBOW
                );

        encantar(
                level,
                ballesta,
                Enchantments.SHARPNESS,
                20
        );

        mob.setItemSlot(
                EquipmentSlot.MAINHAND,
                ballesta
        );

        bloquearDropsEquipamiento(mob);
    }

    /*
     * Crea un Wither Skeleton para las clases 2 y 5.
     */
    private static void reemplazarPorWitherSkeleton(
            ServerLevel level,
            Mob mobOriginal,
            int clase
    ) {

        /*
         * TRIGGERED evita que esta criatura se considere
         * una aparición natural nueva.
         */
        Mob witherSkeleton =
                EntityTypes.WITHER_SKELETON.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (witherSkeleton == null) {
            return;
        }

        /*
         * Copiamos posición y rotación.
         */
        witherSkeleton.setPos(
                mobOriginal.getX(),
                mobOriginal.getY(),
                mobOriginal.getZ()
        );

        witherSkeleton.setYRot(
                mobOriginal.getYRot()
        );

        witherSkeleton.setXRot(
                mobOriginal.getXRot()
        );

        /*
         * Inicializamos la criatura antes de equiparla.
         */
        witherSkeleton.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(
                        witherSkeleton.blockPosition()
                ),
                EntitySpawnReason.TRIGGERED,
                null
        );

        /*
         * Ambas clases tienen veinte corazones.
         */
        establecerVida(
                witherSkeleton,
                40.0D
        );

        if (clase == 2) {

            configurarClaseDos(
                    level,
                    witherSkeleton
            );

        } else {

            configurarClaseCinco(
                    level,
                    witherSkeleton
            );
        }

        witherSkeleton.setPersistenceRequired();

        /*
         * Añadimos primero el nuevo mob.
         */
        level.addFreshEntity(
                witherSkeleton
        );

        /*
         * Después eliminamos el mob original.
         *
         * discard() no genera botín ni experiencia.
         */
        mobOriginal.discard();
    }

    /*
     * CLASE 2:
     *
     * Wither Skeleton.
     * Armadura completa de cota de malla.
     * Arco con Empuje XX.
     */
    private static void configurarClaseDos(
            ServerLevel level,
            Mob mob
    ) {

        establecerNombre(
                mob,
                NOMBRE_CLASE_2,
                ChatFormatting.DARK_PURPLE
        );

        equiparArmadura(
                mob,
                new ItemStack(Items.CHAINMAIL_HELMET),
                new ItemStack(Items.CHAINMAIL_CHESTPLATE),
                new ItemStack(Items.CHAINMAIL_LEGGINGS),
                new ItemStack(Items.CHAINMAIL_BOOTS)
        );

        ItemStack arco =
                new ItemStack(
                        Items.BOW
                );

        encantar(
                level,
                arco,
                Enchantments.PUNCH,
                20
        );

        mob.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );

        bloquearDropsEquipamiento(mob);
    }

    /*
     * CLASE 5:
     *
     * Wither Skeleton.
     * Armadura completa de cuero.
     * Arco con Poder X.
     */
    private static void configurarClaseCinco(
            ServerLevel level,
            Mob mob
    ) {

        establecerNombre(
                mob,
                NOMBRE_CLASE_5,
                ChatFormatting.DARK_RED
        );

        equiparArmadura(
                mob,
                new ItemStack(Items.LEATHER_HELMET),
                new ItemStack(Items.LEATHER_CHESTPLATE),
                new ItemStack(Items.LEATHER_LEGGINGS),
                new ItemStack(Items.LEATHER_BOOTS)
        );

        ItemStack arco =
                new ItemStack(
                        Items.BOW
                );

        encantar(
                level,
                arco,
                Enchantments.POWER,
                10
        );

        mob.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );

        bloquearDropsEquipamiento(mob);
    }

    private static void establecerNombre(
            Mob mob,
            String nombre,
            ChatFormatting color
    ) {

        mob.setCustomName(
                Component.literal(nombre)
                        .withStyle(
                                color,
                                ChatFormatting.BOLD
                        )
        );

        mob.setCustomNameVisible(false);
        mob.setPersistenceRequired();
    }

    /*
     * Establece la salud máxima y cura completamente al mob.
     *
     * 20 puntos = 10 corazones.
     * 40 puntos = 20 corazones.
     */
    private static void establecerVida(
            Mob mob,
            double vidaMaxima
    ) {

        AttributeInstance atributoVida =
                mob.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoVida == null) {
            return;
        }

        atributoVida.setBaseValue(
                vidaMaxima
        );

        mob.setHealth(
                (float) vidaMaxima
        );
    }

    private static void equiparArmadura(
            Mob mob,
            ItemStack casco,
            ItemStack pechera,
            ItemStack pantalones,
            ItemStack botas
    ) {

        mob.setItemSlot(
                EquipmentSlot.HEAD,
                casco
        );

        mob.setItemSlot(
                EquipmentSlot.CHEST,
                pechera
        );

        mob.setItemSlot(
                EquipmentSlot.LEGS,
                pantalones
        );

        mob.setItemSlot(
                EquipmentSlot.FEET,
                botas
        );
    }

    /*
     * Impide que caigan tanto la armadura como
     * el objeto de la mano principal.
     */
    private static void bloquearDropsEquipamiento(
            Mob mob
    ) {

        mob.setDropChance(
                EquipmentSlot.HEAD,
                0.0F
        );

        mob.setDropChance(
                EquipmentSlot.CHEST,
                0.0F
        );

        mob.setDropChance(
                EquipmentSlot.LEGS,
                0.0F
        );

        mob.setDropChance(
                EquipmentSlot.FEET,
                0.0F
        );

        mob.setDropChance(
                EquipmentSlot.MAINHAND,
                0.0F
        );
    }

    /*
     * Añade un encantamiento a un ItemStack.
     */
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

        registro.get(encantamiento)
                .ifPresent(holder ->
                        stack.enchant(
                                holder,
                                nivel
                        )
                );
    }

    private SkeletonClassEvents() {
    }
}
