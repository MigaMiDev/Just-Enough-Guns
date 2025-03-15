package ttv.migami.jeg.entity.throwable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;

@Mod.EventBusSubscriber
public class ThrowablePhantomGunnerBaitEntity extends ThrowableGrenadeEntity
{
    public ThrowablePhantomGunnerBaitEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
        this.setMaxLife(100);
    }

    public ThrowablePhantomGunnerBaitEntity(Level world, LivingEntity player)
    {
        super(ModEntities.THROWABLE_PHANTOM_GUNNER_BAIT.get(), world, player);
        this.setItem(new ItemStack(ModItems.PHANTOM_GUNNER_BAIT.get()));
        this.setMaxLife(100);
    }

    @Override
    public void particleTick() {
    }

    @Override
    public void onDeath()
    {
    }
}