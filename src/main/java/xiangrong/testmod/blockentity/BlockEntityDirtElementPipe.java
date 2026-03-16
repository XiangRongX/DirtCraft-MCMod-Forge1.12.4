package xiangrong.testmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xiangrong.testmod.energy.DirtElementNetwork;
import xiangrong.testmod.energy.DirtElementEnergyStorage;
import xiangrong.testmod.energy.INetworkBlockEntityNode;
import xiangrong.testmod.energy.INetworkBlockNode;

public class BlockEntityDirtElementPipe extends BlockEntity {
    private DirtElementNetwork network;

    public BlockEntityDirtElementPipe(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DIRT_ELEMENT_PIPE_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDirtElementPipe blockEntity) {

    }

    public void setNetwork(DirtElementNetwork network) {
        this.network = network;
    }

    public DirtElementNetwork getNetwork() {
        return network;
    }



}
