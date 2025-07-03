package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.network.ClientPlayHandler;

public class S2CMessageSmokeGrenade extends PlayMessage<S2CMessageSmokeGrenade>
{
    private double x, y, z;

    public S2CMessageSmokeGrenade() {}

    public S2CMessageSmokeGrenade(double x, double y, double z)
    {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    @Override
    public void encode(S2CMessageSmokeGrenade message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    @Override
    public S2CMessageSmokeGrenade decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageSmokeGrenade(x, y, z);
    }

    @Override
    public void handle(S2CMessageSmokeGrenade message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleExplosionSmokeGrenade(message));
        context.setHandled(true);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }
}