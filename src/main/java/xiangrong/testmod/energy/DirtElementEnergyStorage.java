package xiangrong.testmod.energy;

import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.energy.EnergyStorage;

public class DirtElementEnergyStorage extends EnergyStorage {
    private final int maxExtractPerTick;

    public DirtElementEnergyStorage(int capacity, int maxReceive, int maxExtract, int maxExtractPerTick) {
        super(capacity, maxReceive, maxExtract);
        this.maxExtractPerTick = maxExtractPerTick;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        // 在原有 maxExtract 的基础上，再加一个 "每 tick 最大限制"
        int toExtract = Math.min(maxExtract, maxExtractPerTick);
        return super.extractEnergy(toExtract, simulate);
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, getMaxEnergyStored());
    }

    public boolean isFull() {
        return this.energy >= getMaxEnergyStored();
    }

    public boolean isEmpty() {
        return this.energy <= 0;
    }
}