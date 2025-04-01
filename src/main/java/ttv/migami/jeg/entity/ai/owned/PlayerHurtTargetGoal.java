package ttv.migami.jeg.entity.ai.owned;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;

import java.util.EnumSet;

public class PlayerHurtTargetGoal extends TargetGoal {
    private final Mob ownedEntity;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public PlayerHurtTargetGoal(Mob ownedMob) {
        super(ownedMob, false);
        this.ownedEntity = ownedMob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        if (this.ownedEntity instanceof PhantomGunner phantomGunner && phantomGunner.playerOwned) {
            LivingEntity livingentity = null;
            if (this.ownedEntity instanceof PhantomGunner) {
                livingentity = phantomGunner.getPlayer();
            }
            if (livingentity == null) {
                return false;
            } else {
                this.ownerLastHurt = livingentity.getLastHurtMob();
                if (this.ownerLastHurt != null && this.ownerLastHurt.isDeadOrDying()) {
                    return false;
                }
                if (this.ownerLastHurt instanceof Villager) {
                    return false;
                }

                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.ownedEntity.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = null;
        if (this.ownedEntity instanceof PhantomGunner phantomGunner) {
            livingentity = phantomGunner.getPlayer();
        }
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}