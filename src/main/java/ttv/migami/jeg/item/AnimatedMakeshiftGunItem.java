package ttv.migami.jeg.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public class AnimatedMakeshiftGunItem extends AnimatedGunItem {

    public AnimatedMakeshiftGunItem(Properties properties, String path,
                                    SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent ejectorSoundPull, SoundEvent ejectorSoundRelease) {
        super(properties, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, ejectorSoundPull, ejectorSoundRelease);
    }

    public boolean isFoil(ItemStack ignored) {
        return false;
    }

}