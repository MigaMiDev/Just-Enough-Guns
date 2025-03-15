package ttv.migami.jeg.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.particles.BulletHoleData;
import ttv.migami.jeg.particles.LaserData;
import ttv.migami.jeg.particles.TrailData;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID);

    public static final RegistryObject<ParticleType<BulletHoleData>> BULLET_HOLE = REGISTER.register("bullet_hole", () -> new ParticleType<>(false, BulletHoleData.DESERIALIZER) {
        @Override
        public Codec<BulletHoleData> codec() {
            return BulletHoleData.CODEC;
        }
    });
    public static final RegistryObject<ParticleType<LaserData>> LASER = REGISTER.register("laser", () -> new ParticleType<>(false, LaserData.DESERIALIZER) {
        @Override
        public Codec<LaserData> codec() {
            return LaserData.CODEC;
        }
    });
    public static final RegistryObject<SimpleParticleType> BLOOD = REGISTER.register("blood", () -> new SimpleParticleType(true));
    public static final RegistryObject<ParticleType<TrailData>> TRAIL = REGISTER.register("trail", () -> new ParticleType<>(false, TrailData.DESERIALIZER) {
        @Override
        public Codec<TrailData> codec() {
            return TrailData.CODEC;
        }
    });
    public static final RegistryObject<SimpleParticleType> CASING_PARTICLE = REGISTER.register("casing", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SHELL_PARTICLE = REGISTER.register("shell", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SPECTRE_CASING_PARTICLE = REGISTER.register("spectre_casing", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SCRAP = REGISTER.register("scrap", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> HEALING_GLINT = REGISTER.register("healing_glint", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GHOST_FLAME = REGISTER.register("ghost_flame", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GHOST_GLINT = REGISTER.register("ghost_glint", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> TYPHOONEE_BEAM = REGISTER.register("typhoonee_beam", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SOUL_LAVA_PARTICLE = REGISTER.register("soul_lava", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SONIC_RING = REGISTER.register("sonic_ring", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BIG_SONIC_RING = REGISTER.register("big_sonic_ring", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLARE_SMOKE = REGISTER.register("flare_smoke", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLARE = REGISTER.register("flare", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLUE_FLARE = REGISTER.register("flare_blue", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLAME = REGISTER.register("flame", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLUE_FLAME = REGISTER.register("blue_flame", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SPARK = REGISTER.register("spark", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BIG_EXPLOSION = REGISTER.register("big_explosion", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMALL_EXPLOSION = REGISTER.register("small_explosion", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMOKE = REGISTER.register("smoke", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FIRE = REGISTER.register("fire", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> BUBBLE_AMMO = REGISTER.register("bubble_ammo", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ENTITY_LASER = REGISTER.register("entity_laser_glint", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> CONFETTI = REGISTER.register("confetti", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> HIT_MARKER = REGISTER.register("hit_marker", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> POPCORN = REGISTER.register("popcorn", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> PHANTOM_GUNNER = REGISTER.register("phantom_gunner", () -> new SimpleParticleType(true));
}
