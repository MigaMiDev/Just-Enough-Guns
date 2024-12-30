package ttv.migami.jeg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ttv.migami.jeg.entity.projectile.ProjectileEntity;

public class ProjectileRendererB extends EntityRenderer<ProjectileEntity>
{
    public ProjectileRendererB(EntityRendererProvider.Context context)
    {
        super(context);
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/misc/white.png");

    @Override
    public ResourceLocation getTextureLocation(ProjectileEntity arrow) {
        return TEXTURE;
    }

    @Override
    public void render(ProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light)
    {
        poseStack.pushPose();

        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && entity.getShooter() instanceof LocalPlayer) {
            poseStack.popPose();
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.energySwirl(this.getTextureLocation(entity), 0.0F, 0.15625F));
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        // Makes the Trail longer the longer airtime it has
        int size = Math.min(entity.tickCount * 20, 100);

        this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, -1, -1, 0.0F, 0.15625F, -1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.15625F, 0.15625F, -1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.15625F, 0.3125F, -1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, 1, -1, 0.0F, 0.3125F, -1, 0, 0, light);

        this.vertex(matrix4f, matrix3f, vertexConsumer, 1, 1, -1, 0.0F, 0.15625F, 1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.15625F, 0.15625F, 1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.15625F, 0.3125F, 1, 0, 0, light);
        this.vertex(matrix4f, matrix3f, vertexConsumer, 1, -1, -1, 0.0F, 0.3125F, 1, 0, 0, light);

        for(int j = 0; j < 4; ++j) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.0F, 0.0F, 0, 1, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.5F, 0.0F, 0, 1, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.5F, 0.15625F, 0, 1, 0, light);
            this.vertex(matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.0F, 0.15625F, 0, 1, 0, light);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, renderTypeBuffer, light);
    }

    @Override
    protected int getBlockLightLevel(ProjectileEntity pEntity, BlockPos pPos) {
        return 15;
    }

    public void vertex(Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, int pX, int pY, int pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int pPackedLight) {
        pConsumer.vertex(pMatrix, (float)pX, (float)pY, (float)pZ)
                .color(255, 255, 25, 255)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }
}