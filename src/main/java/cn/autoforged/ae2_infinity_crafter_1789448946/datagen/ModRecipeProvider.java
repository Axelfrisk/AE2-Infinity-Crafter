package cn.autoforged.ae2_infinity_crafter_1789448946.datagen;

import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModRecipes;
import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellChipItem;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.function.Consumer;

/**
 * 本 mod 的合成配方：
 * <ul>
 *   <li>机器方块：下界之星 + 钻石块 + 石英块（终局内容）。</li>
 *   <li>提纯奇点：工作台 2x2 由 4 个“目标资源为奇点”的无限元件芯片合成，是无限元件外壳的材料。</li>
 *   <li>无限元件外壳：石英玻璃 / 钻石块 / 提纯奇点 / 下界合金块。</li>
 *   <li>无限元件：无限元件芯片 + 无限元件外壳 —— 特殊配方（读取芯片保存的资源）。</li>
 * </ul>
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INFINITY_CELL_CRAFTER.get())
                .pattern("DQD")
                .pattern("QNQ")
                .pattern("DQD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('Q', Items.QUARTZ_BLOCK)
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_nether_star",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHER_STAR))
                .save(consumer);

        // 提纯奇点：工作台 2x2，由 4 枚“目标资源 = 奇点（ae2:singularity）”的无限元件芯片合成。
        // 需求原文写作“无限奇点芯片”，本 mod 唯一的芯片物品是 infinity_cell_chip（无限元件芯片），
        // 故按此物品实现；如需独立的新物品，请另开任务新增注册。
        // 芯片的目标资源存在 NBT 的 ChipKey 里，普通物品 key 不会比较 NBT，会让任意目标类型的
        // 芯片都能合成提纯奇点；这里用 Forge 的 PartialNBTIngredient 限定必须携带
        // “目标为奇点”的那段 NBT（部分匹配，多余 NBT 不影响），从而只匹配目标：奇点的芯片。
        CompoundTag singularityChipTag = new CompoundTag();
        singularityChipTag.put(InfinityCellChipItem.KEY_TAG,
                AEItemKey.of(AEItems.SINGULARITY).toTagGeneric());
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PURIFIED_SINGULARITY.get())
                .pattern("CC")
                .pattern("CC")
                .define('C', PartialNBTIngredient.of(ModItems.INFINITY_CELL_CHIP.get(), singularityChipTag))
                .unlockedBy("has_infinity_cell_chip",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.INFINITY_CELL_CHIP.get()))
                .save(consumer);

        // 无限元件外壳：
        // 石英玻璃 钻石块 石英玻璃
        // 钻石块   提纯奇点 钻石块
        // 下界合金块 下界合金块 下界合金块
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INFINITY_CELL_HOUSING.get())
                .pattern("QDQ")
                .pattern("DSD")
                .pattern("NNN")
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .define('D', Items.DIAMOND_BLOCK)
                .define('S', ModItems.PURIFIED_SINGULARITY.get())
                .define('N', Items.NETHERITE_BLOCK)
                .unlockedBy("has_purified_singularity",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PURIFIED_SINGULARITY.get()))
                .save(consumer);

        // 无限元件：芯片 + 外壳。特殊配方会把芯片里保存的资源带到产物上。
        SpecialRecipeBuilder.special(ModRecipes.INFINITY_CELL_ASSEMBLY.get())
                .save(consumer, "infinity_cell");
    }
}
