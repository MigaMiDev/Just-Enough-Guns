package ttv.migami.jeg.entity.ai.trumpet;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.TrumpetItem;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TrumpetSkeletonAI {
    private static final int SOUND_INTERVAL_TICKS = 120;
    private final Random random = new Random();

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Skeleton skeleton) {
            Level world = skeleton.level();
            ItemStack heldItem = skeleton.getMainHandItem();

            if (skeleton.getTags().contains("TrumpetBoi") && !(heldItem.getItem() instanceof TrumpetItem)) {
                ItemStack trumpet = new ItemStack(ModItems.TRUMPET.get());
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, trumpet);
                skeleton.setDropChance(EquipmentSlot.MAINHAND, 1F);
            }

            if (heldItem.getItem() instanceof TrumpetItem) {
                if (skeleton.tickCount < 2) {
                    reassessWeaponGoal(skeleton);
                }

                if (skeleton.tickCount % SOUND_INTERVAL_TICKS == 0 && skeleton.getTarget() == null) {
                    if (random.nextFloat() < 0.5) {
                        world.playSound(null, skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                                ModSounds.DOOT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Skeleton skeleton) {
            ItemStack heldItem = skeleton.getMainHandItem();
            if (heldItem.getItem() instanceof TrumpetItem) {
                reassessWeaponGoal(skeleton);
            }
        }
    }

    public static void reassessWeaponGoal(Skeleton skeleton) {
        if (!skeleton.level().isClientSide) {
            skeleton.goalSelector.removeGoal(skeleton.meleeGoal);
            skeleton.goalSelector.removeGoal(skeleton.bowGoal);

            ItemStack itemstack = skeleton.getItemInHand(ProjectileUtil.getWeaponHoldingHand(skeleton, (item) -> item instanceof BowItem || item instanceof TrumpetItem));

            if (itemstack.getItem() instanceof TrumpetItem) {
                skeleton.goalSelector.addGoal(2, new TrumpetRangedAttackGoal<>(skeleton, 1.0D, 60, 5.0F));
            } else if (itemstack.is(Items.BOW)) {
                int i = 20;
                if (skeleton.level().getDifficulty() != Difficulty.HARD) {
                    i = 40;
                }

                skeleton.bowGoal.setMinAttackInterval(i);
                skeleton.goalSelector.addGoal(4, skeleton.bowGoal);
            } else {
                skeleton.goalSelector.addGoal(4, skeleton.meleeGoal);
            }
        }
    }
}