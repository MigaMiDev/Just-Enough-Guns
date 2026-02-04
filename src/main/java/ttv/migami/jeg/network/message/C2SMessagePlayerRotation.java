package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import ttv.migami.jeg.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePlayerRotation extends PlayMessage<C2SMessagePlayerRotation>
{
    private float rotationYaw;
    private float rotationPitch;

    public C2SMessagePlayerRotation() {}

    public C2SMessagePlayerRotation(Player player)
    {
        this.rotationYaw = player.getYRot();
        this.rotationPitch = player.getXRot();
    }

    public C2SMessagePlayerRotation(float rotationYaw, float rotationPitch)
    {
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
    }

    @Override
    public void encode(C2SMessagePlayerRotation message, FriendlyByteBuf buffer)
    {
        buffer.writeFloat(message.rotationYaw);
        buffer.writeFloat(message.rotationPitch);
    }

    @Override
    public C2SMessagePlayerRotation decode(FriendlyByteBuf buffer)
    {
        float rotationYaw = buffer.readFloat();
        float rotationPitch = buffer.readFloat();
        return new C2SMessagePlayerRotation(rotationYaw, rotationPitch);
    }

    @Override
    public void handle(C2SMessagePlayerRotation message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null)
            {
                ServerPlayHandler.rotatePhantom(message, player);
            }
        });
        context.setHandled(true);
    }

    public float getRotationYaw()
    {
        return this.rotationYaw;
    }

    public float getRotationPitch()
    {
        return this.rotationPitch;
    }
}