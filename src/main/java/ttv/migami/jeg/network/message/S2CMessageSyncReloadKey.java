package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.handler.ReloadHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageSyncReloadKey extends PlayMessage<S2CMessageSyncReloadKey>
{
    public S2CMessageSyncReloadKey() {}


    @Override
    public void encode(S2CMessageSyncReloadKey message, FriendlyByteBuf buffer)
    {
    }

    @Override
    public S2CMessageSyncReloadKey decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageSyncReloadKey();
    }

    @Override
    public void handle(S2CMessageSyncReloadKey message, MessageContext context)
    {
        context.execute(ReloadHandler::syncReloadKey);
        context.setHandled(true);
    }
}
