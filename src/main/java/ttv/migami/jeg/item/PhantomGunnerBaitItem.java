package ttv.migami.jeg.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.entity.throwable.ThrowablePhantomGunnerBaitEntity;
import ttv.migami.jeg.init.ModEntities;

public class PhantomGunnerBaitItem extends ScoreStreakItem {
    public PhantomGunnerBaitItem(Properties properties, int maxPoints) {
        super(properties, maxPoints);
    }

    @Override
    public void useScoreStreak(Player player) {
        PhantomGunner gunner = new PhantomGunner(ModEntities.PHANTOM_GUNNER.get(), player.level());
        int offsetX = 32;
        if (player.getRandom().nextBoolean()) {
            offsetX *= -1;
        }
        int offsetZ = 32;
        if (player.getRandom().nextBoolean()) {
            offsetZ *= -1;
        }
        gunner.setPos(player.position().add(offsetX, 15, offsetZ));
        player.level().addFreshEntity(gunner);
        gunner.setPlayer(player);
        gunner.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
        gunner.addTag("PlayerOwned");

        ThrowablePhantomGunnerBaitEntity gunnerBait = new ThrowablePhantomGunnerBaitEntity(player.level(), player);
        gunnerBait.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 1.0F);
        player.level().addFreshEntity(gunnerBait);
    }
}
