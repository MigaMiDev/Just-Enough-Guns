package ttv.migami.jeg.client.render.gun.animated;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import ttv.migami.jeg.item.AnimatedGunItem;

public class AnimatedGunModel extends DefaultedItemGeoModel<AnimatedGunItem> {
    private ResourceLocation currentTexture;
    private ResourceLocation currentModel;

    public AnimatedGunModel(ResourceLocation path) {
        super(path);
    }

    public ResourceLocation getModelResource(AnimatedGunItem gunItem) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(gunItem);
        String modId = registryName.getNamespace();

        return currentModel != null ? currentModel : new ResourceLocation(modId, "geo/item/gun/" + gunItem.toString() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnimatedGunItem gunItem) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(gunItem);
        String modId = registryName.getNamespace();

        return currentTexture != null ? currentTexture : new ResourceLocation(modId, "textures/animated/gun/" + gunItem.toString() + ".png");
    }

    /*@Override
    public ResourceLocation getTextureResource(AnimatedGunItem gunItem) {
        return new ResourceLocation(Reference.MOD_ID, "textures/animated/gun/" + gunItem.toString() + ".png");
    }*/

    /*@Override
    public ResourceLocation getModelResource(AnimatedGunItem gunItem) {
        return new ResourceLocation(Reference.MOD_ID, "geo/item/gun/" + gunItem.toString() + ".geo.json");
    }*/

    @Override
    public ResourceLocation getAnimationResource(AnimatedGunItem gunItem) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(gunItem);
        String modId = registryName.getNamespace();

        return new ResourceLocation(modId, "animations/item/" + gunItem.toString() + ".animation.json");
    }

    public void setCurrentTexture(ResourceLocation texture) {
        this.currentTexture = texture;
    }

    public void setCurrentModel(ResourceLocation model) {
        this.currentModel = model;
    }
}