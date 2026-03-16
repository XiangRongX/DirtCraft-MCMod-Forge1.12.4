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
import xiangrong.testmod.blockentity.BlockEntityDirtElementGenerator;
import xiangrong.testmod.blockentity.BlockEntityDirtElementPipe;
import xiangrong.testmod.blockentity.ModBlockEntities;
import xiangrong.testmod.energy.DirtElementNetwork;
import xiangrong.testmod.energy.INetworkBlockNode;
import xiangrong.testmod.energy.NetworkManager;

public class BlockDirtElementGenerator extends BaseEntityBlock implements INetworkBlockNode {
    public static final MapCodec<BlockDirtElementGenerator> CODEC = simpleCodec(BlockDirtElementGenerator::new);

    protected BlockDirtElementGenerator(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityDirtElementGenerator(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
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
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if(blockEntity instanceof BlockEntityDirtElementGenerator entity) {
                entity.drops(pLevel, pPos);
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);

        if (!pLevel.isClientSide && pLevel instanceof ServerLevel serverLevel) {
            NetworkManager manager = NetworkManager.get(serverLevel);

            // 先把自己所在的网络删掉
            DirtElementNetwork oldNet = manager.getNetwork(pPos);
            if (oldNet != null) {
                manager.removeNetwork(oldNet);
            }

            // 对所有相邻管道重新建网
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pPos.relative(dir);
                if (pLevel.getBlockEntity(neighbor) instanceof BlockEntityDirtElementPipe) {
                    manager.refreshNetwork(pLevel, neighbor);
                }
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if(blockEntity instanceof BlockEntityDirtElementGenerator entity){
                ((ServerPlayer) pPlayer).openMenu(new SimpleMenuProvider(entity, Component.literal("泥素生产机")),pPos);
            }else{
                throw new IllegalStateException("Unexpected blockEntity: " + blockEntity);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.DIRT_ELEMENT_GENERATOR_BE.get(), BlockEntityDirtElementGenerator::serverTick);
    }

}
