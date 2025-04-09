package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageInspectGun extends PlayMessage<C2SMessageInspectGun>
{
    @Override
    public void encode(C2SMessageInspectGun message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageInspectGun decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageInspectGun();
    }

    @Override
    public void handle(C2SMessageInspectGun message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleInspectGun(player);
            }
        });
        context.setHandled(true);
    }
}
