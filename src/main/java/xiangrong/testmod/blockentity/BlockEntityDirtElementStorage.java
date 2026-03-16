package xiangrong.testmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.energy.DirtElementEnergyStorage;
import xiangrong.testmod.energy.INetworkBlockEntityNode;
import xiangrong.testmod.menu.MenuDirtElementStorage;

public class BlockEntityDirtElementStorage extends BlockEntity implements MenuProvider, INetworkBlockEntityNode {
    private final DirtElementEnergyStorage energy = new DirtElementEnergyStorage(100000,200,200,10);
    private int lastTickInput = 0;
    private int lastTickOutput = 0;
    private int lastInputCached = 0;
    private int lastOutputCached = 0;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch(index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                case 2 -> lastInputCached;
                case 3 -> lastOutputCached;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 0 -> energy.setEnergy(value);
                case 1 -> {} // maxEnergy 不变
                case 2 -> lastInputCached = value;
                case 3 -> lastOutputCached = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BlockEntityDirtElementStorage(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DIRT_ELEMENT_STORAGE_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDirtElementStorage blockEntity){
        if (level.isClientSide) return;

        blockEntity.lastInputCached = blockEntity.lastTickInput;
        blockEntity.lastOutputCached = blockEntity.lastTickOutput;

        blockEntity.lastTickInput = 0;
        blockEntity.lastTickOutput = 0;

    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.testmod.dirt_element_storage");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return  new MenuDirtElementStorage(pContainerId,pPlayerInventory,this);
    }

    @Override
    public Type getNodeType() {
        return Type.STORAGE;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        int received = energy.receiveEnergy(amount, simulate);
        if (!simulate && received > 0) {
            lastTickInput += received;
        }
        return received;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        int extracted = energy.extractEnergy(amount, simulate);
        if (!simulate && extracted > 0) {
            lastTickOutput += extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

}
