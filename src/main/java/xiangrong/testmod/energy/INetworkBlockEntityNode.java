package xiangrong.testmod.energy;

public interface INetworkBlockEntityNode {
    enum Type { PRODUCER, STORAGE, CONSUMER }
    Type getNodeType();
    int extractEnergy(int amount, boolean simulate); // 生产机用
    int receiveEnergy(int amount, boolean simulate); // 消费机用
    int getEnergyStored();
    int getMaxEnergyStored();
}
