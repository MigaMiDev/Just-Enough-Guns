package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.ReloadTracker;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageGunUnjammed extends PlayMessage<C2SMessageGunUnjammed>
{
    @Override
    public void encode(C2SMessageGunUnjammed message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageGunUnjammed decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageGunUnjammed();
    }

    @Override
    public void handle(C2SMessageGunUnjammed message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.unjam(player);
            }
        });
        context.setHandled(true);
    }
}
