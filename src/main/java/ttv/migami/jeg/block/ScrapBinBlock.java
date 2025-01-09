package ttv.migami.jeg.block;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ScrapBinBlock extends Block implements WorldlyContainerHolder {
   public static final int READY = 8;
   public static final int MIN_LEVEL = 0;
   public static final int MAX_LEVEL = 7;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
   public static final Object2FloatMap<ItemLike> SCRAPABLE = new Object2FloatOpenHashMap<>();
   private static final int AABB_SIDE_THICKNESS = 2;
   private static final VoxelShape OUTER_SHAPE = Shapes.block();
   private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], (p_51967_) -> {
      for(int i = 0; i < 8; ++i) {
         p_51967_[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0D, (double)Math.max(2, 1 + i * 2), 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);
      }

      p_51967_[8] = p_51967_[7];
   });

   public static void bootStrap() {
      SCRAPABLE.defaultReturnValue(-1.0F);
      float f = 0.3F;
      float f1 = 0.5F;
      float f2 = 0.65F;
      float f3 = 0.85F;
      float f4 = 1.0F;
      add(0.1F, Items.IRON_NUGGET);
      add(0.2F, Items.IRON_BARS);
      add(0.2F, Items.CHAIN);
      add(0.3F, Items.IRON_SHOVEL);
      add(0.3F, Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
      add(0.4F, Items.BUCKET);
      add(0.4F, Items.IRON_HOE);
      add(0.4F, Items.IRON_SWORD);
      add(0.4F, Items.IRON_PICKAXE);
      add(0.4F, Items.IRON_AXE);
      add(0.5F, Items.IRON_TRAPDOOR);
      add(0.5F, Items.IRON_BOOTS);
      add(0.5F, Items.IRON_INGOT);
      add(0.6F, Items.IRON_HELMET);
      add(0.6F, Items.HOPPER);
      add(0.7F, Items.IRON_LEGGINGS);
      add(1.0F, Items.IRON_CHESTPLATE);
      add(1.0F, Items.IRON_HORSE_ARMOR);
      add(1.0F, Items.IRON_DOOR);
      add(1.0F, Items.IRON_BLOCK);
      add(1.0F, Items.MINECART);
   }

   private static void add(float pChance, ItemLike pItem) {
      SCRAPABLE.put(pItem.asItem(), pChance);
   }

   public ScrapBinBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   public static void handleFill(Level pLevel, BlockPos pPos, boolean pSuccess) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      pLevel.playLocalSound(pPos, pSuccess ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      double d0 = blockstate.getShape(pLevel, pPos).max(Direction.Axis.Y, 0.5D, 0.5D) + 0.03125D;
      double d1 = (double)0.13125F;
      double d2 = (double)0.7375F;
      RandomSource randomsource = pLevel.getRandom();

      for(int i = 0; i < 10; ++i) {
         double d3 = randomsource.nextGaussian() * 0.02D;
         double d4 = randomsource.nextGaussian() * 0.02D;
         double d5 = randomsource.nextGaussian() * 0.02D;
         pLevel.addParticle(ModParticleTypes.SCRAP.get(), (double)pPos.getX() + (double)0.13125F + (double)0.7375F * (double)randomsource.nextFloat(), (double)pPos.getY() + d0 + (double)randomsource.nextFloat() * (1.0D - d0), (double)pPos.getZ() + (double)0.13125F + (double)0.7375F * (double)randomsource.nextFloat(), d3, d4, d5);
      }

   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPES[pState.getValue(LEVEL)];
   }

   public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return OUTER_SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPES[0];
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (pState.getValue(LEVEL) == 7) {
         pLevel.scheduleTick(pPos, pState.getBlock(), 20);
      }

   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      int i = pState.getValue(LEVEL);
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (i < 8 && SCRAPABLE.containsKey(itemstack.getItem())) {
         if (i < 7 && !pLevel.isClientSide) {
            BlockState blockstate = addItem(pPlayer, pState, pLevel, pPos, itemstack);
            pLevel.levelEvent(1500, pPos, pState != blockstate ? 1 : 0);
            pPlayer.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }
         }

         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else if (i == 8) {
         extractProduce(pPlayer, pState, pLevel, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static BlockState insertItem(Entity pEntity, BlockState pState, ServerLevel pLevel, ItemStack pStack, BlockPos pPos) {
      int i = pState.getValue(LEVEL);
      if (i < 7 && SCRAPABLE.containsKey(pStack.getItem())) {
         BlockState blockstate = addItem(pEntity, pState, pLevel, pPos, pStack);
         pStack.shrink(1);
         return blockstate;
      } else {
         return pState;
      }
   }

   public static BlockState extractProduce(Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide) {
         Vec3 vec3 = Vec3.atLowerCornerWithOffset(pPos, 0.5D, 1.01D, 0.5D).offsetRandom(pLevel.random, 0.7F);

         for (int i = 0; i < 8; i++) {
            ItemEntity itementity = new ItemEntity(pLevel, vec3.x(), vec3.y(), vec3.z(), new ItemStack(ModItems.SCRAP.get()));
            pLevel.addFreshEntity(itementity);
         }

         int random = new Random().nextInt(1) + 2; //Min 1, Max 2.
         for (int i = 0; i < random; i++) {
            ItemEntity itementity = new ItemEntity(pLevel, vec3.x(), vec3.y(), vec3.z(), new ItemStack(ModItems.TECH_TRASH.get()));
            pLevel.addFreshEntity(itementity);
         }
      }

      BlockState blockstate = empty(pEntity, pState, pLevel, pPos);
      pLevel.playSound((Player)null, pPos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
      return blockstate;
   }

   static BlockState empty(@Nullable Entity pEntity, BlockState pState, LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(0));
      pLevel.setBlock(pPos, blockstate, 3);
      pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, blockstate));
      return blockstate;
   }

   static BlockState addItem(@Nullable Entity pEntity, BlockState pState, LevelAccessor pLevel, BlockPos pPos, ItemStack pStack) {
      int i = pState.getValue(LEVEL);
      float f = SCRAPABLE.getFloat(pStack.getItem());
      if ((i != 0 || !(f > 0.0F)) && !(pLevel.getRandom().nextDouble() < (double)f)) {
         return pState;
      } else {
         int j = i + 1;
         BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(j));
         pLevel.setBlock(pPos, blockstate, 3);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, blockstate));
         if (j == 7) {
            pLevel.scheduleTick(pPos, pState.getBlock(), 20);
         }

         return blockstate;
      }
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(LEVEL) == 7) {
         pLevel.setBlock(pPos, pState.cycle(LEVEL), 3);
         pLevel.playSound((Player) null, pPos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return pBlockState.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public WorldlyContainer getContainer(BlockState pState, LevelAccessor pLevel, BlockPos pPos) {
      int i = pState.getValue(LEVEL);
      if (i == 8) {
         return new ScrapBinBlock.OutputContainer(pState, pLevel, pPos, new ItemStack(Items.BONE_MEAL));
      } else {
         return (WorldlyContainer)(i < 7 ? new ScrapBinBlock.InputContainer(pState, pLevel, pPos) : new ScrapBinBlock.EmptyContainer());
      }
   }

   static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
      public EmptyContainer() {
         super(0);
      }

      public int[] getSlotsForFace(Direction pSide) {
         return new int[0];
      }

      /**
       * Returns {@code true} if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return false;
      }

      /**
       * Returns {@code true} if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return false;
      }
   }

   static class InputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public InputContainer(BlockState pState, LevelAccessor pLevel, BlockPos pPos) {
         super(1);
         this.state = pState;
         this.level = pLevel;
         this.pos = pPos;
      }

      /**
       * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction pSide) {
         return pSide == Direction.UP ? new int[]{0} : new int[0];
      }

      /**
       * Returns {@code true} if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return !this.changed && pDirection == Direction.UP && ScrapBinBlock.SCRAPABLE.containsKey(pItemStack.getItem());
      }

      /**
       * Returns {@code true} if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return false;
      }

      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         ItemStack itemstack = this.getItem(0);
         if (!itemstack.isEmpty()) {
            this.changed = true;
            BlockState blockstate = ScrapBinBlock.addItem((Entity)null, this.state, this.level, this.pos, itemstack);
            this.level.levelEvent(1500, this.pos, blockstate != this.state ? 1 : 0);
            this.removeItemNoUpdate(0);
         }

      }
   }

   static class OutputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public OutputContainer(BlockState pState, LevelAccessor pLevel, BlockPos pPos, ItemStack pStack) {
         super(pStack);
         this.state = pState;
         this.level = pLevel;
         this.pos = pPos;
      }

      /**
       * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction pSide) {
         return pSide == Direction.DOWN ? new int[]{0} : new int[0];
      }

      /**
       * Returns {@code true} if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return false;
      }

      /**
       * Returns {@code true} if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return !this.changed && pDirection == Direction.DOWN && pStack.is(Items.BONE_MEAL);
      }

      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         ScrapBinBlock.empty((Entity)null, this.state, this.level, this.pos);
         this.changed = true;
      }
   }

   public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      pTooltip.add(Component.translatable("info.jeg.tooltip_block_" + this.asItem()).withStyle(ChatFormatting.GRAY));
   }
}