package ttv.migami.jeg.entity.monster.phantom.terror;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import ttv.migami.jeg.init.ModSounds;

public class TerrorPhantomBoostSoundInstance extends AbstractTickableSoundInstance {
    private final TerrorPhantom entity;

    public TerrorPhantomBoostSoundInstance(TerrorPhantom entity, SoundSource soundSource) {
        super(ModSounds.ENTITY_PHANTOM_BOOST.get(), soundSource, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 10.0F;
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