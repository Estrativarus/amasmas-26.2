package com.estrativarus.amasmas.specialbook;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;

public final class SpecialEnchantedBooks {

    /*
     * Niveles máximos especiales definidos por el mod.
     */
    public static final int MAX_RESPIRACION = 5;
    public static final int MAX_FILO = 6;
    public static final int MAX_EFICIENCIA = 7;
    public static final int MAX_PODER = 7;

    /*
     * Registro central de niveles especiales.
     *
     * Si en el futuro queremos añadir más encantamientos,
     * solamente tendremos que incluir otra entrada aquí.
     *
     * Ejemplo:
     *
     * Enchantments.PROTECTION, 5
     */
    private static final Map<
            ResourceKey<Enchantment>,
            Integer
            > NIVELES_MAXIMOS_ESPECIALES =
            Map.of(
                    Enchantments.RESPIRATION,
                    MAX_RESPIRACION,

                    Enchantments.SHARPNESS,
                    MAX_FILO,

                    Enchantments.EFFICIENCY,
                    MAX_EFICIENCIA,

                    Enchantments.POWER,
                    MAX_PODER
            );

    /*
     * Crea directamente el libro especial de Respiración V.
     */
    public static ItemStack crearLibroRespiracion(
            ServerLevel level
    ) {
        return crearLibro(
                level,
                Enchantments.RESPIRATION,
                MAX_RESPIRACION
        );
    }

    /*
     * Crea directamente el libro especial de Filo VI.
     */
    public static ItemStack crearLibroFilo(
            ServerLevel level
    ) {
        return crearLibro(
                level,
                Enchantments.SHARPNESS,
                MAX_FILO
        );
    }

    /*
     * Crea directamente el libro especial de Eficiencia VII.
     */
    public static ItemStack crearLibroEficiencia(
            ServerLevel level
    ) {
        return crearLibro(
                level,
                Enchantments.EFFICIENCY,
                MAX_EFICIENCIA
        );
    }

    /*
     * Crea directamente el libro especial de Poder VII.
     */
    public static ItemStack crearLibroPoder(
            ServerLevel level
    ) {
        return crearLibro(
                level,
                Enchantments.POWER,
                MAX_PODER
        );
    }

    /*
     * Crea el libro de nivel máximo permitido para el
     * encantamiento indicado.
     *
     * Para nuestros cuatro encantamientos especiales:
     *
     * Respiración -> V
     * Filo        -> VI
     * Eficiencia  -> VII
     * Poder       -> VII
     *
     * Para cualquier otro encantamiento:
     *
     * máximo vanilla.
     */
    public static ItemStack crearLibroMaximo(
            ServerLevel level,
            ResourceKey<Enchantment> enchantmentKey
    ) {

        int nivelMaximo =
                obtenerNivelMaximoPermitido(
                        level,
                        enchantmentKey
                );

        return crearLibro(
                level,
                enchantmentKey,
                nivelMaximo
        );
    }

    /*
     * Crea un libro indicando el encantamiento y el nivel.
     *
     * El nivel se limita automáticamente para impedir
     * generar accidentalmente valores superiores a los
     * establecidos por el mod.
     */
    public static ItemStack crearLibro(
            ServerLevel level,
            ResourceKey<Enchantment> enchantmentKey,
            int nivelSolicitado
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

        int nivelMaximo =
                obtenerNivelMaximoPermitido(
                        holder,
                        enchantmentKey
                );

        /*
         * El nivel nunca puede ser menor que 1 ni superar
         * el máximo permitido.
         */
        int nivelFinal =
                Math.max(
                        1,
                        Math.min(
                                nivelSolicitado,
                                nivelMaximo
                        )
                );

        /*
         * Creamos un libro encantado vacío.
         */
        ItemStack libro =
                new ItemStack(
                        Items.ENCHANTED_BOOK
                );

        /*
         * Consultamos los encantamientos almacenados que ya
         * contiene el libro.
         *
         * En este caso estará vacío, pero hacerlo así permite
         * ampliar posteriormente el sistema a libros con
         * varios encantamientos.
         */
        ItemEnchantments encantamientosExistentes =
                libro.getOrDefault(
                        DataComponents.STORED_ENCHANTMENTS,
                        ItemEnchantments.EMPTY
                );

        /*
         * Creamos una versión mutable del componente.
         */
        ItemEnchantments.Mutable encantamientos =
                new ItemEnchantments.Mutable(
                        encantamientosExistentes
                );

        /*
         * Añadimos el encantamiento como encantamiento almacenado,
         * no como efecto activo aplicado directamente al libro.
         */
        encantamientos.set(
                holder,
                nivelFinal
        );

        /*
         * Guardamos el componente inmutable en el libro.
         */
        libro.set(
                DataComponents.STORED_ENCHANTMENTS,
                encantamientos.toImmutable()
        );

        return libro;
    }

    /*
     * Obtiene el máximo permitido sin necesidad de que
     * el código externo consulte manualmente el registro.
     */
    public static int obtenerNivelMaximoPermitido(
            ServerLevel level,
            ResourceKey<Enchantment> enchantmentKey
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

        return obtenerNivelMaximoPermitido(
                holder,
                enchantmentKey
        );
    }

    /*
     * Devuelve el máximo especial si está configurado.
     *
     * Si el encantamiento no aparece en nuestro mapa,
     * devuelve su máximo definido por Minecraft.
     */
    private static int obtenerNivelMaximoPermitido(
            Holder.Reference<Enchantment> holder,
            ResourceKey<Enchantment> enchantmentKey
    ) {

        Integer nivelEspecial =
                NIVELES_MAXIMOS_ESPECIALES.get(
                        enchantmentKey
                );

        if (nivelEspecial != null) {
            return nivelEspecial;
        }

        /*
         * Para cualquier encantamiento no especial,
         * respetamos su límite vanilla.
         */
        return holder.value().getMaxLevel();
    }

    /*
     * Permite comprobar si un encantamiento forma parte
     * del sistema especial.
     */
    public static boolean esEncantamientoEspecial(
            ResourceKey<Enchantment> enchantmentKey
    ) {
        return NIVELES_MAXIMOS_ESPECIALES.containsKey(
                enchantmentKey
        );
    }

    private SpecialEnchantedBooks() {
    }
}