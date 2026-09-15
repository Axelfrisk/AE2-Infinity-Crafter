package cn.autoforged.ae2_infinity_crafter_1789448946.menu;

import cn.autoforged.ae2_infinity_crafter_1789448946.ModBlocks;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModMenuTypes;
import cn.autoforged.ae2_infinity_crafter_1789448946.block.entity.InfinityCellCrafterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InfinityCellCrafterMenu extends AbstractContainerMenu {

    private final InfinityCellCrafterBlockEntity blockEntity;
    private final ContainerData data;

    /** 客户端构造函数：从网络包读取方块位置。 */
    public InfinityCellCrafterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf), new SimpleContainerData(7));
    }

    /** 服务端构造函数。 */
    public InfinityCellCrafterMenu(int containerId, Inventory playerInventory,
                                   InfinityCellCrafterBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.INFINITY_CELL_CRAFTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(() -> new IllegalStateException("InfinityCellCrafter is missing its item handler"));

        this.addSlot(new SlotItemHandler(handler, InfinityCellCrafterBlockEntity.SLOT_CELL, 44, 35));
        this.addSlot(new SlotItemHandler(handler, InfinityCellCrafterBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    private static InfinityCellCrafterBlockEntity readBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof InfinityCellCrafterBlockEntity crafter) {
            return crafter;
        }
        throw new IllegalStateException("Missing InfinityCellCrafterBlockEntity at " + pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.INFINITY_CELL_CRAFTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int customSlots = InfinityCellCrafterBlockEntity.SLOT_COUNT;
            if (index < customSlots) {
                if (!this.moveItemStackTo(stack, customSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (InfinityCellCrafterBlockEntity.isValidInputCell(stack)) {
                if (!this.moveItemStackTo(stack, InfinityCellCrafterBlockEntity.SLOT_CELL,
                        InfinityCellCrafterBlockEntity.SLOT_CELL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 只有 256k 存储元件能进输入槽，其余物品不做 shift-click 移动。
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    /** 当前输入元件里数量最多的那种资源的存量。 */
    public long getProgress() {
        return combine(data.get(0), data.get(1));
    }

    /** 当前展示资源换算后的总需求量（默认物品 = 10000）。 */
    public long getRequired() {
        return combine(data.get(2), data.get(3));
    }

    /** 输入元件里是否有多于一种资源达到了阈值（此时拒绝合成）。 */
    public boolean isConflict() {
        return (data.get(6) & 1) != 0;
    }

    /** AE 电网当前是否有电。 */
    public boolean isNetworkPowered() {
        return (data.get(6) & 2) != 0;
    }

    /** 进度百分比 0-100：注入 100 / 10000 时返回 1。 */
    public int getProgressPercent() {
        long req = getRequired();
        if (req <= 0) {
            return 0;
        }
        long percent = getProgress() * 100L / req;
        return (int) Math.max(0L, Math.min(100L, percent));
    }

    public int getEnergy() {
        return (int) combine(data.get(4), data.get(5));
    }

    public int getMaxEnergy() {
        return InfinityCellCrafterBlockEntity.ENERGY_CAPACITY;
    }

    public int getScaledProgress(int width) {
        long req = getRequired();
        if (req <= 0) {
            return 0;
        }
        long scaled = getProgress() * width / req;
        return (int) Math.max(0L, Math.min(width, scaled));
    }

    public int getScaledEnergy(int height) {
        return getEnergy() * height / getMaxEnergy();
    }

    /** ContainerData 以 16 位 short 传输，这里把低/高 16 位还原成数值。 */
    private static long combine(int low, int high) {
        return ((long) (high & 0xFFFF) << 16) | (low & 0xFFFF);
    }
}
