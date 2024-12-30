package ttv.migami.jeg.crafting.recycling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class SimpleRecyclingSerializer<T extends AbstractRecyclingRecipe> implements RecipeSerializer<T>
{
   private final int defaultRecyclingTime;
   private final SimpleRecyclingSerializer.CookieBaker<T> factory;

   public SimpleRecyclingSerializer(SimpleRecyclingSerializer.CookieBaker<T> tCookieBaker, int i) {
      this.defaultRecyclingTime = i;
      this.factory = tCookieBaker;
   }

   public T fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
      String s = GsonHelper.getAsString(pSerializedRecipe, "group", "");
      JsonElement jsonelement = GsonHelper.isArrayNode(pSerializedRecipe, "ingredient") ? GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredient") : GsonHelper.getAsJsonObject(pSerializedRecipe, "ingredient");
      Ingredient ingredient = Ingredient.fromJson(jsonelement);
      //Forge: Check if primitive string to keep vanilla or a object which can contain a count field.
      if (!pSerializedRecipe.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
      ItemStack itemstack;
      if (pSerializedRecipe.get("result").isJsonObject()) itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));
      else {
      String s1 = GsonHelper.getAsString(pSerializedRecipe, "result");
      ResourceLocation resourcelocation = new ResourceLocation(s1);
      itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalStateException("Item: " + s1 + " does not exist");
      }));
      }
      float f = GsonHelper.getAsFloat(pSerializedRecipe, "experience", 0.0F);
      int i = GsonHelper.getAsInt(pSerializedRecipe, "recyclingtime", this.defaultRecyclingTime);
      return this.factory.create(pRecipeId, s, ingredient, itemstack, f, i);
   }

   public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
      String s = pBuffer.readUtf();
      Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
      ItemStack itemstack = pBuffer.readItem();
      float f = pBuffer.readFloat();
      int i = pBuffer.readVarInt();
      return this.factory.create(pRecipeId, s, ingredient, itemstack, f, i);
   }

   public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
      pBuffer.writeUtf(pRecipe.group);
      pRecipe.ingredient.toNetwork(pBuffer);
      pBuffer.writeItem(pRecipe.result);
      pBuffer.writeFloat(pRecipe.experience);
      pBuffer.writeVarInt(pRecipe.recyclingTime);
   }

   public interface CookieBaker<T extends AbstractRecyclingRecipe> {
      T create(ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack, float v, int i);
   }
}
