package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SparkParticle extends TextureSheetParticle {
    SparkParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ, 0.0, 0.0, 0.0);
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.xd *= 0.800000011920929;
        this.yd *= 0.800000011920929;
        this.zd *= 0.800000011920929;
        this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
        this.quadSize *= this.random.nextFloat();
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public int getLightColor(float pPartialTick) {
        int $$1 = super.getLightColor(pPartialTick);
        int $$3 = $$1 >> 16 & 255;
        return 240 | $$3 << 16;
    }

    public float getQuadSize(float pScaleFactor) {
        float $$1 = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - $$1 * $$1);
    }

    public void tick() {
        super.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            SparkParticle $$8 = new SparkParticle(pLevel, pX, pY, pZ);
            $$8.pickSprite(this.sprite);
            $$8.setColor(1F, 1F, 0.5F);
            return $$8;
        }
    }
}