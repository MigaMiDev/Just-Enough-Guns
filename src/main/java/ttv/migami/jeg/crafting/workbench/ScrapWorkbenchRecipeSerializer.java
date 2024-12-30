package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ScrapWorkbenchRecipeSerializer extends AbstractWorkbenchRecipeSerializer<ScrapWorkbenchRecipe> {
    @Override
    protected ScrapWorkbenchRecipe createRecipe(ResourceLocation recipeId, ItemStack result, ImmutableList<WorkbenchIngredient> materials) {
        return new ScrapWorkbenchRecipe(recipeId, result, materials);
    }
}