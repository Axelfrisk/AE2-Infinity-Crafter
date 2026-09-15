package cn.autoforged.ae2_infinity_crafter_1789448946.item;

import appeng.api.stacks.AEKey;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 无限元件芯片。
 *
 * <p>无限元件制作器不再直接产出完整的无限元件，而是产出“对应某种资源的无限元件芯片”。
 * 芯片本身<b>不是</b>存储元件（不继承 {@code CreativeCellItem}，因此不会被 AE2 当成无限存储），
 * 只保存这次要制作的目标资源 {@link AEKey}；在工作台里与“无限元件外壳”合成后，
 * 由 {@link cn.autoforged.ae2_infinity_crafter_1789448946.recipe.InfinityCellAssemblyRecipe}
 * 读取这里保存的资源，交给 {@link InfinityCellItem#createFor(AEKey)} 正式生成无限元件。
 *
 * <p>资源用 {@link AEKey#toTagGeneric()} / {@link AEKey#fromTagGeneric(CompoundTag)} 读写，
 * 与制作器 / 方块实体持久化资源用的是同一套 AE2 序列化，兼容物品、流体以及扩展模组注册的
 * 化学品 / 气体。
 */
public class InfinityCellChipItem extends Item {

    /** 保存目标资源的 NBT key。 */
    public static final String KEY_TAG = "ChipKey";

    public InfinityCellChipItem(Item.Properties properties) {
        super(properties);
    }

    /** 用某个 AEKey 生成一枚无限元件芯片。 */
    public static ItemStack createFor(@Nullable AEKey key) {
        ItemStack stack = new ItemStack(ModItems.INFINITY_CELL_CHIP.get());
        if (key != null) {
            stack.getOrCreateTag().put(KEY_TAG, key.toTagGeneric());
        }
        return stack;
    }

    /** 读取芯片里保存的目标资源；没有则返回 null。 */
    @Nullable
    public static AEKey getKey(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof InfinityCellChipItem)) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_TAG)) {
            return null;
        }
        return AEKey.fromTagGeneric(tag.getCompound(KEY_TAG));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        AEKey key = getKey(stack);
        if (key != null) {
            tooltip.add(Component.translatable(
                    "item.ae2_infinity_crafter_1789448946.infinity_cell_chip.contains",
                    key.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
