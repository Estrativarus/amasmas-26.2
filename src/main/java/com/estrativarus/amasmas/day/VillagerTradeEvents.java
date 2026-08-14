package com.estrativarus.amasmas.day;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public class VillagerTradeEvents {

    /*
     * Procesamos cada aldeano una vez por segundo.
     *
     * No hace falta comprobar sus ofertas veinte veces
     * por segundo.
     */
    private static final int INTERVALO_COMPROBACION =
            20;

    @SubscribeEvent
    public static void onVillagerTick(
            EntityTickEvent.Post event
    ) {

        /*
         * Solo procesamos aldeanos normales.
         *
         * El comerciante ambulante no está incluido.
         */
        if (!(event.getEntity()
                instanceof Villager villager)) {
            return;
        }

        /*
         * Solo ejecutamos la lógica en el servidor.
         */
        if (!(villager.level()
                instanceof ServerLevel level)) {
            return;
        }

        /*
         * Una comprobación por segundo.
         */
        if (villager.tickCount
                % INTERVALO_COMPROBACION != 0) {
            return;
        }

        MinecraftServer server =
                level.getServer();

        int diaActual =
                SistemaDiasSavedData
                        .get(server)
                        .getDiaActual();

        MerchantOffers ofertas =
                villager.getOffers();

        if (ofertas == null
                || ofertas.isEmpty()) {
            return;
        }

        /*
         * ETAPA 1 — DÍAS 1 A 6
         *
         * Las ofertas no necesitan reposición.
         *
         * Cuando una se agota, sus usos vuelven a cero
         * inmediatamente.
         */
        if (diaActual <= 6) {

            hacerOfertasIlimitadas(ofertas);
            return;
        }

        /*
         * ETAPA 2 — DÍAS 7 A 48
         *
         * Comportamiento completamente vanilla.
         *
         * Los intercambios se gastan y el aldeano puede
         * reponerlos trabajando.
         */
        if (diaActual < 49) {
            return;
        }

        /*
         * ETAPA 3 — DÍA 49 EN ADELANTE
         *
         * Los intercambios pueden gastarse, pero ya no
         * pueden recuperar usos mediante reposiciones.
         */
        impedirReposiciones(
                server,
                villager,
                ofertas
        );
    }

    private static void hacerOfertasIlimitadas(
            MerchantOffers ofertas
    ) {

        for (MerchantOffer oferta : ofertas) {

            /*
             * Solo reiniciamos una oferta cuando está agotada.
             *
             * Así no estamos modificando el contador en cada
             * intercambio innecesariamente.
             */
            if (oferta.isOutOfStock()) {
                oferta.resetUses();
            }
        }
    }

    private static void impedirReposiciones(
            MinecraftServer server,
            Villager villager,
            MerchantOffers ofertas
    ) {

        VillagerTradeSavedData datos =
                VillagerTradeSavedData.get(server);

        for (
                int indice = 0;
                indice < ofertas.size();
                indice++
        ) {

            MerchantOffer oferta =
                    ofertas.get(indice);

            int usosActuales =
                    oferta.getUses();

            int usosGuardados =
                    datos.getUsosGuardados(
                            villager.getUUID(),
                            indice,
                            usosActuales
                    );

            /*
             * Si los usos actuales son mayores, el jugador
             * ha realizado nuevos intercambios.
             *
             * Guardamos el nuevo valor.
             */
            if (usosActuales > usosGuardados) {

                datos.guardarMayorNumeroDeUsos(
                        villager.getUUID(),
                        indice,
                        usosActuales
                );

                continue;
            }

            /*
             * Si Minecraft redujo los usos, significa que el
             * aldeano intentó reponer la oferta.
             *
             * Volvemos a incrementar el contador hasta el
             * número de usos que tenía antes.
             */
            if (usosActuales < usosGuardados) {

                int usosQueFaltan =
                        usosGuardados
                                - usosActuales;

                for (
                        int i = 0;
                        i < usosQueFaltan;
                        i++
                ) {
                    oferta.increaseUses();
                }
            }

            /*
             * En la primera comprobación de una oferta nueva,
             * guardamos su situación actual.
             */
            datos.guardarMayorNumeroDeUsos(
                    villager.getUUID(),
                    indice,
                    oferta.getUses()
            );
        }
    }

    /*
     * Eliminamos los datos guardados cuando muere el aldeano.
     */
    @SubscribeEvent
    public static void onVillagerDeath(
            LivingDeathEvent event
    ) {

        if (!(event.getEntity()
                instanceof Villager villager)) {
            return;
        }

        if (!(villager.level()
                instanceof ServerLevel level)) {
            return;
        }

        VillagerTradeSavedData
                .get(level.getServer())
                .eliminarAldeano(
                        villager.getUUID()
                );
    }

    private VillagerTradeEvents() {
    }
}