package ttv.migami.jeg.common;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.S2CMessageGunSound;
import ttv.migami.jeg.network.message.S2CMessageStopReloadAnimation;
import ttv.migami.jeg.network.message.S2CMessageSyncReloadKey;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ReloadTracker
{
    private static final Map<Player, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final int slot;
    private final ItemStack stack;
    private final Gun gun;

    private ReloadTracker(Player player)
    {
        this.startTick = player.tickCount;
        this.slot = player.getInventory().selected;
        this.stack = player.getInventory().getSelected();
        this.gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
    }

    /**
     * Tests if the current item the player is holding is the same as the one being reloaded
     *
     * @param player the player to check
     * @return True if it's the same weapon and slot
     */
    private boolean isSameWeapon(Player player)
    {
        return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected() == this.stack;
    }

    /**
     * @return
     */
    private boolean isWeaponFull()
    {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") >= GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
    }

    private boolean isWeaponEmpty()
    {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt("AmmoCount") == 0;
    }

    private boolean hasNoAmmo(Player player)
    {
        if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
            return Gun.findAmmo(player, this.gun.getReloads().getReloadItem()).stack().isEmpty();
        }
        return Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
    }

    private boolean canReload(Player player)
    {
        GunItem gunItem = (GunItem) stack.getItem();

        if(!(gunItem instanceof AnimatedGunItem)) {
            if(gun.getReloads().getReloadType() == ReloadType.MAG_FED ||
                    gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM)
            {
                // Extra penalization if the gun is empty
                if(this.isWeaponEmpty())
                {
                    int deltaTicks = player.tickCount - this.startTick;
                    int interval = gun.getReloads().getReloadTimer() + gun.getReloads().getEmptyMagTimer();
                    return deltaTicks > interval;
                }
                else
                {
                    int deltaTicks = player.tickCount - this.startTick;
                    int interval = gun.getReloads().getReloadTimer();
                    return deltaTicks > interval;
                }
            }
            else
            {
                int deltaTicks = player.tickCount - this.startTick;
                int interval = GunEnchantmentHelper.getReloadInterval(this.stack);
                return deltaTicks > 0 && deltaTicks % interval == 0;
            }
        }

        return false;
    }

    public static int ammoInInventory(ItemStack[] ammoStack)
    {
        int result = 0;
        for (ItemStack x: ammoStack)
            result+=x.getCount();
        return result;
    }

    private void shrinkFromAmmoPool(ItemStack[] ammoStack, Player player, int shrinkAmount)
    {
        // Cancels the event if the gun is enchanted with Infinity
        if (player.getMainHandItem().getEnchantmentLevel(ModEnchantments.INFINITY.get()) != 0) {
            return;
        }

        int shrinkAmt = shrinkAmount;
        ArrayList<ItemStack> stacks = new ArrayList<>();

        for (ItemStack x: ammoStack)
        {
            if(!x.isEmpty())
            {
                int max = Math.min(shrinkAmt, x.getCount());
                x.shrink(max);
                shrinkAmt-=max;
            }
            if(shrinkAmt==0)
                return;
        }
    }

    private void increaseMagAmmo(Player player)
    {
        ItemStack[] ammoStack = Gun.findAmmoStack(player, this.gun.getProjectile().getItem());
        if(ammoStack.length > 0)
        {
            CompoundTag tag = this.stack.getTag();
            int ammoAmount = Math.min(ammoInInventory(ammoStack), GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun));
            int currentAmmo = tag.getInt("AmmoCount");
            int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
            int amount = maxAmmo - currentAmmo;
            if(tag != null)
            {
                if (ammoAmount < amount) {
                    tag.putInt("AmmoCount", currentAmmo + ammoAmount);
                    this.shrinkFromAmmoPool(ammoStack, player, ammoAmount);
                } else {
                    tag.putInt("AmmoCount", maxAmmo);
                    this.shrinkFromAmmoPool(ammoStack, player, amount);
                }
            }
        }

        ResourceLocation reloadSound = this.gun.getSounds().getReload();
        if(reloadSound != null && !(player.getMainHandItem().getItem() instanceof AnimatedGunItem))
        {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), soundX, soundY, soundZ, radius), message);
        }
    }

    private void reloadItem(Player player) {
        AmmoContext context = Gun.findAmmo(player, this.gun.getReloads().getReloadItem());
        ItemStack ammo = context.stack();
        if (!ammo.isEmpty()) {
            CompoundTag tag = this.stack.getTag();
            if (tag != null) {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                tag.putInt("AmmoCount", maxAmmo);
                ammo.shrink(1);
            }

            // Trigger that the container changed
            Container container = context.container();
            if (container != null) {
                container.setChanged();
            }
        }

        Item waterBucket = Items.WATER_BUCKET;
        Item lavaBucket = Items.LAVA_BUCKET;
        ResourceLocation waterBucketLocation = ForgeRegistries.ITEMS.getKey(waterBucket);
        ResourceLocation lavaBucketLocation = ForgeRegistries.ITEMS.getKey(lavaBucket);
        if (this.gun.getReloads().getReloadItem().equals(waterBucketLocation) ||
                this.gun.getReloads().getReloadItem().equals(lavaBucketLocation)) {
            Item bucket = Items.BUCKET;
            ResourceLocation bucketLocation = ForgeRegistries.ITEMS.getKey(bucket);
            Item item = ForgeRegistries.ITEMS.getValue(bucketLocation);
            if (item != null && this.stack.getEnchantmentLevel(ModEnchantments.INFINITY.get()) == 0) {
                ItemStack itemStack = new ItemStack(item);
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), itemStack.copy()));
            }
        }

        ResourceLocation reloadSound = this.gun.getSounds().getReload();
        if(reloadSound != null && !(player.getMainHandItem().getItem() instanceof AnimatedGunItem))
        {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), soundX, soundY, soundZ, radius), message);
        }
    }

    public static void inventoryFeed(Player player, Gun gun) {
        AmmoContext context = Gun.findAmmo(player, gun.getProjectile().getItem());
        ItemStack ammo = context.stack();
        CompoundTag tag = player.getMainHandItem().getTag();
        if(tag != null)
        {
            if(!ammo.isEmpty())
            {
                tag.putInt("AmmoCount", tag.getInt("AmmoCount") + 1);
                ammo.shrink(1);
            }
        }
    }

    private void increaseAmmo(Player player)
    {
        AmmoContext context = Gun.findAmmo(player, this.gun.getProjectile().getItem());
        ItemStack ammo = context.stack();
        if(!ammo.isEmpty())
        {
            int amount = Math.min(ammo.getCount(), this.gun.getReloads().getReloadAmount());
            CompoundTag tag = this.stack.getTag();
            if(tag != null)
            {
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(this.stack, this.gun);
                amount = Math.min(amount, maxAmmo - tag.getInt("AmmoCount"));
                tag.putInt("AmmoCount", tag.getInt("AmmoCount") + amount);

                if (ammo.getTag() != null) {
                    if (ammo.getTag().getBoolean("HasRaid")) {
                        if (ammo.getTag().contains("Raid")) {
                            tag.putString("Raid", ammo.getTag().getString("Raid"));
                        }

                        player.displayClientMessage(Component.translatable("chat.jeg.raid_flare_loaded").withStyle(ChatFormatting.RED), true);
                        tag.putBoolean("HasRaid", true);
                    }
                }
            }
            ammo.shrink(amount);

            // Trigger that the container changed
            Container container = context.container();
            if(container != null)
            {
                container.setChanged();
            }
        }

        ResourceLocation reloadSound = this.gun.getSounds().getReload();
        if(reloadSound != null && !(player.getMainHandItem().getItem() instanceof AnimatedGunItem))
        {
            double radius = Config.SERVER.reloadMaxDistance.get();
            double soundX = player.getX();
            double soundY = player.getY() + 1.0;
            double soundZ = player.getZ();
            S2CMessageGunSound message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), soundX, soundY, soundZ, radius), message);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && !event.player.level().isClientSide)
        {
            Player player = event.player;
            CompoundTag tag = player.getMainHandItem().getTag();

            if(ModSyncedDataKeys.RELOADING.getValue(player))
            {
                if(!RELOAD_TRACKER_MAP.containsKey(player))
                {
                    if(!(player.getInventory().getSelected().getItem() instanceof GunItem))
                    {
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        if (player.getInventory().getSelected().getTag() != null) {
                            player.getInventory().getSelected().getTag().putBoolean("IsFinishingReloading", false);
                            player.getInventory().getSelected().getTag().putBoolean("IsReloading", false);
                        }
                        return;
                    }
                    RELOAD_TRACKER_MAP.put(player, new ReloadTracker(player));
                }
                ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
                if(!tracker.isSameWeapon(player) || tracker.isWeaponFull() || tracker.hasNoAmmo(player))
                {
                    RELOAD_TRACKER_MAP.remove(player);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSyncReloadKey());
                    if (player.getInventory().getSelected().getTag() != null) {
                        if (player.getInventory().getSelected().getItem() instanceof AnimatedGunItem) {
                            player.getInventory().getSelected().getTag().putBoolean("IsReloading", false);
                        }
                    }
                    return;
                }
                if(tracker.canReload(player))
                {
                    final Player finalPlayer = player;
                    final Gun gun = tracker.gun;
                    if (!(player.getInventory().getSelected().getItem() instanceof AnimatedGunItem)) {
                        if(gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                            tracker.increaseMagAmmo(player);
                        }
                        else if(gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                            tracker.reloadItem(player);
                        }
                        else if(gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                            tracker.increaseAmmo(player);
                        }
                    }

                    if(tracker.isWeaponFull() || tracker.hasNoAmmo(player))
                    {
                        RELOAD_TRACKER_MAP.remove(player);
                        PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSyncReloadKey());
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        if (player.getInventory().getSelected().getTag() != null) {
                            if (player.getInventory().getSelected().getItem() instanceof AnimatedGunItem) {
                                player.getInventory().getSelected().getTag().putBoolean("IsReloading", false);
                            }
                        }

                        DelayedTask.runAfter(4, () ->
                        {
                            ResourceLocation cockSound = gun.getSounds().getCock();
                            if(cockSound != null && finalPlayer.isAlive() && !(finalPlayer.getMainHandItem().getItem() instanceof AnimatedGunItem))
                            {
                                double soundX = finalPlayer.getX();
                                double soundY = finalPlayer.getY() + 1.0;
                                double soundZ = finalPlayer.getZ();
                                double radius = Config.SERVER.reloadMaxDistance.get();
                                S2CMessageGunSound messageSound = new S2CMessageGunSound(cockSound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, finalPlayer.getId(), false, true);
                                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(finalPlayer.level(), soundX, soundY, soundZ, radius), messageSound);
                            }
                            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSyncReloadKey());
                        });
                    }
                }
            }
            else if(RELOAD_TRACKER_MAP.containsKey(player)) {
                RELOAD_TRACKER_MAP.remove(player);
            }
        }
    }

    public static void stopReloading(Player player) {
        if(player.getMainHandItem().getItem() instanceof AnimatedGunItem gunItem)
        {
            if (player.getMainHandItem().getTag() != null) {
                player.getInventory().getSelected().getTag().putBoolean("IsFinishingReloading", false);
                player.getMainHandItem().getTag().putBoolean("IsReloading", false);
            }
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSyncReloadKey());
        }
    }

    public static void mayStopReloading(Player player) {
        if(player.getMainHandItem().getItem() instanceof AnimatedGunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(player.getMainHandItem());
            if(player.getMainHandItem().getTag() != null && player.getMainHandItem().getTag().getInt("AmmoCount") == GunModifierHelper.getModifiedAmmoCapacity(player.getMainHandItem(), gun) || Gun.findAmmo(player, gun.getProjectile().getItem()).stack().isEmpty())
            {
                if(gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageStopReloadAnimation());
                }
            }
        }
    }

    public static void loaded(Player player) {
        ReloadTracker tracker = RELOAD_TRACKER_MAP.get(player);
        final Gun gun = tracker.gun;
        if(gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
            tracker.increaseMagAmmo(player);
        }
        else if(gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
            tracker.reloadItem(player);
        }
        else if(gun.getReloads().getReloadType() == ReloadType.MANUAL) {
            tracker.increaseAmmo(player);
        }
        else if(gun.getReloads().getReloadType() == ReloadType.INVENTORY_FED) {
            tracker.increaseAmmo(player);
        }
        if (tracker.isWeaponFull() || tracker.hasNoAmmo(player)) {
            if (player.getInventory().getSelected().getTag() != null) {
                player.getInventory().getSelected().getTag().putBoolean("IsFinishingReloading", true);
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSyncReloadKey());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null)
        {
            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
