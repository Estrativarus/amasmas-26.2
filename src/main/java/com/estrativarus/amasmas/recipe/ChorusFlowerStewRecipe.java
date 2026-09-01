package com.estrativarus.amasmas.recipe;

import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class ChorusFlowerStewRecipe
        implements CraftingRecipe {

    public static final MapCodec<ChorusFlowerStewRecipe>
            CODEC =
            MapCodec.unit(
                    ChorusFlowerStewRecipe::new
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ChorusFlowerStewRecipe
            > STREAM_CODEC =
            StreamCodec.unit(
                    new ChorusFlowerStewRecipe()
            );

    public ChorusFlowerStewRecipe() {
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

        if (diaActual < 21) {
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
                    input.getItem(slot);

            if (slot == 4) {

                if (!stack.is(
                        Items.RABBIT_STEW
                )) {

                    return false;
                }

            } else {

                if (!stack.is(
                        Items.CHORUS_FLOWER
                )) {

                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(
            CraftingInput input
    ) {

        return new ItemStack(
                ModItems.CHORUS_FLOWER_STEW.get()
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
                .CHORUS_FLOWER_STEW
                .get();
    }
}