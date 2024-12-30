package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class SonicRingParticle extends RisingParticle {
    SonicRingParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.hasPhysics = false;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /*public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }*/

    @Override
    public void tick() {
        super.tick();
        if (this.age <= 10) {
            this.setAlpha(0.75F * (1.0F - (this.age / 10.0F)));
        }
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F + f * f);
    }

    public int getLightColor(float pPartialTick) {
        return 240;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SonicRingParticle ringParticle = new SonicRingParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            ringParticle.pickSprite(this.sprite);
            ringParticle.setAlpha(0.75F);
            ringParticle.scale(3.0F);
            return ringParticle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class BigProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public BigProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SonicRingParticle ringParticle = new SonicRingParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            ringParticle.pickSprite(this.sprite);
            ringParticle.setAlpha(0.75F);
            ringParticle.scale(17.0F);
            return ringParticle;
        }
    }
}