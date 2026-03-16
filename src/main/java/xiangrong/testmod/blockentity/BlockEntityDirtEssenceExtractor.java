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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.energy.DirtElementEnergyStorage;
import xiangrong.testmod.energy.INetworkBlockEntityNode;
import xiangrong.testmod.energy.INetworkBlockNode;
import xiangrong.testmod.menu.MenuDirtEssenceExtractor;

public class BlockEntityDirtEssenceExtractor extends BlockEntity implements MenuProvider, INetworkBlockEntityNode {
    private final DirtElementEnergyStorage energy = new DirtElementEnergyStorage(10000,200,200,10);
    public final ItemStackHandler inventory = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch(index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 0 -> energy.setEnergy(value);
                case 1 -> {} // maxEnergy 不变
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BlockEntityDirtEssenceExtractor(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DIRT_ESSENCE_EXTRACTOR_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState state, BlockEntityDirtEssenceExtractor blockEntity) {
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.testmod.dirt_essence_extractor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MenuDirtEssenceExtractor(pContainerId, pPlayerInventory, this);
    }

    @Override
    public Type getNodeType() {
        return Type.CONSUMER;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }


}
