package cn.autoforged.ae2_infinity_crafter_1789448946.datagen;

import cn.autoforged.ae2_infinity_crafter_1789448946.AE2InfinityCrafter;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AE2InfinityCrafter.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 机器方块的物品模型由 BlockStateProvider 生成，这里只做纯物品。
        basicItem(ModItems.INFINITY_CELL.get());
        basicItem(ModItems.INFINITY_CELL_CHIP.get());
        basicItem(ModItems.INFINITY_CELL_HOUSING.get());
        basicItem(ModItems.PURIFIED_SINGULARITY.get());
    }
}
