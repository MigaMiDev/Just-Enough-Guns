package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GunmetalWorkbenchRecipeSerializer extends AbstractWorkbenchRecipeSerializer<GunmetalWorkbenchRecipe> {
    @Override
    protected GunmetalWorkbenchRecipe createRecipe(ResourceLocation recipeId, ItemStack result, ImmutableList<WorkbenchIngredient> materials) {
        return new GunmetalWorkbenchRecipe(recipeId, result, materials);
    }
}