package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.block.entity.InfinityCellCrafterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AE2InfinityCrafter.MOD_ID);

    public static final RegistryObject<BlockEntityType<InfinityCellCrafterBlockEntity>> INFINITY_CELL_CRAFTER =
            BLOCK_ENTITIES.register("infinity_cell_crafter", () -> BlockEntityType.Builder
                    .of(InfinityCellCrafterBlockEntity::new, ModBlocks.INFINITY_CELL_CRAFTER.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
