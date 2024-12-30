package ttv.migami.jeg.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.init.ModParticleTypes;

/**
 * Author: MrCrayfish
 */
public class LaserData implements ParticleOptions
{
    public static final Codec<LaserData> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.fieldOf("dir").forGetter((data) -> {
            return data.direction.ordinal();
        }), Codec.LONG.fieldOf("pos").forGetter((p_239806_0_) -> {
            return p_239806_0_.pos.asLong();
        })).apply(builder, LaserData::new);
    });

    public static final Deserializer<LaserData> DESERIALIZER = new Deserializer<LaserData>()
    {
        @Override
        public LaserData fromCommand(ParticleType<LaserData> particleType, StringReader reader) throws CommandSyntaxException
        {
            reader.expect(' ');
            int dir = reader.readInt();
            reader.expect(' ');
            long pos = reader.readLong();
            return new LaserData(dir, pos);
        }

        @Override
        public LaserData fromNetwork(ParticleType<LaserData> particleType, FriendlyByteBuf buffer)
        {
            return new LaserData(buffer.readInt(), buffer.readLong());
        }
    };

    private final Direction direction;
    private final BlockPos pos;

    public LaserData(int dir, long pos)
    {
        this.direction = Direction.values()[dir];
        this.pos = BlockPos.of(pos);
    }

    public LaserData(Direction dir, BlockPos pos)
    {
        this.direction = dir;
        this.pos = pos;
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    @Override
    public ParticleType<?> getType()
    {
        return ModParticleTypes.LASER.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.direction);
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public String writeToString()
    {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + this.direction.getName();
    }

    public static Codec<LaserData> codec(ParticleType<LaserData> type)
    {
        return CODEC;
    }
}
