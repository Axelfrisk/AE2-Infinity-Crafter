package cn.autoforged.ae2_infinity_crafter_1789448946;

import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellChipItem;
import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册。
 *
 * <ul>
 *   <li>{@code infinity_cell} —— 本 mod 的“无限元件”，继承 AE2 的 {@link
 *       appeng.items.storage.CreativeCellItem}，因此天然被 AE2 当成可配置的无限存储元件。</li>
 *   <li>{@code infinity_cell_chip} —— “无限元件芯片”，制作器产出的中间物品，保存目标资源；
 *       在工作台里与外壳合成后得到无限元件。</li>
 *   <li>{@code infinity_cell_housing} —— “无限元件外壳”，芯片合成无限元件所需的壳。</li>
 *   <li>{@code purified_singularity} —— “提纯奇点”，由 4 个“目标资源为奇点”的无限元件芯片在工作台合成，是外壳的材料。</li>
 *   <li>{@code infinity_cell_crafter} —— 机器方块的 BlockItem。</li>
 * </ul>
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AE2InfinityCrafter.MOD_ID);

    public static final RegistryObject<Item> INFINITY_CELL =
            ITEMS.register("infinity_cell",
                    () -> new InfinityCellItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /**
     * 无限元件芯片。制作器产出它，携带目标资源；在工作台里与外壳合成得到无限元件。
     * 本身不是存储元件，所以不会像无限元件那样在 ME 驱动/终端里当无限存储使用。
     */
    public static final RegistryObject<Item> INFINITY_CELL_CHIP =
            ITEMS.register("infinity_cell_chip",
                    () -> new InfinityCellChipItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    /** 无限元件外壳：芯片合成无限元件所需的壳。 */
    public static final RegistryObject<Item> INFINITY_CELL_HOUSING =
            ITEMS.register("infinity_cell_housing",
                    () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));

    /** 提纯奇点：由 4 个“目标资源为奇点（ae2:singularity）”的无限元件芯片在工作台合成，是无限元件外壳的合成材料。 */
    public static final RegistryObject<Item> PURIFIED_SINGULARITY =
            ITEMS.register("purified_singularity",
                    () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> INFINITY_CELL_CRAFTER =
            ITEMS.register("infinity_cell_crafter",
                    () -> new BlockItem(ModBlocks.INFINITY_CELL_CRAFTER.get(), new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
