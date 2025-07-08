package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.client.GunItemStackRenderer;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.NetworkGunManager;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.debug.Debug;
import ttv.migami.jeg.enchantment.EnchantmentTypes;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.modifier.Modifier;
import ttv.migami.jeg.modifier.type.IModifierEffect;
import ttv.migami.jeg.modifier.type.StatModifier;
import ttv.migami.jeg.modifier.type.StatType;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static ttv.migami.jeg.JustEnoughGuns.devilFruitsLoaded;

public class GunItem extends Item implements IColored, IMeta {
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();
    private Gun gun = new Gun();
    private Modifier modifier = null;

    public GunItem(Item.Properties properties) {
        super(properties);
    }

    public void setGun(NetworkGunManager.Supplier supplier) {
        this.gun = supplier.getGun();
    }

    public Gun getGun() {
        return this.gun;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    // Data-driven gun
    public static ItemStack makeGunStack(ResourceLocation gunId) {
        ItemStack stack = new ItemStack(ModItems.ABSTRACT_GUN.get());
        stack.getOrCreateTag().putString("GunId", gunId.toString());
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (this.modifier != null && Config.COMMON.gameplay.gunModifiers.get()) {
            pStack.setHoverName(pStack.getHoverName().copy().withStyle(style -> style.withColor(this.modifier.getColor()).withItalic(false)));

            /*if (pStack.getHoverName().getString().matches(Component.translatable("item." + this.getModID() + "." + this.toString()).getString())) {
                    pStack.setHoverName(Component.translatable("jeg.modifier." + modifierGroup.getName()).append(" ").append(pStack.getHoverName()));
            }*/
        }

        if (pStack.getTag() != null && !pStack.getTag().contains("AmmoCount")) {
            pStack.getTag().putInt("AmmoCount", 0);
        }
        if (pStack.is(ModItems.FLARE_GUN.get())) {
            if (pStack.hasTag() && pStack.getTag().getBoolean("HasRaid")) {
                pStack.setHoverName(Component.translatable("item.jeg.raid_flare_gun").withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)));
            } else if (pStack.getHoverName().equals(Component.translatable("item.jeg.raid_flare_gun").withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)))) {
                pStack.resetHoverName();
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tagCompound = stack.getTag();
        Gun modifiedGun = this.getModifiedGun(stack);

        if (stack.is(ModItems.FLARE_GUN.get())) {
            if (stack.hasTag() && stack.getTag().getBoolean("HasRaid")) {
                String factionName;
                if (tagCompound != null && tagCompound.contains("Raid")) {
                    factionName = tagCompound.getString("Raid");
                } else {
                    factionName = "random";
                }
                tooltip.add(Component.translatable("info.jeg.raid_flare_gun").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("info.jeg.raid_flare").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("info.jeg.flare_raid").withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("faction.jeg." + factionName).withStyle(ChatFormatting.WHITE)));
                tooltip.add(Component.literal(""));
            }
        }

        if (stack.getItem() != ModItems.FINGER_GUN.get()) {
            if (Screen.hasShiftDown()) {
                if (this.modifier != null && Config.COMMON.gameplay.gunModifiers.get()) {
                    tooltip.add(Component.translatable("info.jeg.modifier").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("modifier.jeg." + this.modifier.getName()).withStyle(style -> style.withColor(this.modifier.getColor()))));
                }

                String fireMode = modifiedGun.getGeneral().getFireMode().getId().toString();
                tooltip.add(Component.translatable("info.jeg.fire_mode").withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("fire_mode." + fireMode).withStyle(ChatFormatting.WHITE)));

                Item ammo = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());
                Item reloadItem = ForgeRegistries.ITEMS.getValue(modifiedGun.getReloads().getReloadItem());
                if (modifiedGun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                    ammo = reloadItem;
                }
                if (ammo != null) {
                    tooltip.add(Component.translatable("info.jeg.ammo_type", Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
                }

                String additionalDamageText = "";

                if (tagCompound != null) {
                    if (tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
                        float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                        additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                        if (additionalDamage > 0) {
                            additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                        } else if (additionalDamage < 0) {
                            additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                        }
                    }
                }

                float damage = modifiedGun.getProjectile().getDamage();
                if (this.modifier != null && Config.COMMON.gameplay.gunModifiers.get()) {
                    float fireRateMultiplier = 1.0F;

                    for (IModifierEffect effect : this.modifier.getModifiers()) {
                        if (effect instanceof StatModifier statModifier && statModifier.getStatType() == StatType.DAMAGE) {
                            fireRateMultiplier *= statModifier.getValue();
                        }
                    }

                    damage *= fireRateMultiplier;
                }

                ResourceLocation advantage = modifiedGun.getProjectile().getAdvantage();
                damage = GunModifierHelper.getModifiedProjectileDamage(stack, damage);
                damage = GunEnchantmentHelper.getAcceleratorDamage(stack, damage);
                damage = GunEnchantmentHelper.getWitheredDamage(stack, damage);
                if (modifiedGun.getProjectile().getItem().equals(new ResourceLocation(Items.EMERALD.toString()))){
                    tooltip.add(Component.translatable("info.jeg.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText)
                            .append(Component.literal(" - " + ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((damage * 4) * 0.8) + additionalDamageText)).withStyle(ChatFormatting.GRAY));
                } else tooltip.add(Component.translatable("info.jeg.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));

                if (modifiedGun.getProjectile().getItem().equals(new ResourceLocation(Items.EMERALD.toString()))) {
                    tooltip.add(Component.translatable("info.jeg.resonance").withStyle(ChatFormatting.GREEN));
                    tooltip.add(Component.translatable("info.jeg.resonance_info").withStyle(ChatFormatting.GREEN));
                }

                if (!advantage.equals(ModTags.Entities.NONE.location()) && Config.COMMON.gameplay.gunAdvantage.get()) {
                    tooltip.add(Component.translatable("info.jeg.advantage").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("advantage." + advantage).withStyle(ChatFormatting.GOLD)));
                }
            } else {
                tooltip.add(Component.translatable("info.jeg.shift_tooltip").withStyle(ChatFormatting.WHITE));
            }

            if (tagCompound != null) {
                if (tagCompound.getBoolean("IgnoreAmmo")) {
                    tooltip.add(Component.translatable("info.jeg.ignore_ammo").withStyle(ChatFormatting.AQUA));
                } else {
                    int ammoCount = tagCompound.getInt("AmmoCount");
                    tooltip.add(Component.translatable("info.jeg.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
                }
            }

            if (devilFruitsLoaded && KeyBinds.KEY_ATTACHMENTS.getKey() == ttv.migami.mdf.client.KeyBinds.KEY_Z_ACTION.getKey()) {
                tooltip.add(Component.translatable("info.jeg.attachment_help_mdf", KeyBinds.KEY_ATTACHMENTS.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.translatable("info.jeg.attachment_help", KeyBinds.KEY_ATTACHMENTS.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
            }

            if (this == ModItems.TYPHOONEE.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.typhoonee").withStyle(ChatFormatting.GRAY));
            } else if (this == ModItems.ATLANTEAN_SPEAR.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.atlantean_spear").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.SOULHUNTER_MK2.get() || this == ModItems.HOLLENFIRE_MK2.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.mk2_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.REPEATING_SHOTGUN.get() || this == ModItems.INFANTRY_RIFLE.get() || this == ModItems.SERVICE_RIFLE.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.ww_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.SUBSONIC_RIFLE.get() || this == ModItems.SUPERSONIC_SHOTGUN.get() || this == ModItems.HYPERSONIC_CANNON.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.warden_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.ROCKET_LAUNCHER.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.wither_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.COMPOUND_BOW.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.compound_bow_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.LIGHT_MACHINE_GUN.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.light_machine_gun_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.GRENADE_LAUNCHER.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.grenade_launcher_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.FLAMETHROWER.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.flamethrower_blueprint").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.MINIGUN.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.minigun").withStyle(ChatFormatting.GRAY));
            }
            else if (this == ModItems.PRIMITIVE_BOW.get()) {
                tooltip.add(Component.translatable("info.jeg.tooltip_item.primitive_bow").withStyle(ChatFormatting.GRAY));
            }

            if (modifiedGun.getGeneral().getFireTimer() != 0) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("info.jeg.hold_fire").withStyle(ChatFormatting.WHITE));
            }

            if (this == ModItems.SUBSONIC_RIFLE.get() || this == ModItems.HYPERSONIC_CANNON.get() ||
                    this == ModItems.SUPERSONIC_SHOTGUN.get()) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("info.jeg.echo_shard").withStyle(ChatFormatting.WHITE));
            }


            if (this.getEnchantmentLevel(stack, ModEnchantments.INFINITY.get()) != 0 || this.getEnchantmentLevel(stack, ModEnchantments.WITHERED.get()) != 0) {
                tooltip.add(Component.literal(""));

                if (this.getEnchantmentLevel(stack, ModEnchantments.WITHERED.get()) != 0 ) {
                    tooltip.add(Component.translatable("info.jeg.withered").withStyle(ChatFormatting.DARK_RED));
                }
                if (this.getEnchantmentLevel(stack, ModEnchantments.INFINITY.get()) != 0 ) {
                    tooltip.add(Component.translatable("info.jeg.infinity").withStyle(ChatFormatting.AQUA));
                }
            }
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        return gun.getGeneral().getRate() * 4;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        //CompoundTag tagCompound = stack.getOrCreateTag();
        //Gun modifiedGun = this.getModifiedGun(stack);
        //return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt("AmmoCount") != GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun);
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        Gun modifiedGun = this.getModifiedGun(stack);
        //return (int) (13.0 * (tagCompound.getInt("AmmoCount") / (double) GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun)));
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)this.getMaxDamage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (stack.getDamageValue() >= (stack.getMaxDamage() / 1.5)) {
            return Objects.requireNonNull(ChatFormatting.RED.getColor());
        }
        float stackMaxDamage = this.getMaxDamage(stack);
        float f = Math.max(0.0F, (stackMaxDamage - (float)stack.getDamageValue()) / stackMaxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
        //return Objects.requireNonNull(ChatFormatting.WHITE.getColor());
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            if (stack.getTag().contains("GunId")) {
                ResourceLocation gunID = new ResourceLocation(stack.getTag().getString("GunId"));
                return Component.translatable("item.jeg." + gunID.getPath());
            }
        }

        return super.getName(stack);
    }

    public Gun getModifiedGun(ItemStack stack) {
        // Data-driven gun
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("GunId", Tag.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(tagCompound.getString("GunId"));
            Gun data = NetworkGunManager.get() != null
                    ? NetworkGunManager.get().getRegisteredGuns().get(id)
                    : null;
            return data != null ? data : this.gun;
        }

        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    return Gun.create(tagCompound.getCompound("Gun"));
                } else {
                    Gun gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if (JustEnoughGuns.isDebugging()) {
            return Debug.getGun(this);
        }
        return this.gun;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
            Gun modifiedGun = this.getModifiedGun(stack);
            return (modifiedGun.getGeneral().getFireMode() != FireMode.AUTOMATIC);
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new GunItemStackRenderer();
            }
        });
    }

    // Disables the enchantment foil to allow a different kind of customization.
    // Foil only applies when the gun is blessed by Infinity
    public boolean isFoil(ItemStack stack) {
        return stack.getEnchantmentLevel(ModEnchantments.INFINITY.get()) != 0;
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.REPAIR_KIT.get());
    }

    public String getModID() {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(this);
        if (registryName != null)
            return registryName.getNamespace();
        else return null;
    }
}
