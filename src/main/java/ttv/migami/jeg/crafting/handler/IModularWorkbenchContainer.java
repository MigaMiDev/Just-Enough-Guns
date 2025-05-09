package ttv.migami.jeg.crafting.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;

public interface IModularWorkbenchContainer {
    BlockPos getPos();
    RecipeType<?> getRecipeType();
}