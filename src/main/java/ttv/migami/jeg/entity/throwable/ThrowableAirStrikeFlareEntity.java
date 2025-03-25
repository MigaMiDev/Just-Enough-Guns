package ttv.migami.jeg.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;

@Mod.EventBusSubscriber
public class ThrowableAirStrikeFlareEntity extends ThrowableGrenadeEntity
{
    private Player player;
    private boolean hasSummoned;
    private Vec3 lookAngle;

    private static final EntityDataAccessor<Vector3f> ANGLE = SynchedEntityData.defineId(ThrowableAirStrikeFlareEntity.class, EntityDataSerializers.VECTOR3);

    public ThrowableAirStrikeFlareEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
        this.setMaxLife(200);
    }

    public ThrowableAirStrikeFlareEntity(Level world, LivingEntity player, Vec3 lookAngle)
    {
        super(ModEntities.THROWABLE_TERROR_PHANTOM_FLARE.get(), world, player);
        this.setItem(new ItemStack(ModItems.AIR_STRIKE_FLARE.get()));
        this.setMaxLife(200);
        if (player instanceof Player) {
            this.player = (Player) player;
        }
        this.hasSummoned = false;
        this.lookAngle = lookAngle;
    }

    @Override
    public void particleTick() {
        if (!this.level().isClientSide && this.tickCount > 100 && !this.hasSummoned) {
            Vec3 pos = this.position();
            this.lookAngle = new Vec3(this.getAngle().x(), this.getAngle().y(), this.getAngle().z());
            Vec3 lookDirection = this.lookAngle.normalize();

            Vec3 horizontalLook = new Vec3(lookDirection.x, 0, lookDirection.z).normalize();
            Vec3 spawnPos = pos.subtract(horizontalLook.scale(75)).add(0, 32, 0);

            TerrorPhantom gunner = new TerrorPhantom(ModEntities.TERROR_PHANMTOM.get(), player.level());
            gunner.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0, 0);
            this.level().addFreshEntity(gunner);
            gunner.addTag("PlayerOwned");
            gunner.setPlayer(this.player);
            gunner.lookAt(this, 255, 255);

            this.hasSummoned = true;
            this.noPhysics = true;
        }
        if (this.level().isClientSide && this.tickCount > 20) {
            Vec3 pos = this.position();
            this.lookAngle = new Vec3(this.getAngle().x(), this.getAngle().y(), this.getAngle().z());
            Vec3 lookDirection = this.lookAngle.normalize();

            Vec3 rightOffset = lookDirection.cross(new Vec3(0, 1, 0)).normalize().scale(8);

            Vec3 startPos = pos.add(lookDirection.scale(10));
            Vec3 endPos = pos.add(lookDirection.scale(40));

            int numSteps = 50;
            double fixedY = pos.y + 0.2;

            for (int i = 0; i <= numSteps; i++) {
                double factor = i / (double) numSteps;
                Vec3 horizontalLook = new Vec3(lookDirection.x, 0, lookDirection.z).normalize();
                Vec3 stepPos = startPos.add(horizontalLook.scale(factor * 90));

                Vec3 line1 = new Vec3(stepPos.x, fixedY, stepPos.z).add(rightOffset);
                Vec3 line2 = new Vec3(stepPos.x, fixedY, stepPos.z).subtract(rightOffset);
                Vec3 centerPos = new Vec3(stepPos.x, fixedY, stepPos.z);

                this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, line1.x, fixedY, line1.z, 0, 0, 0);
                this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, line2.x, fixedY, line2.z, 0, 0, 0);

                if (i % 2 == 0) {
                    for (double j = 0; j <= 1; j += 0.2) {
                        Vec3 midPoint = centerPos.add(horizontalLook.scale(j));
                        Vec3 midLeft = midPoint.add(rightOffset.scale(-0.5));
                        Vec3 midRight = midPoint.add(rightOffset.scale(0.5));

                        this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, midPoint.x, fixedY, midPoint.z, 0, 0, 0);
                        this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, midLeft.x, fixedY, midLeft.z, 0, 0, 0);
                        this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, midRight.x, fixedY, midRight.z, 0, 0, 0);

                        Vec3 outerLeft = midPoint.add(rightOffset.scale(-1.05));
                        Vec3 outerRight = midPoint.add(rightOffset.scale(1.05));

                        //this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, outerLeft.x, fixedY, outerLeft.z, 0, 0, 0);
                        //this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(), true, outerRight.x, fixedY, outerRight.z, 0, 0, 0);
                    }
                }
            }
        }
        if(this.level().isClientSide && this.tickCount > 20) {
            for(int i = 0; i < 2; i++) {
                this.level().addParticle(ModParticleTypes.BLUE_FLARE.get(), true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
                this.level().addParticle(ParticleTypes.LAVA, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
                this.level().addParticle(ModParticleTypes.FIRE.get(), true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
            }
        }
    }

    @Override
    public void onDeath()
    {
    }

    public Vector3f getAngle() {
        return this.entityData.get(ANGLE);
    }

    public void setAngle(Vector3f angle) {
        this.entityData.set(ANGLE, angle);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(ANGLE, Vec3.ZERO.toVector3f());
    }
}