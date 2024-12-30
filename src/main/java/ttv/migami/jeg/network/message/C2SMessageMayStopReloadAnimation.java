package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.ReloadTracker;

/**
 * Author: MrCrayfish
 */
public class C2SMessageMayStopReloadAnimation extends PlayMessage<C2SMessageMayStopReloadAnimation>
{
    @Override
    public void encode(C2SMessageMayStopReloadAnimation message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageMayStopReloadAnimation decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageMayStopReloadAnimation();
    }

    @Override
    public void handle(C2SMessageMayStopReloadAnimation message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ReloadTracker.mayStopReloading(player);
            }
        });
        context.setHandled(true);
    }
}
