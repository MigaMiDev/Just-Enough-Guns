package ttv.migami.jeg.client.medal;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import ttv.migami.jeg.client.util.FPSUtil;

public class Medal {
    private final ResourceLocation texture;
    private final MutableComponent description;
    private int x, y;
    private float opacity;
    private double lifetime;

    private final float maxScale = 1.5F;
    private final float minScale = 1.0F;
    private float currentScale = maxScale;

    private final SoundEvent sound;

    public Medal(ResourceLocation texture, int startX, int startY, double lifetime, MutableComponent description, SoundEvent sound) {
        this.texture = texture;
        this.x = startX;
        this.y = startY;
        this.opacity = 1.0f;
        this.lifetime = lifetime;
        this.description = description;
        this.sound = sound;
    }

    public SoundEvent getSound() {
        return sound;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public MutableComponent getDescription() {
        return description;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getScale() {
        return currentScale;
    }

    public float getOpacity() {
        return opacity;
    }

    public double getLifetime() {
        return lifetime;
    }

    public boolean tick() {
        lifetime -= 1 / FPSUtil.calc();
        if (lifetime < 20 * FPSUtil.calc()) {
            opacity = (float) Math.max(0.0f, lifetime / 20.0f);
        }
        if (lifetime < 40 * FPSUtil.calc()) {
            x += (int) (5 / FPSUtil.calc());
            //y -= (int) (5 / FPSUtil.calc());
        }

        if (currentScale > minScale) {
            currentScale -= 0.05f;
        }

        return lifetime <= 0;
    }
}