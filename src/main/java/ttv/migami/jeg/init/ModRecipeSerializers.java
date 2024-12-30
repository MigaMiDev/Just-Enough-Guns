package ttv.migami.jeg.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.crafting.*;
import ttv.migami.jeg.crafting.recycling.RecyclingRecipe;
import ttv.migami.jeg.crafting.recycling.SimpleRecyclingSerializer;
import ttv.migami.jeg.crafting.workbench.*;

/**
 * Author: MrCrayfish
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<DyeItemRecipe>> DYE_ITEM = REGISTER.register("dye_item", () -> new SimpleCraftingRecipeSerializer<>(DyeItemRecipe::new));

    public static final RegistryObject<AbstractWorkbenchRecipeSerializer<ScrapWorkbenchRecipe>> SCRAP_WORKBENCH = REGISTER.register("scrap_workbench", ScrapWorkbenchRecipeSerializer::new);
    public static final RegistryObject<AbstractWorkbenchRecipeSerializer<GunmetalWorkbenchRecipe>> GUNMETAL_WORKBENCH = REGISTER.register("gunmetal_workbench", GunmetalWorkbenchRecipeSerializer::new);
    public static final RegistryObject<AbstractWorkbenchRecipeSerializer<GunniteWorkbenchRecipe>> GUNNITE_WORKBENCH = REGISTER.register("gunnite_workbench", GunniteWorkbenchRecipeSerializer::new);
    public static final RegistryObject<AbstractWorkbenchRecipeSerializer<BlueprintWorkbenchRecipe>> BLUEPRINT_WORKBENCH = REGISTER.register("blueprint_workbench", BlueprintWorkbenchRecipeSerializer::new);

    public static final RegistryObject<SimpleRecyclingSerializer<RecyclingRecipe>> RECYCLING_RECIPE = REGISTER.register("recycling", () -> new SimpleRecyclingSerializer<>(RecyclingRecipe::new, 1200));
}