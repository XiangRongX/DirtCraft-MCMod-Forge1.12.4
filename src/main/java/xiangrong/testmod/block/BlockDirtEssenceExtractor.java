package xiangrong.testmod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.blockentity.*;
import xiangrong.testmod.energy.DirtElementNetwork;
import xiangrong.testmod.energy.INetworkBlockNode;
import xiangrong.testmod.energy.NetworkManager;

public class BlockDirtEssenceExtractor extends BaseEntityBlock implements INetworkBlockNode {
    public static final MapCodec<BlockDirtEssenceExtractor> CODEC = simpleCodec(BlockDirtEssenceExtractor::new);

    protected BlockDirtEssenceExtractor(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityDirtEssenceExtractor(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.DIRT_ESSENCE_EXTRACTOR_BE.get(), BlockEntityDirtEssenceExtractor::serverTick);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            NetworkManager manager = NetworkManager.get(serverLevel);
            manager.refreshNetwork(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof BlockEntityDirtElementGenerator entity) {
                entity.drops(level, pos);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            NetworkManager manager = NetworkManager.get(serverLevel);

            // 先把自己所在的网络删掉
            DirtElementNetwork oldNet = manager.getNetwork(pos);
            if (oldNet != null) {
                manager.removeNetwork(oldNet);
            }

            // 对所有相邻管道重新建网
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (level.getBlockEntity(neighbor) instanceof BlockEntityDirtElementPipe) {
                    manager.refreshNetwork(level, neighbor);
                }
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if(blockEntity instanceof BlockEntityDirtEssenceExtractor entity){
                ((ServerPlayer) pPlayer).openMenu(new SimpleMenuProvider(entity, Component.literal("精华提取机")),pPos);
            }else{
                throw new IllegalStateException("Unexpected blockEntity: " + blockEntity);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
