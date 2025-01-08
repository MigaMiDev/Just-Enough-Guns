package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FlameParticle extends TextureSheetParticle {
    private boolean blue = false;
    protected FlameParticle(ClientLevel level, double xCoord, double yCoord, double zCoord,
                            SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.8F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = 0.1F;
        this.lifetime = 7;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        updateColorAndSize();
    }

    private void updateColorAndSize() {
        float progress = (float) this.age / this.lifetime;

        float orangeProgress = (progress - 0.3F) / 0.7F;
        if (!blue && progress > 0.2F) {
            this.rCol = 1.0F;
            this.gCol = 1.0F - orangeProgress * 0.7F;
            this.bCol = 1.0F - orangeProgress;
            this.quadSize = 0.5F + orangeProgress;
        }
        else {
            this.quadSize = (0.5F + orangeProgress) / 3;
        }
    }

    private void fadeOut() {
        this.alpha = -(1.0F / this.lifetime) * this.age + 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int k = i >> 16 & 255;
        return 255 | k << 16;
    }

    public float getQuadSize(float scaleFactor) {
        return this.quadSize;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new FlameParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlueProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public BlueProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            FlameParticle flame = new FlameParticle(level, x, y, z, this.sprites, dx, dy, dz);
            flame.blue = true;
            flame.lifetime = 3;
            flame.setColor(1F, 1F, 1F);
            return flame;
        }
    }
}