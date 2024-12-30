package ttv.migami.jeg.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.Reference;

public class DynamicTextureLocator {

    public static ResourceLocation getItemTexture(Item item) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);

        if (registryName != null) {
            String modId = registryName.getNamespace();
            String itemName = registryName.getPath();
            return new ResourceLocation(modId, "textures/item/" + itemName + ".png");
        }
        return new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/bayonet.png");
    }
}