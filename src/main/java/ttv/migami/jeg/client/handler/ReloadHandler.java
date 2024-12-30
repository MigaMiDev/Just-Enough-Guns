package ttv.migami.jeg.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.event.GunReloadEvent;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageLeftOverAmmo;
import ttv.migami.jeg.network.message.C2SMessageReload;
import ttv.migami.jeg.network.message.C2SMessageUnload;
import ttv.migami.jeg.util.GunModifierHelper;

/**
 * Author: MrCrayfish
 */
public class ReloadHandler
{
    private static ReloadHandler instance;

    public static ReloadHandler get()
    {
        if(instance == null)
        {
            instance = new ReloadHandler();
        }
        return instance;
    }

    private int startReloadTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private int reloadingSlot;

    private ReloadHandler()
    {
    }

    public static void stopReloadAnimation() {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() instanceof AnimatedGunItem gunItem) {
            final long id = GeoItem.getId(player.getMainHandItem());
            AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
            animationController.tryTriggerAnimation("reload_stop");
            animationController.forceAnimationReset();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.prevReloadTimer = this.reloadTimer;

        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            ItemStack stack = player.getMainHandItem();
            CompoundTag tag = stack.getTag();

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageLeftOverAmmo());

            if(ModSyncedDataKeys.RELOADING.getValue(player))
            {
                if(this.reloadingSlot != player.getInventory().selected)
                {
                    this.setReloading(false);
                }
            }

            this.updateReloadTimer(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event)
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        ItemStack stack = player.getMainHandItem();
        CompoundTag tag = stack.getTag();

        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            // if (!gun.getReloads().getReloadType().equals(ReloadType.INVENTORY_FED)) {
                if(KeyBinds.KEY_RELOAD.isDown() && event.getAction() == GLFW.GLFW_PRESS)
                {
                    //this.setReloading(!ModSyncedDataKeys.RELOADING.getValue(player));
                    this.setReloading(true);
                }
                if(KeyBinds.KEY_UNLOAD.consumeClick() && event.getAction() == GLFW.GLFW_PRESS)
                {
                    this.setReloading(false);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
                }
            //}
        }
    }

    public void setReloading(boolean reloading)
    {
        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            ItemStack stack = player.getMainHandItem();
            CompoundTag tag = stack.getTag();
            if(reloading)
            {
                if(stack.getItem() instanceof GunItem)
                {
                    if(tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
                    {
                        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                        if(tag.getInt("AmmoCount") >= GunModifierHelper.getModifiedAmmoCapacity(stack, gun))
                            return;
                        if(MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)))
                            return;
                        ModSyncedDataKeys.RELOADING.setValue(player, true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                        this.reloadingSlot = player.getInventory().selected;
                        MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                    }
                }
            }
            else
            {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));

                this.reloadingSlot = -1;
            }
        }
    }

    /*
    private void updateReloadTimer(Player player)
    {
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
            ItemStack stack = player.getInventory().getSelected();
            CompoundTag tag = stack.getTag();
            Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            if(gun.getReloads().isMagFed())
            {
                if (tag.getInt("AmmoCount") <= 0) {
                    if (this.reloadTimer < gun.getReloads().getReloadTimer() + gun.getReloads().getEmptyMagTimer()) {
                        this.reloadTimer++;
                    }
                } else {
                    if (this.reloadTimer < gun.getReloads().getReloadTimer()) {
                        this.reloadTimer++;
                    }
                }
            }
            else if(this.startReloadTick == -1)
            {
                this.startReloadTick = player.tickCount + 5;
            }
            if(this.reloadTimer < 5)
            {
                this.reloadTimer++;
            }
        }
        else
        {
            if(this.startReloadTick != -1)
            {
                this.startReloadTick = -1;
            }
            if(this.reloadTimer > 0) {
                this.reloadTimer--;
            }
        }
    }
    */

    private void updateReloadTimer(Player player)
    {
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
            if(this.startReloadTick == -1)
            {
                this.startReloadTick = player.tickCount + 5;
            }
            if(this.reloadTimer < 5)
            {
                this.reloadTimer++;
            }
        }
        else
        {
            if(this.startReloadTick != -1)
            {
                this.startReloadTick = -1;
            }
            if(this.reloadTimer > 0)
            {
                this.reloadTimer--;
            }
        }
    }

    public int getStartReloadTick()
    {
        return this.startReloadTick;
    }

    public int getReloadTimer()
    {
        return this.reloadTimer;
    }

    public float getReloadProgress(float partialTicks)
    {
        return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5F;
    }
}
