package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;

import javax.annotation.Nullable;
import java.util.List;

public class LootDropItem extends Item {

    public LootDropItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.jeg.tooltip_item.treasure_bag").withStyle(ChatFormatting.GOLD));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ResourceLocation lootTableID;
            if (this == ModItems.AMMO_POUCH.get()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.AMMO_POUCH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                lootTableID = new ResourceLocation(Reference.MOD_ID, "loot_drop_items/ammo_pouch");
            } else if (this == ModItems.BADGE_PACK.get()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.BADGE_PACK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                lootTableID = new ResourceLocation(Reference.MOD_ID, "loot_drop_items/badge_pack");
            } else if (this == ModItems.SKIN_CRATE.get()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.GOOSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                lootTableID = new ResourceLocation(Reference.MOD_ID, "loot_drop_items/skin_crate");
            } else {
                lootTableID = new ResourceLocation(Reference.MOD_ID, "loot_drop_items/" + this.asItem());
            }

            ServerLevel serverLevel = (ServerLevel) level;
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableID);

            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.CHEST);

            // Roll the loot table 1 time
            for (int i = 0; i < 1; i++) {
                List<ItemStack> loot = lootTable.getRandomItems(lootParams);

                for (ItemStack itemStack : loot) {
                    if (!player.getInventory().add(itemStack)) {
                        player.drop(itemStack, false);
                    }
                }
            }

            player.getItemInHand(hand).shrink(1);
        }

        Vec3 playerPos = player.position();
        if (level.isClientSide && this == ModItems.BADGE_PACK.get() || this == ModItems.SKIN_CRATE.get()) {
            for (int i = 0; i < 64; i++) {
                level.addAlwaysVisibleParticle(ModParticleTypes.CONFETTI.get(), playerPos.x, playerPos.y + 1, playerPos.z, 0, 0, 0);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    public boolean isFoil(ItemStack stack) {
        return this == ModItems.SKIN_CRATE.get() || this == ModItems.BADGE_PACK.get();
    }
}