package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.ReloadTracker;

/**
 * Author: MrCrayfish
 */
public class C2SMessageGunLoaded extends PlayMessage<C2SMessageGunLoaded>
{
    @Override
    public void encode(C2SMessageGunLoaded message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageGunLoaded decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageGunLoaded();
    }

    @Override
    public void handle(C2SMessageGunLoaded message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ReloadTracker.loaded(player);
            }
        });
        context.setHandled(true);
    }
}
