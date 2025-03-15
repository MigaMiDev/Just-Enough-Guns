package ttv.migami.jeg.client.particle.phantom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class PhantomGunnerParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float MAGICAL_X_ROT = 1.0472F;

    protected PhantomGunnerParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        this.renderRotatedParticle(pBuffer, pRenderInfo, pPartialTicks, (quaternionf) -> {
            // Rotate the particle to always be flat (XZ plane)
            quaternionf.mul((new Quaternionf()).rotationX((float) Math.PI / 2));
        });
    }

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera pCamera, float pPartialTick, Consumer<Quaternionf> pQuaternion) {
        Vec3 vec3 = pCamera.getPosition();
        float f = (float)(Mth.lerp((double)pPartialTick, this.xo, this.x) - vec3.x());
        float f1 = (float)(175.0 + vec3.y());
        float f2 = (float)(Mth.lerp((double)pPartialTick, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
        pQuaternion.accept(quaternionf);
        quaternionf.transform(TRANSFORM_VECTOR);
        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(-24F, -24F, 24F),
                new Vector3f(-24F, 24F, 24F),
                new Vector3f(24F, 24F, 24F),
                new Vector3f(24F, -24F, 24F)
        };
        float f3 = this.getQuadSize(pPartialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(pPartialTick);

        // Render front side
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);

        // Render back side (reverse order)
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight) {
        pConsumer.vertex((double)pVertex.x(), (double)pVertex.y(), (double)pVertex.z()).uv(pU, pV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(pPackedLight).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            PhantomGunnerParticle phantomGunnerParticle = new PhantomGunnerParticle(pLevel, pX, pY, pZ);
            phantomGunnerParticle.pickSprite(this.sprite);
            phantomGunnerParticle.setLifetime(100);
            return phantomGunnerParticle;
        }
    }
}