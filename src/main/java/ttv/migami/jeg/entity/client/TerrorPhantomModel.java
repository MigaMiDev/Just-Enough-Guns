package ttv.migami.jeg.entity.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;

public class TerrorPhantomModel extends GeoModel<TerrorPhantom> {

    @Override
    public ResourceLocation getModelResource(TerrorPhantom animatable) {
        return new ResourceLocation(Reference.MOD_ID, "geo/entity/terror_phantom.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TerrorPhantom animatable) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/terror_phantom/terror_phantom.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TerrorPhantom animatable) {
        return new ResourceLocation(Reference.MOD_ID, "animations/entity/terror_phantom.animation.json");
    }

    @Override
    public void setCustomAnimations(TerrorPhantom animatable, long instanceId, AnimationState<TerrorPhantom> animationState) {
        CoreGeoBone root = getAnimationProcessor().getBone("root");
        if (root != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            //root.setRotX(root.getRotX() + entityData.headPitch() * Mth.DEG_TO_RAD);
            //root.setRotY(root.getRotY() + entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
