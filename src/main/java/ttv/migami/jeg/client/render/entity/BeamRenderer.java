package ttv.migami.jeg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.entity.projectile.BeamEntity;

public class BeamRenderer extends EntityRenderer<BeamEntity> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation LASER_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/effect/beam.png");
    private static final float LASER_RADIUS = 0.05F / 4;
    private static final float LASER_GLOW_RADIUS = 0.055F / 4;

    public BeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BeamEntity entity) {
        return LASER_TEXTURE;
    }

    @Override
    public boolean shouldRender(BeamEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    protected Vector3f getBeamOffset(){
        return new Vector3f(0.20f, 0.25f, 0.07f);
    }

    protected float getLaserRadius(){
        return LASER_RADIUS;
    }

    protected float getLaserGlowRadius(){
        return LASER_GLOW_RADIUS;
    }

    @Override
    public void render(BeamEntity projectile, float entityYaw, float partialTicks,
                       PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int light) {
        var prog = ((float) projectile.tickCount) / ((float) projectile.getProjectile().getLife());
        var fadingValue = Math.sin(Math.sqrt(prog) * Math.PI);
        var radius = (float) (getLaserRadius() * fadingValue * 2);
        var glowRadius = (float) (getLaserGlowRadius() * fadingValue * 2);
        var shooterId = projectile.getShooterId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        if (shooter == null) return;

        var playerPos = projectile.getEndVec();
        var laserPos = shooter.getEyePosition(partialTicks);
        var pos = playerPos.subtract(laserPos);
        var offset = getBeamOffset();
        var distance = projectile.getDistance() - offset.y;

        pos = pos.normalize();

        var yPos = (float) Math.acos(pos.y);
        var xzPos = (float) Math.atan2(pos.z, pos.x);
        var side = -1;

        poseStack.pushPose();
        {
            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

            poseStack.translate(side * offset.x, offset.y, offset.z);
            long gameTime = projectile.level().getGameTime();
            int yOffset = 0;
            var color = projectile.getProjectile().getTrailColor();

            RenderUtil.renderBeam(poseStack, bufferSource, getTextureLocation(projectile), partialTicks, 1.0F,
                    gameTime, (float) yOffset, distance, color, 1, radius, glowRadius);
        }
        poseStack.popPose();
    }
}