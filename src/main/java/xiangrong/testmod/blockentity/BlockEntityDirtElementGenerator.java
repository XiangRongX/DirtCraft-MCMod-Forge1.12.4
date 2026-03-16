package xiangrong.testmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.energy.DirtElementEnergyStorage;
import xiangrong.testmod.energy.INetworkBlockEntityNode;
import xiangrong.testmod.menu.MenuDirtElementGenerator;

public class BlockEntityDirtElementGenerator extends BlockEntity implements MenuProvider, INetworkBlockEntityNode {
    private final DirtElementEnergyStorage energy = new DirtElementEnergyStorage(10000,200,200,5);
    private int burnTime = 0;
    private int burnTimeTotal = 0;
    private int cookTime = 0;
    private int cookTimeTotal = 200;
    private int lastTickOutput = 0;
    private int lastOutputCached = 0;
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
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                case 4 -> energy.getEnergyStored();
                case 5 -> energy.getMaxEnergyStored();
                case 6 -> lastOutputCached;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index) {
                case 0 -> burnTime = value;
                case 1 -> burnTimeTotal = value;
                case 2 -> cookTime = value;
                case 3 -> cookTimeTotal = value;
                case 4 -> energy.setEnergy(value);
                case 5 -> {} // maxEnergy 不变
                case 6 -> lastOutputCached = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public BlockEntityDirtElementGenerator(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DIRT_ELEMENT_GENERATOR_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.testmod.dirt_element_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MenuDirtElementGenerator(pContainerId,pPlayerInventory,this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDirtElementGenerator blockEntity) {
        boolean dirty = false;

        // 是否有可加工的压缩泥土
        ItemStack dirt = blockEntity.inventory.getStackInSlot(1);
        boolean hasDirt = !dirt.isEmpty() && dirt.getItem() == ModBlocks.COMPRESSED_DIRT_BLOCK.get().asItem();

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;

            if (level.getGameTime() % 80 == 0) {
                level.playSound(
                        null, // null = 所有玩家都能听见
                        pos,
                        SoundEvents.FIRE_AMBIENT, // 火焰噼啪声
                        SoundSource.BLOCKS,
                        0.5F, // 音量
                        1.0F  // 音高
                );
            }

            // 只有在有泥土的情况下才增加 cookTime
            if (hasDirt) {
                blockEntity.cookTime++;
                if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                    blockEntity.inventory.extractItem(1, 1, false);
                    blockEntity.energy.receiveEnergy(10000, false);
                    blockEntity.cookTime = 0;
                    dirty = true;
                }
            } else {
                // 没泥土就重置 cookTime（不白烧）
                if (blockEntity.cookTime > 0) {
                    blockEntity.cookTime = 0;
                    dirty = true;
                }
            }
        } else {
            // 只有在需要工作（有泥土）的情况下才取燃料
            if (hasDirt) {
                ItemStack fuel = blockEntity.inventory.getStackInSlot(0);
                int time = level.fuelValues().burnDuration(fuel, RecipeType.SMELTING);
                if (time > 0) {
                    blockEntity.burnTime = time;
                    blockEntity.burnTimeTotal = time;
                    blockEntity.inventory.extractItem(0, 1, false);
                    dirty = true;
                }
            }
        }

        if (dirty) {
            setChanged(level, pos, state);
        }

        blockEntity.lastOutputCached = blockEntity.lastTickOutput;
        blockEntity.lastTickOutput = 0;
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookTimeTotal", cookTimeTotal);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries,tag.getCompound("Inventory"));
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        cookTime = tag.getInt("CookTime");
        cookTimeTotal = tag.getInt("CookTimeTotal");
        energy.setEnergy(tag.getInt("Energy"));
    }

    private final LazyOptional<ItemStackHandler> inventoryOptional = LazyOptional.of(() -> inventory);
    private final LazyOptional<DirtElementEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryOptional.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
        energyOptional.invalidate();
    }

    public void drops(Level level, BlockPos pos) {
        SimpleContainer inventory = new SimpleContainer(this.inventory.getSlots());
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            inventory.setItem(i, this.inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, inventory);
    }

    @Override
    public Type getNodeType() {
        return Type.PRODUCER;
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
    public int receiveEnergy(int amount, boolean simulate) {
        return energy.receiveEnergy(amount, simulate);
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }
    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }
}
