package ttv.migami.jeg.world.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import ttv.migami.jeg.Reference;

import java.util.function.Supplier;

public class AtlanticShooterOnShipwreck extends LootModifier {
    public static final Supplier<Codec<AtlanticShooterOnShipwreck>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.create(inst -> codecStart(inst).and(ForgeRegistries.ITEMS.getCodec()
            .fieldOf("item").forGetter(m -> m.item)).apply(inst, AtlanticShooterOnShipwreck::new)));
    private final Item item;

    protected AtlanticShooterOnShipwreck(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() > 0.0F) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

            Enchantment atlanticShooter = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(Reference.MOD_ID, "atlantic_shooter"));
            if (atlanticShooter != null) {
                EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(atlanticShooter, 1));
            }

            generatedLoot.add(enchantedBook);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}