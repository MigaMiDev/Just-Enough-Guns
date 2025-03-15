package ttv.migami.jeg.item;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.player.Player;
import ttv.migami.jeg.entity.throwable.ThrowableTerrorPhantomFlareEntity;

public class TerrorPhantomFlareItem extends ScoreStreakItem {
    public TerrorPhantomFlareItem(Properties properties, int maxPoints) {
        super(properties, maxPoints);
    }

    @Override
    public void useScoreStreak(Player player) {
        ThrowableTerrorPhantomFlareEntity terrorFlare = new ThrowableTerrorPhantomFlareEntity(player.level(), player, player.getLookAngle());
        terrorFlare.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 1.0F);
        player.level().addFreshEntity(terrorFlare);
        terrorFlare.lookAt(EntityAnchorArgument.Anchor.FEET, player.getLookAngle());
        terrorFlare.setAngle(player.getLookAngle().toVector3f());
    }
}
