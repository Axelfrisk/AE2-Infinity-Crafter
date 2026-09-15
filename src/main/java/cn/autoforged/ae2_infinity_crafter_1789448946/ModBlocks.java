package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.block.InfinityCellCrafterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块注册。机器方块：硬度 3.5 / 爆炸抗性 8.0 / 需镐子 / 亮度 7。
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AE2InfinityCrafter.MOD_ID);

    public static final RegistryObject<Block> INFINITY_CELL_CRAFTER =
            BLOCKS.register("infinity_cell_crafter", () -> new InfinityCellCrafterBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.5F, 8.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 7)
                            .requiresCorrectToolForDrops()));

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
