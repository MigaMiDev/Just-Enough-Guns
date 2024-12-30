package ttv.migami.jeg.client.render.block;

/*@OnlyIn(Dist.CLIENT)
public class BasicTurretRenderer implements BlockEntityRenderer<BasicTurretBlockEntity> {

    public BasicTurretRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BasicTurretBlockEntity turret, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        double previousYaw = turret.getPreviousYaw();
        double yaw = turret.getYaw();
        float interpolatedYaw = (float) (previousYaw + Mth.wrapDegrees(yaw - previousYaw) * partialTicks);
        double previousPitch = turret.getPreviousPitch();
        double pitch = turret.getPitch();
        float interpolatedPitch = (float) (previousPitch + (pitch - previousPitch) * partialTicks);
        renderTurretTop(turret, matrixStack, buffer, light, overlay, SpecialModels.BASIC_TURRET_TOP.getModel(), 0.5, 1.0, 0.5, interpolatedYaw, interpolatedPitch);
    }

    private void renderTurretTop(BasicTurretBlockEntity turret, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, double x, double y, double z, float yaw, float pitch) {
        if (model != null) {
            matrixStack.pushPose();
            matrixStack.translate(x, y, z);
            matrixStack.mulPose(Axis.YP.rotationDegrees(yaw));

            float recoilPitch = pitch + turret.getRecoilPitchOffset();
            matrixStack.mulPose(Axis.XP.rotationDegrees(recoilPitch));

            matrixStack.translate(-x, -y, -z);
            RenderUtil.renderMacerateWheel(model, matrixStack, buffer, light, overlay);
            matrixStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BasicTurretBlockEntity p_112304_) {
        return true;
    }
}*/