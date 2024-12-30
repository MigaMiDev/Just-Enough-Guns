package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.network.ClientPlayHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageExplosiveAmmo extends PlayMessage<S2CMessageExplosiveAmmo>
{
    private double x;
    private double y;
    private double z;

    public S2CMessageExplosiveAmmo() {}

    public S2CMessageExplosiveAmmo(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void encode(S2CMessageExplosiveAmmo message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    @Override
    public S2CMessageExplosiveAmmo decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageExplosiveAmmo(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void handle(S2CMessageExplosiveAmmo message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleExplosiveAmmo(message));
        context.setHandled(true);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }
}