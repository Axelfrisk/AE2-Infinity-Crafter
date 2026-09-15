package cn.autoforged.ae2_infinity_crafter_1789448946.datagen;

import cn.autoforged.ae2_infinity_crafter_1789448946.AE2InfinityCrafter;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AE2InfinityCrafter.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.INFINITY_CELL_CRAFTER.get(), cubeAll(ModBlocks.INFINITY_CELL_CRAFTER.get()));
    }
}
