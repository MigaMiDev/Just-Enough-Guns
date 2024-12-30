package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GunniteWorkbenchRecipeSerializer extends AbstractWorkbenchRecipeSerializer<GunniteWorkbenchRecipe> {
    @Override
    protected GunniteWorkbenchRecipe createRecipe(ResourceLocation recipeId, ItemStack result, ImmutableList<WorkbenchIngredient> materials) {
        return new GunniteWorkbenchRecipe(recipeId, result, materials);
    }
}