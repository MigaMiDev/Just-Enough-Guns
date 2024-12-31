package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.handler.ShootingHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageShootAnimation extends PlayMessage<S2CMessageShootAnimation>
{
    private boolean headshot;

    public S2CMessageShootAnimation() {}

    public S2CMessageShootAnimation(boolean headshot)
    {
        this.headshot = headshot;
    }

    @Override
    public void encode(S2CMessageShootAnimation message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.headshot);
    }

    @Override
    public S2CMessageShootAnimation decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageShootAnimation(buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageShootAnimation message, MessageContext context)
    {
        context.execute(ShootingHandler::playFireAnimation);
        context.setHandled(true);
    }
}
