package ttv.migami.jeg.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.crafting.recycling.RecyclerRecipes;
import ttv.migami.jeg.crafting.recycling.RecyclingRecipe;
import ttv.migami.jeg.crafting.workbench.*;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModRecipeTypes;
import ttv.migami.jeg.jei.recycling.RecyclingCategory;
import ttv.migami.jeg.jei.workbench.BlueprintWorkbenchCategory;
import ttv.migami.jeg.jei.workbench.GunmetalWorkbenchCategory;
import ttv.migami.jeg.jei.workbench.GunniteWorkbenchCategory;
import ttv.migami.jeg.jei.workbench.ScrapWorkbenchCategory;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEGPlugin implements IModPlugin
{
    public static final RecipeType<RecyclingRecipe> RECYCLING = RecipeType.create(Reference.MOD_ID, "recycling", RecyclingRecipe.class);
    public static final RecipeType<ScrapWorkbenchRecipe> SCRAP_WORKBENCH = RecipeType.create(Reference.MOD_ID, "scrap_workbench", ScrapWorkbenchRecipe.class);
    public static final RecipeType<GunmetalWorkbenchRecipe> GUNMETAL_WORKBENCH = RecipeType.create(Reference.MOD_ID, "gunmetal_workbench", GunmetalWorkbenchRecipe.class);
    public static final RecipeType<GunniteWorkbenchRecipe> GUNNITE_WORKBENCH = RecipeType.create(Reference.MOD_ID, "gunnite_workbench", GunniteWorkbenchRecipe.class);
    public static final RecipeType<BlueprintWorkbenchRecipe> BLUEPRINT_WORKBENCH = RecipeType.create(Reference.MOD_ID, "blueprint_workbench", BlueprintWorkbenchRecipe.class);

    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Reference.MOD_ID, "crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new RecyclingCategory(helper));
        registration.addRecipeCategories(new ScrapWorkbenchCategory(helper));
        registration.addRecipeCategories(new GunmetalWorkbenchCategory(helper));
        registration.addRecipeCategories(new GunniteWorkbenchCategory(helper));
        registration.addRecipeCategories(new BlueprintWorkbenchCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);

        List<RecyclingRecipe> recyclingRecipes = RecyclerRecipes.getAll(world, ModRecipeTypes.RECYCLING.get());
        JustEnoughGuns.LOGGER.atInfo().log("Registering {} recycling recipes", recyclingRecipes.size());
        registration.addRecipes(RECYCLING, recyclingRecipes);

        List<ScrapWorkbenchRecipe> scrapWorkbenchRecipes = WorkbenchRecipes.getAll(world, ModRecipeTypes.SCRAP_WORKBENCH.get());
        JustEnoughGuns.LOGGER.atInfo().log("Registering {} Scrap Workbench recipes", scrapWorkbenchRecipes.size());
        registration.addRecipes(SCRAP_WORKBENCH, scrapWorkbenchRecipes);

        List<GunmetalWorkbenchRecipe> gunmetalWorkbenchRecipes = WorkbenchRecipes.getAll(world, ModRecipeTypes.GUNMETAL_WORKBENCH.get());
        JustEnoughGuns.LOGGER.atInfo().log("Registering {} Gunmetal Workbench recipes", gunmetalWorkbenchRecipes.size());
        registration.addRecipes(GUNMETAL_WORKBENCH, gunmetalWorkbenchRecipes);

        List<GunniteWorkbenchRecipe> gunniteWorkbenchRecipes = WorkbenchRecipes.getAll(world, ModRecipeTypes.GUNNITE_WORKBENCH.get());
        JustEnoughGuns.LOGGER.atInfo().log("Registering {} Gunnite Workbench recipes", gunniteWorkbenchRecipes.size());
        registration.addRecipes(GUNNITE_WORKBENCH, gunniteWorkbenchRecipes);

        List<BlueprintWorkbenchRecipe> blueprintWorkbenchRecipes = WorkbenchRecipes.getAll(world, ModRecipeTypes.BLUEPRINT_WORKBENCH.get());
        JustEnoughGuns.LOGGER.atInfo().log("Registering {} Blueprint Workbench recipes", blueprintWorkbenchRecipes.size());
        registration.addRecipes(BLUEPRINT_WORKBENCH, blueprintWorkbenchRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RECYCLER.get()), RECYCLING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SCRAP_WORKBENCH.get()), SCRAP_WORKBENCH);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GUNMETAL_WORKBENCH.get()), GUNMETAL_WORKBENCH);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GUNNITE_WORKBENCH.get()), GUNNITE_WORKBENCH);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLUEPRINT_WORKBENCH.get()), BLUEPRINT_WORKBENCH);
    }
}