package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BlueprintWorkbenchRecipeSerializer extends AbstractWorkbenchRecipeSerializer<BlueprintWorkbenchRecipe> {
    @Override
    protected BlueprintWorkbenchRecipe createRecipe(ResourceLocation recipeId, ItemStack result, ImmutableList<WorkbenchIngredient> materials) {
        return new BlueprintWorkbenchRecipe(recipeId, result, materials);
    }
}