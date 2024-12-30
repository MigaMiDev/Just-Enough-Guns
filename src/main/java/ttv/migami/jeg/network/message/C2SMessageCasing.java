package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.event.GunEventBus;

/**
 * Author: MrCrayfish
 */
public class C2SMessageCasing extends PlayMessage<C2SMessageCasing>
{
    @Override
    public void encode(C2SMessageCasing message, FriendlyByteBuf buffer) {}

    @Override
    public C2SMessageCasing decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageCasing();
    }

    @Override
    public void handle(C2SMessageCasing message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                GunEventBus.ejectCasing(player.level(), player);
            }
        });
        context.setHandled(true);
    }
}
