package ttv.migami.jeg.common.container;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModContainers;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.item.AdvancedBlueprintItem;
import ttv.migami.jeg.item.BlueprintItem;
import ttv.migami.jeg.item.GunItem;

public class SchematicStationMenu extends AbstractContainerMenu {
   public static final int MAP_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final ContainerLevelAccess access;
   long lastSoundTime;
   public final Container container = new SimpleContainer(2) {
      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         SchematicStationMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };
   private final ResultContainer resultContainer = new ResultContainer() {
      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         SchematicStationMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };

   public SchematicStationMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public SchematicStationMenu(int pContainerId, Inventory pPlayerInventory, final ContainerLevelAccess pAccess) {
      super(ModContainers.SCHEMATIC_STATION.get(), pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.container, 0, 15, 52) {
         /**
          * Item to get the copy from
          */
         public boolean mayPlace(ItemStack stack) {
            return (stack.getItem() instanceof BlueprintItem && !(stack.getItem() instanceof AdvancedBlueprintItem)) || (stack.getItem() instanceof GunItem gunItem && gunItem.getGun().getGeneral().canBeBlueprinted());
         }
      });
      this.addSlot(new Slot(this.container, 1, 15, 15) {
         /**
          * Blueprint to write to.
          */
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof BlueprintItem && !(stack.getItem() instanceof AdvancedBlueprintItem);
         }
      });
      this.addSlot(new Slot(this.resultContainer, 2, 145, 35) {
         /**
          * Result slot.
          */
         public boolean mayPlace(ItemStack pStack) {
            return false;
         }

         public void onTake(Player player, ItemStack stack) {
            //SchematicStationMenu.this.slots.get(0).remove(1);
            SchematicStationMenu.this.slots.get(1).remove(1);
            stack.getItem().onCraftedBy(stack, player.level(), player);
            pAccess.execute((level, blockPos) -> {
               long l = level.getGameTime();
               if (SchematicStationMenu.this.lastSoundTime != l) {
                  level.playSound((Player)null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  SchematicStationMenu.this.lastSoundTime = l;
               }

            });
            super.onTake(player, stack);
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, ModBlocks.SCHEMATIC_STATION.get());
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(Container pInventory) {
      ItemStack blueprintFrom = this.container.getItem(0);
      ItemStack blueprintTo = this.container.getItem(1);
      ItemStack result = this.resultContainer.getItem(2);
      if (result.isEmpty() || !blueprintFrom.isEmpty() && !blueprintTo.isEmpty()) {
         if (!blueprintFrom.isEmpty() && !blueprintTo.isEmpty()) {
            this.setupResultSlot(blueprintFrom, blueprintTo, result);
         }
      } else {
         this.resultContainer.removeItemNoUpdate(2);
      }

   }

   private void setupResultSlot(ItemStack blueprintFrom, ItemStack blueprintTo, ItemStack result) {
      this.access.execute((level, blockPos) -> {
         if (blueprintTo != null) {
            ItemStack itemstack;
            if (blueprintFrom.getItem() instanceof GunItem) {
               ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(blueprintFrom.getItem());

               itemstack = blueprintTo.copyWithCount(1);
               itemstack.getOrCreateTag().putString("Namespace", registryName.getNamespace());
               itemstack.getOrCreateTag().putString("Path", registryName.getPath());
               this.broadcastChanges();

               if (!ItemStack.matches(itemstack, result)) {
                  this.resultContainer.setItem(1, itemstack);
                  this.broadcastChanges();
               }
            } else if (blueprintFrom.getItem() instanceof BlueprintItem && !(blueprintFrom.getItem() instanceof AdvancedBlueprintItem)) {
               itemstack = blueprintTo.copyWithCount(1);
               itemstack.getOrCreateTag().putString("Namespace", blueprintFrom.getTag().getString("Namespace"));
               itemstack.getOrCreateTag().putString("Path", blueprintFrom.getTag().getString("Path"));
               this.broadcastChanges();

               if (!ItemStack.matches(itemstack, result)) {
                  this.resultContainer.setItem(2, itemstack);
                  this.broadcastChanges();
               }
            }

         }
      });
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 2) {
            itemstack1.getItem().onCraftedBy(itemstack1, pPlayer.level(), pPlayer);
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != 1 && pIndex != 0) {
            if ((itemstack1.is(ModItems.FIREARM_BLUEPRINT.get()) && itemstack1.hasTag()) || (itemstack1.getItem() instanceof GunItem)) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!itemstack1.is(ModItems.FIREARM_BLUEPRINT.get()) && !(itemstack1.getItem() instanceof GunItem)) {
               if (pIndex >= 3 && pIndex < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(2);
      this.access.execute((level, blockPos) -> {
         this.clearContainer(pPlayer, this.container);
      });
   }
}