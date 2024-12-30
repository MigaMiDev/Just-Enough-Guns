package ttv.migami.jeg.mixin.common;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ttv.migami.jeg.item.TelescopicScopeItem;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
    private void customIsScoping(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof TelescopicScopeItem) {
            cir.setReturnValue(true);
        }
    }
}