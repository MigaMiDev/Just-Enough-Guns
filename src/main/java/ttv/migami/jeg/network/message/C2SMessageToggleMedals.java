package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.jeg.common.network.ServerPlayHandler;

public class C2SMessageToggleMedals extends PlayMessage<C2SMessageToggleMedals>
{

    public C2SMessageToggleMedals() {}

    @Override
    public void encode(C2SMessageToggleMedals message, FriendlyByteBuf buffer)
    {
    }

    @Override
    public C2SMessageToggleMedals decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageToggleMedals();
    }

    @Override
    public void handle(C2SMessageToggleMedals message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.toggleMedals(player);
            }
        });
        context.setHandled(true);
    }
}