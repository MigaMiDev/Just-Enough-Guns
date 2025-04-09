package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageFirstPersonReload extends PlayMessage<C2SMessageFirstPersonReload>
{
    private boolean firstPerson;

    public C2SMessageFirstPersonReload() {}

    public C2SMessageFirstPersonReload(boolean firstPerson)
    {
        this.firstPerson = firstPerson;
    }

    @Override
    public void encode(C2SMessageFirstPersonReload message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.firstPerson);
    }

    @Override
    public C2SMessageFirstPersonReload decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageFirstPersonReload(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageFirstPersonReload message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null)
            {
                ServerPlayHandler.handleReloadPerspective(player, message.firstPerson);
            }
        });
        context.setHandled(true);
    }
}