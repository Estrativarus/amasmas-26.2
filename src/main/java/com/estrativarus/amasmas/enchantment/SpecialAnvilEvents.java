package com.estrativarus.amasmas.enchantment;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class SpecialAnvilEvents {

    private static final int NIVEL_RESPIRACION =
            5;

    private static final int NIVEL_FILO =
            6;

    private static final int NIVEL_EFICIENCIA =
            7;

    private static final int NIVEL_PODER =
            7;

    private static final int COSTE_RESPIRACION =
            25;

    private static final int COSTE_FILO =
            30;

    private static final int COSTE_EFICIENCIA =
            35;

    private static final int COSTE_PODER =
            35;

    @SubscribeEvent
    public static void onAnvilUpdate(
            AnvilUpdateEvent event
    ) {

        ItemStack objetoBase =
                event.getLeft();

        ItemStack libro =
                event.getRight();

        if (objetoBase.isEmpty()
                || libro.isEmpty()) {

            return;
        }

        if (!libro.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        ItemEnchantments encantamientosLibro =
                libro.getOrDefault(
                        DataComponents.STORED_ENCHANTMENTS,
                        ItemEnchantments.EMPTY
                );

        if (encantamientosLibro.isEmpty()) {
            return;
        }

        Registry<Enchantment> registro =
                event.getPlayer()
                        .registryAccess()
                        .lookupOrThrow(
                                Registries.ENCHANTMENT
                        );

        Holder.Reference<Enchantment> respiracion =
                registro.getOrThrow(
                        Enchantments.RESPIRATION
                );

        Holder.Reference<Enchantment> filo =
                registro.getOrThrow(
                        Enchantments.SHARPNESS
                );

        Holder.Reference<Enchantment> eficiencia =
                registro.getOrThrow(
                        Enchantments.EFFICIENCY
                );

        Holder.Reference<Enchantment> poder =
                registro.getOrThrow(
                        Enchantments.POWER
                );

        int nivelRespiracion =
                encantamientosLibro.getLevel(
                        respiracion
                );

        int nivelFilo =
                encantamientosLibro.getLevel(
                        filo
                );

        int nivelEficiencia =
                encantamientosLibro.getLevel(
                        eficiencia
                );

        int nivelPoder =
                encantamientosLibro.getLevel(
                        poder
                );

        if (nivelRespiracion
                == NIVEL_RESPIRACION) {

            aplicarEncantamientoEspecial(
                    event,
                    objetoBase,
                    respiracion,
                    NIVEL_RESPIRACION,
                    COSTE_RESPIRACION
            );

            return;
        }

        if (nivelFilo
                == NIVEL_FILO) {

            aplicarEncantamientoEspecial(
                    event,
                    objetoBase,
                    filo,
                    NIVEL_FILO,
                    COSTE_FILO
            );

            return;
        }

        if (nivelEficiencia
                == NIVEL_EFICIENCIA) {

            aplicarEncantamientoEspecial(
                    event,
                    objetoBase,
                    eficiencia,
                    NIVEL_EFICIENCIA,
                    COSTE_EFICIENCIA
            );

            return;
        }

        if (nivelPoder
                == NIVEL_PODER) {

            aplicarEncantamientoEspecial(
                    event,
                    objetoBase,
                    poder,
                    NIVEL_PODER,
                    COSTE_PODER
            );
        }
    }

    private static void aplicarEncantamientoEspecial(
            AnvilUpdateEvent event,
            ItemStack objetoBase,
            Holder<Enchantment> encantamiento,
            int nivel,
            int costeExperiencia
    ) {

        ItemStack resultadoVanilla =
                event
                        .getVanillaResult()
                        .output();

        ItemStack resultado;

        if (resultadoVanilla.isEmpty()) {

            resultado =
                    objetoBase.copy();

        } else {

            resultado =
                    resultadoVanilla.copy();
        }

        ItemEnchantments encantamientosActuales =
                resultado.getOrDefault(
                        DataComponents.ENCHANTMENTS,
                        ItemEnchantments.EMPTY
                );

        ItemEnchantments.Mutable encantamientosNuevos =
                new ItemEnchantments.Mutable(
                        encantamientosActuales
                );

        encantamientosNuevos.set(
                encantamiento,
                nivel
        );

        resultado.set(
                DataComponents.ENCHANTMENTS,
                encantamientosNuevos.toImmutable()
        );

        event.setOutput(
                resultado
        );

        event.setXpCost(
                costeExperiencia
        );

        event.setMaterialCost(
                1
        );
    }

    private SpecialAnvilEvents() {
    }
}