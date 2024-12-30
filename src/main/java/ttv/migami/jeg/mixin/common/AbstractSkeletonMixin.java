package ttv.migami.jeg.mixin.common;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ttv.migami.jeg.item.TrumpetItem;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {

    @Inject(method = "reassessWeaponGoal", at = @At("HEAD"), cancellable = true)
    private void checkForTrumpetItem(CallbackInfo ci) {
        // Cast to AbstractSkeleton to access its methods
        AbstractSkeleton skeleton = (AbstractSkeleton) (Object) this;

        // Check if the skeleton is holding a TrumpetItem
        if (skeleton.getMainHandItem().getItem() instanceof TrumpetItem) {
            // Cancel the reassessWeaponGoal method if TrumpetItem is in hand
            ci.cancel();
        }
    }
}