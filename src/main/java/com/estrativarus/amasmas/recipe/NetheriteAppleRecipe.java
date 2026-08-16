package com.estrativarus.amasmas.recipe;

import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.item.ModItems;
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
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class NetheriteAppleRecipe
        implements CraftingRecipe {

    public static final MapCodec<NetheriteAppleRecipe>
            CODEC =
            MapCodec.unit(
                    NetheriteAppleRecipe::new
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            NetheriteAppleRecipe
            > STREAM_CODEC =
            StreamCodec.unit(
                    new NetheriteAppleRecipe()
            );

    public NetheriteAppleRecipe() {
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

        if (diaActual < 7) {
            return false;
        }

        if (input.width() != 3
                || input.height() != 3) {

            return false;
        }

        for (int slot = 0; slot < 9; slot++) {

            ItemStack stack =
                    input.getItem(slot);

            if (slot == 4) {

                if (!stack.is(
                        Items.GOLDEN_APPLE
                )) {

                    return false;
                }

            } else {

                if (!stack.is(
                        Items.NETHERITE_INGOT
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
                ModItems.MANZANA_NETHERITA.get()
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
                .MANZANA_NETHERITA
                .get();
    }
}