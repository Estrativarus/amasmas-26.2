package com.estrativarus.amasmas.recipe;

import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ResonantiteArrowRecipe
        implements CraftingRecipe {

    public static final MapCodec<ResonantiteArrowRecipe>
            CODEC =
            MapCodec.unit(
                    ResonantiteArrowRecipe::new
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ResonantiteArrowRecipe
            > STREAM_CODEC =
            StreamCodec.unit(
                    new ResonantiteArrowRecipe()
            );

    private static final int DIA_INICIO =
            21;

    public ResonantiteArrowRecipe() {
    }

    @Override
    public boolean matches(
            CraftingInput input,
            Level level
    ) {

        if (input.ingredientCount() != 3) {
            return false;
        }

        boolean ingredientesCorrectos =
                false;

        /*
         * Minecraft puede recortar las columnas vacías
         * y representar esta receta vertical como 1 x 3.
         */
        if (input.width() == 1
                && input.height() == 3) {

            ingredientesCorrectos =
                    input.getItem(0).is(
                            ModItems.FRAGMENTO_RESONANTITA.get()
                    )

                            && input.getItem(1).is(
                            Items.BLAZE_ROD
                    )

                            && input.getItem(2).is(
                            Items.TWISTING_VINES
                    );
        }

        /*
         * Variante de respaldo por si la versión entrega
         * la cuadrícula completa de 3 x 3.
         */
        if (input.width() == 3
                && input.height() == 3) {

            ingredientesCorrectos =
                    input.getItem(1).is(
                            ModItems.FRAGMENTO_RESONANTITA.get()
                    )

                            && input.getItem(4).is(
                            Items.BLAZE_ROD
                    )

                            && input.getItem(7).is(
                            Items.TWISTING_VINES
                    )

                            && input.getItem(0).isEmpty()
                            && input.getItem(2).isEmpty()
                            && input.getItem(3).isEmpty()
                            && input.getItem(5).isEmpty()
                            && input.getItem(6).isEmpty()
                            && input.getItem(8).isEmpty();
        }

        if (!ingredientesCorrectos) {
            return false;
        }

        /*
         * El cliente necesita considerar la receta válida
         * para mostrar visualmente el resultado.
         */
        if (level.isClientSide()) {
            return true;
        }

        if (level.getServer() == null) {
            return false;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        return diaActual >= DIA_INICIO;
    }

    @Override
    public ItemStack assemble(
            CraftingInput input
    ) {

        return new ItemStack(
                ModItems.FLECHA_RESONANTITA.get(),
                8
        );
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<
            ? extends CraftingRecipe
            > getSerializer() {

        return ModRecipeSerializers
                .FLECHA_RESONANTITA
                .get();
    }
}