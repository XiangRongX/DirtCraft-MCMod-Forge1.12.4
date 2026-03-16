package xiangrong.testmod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.blockentity.BlockEntityDirtElementPipe;
import xiangrong.testmod.blockentity.ModBlockEntities;
import xiangrong.testmod.energy.DirtElementNetwork;
import xiangrong.testmod.energy.INetworkBlockNode;
import xiangrong.testmod.energy.NetworkManager;

public class BlockDirtElementPipe extends BaseEntityBlock implements INetworkBlockNode {
    public static final MapCodec<BlockDirtElementPipe> CODEC = simpleCodec(BlockDirtElementPipe::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    // 基础碰撞箱（中心部分）
    private static final VoxelShape CENTER_SHAPE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

    // 各个方向的连接碰撞箱
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    private static final VoxelShape UP_SHAPE = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);

    public BlockDirtElementPipe(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityDirtElementPipe(pPos, pState);
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

        return createTickerHelper(pBlockEntityType, ModBlockEntities.DIRT_ELEMENT_PIPE_BE.get(), BlockEntityDirtElementPipe::serverTick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        boolean canConnect = canConnectTo(level, neighborPos, direction.getOpposite());
        return state.setValue(getProperty(direction), canConnect);
    }

    private BooleanProperty getProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private boolean canConnectTo(LevelReader level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof INetworkBlockNode) {
            return true;
        }

        return false;
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

    private BlockState updateConnections(BlockState state, Level level, BlockPos pos) {
        return state
                .setValue(NORTH, canConnectTo(level, pos.north(), Direction.SOUTH))
                .setValue(EAST, canConnectTo(level, pos.east(), Direction.WEST))
                .setValue(SOUTH, canConnectTo(level, pos.south(), Direction.NORTH))
                .setValue(WEST, canConnectTo(level, pos.west(), Direction.EAST))
                .setValue(UP, canConnectTo(level, pos.above(), Direction.DOWN))
                .setValue(DOWN, canConnectTo(level, pos.below(), Direction.UP));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER_SHAPE;

        // 根据连接状态添加各个方向的碰撞箱
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);

        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return getShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
    }

}
