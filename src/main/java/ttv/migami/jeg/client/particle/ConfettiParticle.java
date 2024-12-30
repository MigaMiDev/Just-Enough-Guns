package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfettiParticle extends TextureSheetParticle {
    ConfettiParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ, 0.0, 0.0, 0.0);
        this.gravity = 0.2F;
        this.friction = 0.999F;
        this.xd *= 0.800000011920929;
        this.yd *= 0.800000011920929;
        this.zd *= 0.800000011920929;
        this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
        this.quadSize *= this.random.nextFloat();
        this.lifetime = (int)(64 / (Math.random() * 0.8 + 0.2));
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
            ConfettiParticle confettiParticle = new ConfettiParticle(pLevel, pX, pY, pZ);
            confettiParticle.pickSprite(this.sprite);

            float[][] confettiColors = {
                    {1F, 0F, 0F},     // Red
                    {1F, 0.75F, 0.8F},// Pink
                    {1F, 1F, 0F},     // Yellow
                    {0.5F, 1F, 0F},   // Lime
                    {0F, 0.5F, 1F},   // Sky Blue
                    {1F, 1F, 1F},     // White
                    {1F, 0.5F, 0F},   // Orange
                    {0.58F, 0F, 0.83F}, // Purple
                    {0F, 1F, 1F},     // Cyan
                    {1F, 0F, 1F},     // Magenta
                    {0F, 1F, 0.5F},   // Mint
                    {0F, 0F, 1F},     // Deep Blue
                    {1F, 0.2F, 0.2F}, // Coral Red
                    {0F, 1F, 0F},     // Bright Green
                    {0.94F, 0.9F, 0.55F}, // Light Gold
                    {1F, 0.3F, 0.7F}, // Hot Pink
                    {0.75F, 0F, 0.2F},// Crimson
                    {1F, 0.85F, 0F},  // Golden Yellow
                    {0.13F, 0.55F, 0.13F} // Forest Green
            };

            int randomIndex = pLevel.getRandom().nextInt(confettiColors.length);
            float[] selectedColor = confettiColors[randomIndex];
            confettiParticle.setColor(selectedColor[0], selectedColor[1], selectedColor[2]);

            return confettiParticle;
        }
    }
}