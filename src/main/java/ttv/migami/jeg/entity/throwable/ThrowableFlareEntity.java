package ttv.migami.jeg.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.faction.Faction;
import ttv.migami.jeg.faction.GunnerManager;
import ttv.migami.jeg.init.ModCommands;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;

@Mod.EventBusSubscriber
public class ThrowableFlareEntity extends ThrowableGrenadeEntity
{
    private boolean hasRaid = false;
    private String raidName = null;

    public ThrowableFlareEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
        this.setMaxLife(this.hasRaid ? 3200 : 620);
    }

    public ThrowableFlareEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.FLARE.get()));
        this.setMaxLife(this.hasRaid ? 3200 : 620);
    }

    public ThrowableFlareEntity(Level world, LivingEntity player)
    {
        super(ModEntities.THROWABLE_FLARE.get(), world, player);
        this.setItem(new ItemStack(ModItems.FLARE.get()));
        this.setMaxLife(this.hasRaid ? 3200 : 620);
    }

    public ThrowableFlareEntity(Level world, LivingEntity player, boolean hasRaid)
    {
        super(ModEntities.THROWABLE_FLARE.get(), world, player);
        this.setItem(new ItemStack(ModItems.FLARE.get()));
        this.hasRaid = hasRaid;
        this.setMaxLife(3200);
    }

    public ThrowableFlareEntity(Level world, LivingEntity player, boolean hasRaid, String raid)
    {
        super(ModEntities.THROWABLE_FLARE.get(), world, player);
        this.setItem(new ItemStack(ModItems.FLARE.get()));
        this.hasRaid = hasRaid;
        this.raidName = raid;
        this.setMaxLife(3200);
    }

    @Override
    public void particleTick() {
        if (!this.level().isClientSide) {
            if (this.tickCount > 100 && this.hasRaid) {
                GunnerManager gunnerManager = GunnerManager.getInstance();
                Faction faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());
                if (this.raidName != null) {
                    faction = gunnerManager.getFactionByName(this.raidName);
                }
                ModCommands.startRaid((ServerLevel) this.level(), faction, this.position(), true);
                this.hasRaid = false;
            }
        }
        if(this.level().isClientSide && this.tickCount > 20) {
            for(int i = 0; i < 2; i++) {
                this.level().addParticle(ModParticleTypes.FLARE.get(), true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
                this.level().addParticle(ParticleTypes.LAVA, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
                this.level().addParticle(ModParticleTypes.FIRE.get(), true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
            }
        }
    }

    @Override
    public void onDeath()
    {
    }
}