package ttv.migami.jeg.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.client.network.ClientPlayHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageLaser extends PlayMessage<S2CMessageLaser>
{
    private double x;
    private double y;
    private double z;
    private BlockPos pos;
    private Direction face;

    public S2CMessageLaser() {}

    public S2CMessageLaser(double x, double y, double z, BlockPos pos, Direction face)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pos = pos;
        this.face = face;
    }

    @Override
    public void encode(S2CMessageLaser message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeBlockPos(message.pos);
        buffer.writeEnum(message.face);
    }

    @Override
    public S2CMessageLaser decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        BlockPos pos = buffer.readBlockPos();
        Direction face = buffer.readEnum(Direction.class);
        return new S2CMessageLaser(x, y, z, pos, face);
    }

    @Override
    public void handle(S2CMessageLaser message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleLaser(message));
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

    public BlockPos getPos()
    {
        return this.pos;
    }

    public Direction getFace()
    {
        return this.face;
    }
}