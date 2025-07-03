package ttv.migami.jeg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import ttv.migami.jeg.Config;

@OnlyIn(Dist.CLIENT)
public class SmokeCloudParticle extends TextureSheetParticle
    
{
    private final SpriteSet sprites;
    public SmokeCloudParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites)
    {
        super(level, x, y, z);
        this.scale(3.5f);
        this.setAlpha(1.00f);
        this.setSize(0.25F, 0.25F);

        int life = (int) (Config.COMMON.smokeGrenades.smokeGrenadeCloudDuration.get() * 20);
        this.lifetime = (int) (life + ((Math.random()-0.5) * (life * 0.25)));
        this.sprites = sprites;
        this.gravity = 3.0E-6F;
        this.xd = xSpeed;
        this.yd = ySpeed + (double)(this.random.nextFloat() / 500.0F);
        this.zd = zSpeed;
    }

    public void tick()
    {
        this.setSpriteFromAge(this.sprites);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F))
        {
            this.xd += (this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.zd += (this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);

            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F)
            {
                this.alpha -= 0.1F; //Fade out
            }
        }
        else
        {
            this.remove();
        }
    }

    public @NotNull ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float scaleFactor)
    {
        return this.quadSize * Mth.clamp(((float)this.age + scaleFactor) / (float)this.lifetime * 48.0f, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            SmokeCloudParticle smokeCloudParticle = new SmokeCloudParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            smokeCloudParticle.scale(3.5f);
            smokeCloudParticle.setAlpha(1.00f);
            smokeCloudParticle.pickSprite(this.sprites);
            return smokeCloudParticle;
        }
    }
}