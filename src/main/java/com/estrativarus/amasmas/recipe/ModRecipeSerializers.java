package com.estrativarus.amasmas.recipe;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {

    public static final DeferredRegister<
            RecipeSerializer<?>
            > RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    Amasmas.MOD_ID
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<NetheriteAppleRecipe>
            > MANZANA_NETHERITA =
            RECIPE_SERIALIZERS.register(
                    "manzana_netherita",
                    () -> new RecipeSerializer<>(
                            NetheriteAppleRecipe.CODEC,
                            NetheriteAppleRecipe.STREAM_CODEC
                    )
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<ChorusFlowerStewRecipe>
            > CHORUS_FLOWER_STEW =
            RECIPE_SERIALIZERS.register(
                    "chorus_flower_stew",
                    () -> new RecipeSerializer<>(
                            ChorusFlowerStewRecipe.CODEC,
                            ChorusFlowerStewRecipe.STREAM_CODEC
                    )
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<ResonantiteArrowRecipe>
            > FLECHA_RESONANTITA =
            RECIPE_SERIALIZERS.register(
                    "flecha_resonantita",
                    () -> new RecipeSerializer<>(
                            ResonantiteArrowRecipe.CODEC,
                            ResonantiteArrowRecipe.STREAM_CODEC
                    )
            );

    public static void register(
            IEventBus modEventBus
    ) {

        RECIPE_SERIALIZERS.register(
                modEventBus
        );
    }

    private ModRecipeSerializers() {
    }
}