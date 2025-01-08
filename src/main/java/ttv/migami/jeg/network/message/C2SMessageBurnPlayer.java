package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageBurnPlayer extends PlayMessage<C2SMessageBurnPlayer>
{
    @Override
    public void encode(C2SMessageBurnPlayer message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageBurnPlayer decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageBurnPlayer();
    }

    @Override
    public void handle(C2SMessageBurnPlayer message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.burnPlayer(player);
            }
        });
        context.setHandled(true);
    }
}
