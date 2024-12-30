package ttv.migami.jeg.mixin.common;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Author: MrCrayfish
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    private DamageSource source;

    // Disabled to make VPB compatible :)
    /*@Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void capture(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        this.source = source;
    }

    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), index = 0)
    private double modifyApplyKnockbackArgs(double original)
    {
        if(this.source.getEntity() instanceof ProjectileEntity)
        {
            if(!Config.COMMON.gameplay.enableKnockback.get())
            {
                return 0;
            }

            double strength = Config.COMMON.gameplay.knockbackStrength.get();
            if(strength > 0)
            {
                return strength;
            }
        }
        return original;
    }*/

}
