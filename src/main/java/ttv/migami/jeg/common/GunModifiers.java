package ttv.migami.jeg.common;

import ttv.migami.jeg.interfaces.IGunModifier;

/**
 * Author: MrCrayfish
 */
public class GunModifiers
{
    public static final IGunModifier SILENCED = new IGunModifier()
    {
        @Override
        public boolean silencedFire()
        {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.25;
        }
    };

    public static final IGunModifier ANNOYING = new IGunModifier()
    {
        @Override
        public boolean annoying() {
            return true;
        }
    };

    public static final IGunModifier FLASHLIGHT = new IGunModifier()
    {
        @Override
        public boolean flashlight() {
            return true;
        }
    };

    public static final IGunModifier LASER_POINTER = new IGunModifier()
    {
        @Override
        public boolean laserPointer() {
            return true;
        }
    };

    public static final IGunModifier EXPLOSIVE_AMMO = new IGunModifier()
    {
        @Override
        public boolean explosiveAmmo() {
            return true;
        }
    };

    public static final IGunModifier INCREASED_JAMMING = new IGunModifier()
    {
        @Override
        public boolean increasedJamming() {
            return true;
        }
    };


    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        {
            return damage * 0.75F;
        }
    };

    public static final IGunModifier INSCREASED_DAMAGE = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        {
            return damage * 1.1F;
        }
    };

    public static final IGunModifier SLOW_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier SLOWER_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier SLOWEST_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.76F;
        }
    };

    public static final IGunModifier MAKESHIFT_CONTROL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.7F;
        }

        @Override
        public float kickModifier()
        {
            return 0.7F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 1.0F;
        }
    };

    public static final IGunModifier BETTER_CONTROL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.4F;
        }

        @Override
        public float kickModifier()
        {
            return 0.9F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier WORSE_CONTROL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 1.5F;
        }

        @Override
        public float kickModifier()
        {
            return 1.35F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 1.3F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier STABILISED = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.5F;
        }

        @Override
        public float kickModifier()
        {
            return 0.4F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.9F;
        }
    };

    public static final IGunModifier SUPER_STABILISED = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.4F;
        }

        @Override
        public float kickModifier()
        {
            return 0.3F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.25F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.9F;
        }

        /*@Override
        public int modifyFireRate(int rate)
        {
            return Mth.clamp((int) (rate * 1.50), rate + 1, Integer.MAX_VALUE);
        }
        public int modifyFireRate(int rate)
        {
            return Mth.clamp(rate, rate, Integer.MAX_VALUE);
        }*/
    };

    public static final IGunModifier LIGHT_RECOIL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.75F;
        }

        @Override
        public float kickModifier()
        {
            return 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 1.2F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.8F;
        }
    };

    public static final IGunModifier REDUCED_RECOIL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.65F;
        }

        @Override
        public float kickModifier()
        {
            return 0.65F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.5F;
        }
    };
}
