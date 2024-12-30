package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;

public abstract class AbstractWorkbenchRecipeSerializer<T extends AbstractWorkbenchRecipe<?>> implements RecipeSerializer<T> {
    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject json) {
        ImmutableList.Builder<WorkbenchIngredient> builder = ImmutableList.builder();

        JsonArray input = GsonHelper.getAsJsonArray(json, "materials");
        for (int i = 0; i < input.size(); i++) {
            JsonObject object = input.get(i).getAsJsonObject();
            builder.add(WorkbenchIngredient.fromJson(object));
        }
        if (!json.has("result")) {
            throw new JsonSyntaxException("Missing result item entry");
        }
        JsonObject resultObject = GsonHelper.getAsJsonObject(json, "result");
        ItemStack resultItem = ShapedRecipe.itemStackFromJson(resultObject);
        return createRecipe(recipeId, resultItem, builder.build());
    }

    @Nullable
    @Override
    public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ItemStack result = buffer.readItem();
        ImmutableList.Builder<WorkbenchIngredient> builder = ImmutableList.builder();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            builder.add((WorkbenchIngredient) Ingredient.fromNetwork(buffer));
        }
        return createRecipe(recipeId, result, builder.build());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        buffer.writeItem(recipe.getItem());
        buffer.writeVarInt(recipe.getMaterials().size());
        for (WorkbenchIngredient ingredient : recipe.getMaterials()) {
            ingredient.toNetwork(buffer);
        }
    }

    protected abstract T createRecipe(ResourceLocation recipeId, ItemStack result, ImmutableList<WorkbenchIngredient> materials);
}