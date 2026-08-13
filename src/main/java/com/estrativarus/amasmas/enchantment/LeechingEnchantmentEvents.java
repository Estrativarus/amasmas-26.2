package com.estrativarus.amasmas.enchantment;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class LeechingEnchantmentEvents {

    @SubscribeEvent
    public static void onLivingDeath(
            LivingDeathEvent event
    ) {

        /*
         * Obtenemos la entidad responsable de la muerte.
         *
         * Si ha muerto por caída, fuego, cactus u otra causa
         * sin atacante, no se activa Drenaje.
         */
        Entity atacante =
                event.getSource().getEntity();

        /*
         * Solo permitimos que Drenaje cure a jugadores.
         */
        if (!(atacante instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Obtenemos la criatura que acaba de morir.
         */
        LivingEntity victima =
                event.getEntity();

        /*
         * Evitamos cualquier caso anómalo en el que atacante
         * y víctima sean la misma entidad.
         */
        if (victima.getUUID().equals(player.getUUID())) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * El encantamiento debe estar en el arma sostenida
         * en la mano principal en el momento de la muerte.
         */
        ItemStack arma =
                player.getMainHandItem();

        if (arma.isEmpty()) {
            return;
        }

        int nivelDrenaje =
                obtenerNivelDrenaje(
                        level,
                        arma
                );

        /*
         * El arma no tiene Drenaje.
         */
        if (nivelDrenaje <= 0) {
            return;
        }

        /*
         * Porcentajes:
         *
         * Nivel I   = 10 %
         * Nivel II  = 15 %
         * Nivel III = 20 %
         */
        float porcentaje =
                switch (nivelDrenaje) {
                    case 1 -> 0.10F;
                    case 2 -> 0.15F;
                    default -> 0.20F;
                };

        /*
         * La curación se calcula utilizando la vida máxima
         * de la víctima, no la salud que le quedaba antes
         * del último golpe.
         *
         * Ejemplo:
         *
         * Zombi con 20 de vida máxima:
         * Drenaje I -> 2 puntos de salud = 1 corazón.
         *
         * Warden con 500 de vida máxima:
         * Drenaje III -> 100 puntos, limitado por la
         * vida máxima que pueda recuperar el jugador.
         */
        float cantidadCuracion =
                victima.getMaxHealth()
                        * porcentaje;

        /*
         * Si el jugador ya está completamente curado,
         * no reproducimos sonido ni hacemos cálculos extra.
         */
        if (player.getHealth()
                >= player.getMaxHealth()) {
            return;
        }

        player.heal(cantidadCuracion);

        /*
         * Sonido suave para indicar que Drenaje se activó.
         */
        player.playSound(
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                0.6F,
                0.7F
        );
    }

    /*
     * Localiza el encantamiento cargado desde el JSON
     * y consulta su nivel en el arma.
     */
    private static int obtenerNivelDrenaje(
            ServerLevel level,
            ItemStack arma
    ) {

        Registry<Enchantment> registro =
                level
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Optional<Holder.Reference<Enchantment>> holder =
                registro.get(
                        ModEnchantments.DRENAJE
                );

        if (holder.isEmpty()) {
            return 0;
        }

        return EnchantmentHelper
                .getItemEnchantmentLevel(
                        holder.get(),
                        arma
                );
    }

    private LeechingEnchantmentEvents() {
    }
}
