package ttv.migami.jeg.client.render.gun.animated;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.util.ClientUtils;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.item.AnimatedGunItem;

public class AnimatedGunModel extends DefaultedItemGeoModel<AnimatedGunItem> {
    private ResourceLocation currentTexture;
    private ResourceLocation currentModel;
    private ResourceLocation currentAnimation;

    public AnimatedGunModel(ResourceLocation path) {
        super(path);
    }

    public ResourceLocation getModelResource(AnimatedGunItem gunItem) {
        return currentModel != null ? currentModel : new ResourceLocation(gunItem.getModID(), "geo/item/gun/" + gunItem.toString() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnimatedGunItem gunItem) {
        return currentTexture != null ? currentTexture : new ResourceLocation(gunItem.getModID(), "textures/animated/gun/" + gunItem.toString() + ".png");
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
        Player player = ClientUtils.getClientPlayer();
        ItemStack gunStack = player.getMainHandItem();

        if (gunStack.is(ModItems.ABSTRACT_GUN.get())) {
            if (gunStack.getOrCreateTag().contains("GunId")) {
                ResourceLocation id = new ResourceLocation(gunStack.getOrCreateTag().getString("GunId"));
                ResourceLocation anim = new ResourceLocation(Reference.MOD_ID, "animations/" + id.getPath() + ".animation.json");

                if (Minecraft.getInstance().getResourceManager().getResource(anim).isPresent()) {
                    return anim;
                } else {
                    return new ResourceLocation(Reference.MOD_ID, "animations/item/" + "abstract_gun" + ".animation.json");
                }
            }
        }

        return currentAnimation != null ? currentAnimation : new ResourceLocation(gunItem.getModID(), "animations/item/" + gunItem.toString() + ".animation.json");
    }

    public void setCurrentTexture(ResourceLocation texture) {
        this.currentTexture = texture;
    }

    public void setCurrentModel(ResourceLocation model) {
        this.currentModel = model;
    }

    public void setCurrentAnimation(ResourceLocation animation) {
        this.currentAnimation = animation;
    }
}