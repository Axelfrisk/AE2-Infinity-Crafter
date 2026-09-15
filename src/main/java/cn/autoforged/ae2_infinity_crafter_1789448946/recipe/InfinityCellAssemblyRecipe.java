package cn.autoforged.ae2_infinity_crafter_1789448946.recipe;

import appeng.api.stacks.AEKey;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModRecipes;
import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellChipItem;
import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * 无限元件组装配方：一枚“无限元件芯片” + 一个“无限元件外壳” → 一个配置好的无限元件。
 *
 * <p>用 {@link CustomRecipe}（特殊配方）而不是数据驱动的有序合成，是因为目标资源保存在
 * 芯片的 NBT 里：普通有序/无序合成无法把原料的 NBT 带到产物上。这里在 {@link #assemble}
 * 里读取芯片保存的 {@link AEKey}，再调用 {@link InfinityCellItem#createFor(AEKey)} 生成
 * 带正确配置的无限元件。
 */
public class InfinityCellAssemblyRecipe extends CustomRecipe {

    public InfinityCellAssemblyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack chip = ItemStack.EMPTY;
        ItemStack housing = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof InfinityCellChipItem) {
                if (!chip.isEmpty()) {
                    return false;
                }
                chip = stack;
            } else if (stack.is(ModItems.INFINITY_CELL_HOUSING.get())) {
                if (!housing.isEmpty()) {
                    return false;
                }
                housing = stack;
            } else {
                return false;
            }
        }
        return !chip.isEmpty() && !housing.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        AEKey key = findChipKey(container);
        if (key == null) {
            return ItemStack.EMPTY;
        }
        return InfinityCellItem.createFor(key);
    }

    @Nullable
    private static AEKey findChipKey(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof InfinityCellChipItem) {
                return InfinityCellChipItem.getKey(stack);
            }
        }
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INFINITY_CELL_ASSEMBLY.get();
    }
}
