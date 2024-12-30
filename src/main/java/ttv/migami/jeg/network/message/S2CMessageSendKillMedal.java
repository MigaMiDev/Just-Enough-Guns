package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.medal.MedalManager;

/**
 * Author: MrCrayfish
 */
public class S2CMessageSendKillMedal extends PlayMessage<S2CMessageSendKillMedal>
{
    private static final ResourceLocation KILL_SINGLE = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_kill_single.png");

    @Override
    public void encode(S2CMessageSendKillMedal message, FriendlyByteBuf buffer) {}

    @Override
    public S2CMessageSendKillMedal decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageSendKillMedal();
    }

    @Override
    public void handle(S2CMessageSendKillMedal message, MessageContext context)
    {
        context.execute(() -> MedalManager.addKillMedal(KILL_SINGLE, Component.translatable("medal.jeg.multikill_kill_single")));
        context.setHandled(true);
    }
}
