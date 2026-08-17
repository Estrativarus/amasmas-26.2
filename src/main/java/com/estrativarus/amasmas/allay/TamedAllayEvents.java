package com.estrativarus.amasmas.allay;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;


import java.util.UUID;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class TamedAllayEvents {

    /*
     * Distancia máxima deseada respecto al propietario.
     */
    private static final double DISTANCIA_SEGUIMIENTO = 8.0D;

    /*
     * Si supera esta distancia, se teletransporta cerca
     * del propietario para evitar perderse.
     */
    private static final double DISTANCIA_TELETRANSPORTE = 24.0D;

    /*
     * Radio dentro del cual puede perseguir objetivos.
     */
    private static final double DISTANCIA_COMBATE = 12.0D;

    /*
     * Distancia necesaria para golpear.
     */
    private static final double DISTANCIA_ATAQUE = 2.5D;

    /*
     * Un ataque cada 20 ticks, aproximadamente un segundo.
     */
    private static final int ENFRIAMIENTO_ATAQUE = 20;

    /*
     * Cada manzana dorada tiene un 25 % de probabilidad
     * de domesticar al Allay.
     */
    private static final float PROBABILIDAD_DOMESTICACION =
            0.25F;

    @SubscribeEvent
    public static void onInteractWithAllay(
            PlayerInteractEvent.EntityInteract event
    ) {

        /*
         * Solo procesamos la mano principal.
         *
         * Minecraft puede lanzar eventos de interacción para
         * ambas manos y no queremos consumir dos objetos.
         */
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        /*
         * Comprobamos el tipo registrado en vez de importar
         * la clase concreta del Allay.
         */
        if (event.getTarget().getType() != EntityTypes.ALLAY) {
            return;
        }

        if (!(event.getTarget() instanceof Mob allay)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        MinecraftServer server = level.getServer();

        TamedAllaySavedData datos =
                TamedAllaySavedData.get(server);

        ItemStack objetoEnMano =
                player.getItemInHand(event.getHand());

        /*
         * CASO 1:
         *
         * El Allay todavía no está domesticado.
         */
        /*
         * CASO 1:
         *
         * El Allay todavía no está domesticado.
         */
        if (!datos.estaDomesticado(allay.getUUID())) {

            /*
             * Solo puede intentarse con una manzana dorada.
             */
            if (!objetoEnMano.is(Items.GOLDEN_APPLE)) {
                return;
            }

            /*
             * Consumimos una manzana en cada intento,
             * salvo que el jugador esté en creativo.
             */
            if (!player.getAbilities().instabuild) {
                objetoEnMano.shrink(1);
            }

            /*
             * Realizamos la tirada de domesticación.
             *
             * nextFloat genera un número entre 0 y 1.
             * Si es inferior a 0,25, el intento tiene éxito.
             */
            boolean domesticacionExitosa =
                    allay.getRandom().nextFloat()
                            < PROBABILIDAD_DOMESTICACION;

            if (domesticacionExitosa) {

                datos.domesticar(
                        allay.getUUID(),
                        player.getUUID()
                );

                /*
                 * El Allay armado deja de utilizarse como
                 * recolector vanilla.
                 */
                allay.setCanPickUpLoot(false);

                allay.playSound(
                        SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                        1.0F,
                        1.2F
                );

                player.sendSystemMessage(
                        Component.empty()

                                .append(
                                        Component.literal(
                                                "¡Has domesticado al "
                                        ).withStyle(
                                                ChatFormatting.GREEN
                                        )
                                )

                                .append(
                                        Component.literal("Allay")
                                                .withStyle(
                                                        ChatFormatting.AQUA,
                                                        ChatFormatting.BOLD
                                                )
                                )

                                .append(
                                        Component.literal("!")
                                                .withStyle(
                                                        ChatFormatting.GREEN
                                                )
                                )
                );

            } else {

                /*
                 * El intento ha fallado.
                 */
                allay.playSound(
                        SoundEvents.ALLAY_HURT,
                        0.7F,
                        1.4F
                );

                player.sendSystemMessage(
                        Component.empty()

                                .append(
                                        Component.literal(
                                                "La domesticación ha "
                                        ).withStyle(
                                                ChatFormatting.GRAY
                                        )
                                )

                                .append(
                                        Component.literal("fallado")
                                                .withStyle(
                                                        ChatFormatting.RED,
                                                        ChatFormatting.BOLD
                                                )
                                )

                                .append(
                                        Component.literal(
                                                ". Inténtalo con otra manzana dorada."
                                        ).withStyle(
                                                ChatFormatting.GRAY
                                        )
                                )
                );
            }

            /*
             * Cancelamos la interacción vanilla tanto si la
             * domesticación ha funcionado como si ha fallado.
             *
             * Así el Allay no toma la manzana como objeto
             * para su comportamiento normal de recolección.
             */
            event.setCanceled(true);
            event.setCancellationResult(
                    InteractionResult.SUCCESS
            );

            return;
        }

        /*
         * CASO 2:
         *
         * Está domesticado, pero pertenece a otra persona.
         */
        if (!datos.esPropietario(
                allay.getUUID(),
                player.getUUID()
        )) {

            player.sendSystemMessage(
                    Component.literal(
                            "Este Allay pertenece a otro jugador."
                    ).withStyle(ChatFormatting.RED)
            );

            event.setCanceled(true);
            event.setCancellationResult(
                    InteractionResult.FAIL
            );

            return;
        }

        /*
         * CASO 3:
         *
         * El propietario utiliza un estofado de remolacha
         * para curar completamente al Allay.
         */
        if (objetoEnMano.is(Items.BEETROOT_SOUP)) {

            /*
             * Si ya está completamente curado, no consumimos
             * el estofado.
             */
            if (allay.getHealth() >= allay.getMaxHealth()) {

                player.sendSystemMessage(
                        Component.empty()

                                .append(
                                        Component.literal("El ")
                                                .withStyle(
                                                        ChatFormatting.GRAY
                                                )
                                )

                                .append(
                                        Component.literal("Allay")
                                                .withStyle(
                                                        ChatFormatting.AQUA,
                                                        ChatFormatting.BOLD
                                                )
                                )

                                .append(
                                        Component.literal(
                                                " ya tiene toda la vida."
                                        ).withStyle(
                                                ChatFormatting.GRAY
                                        )
                                )
                );

                event.setCanceled(true);
                event.setCancellationResult(
                        InteractionResult.SUCCESS
                );

                return;
            }

            /*
             * Restauramos toda su salud.
             */
            allay.setHealth(
                    allay.getMaxHealth()
            );

            /*
             * En supervivencia consumimos el estofado
             * y devolvemos un cuenco vacío.
             */
            if (!player.getAbilities().instabuild) {

                objetoEnMano.shrink(1);

                ItemStack cuenco =
                        new ItemStack(Items.BOWL);

                /*
                 * Intentamos colocar el cuenco en el inventario.
                 * Si no hay espacio, cae al suelo.
                 */
                if (!player.getInventory().add(cuenco)) {
                    player.drop(
                            cuenco,
                            false
                    );
                }
            }

            /*
             * Efectos visuales y sonoros.
             */
            allay.playSound(
                    SoundEvents.PLAYER_BURP,
                    0.8F,
                    1.5F
            );

            player.sendSystemMessage(
                    Component.empty()

                            .append(
                                    Component.literal("El ")
                                            .withStyle(
                                                    ChatFormatting.GREEN
                                            )
                            )

                            .append(
                                    Component.literal("Allay")
                                            .withStyle(
                                                    ChatFormatting.AQUA,
                                                    ChatFormatting.BOLD
                                            )
                            )

                            .append(
                                    Component.literal(
                                            " ha recuperado toda su vida."
                                    ).withStyle(
                                            ChatFormatting.GREEN
                                    )
                            )
            );

            event.setCanceled(true);
            event.setCancellationResult(
                    InteractionResult.SUCCESS
            );

            return;
        }

        /*
         * CASO 4:
         *
         * El propietario le entrega una espada o un hacha.
         */
        if (esArmaPermitida(objetoEnMano)) {

            /*
             * Guardamos una copia del arma que ya tenía el Allay.
             *
             * Si no tenía ninguna, será un ItemStack vacío.
             */
            ItemStack armaAnterior =
                    allay.getMainHandItem().copy();

            /*
             * Creamos una copia del arma que lleva el jugador.
             */
            ItemStack armaNueva =
                    objetoEnMano.copy();

            /*
             * El Allay solamente equipará una unidad.
             */
            armaNueva.setCount(1);

            /*
             * Equipamos el arma nueva en la mano principal.
             */
            allay.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    armaNueva
            );

            /*
             * En supervivencia consumimos una unidad del arma
             * que tiene el jugador en la mano.
             */
            if (!player.getAbilities().instabuild) {
                objetoEnMano.shrink(1);
            }

            /*
             * Si el Allay ya tenía un arma, intentamos devolverla
             * al inventario del propietario.
             */
            if (!armaAnterior.isEmpty()) {

                /*
                 * Si el inventario está lleno, el arma anterior
                 * caerá al suelo junto al jugador.
                 */
                if (!player.getInventory().add(armaAnterior)) {
                    player.drop(
                            armaAnterior,
                            false
                    );
                }
            }

            /*
             * Informamos al propietario del arma equipada.
             */
            player.sendSystemMessage(
                    Component.empty()

                            .append(
                                    Component.literal(
                                            "El Allay ahora utilizará "
                                    ).withStyle(
                                            ChatFormatting.GREEN
                                    )
                            )

                            .append(
                                    armaNueva
                                            .getHoverName()
                                            .copy()
                                            .withStyle(
                                                    ChatFormatting.GOLD,
                                                    ChatFormatting.BOLD
                                            )
                            )

                            .append(
                                    Component.literal(".")
                                            .withStyle(
                                                    ChatFormatting.GREEN
                                            )
                            )
            );

            /*
             * Cancelamos la interacción vanilla para impedir que
             * el Allay procese el arma como un objeto recolectable.
             */
            event.setCanceled(true);
            event.setCancellationResult(
                    InteractionResult.SUCCESS
            );

            return;
        }
    }
    private static boolean esArmaPermitida(
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return false;
        }

        String nombreRegistrado =
                BuiltInRegistries.ITEM
                        .getKey(stack.getItem())
                        .getPath();

        return nombreRegistrado.endsWith("_sword")
                || nombreRegistrado.endsWith("_axe");
    }
    @SubscribeEvent
    public static void onAllayTick(
            EntityTickEvent.Post event
    ) {

        /*
         * Solo nos interesan los Allays.
         */
        if (event.getEntity().getType() != EntityTypes.ALLAY) {
            return;
        }

        if (!(event.getEntity() instanceof Mob allay)) {
            return;
        }

        if (!(allay.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Ejecutamos la lógica cinco veces por segundo.
         */
        if (allay.tickCount % 4 != 0) {
            return;
        }

        MinecraftServer server = level.getServer();

        TamedAllaySavedData datos =
                TamedAllaySavedData.get(server);

        UUID propietarioUuid =
                datos.getPropietario(allay.getUUID());

        /*
         * No está domesticado.
         */
        if (propietarioUuid == null) {
            return;
        }

        ServerPlayer propietario =
                server
                        .getPlayerList()
                        .getPlayer(propietarioUuid);

        /*
         * El propietario está desconectado.
         */
        if (propietario == null) {
            return;
        }

        /*
         * Si están en dimensiones diferentes, no intentamos
         * navegar hasta el propietario.
         */
        if (propietario.level() != allay.level()) {
            return;
        }

        double distanciaAlPropietario =
                allay.distanceToSqr(propietario);

        /*
         * Si está demasiado lejos, se teletransporta cerca.
         */
        if (distanciaAlPropietario
                > DISTANCIA_TELETRANSPORTE
                * DISTANCIA_TELETRANSPORTE) {

            allay.teleportTo(
                    propietario.getX() + 1.0D,
                    propietario.getY() + 1.0D,
                    propietario.getZ() + 1.0D
            );

            return;
        }

        /*
         * Buscamos un objetivo de combate.
         */
        LivingEntity objetivo =
                obtenerObjetivo(propietario);

        /*
         * Si tiene arma y existe un objetivo válido,
         * lo persigue y ataca.
         */
        if (!allay.getMainHandItem().isEmpty()
                && objetivo != null
                && objetivo.isAlive()
                && objetivo.level() == allay.level()
                && propietario.distanceToSqr(objetivo)
                <= DISTANCIA_COMBATE
                * DISTANCIA_COMBATE) {

            allay.getNavigation().moveTo(
                    objetivo,
                    1.4D
            );

            if (allay.distanceToSqr(objetivo)
                    <= DISTANCIA_ATAQUE
                    * DISTANCIA_ATAQUE
                    && allay.tickCount
                    % ENFRIAMIENTO_ATAQUE == 0) {

                atacar(
                        level,
                        allay,
                        objetivo
                );
            }

            return;
        }

        /*
         * Si no hay combate, permanece cerca del propietario.
         */
        if (distanciaAlPropietario
                > DISTANCIA_SEGUIMIENTO
                * DISTANCIA_SEGUIMIENTO) {

            allay.getNavigation().moveTo(
                    propietario,
                    1.25D
            );

        } else if (distanciaAlPropietario < 9.0D) {

            /*
             * Si está a menos de tres bloques, dejamos de empujarlo
             * continuamente hacia el propietario.
             */
            allay.getNavigation().stop();
        }
    }
    private static LivingEntity obtenerObjetivo(
            ServerPlayer propietario
    ) {

        /*
         * Prioridad 1:
         *
         * La última criatura que atacó al propietario.
         */
        LivingEntity atacante =
                propietario.getLastHurtByMob();

        if (esObjetivoValido(
                propietario,
                atacante
        )) {
            return atacante;
        }

        /*
         * Prioridad 2:
         *
         * La última criatura atacada por el propietario.
         */
        LivingEntity atacado =
                propietario.getLastHurtMob();

        if (esObjetivoValido(
                propietario,
                atacado
        )) {
            return atacado;
        }

        return null;
    }

    private static boolean esObjetivoValido(
            ServerPlayer propietario,
            LivingEntity objetivo
    ) {

        if (objetivo == null) {
            return false;
        }

        if (!objetivo.isAlive()) {
            return false;
        }

        /*
         * Nunca atacará a su propietario.
         */
        if (objetivo.getUUID().equals(
                propietario.getUUID()
        )) {
            return false;
        }

        /*
         * Nunca atacará a otro Allay.
         */
        return objetivo.getType() != EntityTypes.ALLAY;
    }
    private static void atacar(
            ServerLevel level,
            Mob allay,
            LivingEntity objetivo
    ) {

        ItemStack arma =
                allay.getMainHandItem();

        float dano =
                calcularDano(arma);

        /*
         * Aplicamos daño atribuido al Allay.
         *
         * Entity#hurtServer forma parte de la canalización
         * del daño para entidades vivas.
         */
        boolean ataqueExitoso =
                objetivo.hurtServer(
                        level,
                        level
                                .damageSources()
                                .mobAttack(allay),
                        dano
                );

        if (ataqueExitoso) {

            allay.swing(
                    InteractionHand.MAIN_HAND
            );

            allay.playSound(
                    SoundEvents.PLAYER_ATTACK_STRONG,
                    0.8F,
                    1.4F
            );
        }
    }
    private static float calcularDano(
            ItemStack arma
    ) {

        String nombre =
                BuiltInRegistries.ITEM
                        .getKey(arma.getItem())
                        .getPath();

        /*
         * Espadas.
         */
        if (nombre.endsWith("wooden_sword")) {
            return 4.0F;
        }

        if (nombre.endsWith("golden_sword")) {
            return 4.0F;
        }

        if (nombre.endsWith("stone_sword")) {
            return 5.0F;
        }

        if (nombre.endsWith("iron_sword")) {
            return 6.0F;
        }

        if (nombre.endsWith("diamond_sword")) {
            return 7.0F;
        }

        if (nombre.endsWith("netherite_sword")) {
            return 8.0F;
        }

        /*
         * Hachas.
         *
         * Hacen más daño, pero el Allay mantiene el mismo
         * enfriamiento para no complicar todavía el sistema.
         */
        if (nombre.endsWith("wooden_axe")) {
            return 7.0F;
        }

        if (nombre.endsWith("golden_axe")) {
            return 7.0F;
        }

        if (nombre.endsWith("stone_axe")) {
            return 9.0F;
        }

        if (nombre.endsWith("iron_axe")) {
            return 9.0F;
        }

        if (nombre.endsWith("diamond_axe")) {
            return 9.0F;
        }

        if (nombre.endsWith("netherite_axe")) {
            return 10.0F;
        }

        /*
         * Armas de otros mods.
         */
        if (nombre.endsWith("_axe")) {
            return 8.0F;
        }

        if (nombre.endsWith("_sword")) {
            return 6.0F;
        }

        return 1.0F;
    }
    @SubscribeEvent
    public static void onAllayDeath(
            LivingDeathEvent event
    ) {

        if (event.getEntity().getType()
                != EntityTypes.ALLAY) {
            return;
        }

        if (!(event.getEntity().level()
                instanceof ServerLevel level)) {
            return;
        }

        TamedAllaySavedData
                .get(level.getServer())
                .eliminarAllay(
                        event.getEntity().getUUID()
                );
    }
    @SubscribeEvent
    public static void onAllayInvulnerabilityCheck(
            EntityInvulnerabilityCheckEvent event
    ) {

        if (event.getEntity().getType()
                != EntityTypes.ALLAY) {

            return;
        }

        if (!(event.getEntity() instanceof Mob allay)) {
            return;
        }

        if (!(allay.level() instanceof ServerLevel level)) {
            return;
        }

        TamedAllaySavedData datos =
                TamedAllaySavedData.get(
                        level.getServer()
                );

        if (!datos.estaDomesticado(
                allay.getUUID()
        )) {

            return;
        }

        event.setInvulnerable(false);
    }
    private TamedAllayEvents() {
    }
}