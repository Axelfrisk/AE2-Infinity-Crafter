package cn.autoforged.ae2_infinity_crafter_1789448946.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import cn.autoforged.ae2_infinity_crafter_1789448946.ModBlockEntities;
import cn.autoforged.ae2_infinity_crafter_1789448946.item.InfinityCellChipItem;
import cn.autoforged.ae2_infinity_crafter_1789448946.menu.InfinityCellCrafterMenu;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;

/**
 * 无限元件制作器的核心逻辑（单输入槽的“检测式”合成）。
 *
 * <p>槽位布局只有两个：
 * <ul>
 *   <li>槽 0：输入一个 256k 存储元件（物品 / 流体 / 化学品，只要是 AE2 能识别的存储元件）</li>
 *   <li>槽 1：输出槽（对应资源的无限元件芯片，需在工作台里与无限元件外壳合成成无限元件）</li>
 * </ul>
 *
 * <p>合成规则：不要求资源另外注入，直接读取输入元件里已有的存储内容。把其中数量达到
 * 阈值（默认 10000 个物品 / 10000B 流体，可用调试命令 {@code /aeinfinite <数量>} 调整）的
 * 资源找出来：
 * <ul>
 *   <li>恰好一种资源达标 → 从该元件中扣除阈值份资源，输出配置了该资源的“无限元件芯片”；</li>
 *   <li>两种及以上资源同时达标 → 拒绝合成，并打印“有多个物品数量超过&lt;阈值&gt;”。</li>
 * </ul>
 *
 * <p>资源判定完全基于 AE2 的 {@link AEKey} / {@link StorageCell} 抽象，因此 MEGA / AppBot
 * 等扩展模组注册的化学品 / 气体 {@code AEKeyType} 无需任何额外代码即可支持。阈值按
 * {@link AEKey#getAmountPerUnit()} 换算：物品为 1（即 10000 个），流体为 1000（即 10000 B）。
 *
 * <p>供电：方块实体本身就是 AE2 的 ME 电网节点（{@link IInWorldGridNodeHost}），
 * 会从电网能量服务取电充入内部缓冲；同时保留 Forge Energy 输入作为外部供能方式。
 */
