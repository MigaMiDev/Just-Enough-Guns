package ttv.migami.jeg.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.init.ModParticleTypes;

public class PhantomGunnerRenderer extends GeoEntityRenderer<PhantomGunner> {
    private int currentTick = -1;

    public PhantomGunnerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PhantomGunnerModel());
    }

    @Override
    public void render(PhantomGunner entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    // Add some particles in the wing
    @Override
    public void renderFinal(PoseStack poseStack, PhantomGunner animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            if (animatable.isDying()) {
                this.model.getBone("left_wing_tip").ifPresent(wing -> {
                    RandomSource rand = animatable.getRandom();
                    Vector3d wingPos = wing.getWorldPosition();

                    animatable.getCommandSenderWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            wingPos.x(),
                            wingPos.y(),
                            wingPos.z(),
                            0,
                            0,
                            0);

                    animatable.getCommandSenderWorld().addParticle(ModParticleTypes.FIRE.get(),
                            wingPos.x(),
                            wingPos.y(),
                            wingPos.z(),
                            0,
                            0,
                            0);
                });
            }
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}