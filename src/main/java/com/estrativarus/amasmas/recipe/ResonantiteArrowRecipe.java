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

        if (level.isClientSide()) {
            return false;
        }

        if (level.getServer() == null) {
            return false;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return false;
        }

        if (input.width() != 3
                || input.height() != 3) {

            return false;
        }

        for (int slot = 0;
             slot < 9;
             slot++) {

            ItemStack stack =
                    input.getItem(
                            slot
                    );

            if (slot == 1) {

                if (!stack.is(
                        ModItems.FRAGMENTO_RESONANTITA.get()
                )) {

                    return false;
                }

                continue;
            }

            if (slot == 4) {

                if (!stack.is(
                        Items.BLAZE_ROD
                )) {

                    return false;
                }

                continue;
            }

            if (slot == 7) {

                if (!stack.is(
                        Items.TWISTING_VINES
                )) {

                    return false;
                }

                continue;
            }

            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(
            CraftingInput input
    ) {

        return new ItemStack(
                ModItems.FLECHA_RESONANTITA.get()
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