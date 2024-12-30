package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageOverheat extends PlayMessage<C2SMessageOverheat>
{
    @Override
    public void encode(C2SMessageOverheat message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageOverheat decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageOverheat();
    }

    @Override
    public void handle(C2SMessageOverheat message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.overheat(player);
            }
        });
        context.setHandled(true);
    }
}
