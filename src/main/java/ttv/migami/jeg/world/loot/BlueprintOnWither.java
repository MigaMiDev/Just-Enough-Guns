package ttv.migami.jeg.world.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import ttv.migami.jeg.Reference;

import java.util.function.Supplier;

public class BlueprintOnWither extends LootModifier {
    public static final Supplier<Codec<BlueprintOnWither>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.create(inst -> codecStart(inst).and(ForgeRegistries.ITEMS.getCodec()
            .fieldOf("item").forGetter(m -> m.item)).apply(inst, BlueprintOnWither::new)));
    private final Item item;

    protected BlueprintOnWither(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if(context.getRandom().nextFloat() > 0.0F) { // 100% chance of the item spawning in.

            // Array of possible item paths
            String[] itemPaths = {"explosive_charge", "rocket_launcher", "explosive_muzzle"};

            // Randomly select one of the items
            String selectedItemPath = itemPaths[context.getRandom().nextInt(itemPaths.length)];

            ItemStack itemStack = item.getDefaultInstance();
            itemStack.getOrCreateTag().putString("Namespace", Reference.MOD_ID);
            itemStack.getOrCreateTag().putString("Path", selectedItemPath);

            generatedLoot.add(itemStack);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
