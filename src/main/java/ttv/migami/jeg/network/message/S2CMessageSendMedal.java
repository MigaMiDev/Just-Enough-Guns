package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.medal.MedalManager;

/**
 * Author: MrCrayfish
 */
public class S2CMessageSendMedal extends PlayMessage<S2CMessageSendMedal>
{
    private int medal;

    public S2CMessageSendMedal() {}

    public S2CMessageSendMedal(int medal)
    {
        this.medal = medal;
    }

    @Override
    public void encode(S2CMessageSendMedal message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.medal);
    }

    @Override
    public S2CMessageSendMedal decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageSendMedal(buffer.readInt());
    }

    @Override
    public void handle(S2CMessageSendMedal message, MessageContext context)
    {
        context.execute(() -> MedalManager.addEnumMedal(message.medal));
        context.setHandled(true);
    }
}
