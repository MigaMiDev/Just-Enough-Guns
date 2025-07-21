package ttv.migami.jeg.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import ttv.migami.jeg.init.ModParticleTypes;

import java.util.Locale;

public class ColoredFlareData implements ParticleOptions {
    public static final Codec<ColoredFlareData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("r").forGetter(d -> d.r),
            Codec.INT.fieldOf("g").forGetter(d -> d.g),
            Codec.INT.fieldOf("b").forGetter(d -> d.b)
    ).apply(instance, ColoredFlareData::new));

    public static final Deserializer<ColoredFlareData> DESERIALIZER = new Deserializer<>() {
        @Override
        public ColoredFlareData fromCommand(ParticleType<ColoredFlareData> type, StringReader reader) throws CommandSyntaxException {
            int r = reader.readInt();
            reader.expect(' ');
            int g = reader.readInt();
            reader.expect(' ');
            int b = reader.readInt();
            return new ColoredFlareData(r, g, b);
        }

        public ColoredFlareData fromNetwork(ParticleType<ColoredFlareData> type, FriendlyByteBuf buf) {
            return new ColoredFlareData(buf.readInt(), buf.readInt(), buf.readInt());
        }
    };

    private final int r, g, b;

    public ColoredFlareData(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.COLORED_FLARE_SMOKE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%d %d %d", r, g, b);
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }
}