package com.estrativarus.amasmas.enchantment;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {

    /*
     * Clave utilizada para localizar el encantamiento
     * amasmas:drenaje dentro del registro.
     */
    public static final ResourceKey<Enchantment> DRENAJE =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "drenaje"
                    )
            );

    private ModEnchantments() {
    }
}