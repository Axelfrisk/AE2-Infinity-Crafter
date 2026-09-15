package cn.autoforged.ae2_infinity_crafter_1789448946.block;

import cn.autoforged.ae2_infinity_crafter_1789448946.ModBlockEntities;
import cn.autoforged.ae2_infinity_crafter_1789448946.block.entity.InfinityCellCrafterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * 无限元件制作器方块。硬度 / 爆炸抗性 / 亮度等属性在 {@link
 * cn.autoforged.ae2_infinity_crafter_1789448946.ModBlocks} 里配置。
 */
public class InfinityCellCrafterBlock extends Block implements EntityBlock {

    public InfinityCellCrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityCellCrafterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.INFINITY_CELL_CRAFTER.get()) {
            return (lvl, pos, st, be) -> InfinityCellCrafterBlockEntity.tick(
                    lvl, pos, st, (InfinityCellCrafterBlockEntity) be);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof InfinityCellCrafterBlockEntity crafter && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, crafter, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof InfinityCellCrafterBlockEntity crafter) {
                crafter.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
