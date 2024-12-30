package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.event.FlashlightEvent;

/**
 * Author: MrCrayfish
 */
public class C2SMessageFlashlight extends PlayMessage<C2SMessageFlashlight>
{
    @Override
    public void encode(C2SMessageFlashlight message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageFlashlight decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageFlashlight();
    }

    @Override
    public void handle(C2SMessageFlashlight message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                FlashlightEvent.chargeFlashlight(player);
            }
        });
        context.setHandled(true);
    }
}
