package ttv.migami.jeg.entity.monster.phantom.gunner;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.jeg.init.ModSounds;

@OnlyIn(Dist.CLIENT)
public class PhantomGunnerFlySoundInstance extends AbstractTickableSoundInstance {
    private final PhantomGunner entity;

    public PhantomGunnerFlySoundInstance(PhantomGunner entity, SoundSource soundSource) {
        super(ModSounds.ENTITY_PHANTOM_FLY.get(), soundSource, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 5.0F;
        this.pitch = 1.0F;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        if (this.entity.isDying() || this.entity.isRemoved()) {
            this.stop();
        } else {
            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }
    }
}