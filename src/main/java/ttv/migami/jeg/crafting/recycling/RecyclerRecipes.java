package ttv.migami.jeg.crafting.recycling;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class RecyclerRecipes {
    public static boolean isEmpty(Level world, RecipeType<?> type) {
        return world.getRecipeManager().getRecipes().stream().noneMatch(recipe -> recipe.getType() == type);
    }

    public static <T extends Recipe<?>> NonNullList<T> getAll(Level world, RecipeType<T> type) {
        return world.getRecipeManager().getRecipes().stream()
            .filter(recipe -> recipe.getType() == type)
            .map(recipe -> (T) recipe)
            .collect(Collectors.toCollection(NonNullList::create));
    }

    /*
    public static List<RecyclingRecipe> getAll(ClientLevel world, RecipeType<RecyclingRecipe> recipeType) {
        return world.getRecipeManager().getAllRecipesFor(recipeType);
    }
     */

    @Nullable
    public static <T extends Recipe<?>> T getRecipeById(Level world, ResourceLocation id, RecipeType<T> type) {
        return world.getRecipeManager().getRecipes().stream()
            .filter(recipe -> recipe.getType() == type)
            .map(recipe -> (T) recipe)
            .filter(recipe -> recipe.getId().equals(id))
            .findFirst().orElse(null);
    }
}