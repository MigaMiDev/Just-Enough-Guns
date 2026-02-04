package ttv.migami.jeg.mixin.common;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Redirect(
            method = "turn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
            )
    )
    private float onTurnClampPitch(float value, float min, float max) {
        Entity self = (Entity)(Object)this;

        if (self.getVehicle() instanceof TerrorPhantom) {
            value = Mth.positiveModulo(value, 360.0F);
            if (value > 180.0F) {
                value -= 360.0F;
            }
            return value;
        }

        return Mth.clamp(value, min, max);
    }

    @Inject(
            method = "turn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTurn(double pYaw, double pPitch, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;

        if (self.getVehicle() instanceof TerrorPhantom) {
            double yaw = pYaw;
            double pitch = pPitch;

            Vec3 up = self.getUpVector(1.0F);

            if (up.y < 0) {
                yaw = -yaw;
            }

            self.setYRot((float)(self.getYRot() + yaw * 0.15F));
            self.setXRot((float)(self.getXRot() + pitch * 0.15F));

            ci.cancel();
        }
    }
}