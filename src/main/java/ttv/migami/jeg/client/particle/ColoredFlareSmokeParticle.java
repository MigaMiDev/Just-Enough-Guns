package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.jeg.particles.ColoredFlareData;

@OnlyIn(Dist.CLIENT)
public class ColoredFlareSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean isThrowable;

    protected ColoredFlareSmokeParticle(ClientLevel level, double xCoord, double yCoord, double zCoord,
                                        SpriteSet spriteSet, double xd, double yd, double zd, boolean isFlare, ColoredFlareData data) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.8F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 21;
        this.sprites = spriteSet;
        this.isThrowable = isFlare;
        this.setSpriteFromAge(spriteSet);
        if (this.isThrowable) {
            this.lifetime = 400 + this.random.nextInt(401);
        } else {
            this.lifetime = 1500 + this.random.nextInt(1500);
        }
        this.rCol = data.getR() / 255.0f;
        this.gCol = data.getG() / 255.0f;
        this.bCol = data.getB() / 255.0f;
    }

    @Override
    public void tick() {
        super.tick();
        updateAlpha();

        if (this.isThrowable) {
            float normalizedAge = (float) this.age / this.lifetime;

            // If the particle is still young, make it rise quickly
            if (normalizedAge < 0.7F) {
                this.yd += 0.05; // Faster initial rise
            } else {
                // After the initial stage, slow down the rise
                this.yd += 0.03F; // Slow continuous upward movement
            }
        } else {
            // For non-flare particles, a slight upward drift
            this.yd += 0.002F;
        }

        // Apply southeast wind effect, consistent for both flare and non-flare
        this.xd += 0.003; // Slightly drift towards the east
        this.zd += 0.003; // Slightly drift towards the south

        // Random motion after 20% of its lifetime
        float normalizedAge = (float) this.age / this.lifetime;
        if (normalizedAge > 0.2F) {
            this.xd += (this.random.nextFloat() - 0.5F) * 0.01F;
            this.zd += (this.random.nextFloat() - 0.5F) * 0.01F;
            this.yd += (this.random.nextFloat()) * 0.005F;
        }

        this.xd *= 0.98F;
        this.zd *= 0.98F;
        this.yd *= 0.98F;
    }

    private void applyWindEffect() {
        if (this.age > 10) {
            this.xd += (random.nextDouble() - 0.5) * 0.01;
            this.zd += (random.nextDouble() - 0.5) * 0.01;
        }
    }

    private void updateAlpha() {
        float normalizedAge = (float) this.age / this.lifetime;
        if (normalizedAge < 0.2F) {
            this.alpha = 1 * 5.0F;
        } else {
            this.alpha = 1.0F - (normalizedAge - 0.2F) / 0.8F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float pPartialTick) {
        float normalizedAge = ((float) this.age + pPartialTick) / (float) this.lifetime;
        normalizedAge = Mth.clamp(normalizedAge, 0.0F, 1.0F);

        int maxLight = 240;
        int minLight = 10;
        int finalLight = 200;
        int transitionTicks1 = 30;
        int transitionTicks2 = 10;

        if (this.isThrowable) {
            transitionTicks2 = 60;
        }

        int lightValue;

        // Handle the first transition from 240 to 125 for the first 10 ticks
        if (this.age < transitionTicks1) {
            float transitionFactor = (float) this.age / transitionTicks1;
            lightValue = (int) (maxLight - transitionFactor * (maxLight - minLight));
        }
        // Handle the second transition from 125 to 200 after 80 ticks for 30 ticks
        else if (this.age >= 80 && this.age < (80 + transitionTicks2)) {
            float transitionFactor = (float) (this.age - 80) / transitionTicks2;
            lightValue = (int) (minLight + transitionFactor * (finalLight - minLight));
        }
        // After 80 ticks, keep the light at 200
        else if (this.age >= (80 + transitionTicks2)) {
            lightValue = finalLight;
        }
        // Before 80 ticks, keep the light at minLight
        else {
            lightValue = minLight;
        }

        // Ensure the value doesn't go below minLight
        if (lightValue < minLight) {
            lightValue = minLight;
        }

        int packedLight = super.getLightColor(pPartialTick);
        int blockLight = packedLight & 255;
        int skyLight = packedLight >> 16 & 255;

        return lightValue | (skyLight << 16);
    }

    public float getQuadSize(float scaleFactor) {
        float normalizedAge = ((float) this.age + scaleFactor) / (float) this.lifetime;
        if (normalizedAge < 0.2F) {
            return this.quadSize * (0.5F + normalizedAge * 5.0F);
        }
        else {
            float slowGrowth = 1.5F + (normalizedAge - 0.2F) * 0.8F;
            return this.quadSize * slowGrowth;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ColoredFlareData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(ColoredFlareData data, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ColoredFlareSmokeParticle(level, x,y,z, this.sprites, dx,dy,dz, false, data);
        }
    }
}
