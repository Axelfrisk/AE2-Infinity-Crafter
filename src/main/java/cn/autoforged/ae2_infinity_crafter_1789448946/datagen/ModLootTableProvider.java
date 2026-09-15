package cn.autoforged.ae2_infinity_crafter_1789448946.datagen;

import cn.autoforged.ae2_infinity_crafter_1789448946.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.List;

public final class ModLootTableProvider {

    private ModLootTableProvider() {
    }

    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)));
    }

    public static class ModBlockLoot extends BlockLootSubProvider {

        public ModBlockLoot() {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.INFINITY_CELL_CRAFTER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
        }
    }
}