public class InfinityCellCrafterBlockEntity extends BlockEntity
        implements MenuProvider, IInWorldGridNodeHost {

    public static final int SLOT_CELL = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 内部能量缓冲容量（单位：AE）。 */
    public static final int ENERGY_CAPACITY = 100_000;
    /** 每 tick 加工消耗的 AE。 */
    public static final int ENERGY_PER_TICK = 200;
    /** 每 tick 最多从 AE 电网取多少 AE（大于加工消耗，便于回充）。 */
    public static final double AE_CHARGE_PER_TICK = 400.0D;
    /** 1 AE 换算成多少内部能量单位（内部缓冲直接以 AE 计）。 */
    public static final double ENERGY_UNITS_PER_AE = 1.0D;

    /**
     * 输入元件的最小容量，单位是 AE2 的 byte（不是“k”）。
     *
     * <p>依据前置源码 {@code appeng.items.storage.BasicStorageCell}：{@code
     * totalBytes = kilobytes * 1024}，且 {@code getBytes(ItemStack)} 直接返回该 totalBytes。
     * 所以 256k 元件的 {@code getBytes()} 返回 262144（而非 256）。这里要求 >= 256k，
     * 以拒绝 1k / 4k / 16k / 64k 等更低档的元件。
     */
    public static final int MIN_CELL_BYTES = 256 * 1024;

    /**
     * 调试命令 {@code /aeinfinite <数量>} 设置的基础需求量（默认 10000）。
     * 物品按“个”计；流体按“B”计（用时再乘 {@link AEKey#getAmountPerUnit()}）。
     */
    private static long requiredBase = 10_000L;

    /** GUI 进度：当前数量最多的资源已积累量。 */
    private long progress;

    /** GUI 进度：当前展示资源换算后的总需求量。 */
    private long required;

    /** 是否检测到多种资源同时达标（此时拒绝合成）。 */
    private boolean conflict;

    /** 冲突提示是否已打印，避免每 tick 刷屏。 */
    private boolean conflictNotified;

    /** 当前展示的资源（数量最多者），仅用于显示 / 持久化。 */
    @Nullable
    private AEKey craftingKey;

    private int energy;

    /** 对元件存储做提取时使用的空动作来源。 */
    private static final IActionSource ACTION_SOURCE = IActionSource.empty();

    /**
     * ME 电网节点。方块实体直接实现 {@link IInWorldGridNodeHost}，因此 AE2 的线缆会把它
     * 当作一个可连接的机器节点。节点在服务端创建 / 销毁，客户端不建节点。
     */
    private final IGridNodeListener<InfinityCellCrafterBlockEntity> nodeListener =
            new IGridNodeListener<>() {
                @Override
                public void onSaveChanges(InfinityCellCrafterBlockEntity owner, IGridNode node) {
                    InfinityCellCrafterBlockEntity.this.setChanged();
                }
            };

    private final IManagedGridNode aeNode = GridHelper.createManagedNode(this, nodeListener)
            .setInWorldNode(true)
            .setTagName("ae_node")
            .setIdlePowerUsage(0.0D)
            .setExposedOnSides(EnumSet.allOf(Direction.class));

    private boolean nodeCreated;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SLOT_CELL && isValidInputCell(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_OUTPUT ? 1 : 64;
        }
    };

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    /**
     * Forge Energy 输入适配器。机器主要从 AE 电网取电（见 {@link #chargeFromGrid()}），
     * 这里额外保留 FE 能力，方便外部 FE 模组直接给内部缓冲充电。
     */
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int accepted = Math.min(maxReceive, ENERGY_CAPACITY - energy);
            if (accepted < 0) {
                accepted = 0;
            }
            if (!simulate && accepted > 0) {
                energy += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return ENERGY_CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    /**
     * 同步给 GUI 的数据。ContainerData 在网络上按 16 位 short 传输，
     * 因此进度 / 需求量 / 能量都拆成低 16 位和高 16 位两组：
     * 0/1=进度低高，2/3=需求量低高，4/5=能量低高，
     * 6=状态位（bit0=输入冲突，bit1=AE 电网已供电）。
     */
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) (progress & 0xFFFF);
                case 1 -> (int) ((progress >>> 16) & 0xFFFF);
                case 2 -> (int) (required & 0xFFFF);
                case 3 -> (int) ((required >>> 16) & 0xFFFF);
                case 4 -> energy & 0xFFFF;
                case 5 -> (energy >>> 16) & 0xFFFF;
                case 6 -> (conflict ? 1 : 0) | (isGridPowered() ? 2 : 0);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 客户端 -> 服务端不会用到；数据由服务端单向推送。
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public InfinityCellCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFINITY_CELL_CRAFTER.get(), pos, state);
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    // ------------------------------------------------------------------
    // Capability
    // ------------------------------------------------------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> inventory);
        energyHandler = LazyOptional.of(() -> energyStorage);
    }

    // ------------------------------------------------------------------
    // AE 电网节点（IInWorldGridNodeHost）
    // ------------------------------------------------------------------

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        scheduleNodeInit();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        scheduleNodeInit();
    }

    /**
     * 电网节点只在服务端、且只在第一个 tick 创建一次。AE2 的线缆通过
     * {@link appeng.api.networking.GridHelper#getNodeHost} 发现本方块实体。
     */
    private void scheduleNodeInit() {
        if (!nodeCreated && level != null && !level.isClientSide) {
            nodeCreated = true;
            GridHelper.onFirstTick(this, be -> {
                if (!be.isRemoved() && be.level != null) {
                    be.aeNode.create(be.level, be.worldPosition);
                }
            });
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        aeNode.destroy();
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        return aeNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    /** AE 电网当前是否有电。 */
    public boolean isGridPowered() {
        IGrid grid = aeNode.getGrid();
        return grid != null && grid.getEnergyService().isNetworkPowered();
    }

    // ------------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, InfinityCellCrafterBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.serverTick(level, pos);
    }

    private void serverTick(Level level, BlockPos pos) {
        // 先尝试从 AE 电网充电（也允许外部 FE 直接输入）。
        boolean dirty = chargeFromGrid();

        ItemStack cell = inventory.getStackInSlot(SLOT_CELL);
        boolean cellValid = isValidInputCell(cell);
        boolean outputRoom = inventory.getStackInSlot(SLOT_OUTPUT).isEmpty();

        long prevProgress = progress;
        long prevRequired = required;
        boolean prevConflict = conflict;
        boolean prevNotified = conflictNotified;
        AEKey prevKey = craftingKey;

        progress = 0L;
        required = 0L;
        craftingKey = null;
        conflict = false;

        if (cellValid) {
            StorageCell contents = StorageCells.getCellInventory(cell, null);
            if (contents != null) {
                AEKey leadingKey = null;
                long leadingAmount = 0L;
                AEKey overKey = null;
                int overCount = 0;

                for (Object2LongMap.Entry<AEKey> entry : contents.getAvailableStacks()) {
                    AEKey key = entry.getKey();
                    long amount = entry.getLongValue();
                    if (amount > leadingAmount) {
                        leadingAmount = amount;
                        leadingKey = key;
                    }
                    if (amount >= thresholdFor(key)) {
                        overCount++;
                        overKey = key;
                    }
                }

                if (leadingKey != null) {
                    craftingKey = leadingKey;
                    required = thresholdFor(leadingKey);
                    progress = leadingAmount;
                }

                if (overCount >= 2) {
                    // 多种资源同时达标：拒绝合成并打印提示。
                    conflict = true;
                } else if (overCount == 1 && outputRoom && consumeEnergy()) {
                    craft(contents, cell, overKey);
                    progress = 0L;
                    required = 0L;
                    craftingKey = null;
                    dirty = true;
                }
            }
        }

        if (conflict) {
            if (!conflictNotified) {
                notifyConflict(level, pos);
            }
            conflictNotified = true;
        } else {
            conflictNotified = false;
        }

        if (dirty || prevProgress != progress || prevRequired != required
                || prevConflict != conflict || prevNotified != conflictNotified
                || !Objects.equals(prevKey, craftingKey)) {
            setChanged();
        }

        if (!conflict && progress > 0L) {
            spawnWorkingParticles(level, pos);
        }
    }

    /** 从 AE 电网取电充入内部缓冲。返回内部能量是否发生变化。 */
    private boolean chargeFromGrid() {
        if (energy >= ENERGY_CAPACITY) {
            return false;
        }
        IGrid grid = aeNode.getGrid();
        if (grid == null) {
            return false;
        }
        double accepted = grid.getEnergyService()
                .extractAEPower(AE_CHARGE_PER_TICK, Actionable.MODULATE, PowerMultiplier.ONE);
        if (accepted <= 0.0D) {
            return false;
        }
        int units = (int) Math.round(accepted * ENERGY_UNITS_PER_AE);
        energy = Math.min(ENERGY_CAPACITY, energy + units);
        return true;
    }

    /** 消耗一次加工所需的内部能量；能量不足时返回 false（本 tick 不加工）。 */
    private boolean consumeEnergy() {
        if (energy < ENERGY_PER_TICK) {
            return false;
        }
        energy -= ENERGY_PER_TICK;
        return true;
    }

    /**
     * 从输入元件中扣除阈值份目标资源，并在输出槽放入对应的无限元件芯片。
     * 元件本身不消耗，只扣元件里的资源。
     */
    private void craft(StorageCell contents, ItemStack cell, AEKey key) {
        long need = thresholdFor(key);
        long extracted = contents.extract(key, need, Actionable.MODULATE, ACTION_SOURCE);
        if (extracted < need) {
            // 理论上不会发生：数量检查已经确认元件里足够。
            return;
        }
        // extract 直接改了元件 NBT，重新写回槽位以触发 setChanged / 客户端同步。
        inventory.setStackInSlot(SLOT_CELL, cell);
        inventory.setStackInSlot(SLOT_OUTPUT, InfinityCellChipItem.createFor(key));
        setChanged();
    }

    /** 打印“有多个物品数量超过阈值”的提示（日志 + 附近玩家的动作栏）。 */
    private void notifyConflict(Level level, BlockPos pos) {
        long threshold = requiredBase;
        LOGGER.warn("有多个物品数量超过{}（无限元件制作器 @ {}）", threshold, pos);
        if (level instanceof ServerLevel server) {
            Component message = Component.translatable(
                    "message.ae2_infinity_crafter_1789448946.multiple_over_threshold", threshold);
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.5D;
            double z = pos.getZ() + 0.5D;
            for (ServerPlayer player : server.getPlayers(p -> p.distanceToSqr(x, y, z) <= 64.0D * 64.0D)) {
                player.displayClientMessage(message, true);
            }
        }
    }

    private void spawnWorkingParticles(Level level, BlockPos pos) {
        // 每 4 tick 出一次粒子，避免每 tick 发包。
        if ((level.getGameTime() & 3) != 0) {
            return;
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                    2, 0.25D, 0.05D, 0.25D, 0.01D);
        }
    }

    // ------------------------------------------------------------------
    // 逻辑辅助
    // ------------------------------------------------------------------

    /** 基础需求量（调试命令可改）换算成某种资源的实际需求量。 */
    public static long thresholdFor(AEKey key) {
        return requiredBase * key.getAmountPerUnit();
    }

    public static long getRequiredBase() {
        return requiredBase;
    }

    /** 调试命令 {@code /aeinfinite <数量>} 用；数量至少为 1。 */
    public static void setRequiredBase(long value) {
        requiredBase = Math.max(1L, value);
    }

    /** 方块被破坏时把库存掉到世界上，避免玩家丢东西。 */
    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    public static boolean isValidInputCell(ItemStack stack) {
        if (stack.isEmpty() || !StorageCells.isCellHandled(stack)) {
            return false;
        }
        if (stack.getItem() instanceof IBasicCellItem basic) {
            return basic.getBytes(stack) >= MIN_CELL_BYTES;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // 持久化 / 同步
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Progress", progress);
        tag.putLong("Required", required);
        tag.putInt("Energy", energy);
        tag.putBoolean("Conflict", conflict);
        tag.put("Inventory", inventory.serializeNBT());
        aeNode.saveToNBT(tag);
        if (craftingKey != null) {
            tag.put("CraftingKey", craftingKey.toTagGeneric());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getLong("Progress");
        required = tag.getLong("Required");
        energy = tag.getInt("Energy");
        conflict = tag.getBoolean("Conflict");
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        aeNode.loadFromNBT(tag);
        craftingKey = tag.contains("CraftingKey") ? AEKey.fromTagGeneric(tag.getCompound("CraftingKey")) : null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    // ------------------------------------------------------------------
    // MenuProvider
    // ------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ae2_infinity_crafter_1789448946.infinity_cell_crafter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new InfinityCellCrafterMenu(containerId, playerInventory, this, dataAccess);
    }
}
