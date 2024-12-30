package ttv.migami.jeg.client.util;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.crafting.workbench.BlueprintWorkbenchRecipe;

public class RecipeHelper {

    public static BlueprintWorkbenchRecipe getRecipeFromItem(String namespace, String path, NonNullList<BlueprintWorkbenchRecipe> recipes) {
        ResourceLocation itemLocation = new ResourceLocation(namespace, path);

        Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
        if (item == null) {
            return null;
        }

        //if (item instanceof GunItem) {
            for (BlueprintWorkbenchRecipe recipe : recipes) {
                if (recipe.getItem().getItem() == item) {
                    return recipe;
                }
            }
        //}

        return null;
    }
}