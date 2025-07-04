package ttv.migami.jeg.entity.ai.phantom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.entity.ai.AIGunEvent;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.item.GunItem;

import static ttv.migami.jeg.event.GunEventBus.ejectCasing;

public class TerrorPhantomGunAttackGoal<T extends PathfinderMob> extends Goal {
    protected final TerrorPhantom shooter;
    protected int seeTime;
    protected int attackTime;
    protected final float attackRadiusSqr;

    protected int reloadTick = 0;
    protected boolean isReloading = false;

    protected Vec3 lastKnownPosition;

    protected float spreadModifier = 10;
    protected int burstAmount = 3;
    protected int burstTimer = 20;

    public TerrorPhantomGunAttackGoal(TerrorPhantom shooter, double stopRange, int difficulty) {
        this.shooter = shooter;
        this.attackTime = -1;
        this.attackRadiusSqr = (float) (stopRange * stopRange);
        if (this.shooter.getTarget() != null) {
            this.lastKnownPosition = this.shooter.getTarget().position();
        }

        this.spreadModifier /= difficulty;
        this.burstAmount *= difficulty;
        this.burstTimer /= difficulty;
    }

    @Override
    public boolean canUse() {
        return !this.shooter.isDying() && this.shooter.getTarget() != null && this.isHoldingGun() && !this.shooter.getTarget().isDeadOrDying();
    }

    protected boolean isHoldingGun() {
        return this.shooter.isHolding((itemStack) -> itemStack.getItem() instanceof GunItem);
    }

    @Override
    public void start() {
        super.start();
        this.shooter.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.shooter.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.shooter.stopUsingItem();
        this.reloadTick = 0;
        this.isReloading = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.shooter.getTarget();
        ItemStack heldItem = this.shooter.getMainHandItem();

        if (target != null && heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);

            // Shooting
            if (this.shooter.getAttackPhase().equals(TerrorPhantom.AttackPhase.SWOOP)) {
                shoot(target, gun);
            }
        }
    }

    private void shoot(LivingEntity target, Gun gun) {
        ItemStack heldItem = this.shooter.getMainHandItem();
        AIGunEvent.performGunAttack(this.shooter, target, heldItem, gun, this.spreadModifier);
        this.attackTime = gun.getGeneral().getRate() + 1;
        ejectCasing(this.shooter.level(), this.shooter);
        ResourceLocation fireSound = gun.getSounds().getFire();
        if(fireSound != null) {
            double posX = this.shooter.getX();
            double posY = this.shooter.getY() + this.shooter.getEyeHeight();
            double posZ = this.shooter.getZ();
            float volume = Config.COMMON.world.mobGunfireVolume.get();
            float pitch = 0.9F + this.shooter.level().random.nextFloat() * 0.2F;
            this.shooter.level().playSound(null, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.HOSTILE, volume - 0.5F, pitch);
        }
    }

    public static Vec3 getDirectionToTarget(TerrorPhantom entity, Entity target) {
        if (target == null) return Vec3.ZERO;

        Vec3 entityPos = entity.position();
        Vec3 targetPos = target.position();

        return targetPos.subtract(entityPos).normalize();
    }
}