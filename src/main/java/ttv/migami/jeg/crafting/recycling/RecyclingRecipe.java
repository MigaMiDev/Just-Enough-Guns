package ttv.migami.jeg.crafting.recycling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModRecipeSerializers;
import ttv.migami.jeg.init.ModRecipeTypes;

public class RecyclingRecipe extends AbstractRecyclingRecipe {
    public RecyclingRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
        super(ModRecipeTypes.RECYCLING.get(), pId, pGroup, pIngredient, pResult, pExperience, pCookingTime);
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.RECYCLER.get());
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RECYCLING_RECIPE.get();
    }
}