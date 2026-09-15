package cn.autoforged.ae2_infinity_crafter_1789448946.item;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.items.storage.CreativeCellItem;
import appeng.util.ConfigInventory;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 无限元件。
 *
 * <p>继承 AE2 的 {@link CreativeCellItem} 是关键设计：AE2 的 {@code CreativeCellHandler}
 * 通过 {@code item instanceof CreativeCellItem} 判定“无限/创造存储元件”，所以任何子类都会被
 * AE2 的存储系统自动识别为“配置了什么资源，就无限提供什么资源”的元件。
 *
 * <p>这同时意味着它对 <b>任意 {@link appeng.api.stacks.AEKeyType}</b> 生效 —— 物品、流体，以及
 * 扩展模组（MEGA / AppBot 等）注册的化学品 / 气体，无需为每种类型单独建类。
 */
public class InfinityCellItem extends CreativeCellItem {

    public InfinityCellItem(Item.Properties properties) {
        super(properties);
    }

    /** 用某个 AEKey 配置出一个无限元件。 */
    public static ItemStack createFor(AEKey key) {
        ItemStack stack = new ItemStack(ModItems.INFINITY_CELL.get());
        if (key != null && stack.getItem() instanceof InfinityCellItem cell) {
            ConfigInventory config = cell.getConfigInventory(stack);
            config.setStack(0, new GenericStack(key, 1L));
        }
        return stack;
    }

    /** 判断这个无限元件是否已经配置了给定资源。 */
    public static boolean isConfiguredFor(ItemStack stack, AEKey key) {
        if (stack.isEmpty() || !(stack.getItem() instanceof InfinityCellItem cell) || key == null) {
            return false;
        }
        return cell.getConfigInventory(stack).keySet().contains(key);
    }
}
