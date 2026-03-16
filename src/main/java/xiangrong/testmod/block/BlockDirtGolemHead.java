package xiangrong.testmod.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockDirtGolemHead extends AbstractSkullBlock {
    public static final MapCodec<BlockDirtGolemHead> CODEC = RecordCodecBuilder.mapCodec(
            instance->instance.group(SkullBlock.Type.CODEC.fieldOf("kind").forGetter(AbstractSkullBlock::getType),propertiesCodec())
                    .apply(instance, BlockDirtGolemHead::new)
    );
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Block.box(4,0,4,12,8,12);

    public BlockDirtGolemHead(SkullBlock.Type pType, Properties pProperties) {
        super(pType, pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected MapCodec<? extends AbstractSkullBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState pState) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(ROTATION, Mth.floor(((double)context.getRotation() * 16.0D / 360.0D) + 0.5D) & 15);
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(ROTATION, pRotation.rotate(pState.getValue(ROTATION),16));
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.setValue(ROTATION, pMirror.mirror(pState.getValue(ROTATION),16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }

    public enum Types implements SkullBlock.Type {
        DIRT_GOLEM("testmod:dirt_golem");


        private final String name;

        Types(String name) {
            this.name = name;
            TYPES.put(this.name, this);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

}
