package ttv.migami.jeg.entity.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;

public class PhantomGunnerModel extends GeoModel<PhantomGunner> {

    @Override
    public ResourceLocation getModelResource(PhantomGunner animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/entity/phantom_gunner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PhantomGunner animatable) {
        if (animatable.isPlayerOwned()) {
            return new ResourceLocation(Reference.MOD_ID, "textures/entity/phantom_gunner/phantom_gunner_friendly.png");
        }
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/phantom_gunner/phantom_gunner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PhantomGunner animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/entity/phantom_gunner.animation.json");
    }

    @Override
    public void setCustomAnimations(PhantomGunner animatable, long instanceId, AnimationState<PhantomGunner> animationState) {
        CoreGeoBone root = getAnimationProcessor().getBone("root");
        if (root != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            //root.setRotX(root.getRotX() + entityData.headPitch() * Mth.DEG_TO_RAD);
            //root.setRotY(root.getRotY() + entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
