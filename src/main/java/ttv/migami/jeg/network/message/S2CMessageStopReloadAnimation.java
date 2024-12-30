package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.handler.ReloadHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageStopReloadAnimation extends PlayMessage<S2CMessageStopReloadAnimation>
{
    @Override
    public void encode(S2CMessageStopReloadAnimation message, FriendlyByteBuf buffer) {}

    @Override
    public S2CMessageStopReloadAnimation decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageStopReloadAnimation();
    }

    @Override
    public void handle(S2CMessageStopReloadAnimation message, MessageContext context)
    {
        context.execute(ReloadHandler::stopReloadAnimation);
        context.setHandled(true);
    }
}
