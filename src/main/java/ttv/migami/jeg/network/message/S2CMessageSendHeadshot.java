package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.medal.MedalManager;

/**
 * Author: MrCrayfish
 */
public class S2CMessageSendHeadshot extends PlayMessage<S2CMessageSendHeadshot>
{
    private boolean headshot;

    public S2CMessageSendHeadshot() {}

    public S2CMessageSendHeadshot(boolean headshot)
    {
        this.headshot = headshot;
    }

    @Override
    public void encode(S2CMessageSendHeadshot message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.headshot);
    }

    @Override
    public S2CMessageSendHeadshot decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageSendHeadshot(buffer.readBoolean());
    }

    @Override
    public void handle(S2CMessageSendHeadshot message, MessageContext context)
    {
        //context.execute(() -> MedalManager.setHeadshot(message.headshot));
        context.execute(() -> MedalManager.setHeadshot(true));
        context.setHandled(true);
    }
}
