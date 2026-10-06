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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.estrativarus.amasmas.entity.EntityReplacementHelper;

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

    private static final String TAG_CLASE_1 =
            "amasmas_esqueleto_clase_1";

    private static final String TAG_CLASE_2 =
            "amasmas_esqueleto_clase_2";

    private static final String TAG_CLASE_3 =
            "amasmas_esqueleto_clase_3";

    private static final String TAG_CLASE_4 =
            "amasmas_esqueleto_clase_4";

    private static final String TAG_CLASE_5 =
            "amasmas_esqueleto_clase_5";

    private static final String TAG_MEJORA_DIA_14 =
            "amasmas_esqueleto_mejora_dia_14";

    private static final int DIA_INICIO_CLASES =
            7;

    private static final String TAG_CLASE_SELECCIONADA =
            "amasmas_esqueleto_clase_seleccionada";

    private static final String TAG_SELECCION_REALIZADA =
            "amasmas_esqueleto_seleccion_realizada";

    private static final int DIA_MEJORA =
            14;

    private static final int DURACION_VELOCIDAD =
            20 * 15;

    private static final int AMPLIFICADOR_VELOCIDAD_2 =
            1;

    @SubscribeEvent
    public static void onClassedSkeletonTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob skeleton)) {

            return;
        }

        if (skeleton
                .getPersistentData()
                .contains(
                        UndeadHorseConversionEvents
                                .TAG_JINETE_NO_CLASIFICAR
                )) {

            return;
        }

        if (!esEsqueletoDeClase(skeleton)) {
            return;
        }

        if (!(skeleton.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((skeleton.tickCount
                + skeleton.getId()) % 100 != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_MEJORA) {
            return;
        }

        aplicarMejoraDia14(
                level,
                skeleton
        );
    }

    private static boolean esEsqueletoDeClase(
            Mob skeleton
    ) {

        return skeleton
                .getPersistentData()
                .contains(TAG_CLASE_1)

                || skeleton
                .getPersistentData()
                .contains(TAG_CLASE_2)

                || skeleton
                .getPersistentData()
                .contains(TAG_CLASE_3)

                || skeleton
                .getPersistentData()
                .contains(TAG_CLASE_4)

                || skeleton
                .getPersistentData()
                .contains(TAG_CLASE_5);
    }
    private static void aplicarMejoraDia14(
            ServerLevel level,
            Mob skeleton
    ) {

        if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_4)) {

            renovarVelocidadClaseCuatro(
                    skeleton
            );
        }

        if (skeleton
                .getPersistentData()
                .contains(TAG_MEJORA_DIA_14)) {

            return;
        }

        establecerVida(
                skeleton,
                40.0D
        );

        if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_1)) {

            mejorarClaseUno(
                    level,
                    skeleton
            );

        } else if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_2)) {

            mejorarClaseDos(
                    level,
                    skeleton
            );

        } else if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_3)) {

            mejorarClaseTres(
                    level,
                    skeleton
            );

        } else if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_4)) {

            mejorarClaseCuatro(
                    level,
                    skeleton
            );

        } else if (skeleton
                .getPersistentData()
                .contains(TAG_CLASE_5)) {

            mejorarClaseCinco(
                    level,
                    skeleton
            );

        } else {

            return;
        }

        bloquearDropsEquipamiento(
                skeleton
        );

        skeleton
                .getPersistentData()
                .putBoolean(
                        TAG_MEJORA_DIA_14,
                        true
                );
    }
    private static void mejorarClaseUno(
            ServerLevel level,
            Mob skeleton
    ) {

        ItemStack casco =
                new ItemStack(
                        Items.DIAMOND_HELMET
                );

        ItemStack pechera =
                new ItemStack(
                        Items.DIAMOND_CHESTPLATE
                );

        ItemStack pantalones =
                new ItemStack(
                        Items.DIAMOND_LEGGINGS
                );

        ItemStack botas =
                new ItemStack(
                        Items.DIAMOND_BOOTS
                );

        encantar(
                level,
                casco,
                Enchantments.PROTECTION,
                4
        );

        encantar(
                level,
                pechera,
                Enchantments.PROTECTION,
                4
        );

        encantar(
                level,
                pantalones,
                Enchantments.PROTECTION,
                4
        );

        encantar(
                level,
                botas,
                Enchantments.PROTECTION,
                4
        );

        equiparArmadura(
                skeleton,
                casco,
                pechera,
                pantalones,
                botas
        );
    }
    private static void mejorarClaseDos(
            ServerLevel level,
            Mob skeleton
    ) {

        equiparArmadura(
                skeleton,
                new ItemStack(
                        Items.CHAINMAIL_HELMET
                ),
                new ItemStack(
                        Items.CHAINMAIL_CHESTPLATE
                ),
                new ItemStack(
                        Items.CHAINMAIL_LEGGINGS
                ),
                new ItemStack(
                        Items.CHAINMAIL_BOOTS
                )
        );

        ItemStack arco =
                new ItemStack(
                        Items.BOW
                );

        encantar(
                level,
                arco,
                Enchantments.PUNCH,
                30
        );

        encantar(
                level,
                arco,
                Enchantments.POWER,
                25
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );
    }
    private static void mejorarClaseTres(
            ServerLevel level,
            Mob skeleton
    ) {

        equiparArmadura(
                skeleton,
                new ItemStack(
                        Items.IRON_HELMET
                ),
                new ItemStack(
                        Items.IRON_CHESTPLATE
                ),
                new ItemStack(
                        Items.IRON_LEGGINGS
                ),
                new ItemStack(
                        Items.IRON_BOOTS
                )
        );

        ItemStack hacha =
                new ItemStack(
                        Items.DIAMOND_AXE
                );

        encantar(
                level,
                hacha,
                Enchantments.FIRE_ASPECT,
                10
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                hacha
        );
    }
    private static void mejorarClaseCuatro(
            ServerLevel level,
            Mob skeleton
    ) {

        equiparArmadura(
                skeleton,
                new ItemStack(
                        Items.GOLDEN_HELMET
                ),
                new ItemStack(
                        Items.GOLDEN_CHESTPLATE
                ),
                new ItemStack(
                        Items.GOLDEN_LEGGINGS
                ),
                new ItemStack(
                        Items.GOLDEN_BOOTS
                )
        );

        ItemStack ballesta =
                new ItemStack(
                        Items.CROSSBOW
                );

        encantar(
                level,
                ballesta,
                Enchantments.SHARPNESS,
                25
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                ballesta
        );

        renovarVelocidadClaseCuatro(
                skeleton
        );
    }
    private static void renovarVelocidadClaseCuatro(
            Mob skeleton
    ) {

        MobEffectInstance efectoActual =
                skeleton.getEffect(
                        MobEffects.SPEED
                );

        if (efectoActual != null
                && efectoActual.getAmplifier()
                == AMPLIFICADOR_VELOCIDAD_2
                && efectoActual.getDuration() > 80) {

            return;
        }

        skeleton.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        DURACION_VELOCIDAD,
                        AMPLIFICADOR_VELOCIDAD_2,
                        false,
                        false,
                        false
                )
        );
    }
    private static void mejorarClaseCinco(
            ServerLevel level,
            Mob skeleton
    ) {

        equiparArmadura(
                skeleton,
                new ItemStack(
                        Items.LEATHER_HELMET
                ),
                new ItemStack(
                        Items.LEATHER_CHESTPLATE
                ),
                new ItemStack(
                        Items.LEATHER_LEGGINGS
                ),
                new ItemStack(
                        Items.LEATHER_BOOTS
                )
        );

        ItemStack arco =
                new ItemStack(
                        Items.BOW
                );

        encantar(
                level,
                arco,
                Enchantments.POWER,
                50
        );

        skeleton.setItemSlot(
                EquipmentSlot.MAINHAND,
                arco
        );
    }

    private static void marcarClase(
            Mob mob,
            String tagClase
    ) {

        mob.getPersistentData()
                .putBoolean(
                        tagClase,
                        true
                );
    }
    private static void procesarReemplazoNuevo(
            EntityJoinLevelEvent event,
            ServerLevel level,
            Mob skeleton,
            int clase,
            int diaActual
    ) {

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        skeleton
                )) {

            return;
        }

        SkeletonReplacementData datos =
                SkeletonReplacementData.from(
                        skeleton
                );

        event.setCanceled(
                true
        );

        level.getServer().execute(() ->
                crearWitherSkeletonDesdeDatos(
                        level,
                        datos,
                        clase,
                        diaActual
                )
        );
    }

    private static void crearWitherSkeletonDesdeDatos(
            ServerLevel level,
            SkeletonReplacementData datos,
            int clase,
            int diaActual
    ) {

        Mob witherSkeleton =
                EntityTypes.WITHER_SKELETON.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (witherSkeleton == null) {
            return;
        }

        datos.aplicarA(
                witherSkeleton
        );

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

        if (diaActual >= DIA_MEJORA) {

            aplicarMejoraDia14(
                    level,
                    witherSkeleton
            );
        }

        witherSkeleton.setPersistenceRequired();

        boolean anadido =
                level.addFreshEntity(
                        witherSkeleton
                );

        if (!anadido) {

            witherSkeleton.discard();
        }
    }

    private static boolean esEsqueletoPermitido(
            Mob skeleton
    ) {

        return skeleton.getType()
                == EntityTypes.SKELETON

                || skeleton.getType()
                == EntityTypes.STRAY

                || skeleton.getType()
                == EntityTypes.PARCHED;
    }

    private static boolean debeIgnorarse(
            Mob skeleton
    ) {

        if (skeleton
                .getPersistentData()
                .contains(
                        UndeadHorseConversionEvents
                                .TAG_JINETE_NO_CLASIFICAR
                )) {

            return true;
        }

        return skeleton
                .getPersistentData()
                .contains(
                        EntityReplacementHelper
                                .TAG_REEMPLAZO_EN_CURSO
                );
    }

    private static int obtenerOSeleccionarClase(
            Mob skeleton
    ) {

        if (skeleton
                .getPersistentData()
                .contains(
                        TAG_SELECCION_REALIZADA
                )) {

            return skeleton
                    .getPersistentData()
                    .getInt(
                            TAG_CLASE_SELECCIONADA
                    )
                    .orElse(1);
        }

        int clase =
                skeleton
                        .getRandom()
                        .nextInt(5)
                        + 1;

        skeleton
                .getPersistentData()
                .putInt(
                        TAG_CLASE_SELECCIONADA,
                        clase
                );

        skeleton
                .getPersistentData()
                .putBoolean(
                        TAG_SELECCION_REALIZADA,
                        true
                );

        return clase;
    }

    private static int obtenerDiaActual(
            ServerLevel level
    ) {

        return SistemaDiasSavedData
                .get(level.getServer())
                .getDiaActual();
    }

    private static void reemplazarEsqueletoYaCargado(
            ServerLevel level,
            Mob skeleton,
            int clase,
            int diaActual
    ) {

        if (!skeleton.isAlive()
                || skeleton.isRemoved()) {

            return;
        }

        if (!EntityReplacementHelper
                .iniciarReemplazo(
                        skeleton
                )) {

            return;
        }

        Mob witherSkeleton =
                EntityTypes.WITHER_SKELETON.create(
                        level,
                        EntitySpawnReason.TRIGGERED
                );

        if (witherSkeleton == null) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            skeleton
                    );

            return;
        }

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

        if (diaActual >= DIA_MEJORA) {

            aplicarMejoraDia14(
                    level,
                    witherSkeleton
            );
        }

        witherSkeleton.setPersistenceRequired();

        boolean reemplazado =
                EntityReplacementHelper.reemplazar(
                        level,
                        skeleton,
                        witherSkeleton
                );

        if (!reemplazado) {

            EntityReplacementHelper
                    .cancelarReemplazo(
                            skeleton
                    );
        }
    }

    /*
     * Se ejecuta cuando una entidad entra en el mundo.
     */
    @SubscribeEvent
    public static void onSkeletonJoinLevel(
            EntityJoinLevelEvent event
    ) {

        if (event.isCanceled()) {
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

        if (!esEsqueletoPermitido(
                skeleton
        )) {

            return;
        }

        if (debeIgnorarse(
                skeleton
        )) {

            return;
        }

        if (esEsqueletoDeClase(
                skeleton
        )) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (diaActual < DIA_INICIO_CLASES) {
            return;
        }

        int clase =
                obtenerOSeleccionarClase(
                        skeleton
                );

        if (clase == 2
                || clase == 5) {

            procesarReemplazoNuevo(
                    event,
                    level,
                    skeleton,
                    clase,
                    diaActual
            );

            return;
        }

        level.getServer().execute(() -> {

            if (!skeleton.isAlive()
                    || skeleton.isRemoved()) {

                return;
            }

            if (debeIgnorarse(
                    skeleton
            )) {

                return;
            }

            if (esEsqueletoDeClase(
                    skeleton
            )) {

                return;
            }

            aplicarClaseSinReemplazo(
                    level,
                    skeleton,
                    clase,
                    diaActual
            );
        });
    }

    @SubscribeEvent
    public static void onUnclassifiedSkeletonTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof Mob skeleton)) {

            return;
        }

        if (!esEsqueletoPermitido(
                skeleton
        )) {

            return;
        }

        if (debeIgnorarse(
                skeleton
        )) {

            return;
        }

        if (esEsqueletoDeClase(
                skeleton
        )) {

            return;
        }

        if (!(skeleton.level()
                instanceof ServerLevel level)) {

            return;
        }

        if ((skeleton.tickCount + skeleton.getId())
                % 100 != 0) {

            return;
        }

        int diaActual =
                obtenerDiaActual(
                        level
                );

        if (diaActual < DIA_INICIO_CLASES) {
            return;
        }

        int clase =
                obtenerOSeleccionarClase(
                        skeleton
                );

        if (clase == 2
                || clase == 5) {

            reemplazarEsqueletoYaCargado(
                    level,
                    skeleton,
                    clase,
                    diaActual
            );

            return;
        }

        aplicarClaseSinReemplazo(
                level,
                skeleton,
                clase,
                diaActual
        );
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
        marcarClase(
                mob,
                TAG_CLASE_1
        );

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
        marcarClase(
                mob,
                TAG_CLASE_3
        );

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
        marcarClase(
                mob,
                TAG_CLASE_4
        );

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
    private static void aplicarClaseSinReemplazo(
            ServerLevel level,
            Mob skeleton,
            int clase,
            int diaActual
    ) {

        switch (clase) {

            case 1 ->
                    configurarClaseUno(
                            level,
                            skeleton
                    );

            case 3 ->
                    configurarClaseTres(
                            level,
                            skeleton
                    );

            case 4 ->
                    configurarClaseCuatro(
                            level,
                            skeleton
                    );

            default -> {
                return;
            }
        }

        if (diaActual >= DIA_MEJORA) {

            aplicarMejoraDia14(
                    level,
                    skeleton
            );
        }
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
        marcarClase(
                mob,
                TAG_CLASE_2
        );

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
        marcarClase(
                mob,
                TAG_CLASE_5
        );

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

    public static void configurarClaseCincoExterna(
            ServerLevel level,
            Mob witherSkeleton
    ) {

        if (witherSkeleton.getType()
                != EntityTypes.WITHER_SKELETON) {

            return;
        }

        configurarClaseCinco(
                level,
                witherSkeleton
        );

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual >= DIA_MEJORA) {

            aplicarMejoraDia14(
                    level,
                    witherSkeleton
            );
        }

        witherSkeleton.setPersistenceRequired();

        bloquearDropsEquipamiento(
                witherSkeleton
        );
    }

    private record SkeletonReplacementData(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            float yHeadRot,
            double movimientoX,
            double movimientoY,
            double movimientoZ,
            boolean persistente,
            Component nombre,
            boolean nombreVisible
    ) {

        private static SkeletonReplacementData from(
                Mob skeleton
        ) {

            return new SkeletonReplacementData(
                    skeleton.getX(),
                    skeleton.getY(),
                    skeleton.getZ(),
                    skeleton.getYRot(),
                    skeleton.getXRot(),
                    skeleton.getYHeadRot(),
                    skeleton.getDeltaMovement().x,
                    skeleton.getDeltaMovement().y,
                    skeleton.getDeltaMovement().z,
                    skeleton.isPersistenceRequired(),
                    skeleton.getCustomName() == null
                            ? null
                            : skeleton
                            .getCustomName()
                            .copy(),
                    skeleton.isCustomNameVisible()
            );
        }

        private void aplicarA(
                Mob witherSkeleton
        ) {

            witherSkeleton.setPos(
                    x,
                    y,
                    z
            );

            witherSkeleton.setYRot(
                    yRot
            );

            witherSkeleton.setXRot(
                    xRot
            );

            witherSkeleton.setYHeadRot(
                    yHeadRot
            );

            witherSkeleton.setDeltaMovement(
                    movimientoX,
                    movimientoY,
                    movimientoZ
            );

            if (persistente) {

                witherSkeleton.setPersistenceRequired();
            }

            if (nombre != null) {

                witherSkeleton.setCustomName(
                        nombre
                );

                witherSkeleton.setCustomNameVisible(
                        nombreVisible
                );
            }
        }
    }

    private SkeletonClassEvents() {
    }
}
