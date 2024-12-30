package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.ReloadTracker;

/**
 * Author: MrCrayfish
 */
public class C2SMessageStopReloading extends PlayMessage<C2SMessageStopReloading>
{
    @Override
    public void encode(C2SMessageStopReloading message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageStopReloading decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageStopReloading();
    }

    @Override
    public void handle(C2SMessageStopReloading message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ReloadTracker.stopReloading(player);
            }
        });
        context.setHandled(true);
    }
}
