package xiangrong.testmod.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import xiangrong.testmod.blockentity.BlockEntityDirtElementPipe;

import java.util.*;

public class DirtElementNetwork {
    private final Set<BlockPos> pipes = new HashSet<>();
    private final Set<BlockPos> producers = new HashSet<>();
    private final Set<BlockPos> storages = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();

    public void addPipe(BlockPos pos) { pipes.add(pos); }
    public void addProducer(BlockPos pos) { producers.add(pos); }
    public void addStorage(BlockPos pos) { storages.add(pos); }
    public void addConsumer(BlockPos pos) { consumers.add(pos); }

    public Set<BlockPos> getPipes() { return pipes; }
    public Set<BlockPos> getProducers() { return producers; }
    public Set<BlockPos> getStorages() { return storages; }
    public Set<BlockPos> getConsumers() { return consumers; }

    /**
     * 网络 tick：收集所有节点，生产机+存储器输出为输出端，消费者+存储器输入为输入端。
     */
    public void tick(Level level) {
        List<INetworkBlockEntityNode> producerNodes = new ArrayList<>();
        List<INetworkBlockEntityNode> storageNodes = new ArrayList<>();
        List<INetworkBlockEntityNode> consumerNodes = new ArrayList<>();

        // 1. 收集节点（按类型分好）
        for (BlockPos pos : producers) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof INetworkBlockEntityNode node && node.getNodeType() == INetworkBlockEntityNode.Type.PRODUCER) {
                producerNodes.add(node);
            }
        }

        for (BlockPos pos : storages) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof INetworkBlockEntityNode node && node.getNodeType() == INetworkBlockEntityNode.Type.STORAGE) {
                storageNodes.add(node);
            }
        }

        for (BlockPos pos : consumers) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof INetworkBlockEntityNode node && node.getNodeType() == INetworkBlockEntityNode.Type.CONSUMER) {
                consumerNodes.add(node);
            }
        }

        int totalProducerEnergy = 0;
        for(INetworkBlockEntityNode producer : producerNodes) {
            totalProducerEnergy += producer.getEnergyStored();
        }
        int consumerInNeedNum = 0;
        int consumerInNeedEnergy = 0;
        if(totalProducerEnergy >= consumerInNeedEnergy) {
            for(INetworkBlockEntityNode consumer : consumerNodes) {
                if(consumer.getEnergyStored() < consumer.getMaxEnergyStored()) {
                    consumerInNeedNum++;
                    consumerInNeedEnergy += consumer.getMaxEnergyStored() - consumer.getEnergyStored();
                }
            }
            int energyPerConsumer = consumerInNeedEnergy / consumerInNeedNum;
            for(INetworkBlockEntityNode consumer : consumerNodes) {
                if(consumer.getEnergyStored() < consumer.getMaxEnergyStored()) {
                    consumer.receiveEnergy(energyPerConsumer, false);
                }
            }
            for(INetworkBlockEntityNode producer : producerNodes) {
                consumerInNeedEnergy -= producer.extractEnergy(consumerInNeedEnergy, false);
            }

            totalProducerEnergy -= consumerInNeedEnergy;
            for(INetworkBlockEntityNode storage : storageNodes) {
                totalProducerEnergy -= storage.receiveEnergy(totalProducerEnergy, false);
            }

        }else{ //生产者的能量不够

        }


    }


    /**
     * 从起点构建网络，依据 INetworkBlockEntityNode.Type 判定
     */
    public static DirtElementNetwork buildNetwork(Level level, BlockPos startPos) {
        DirtElementNetwork network = new DirtElementNetwork();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity blockEntity = level.getBlockEntity(current);

            if (blockEntity instanceof BlockEntityDirtElementPipe pipe) {
                pipe.setNetwork(network);
                network.addPipe(current);

                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = current.relative(dir);
                    if (visited.contains(neighborPos)) continue;

                    BlockEntity neighbor = level.getBlockEntity(neighborPos);
                    if (neighbor instanceof BlockEntityDirtElementPipe) {
                        queue.add(neighborPos);
                        visited.add(neighborPos);
                    } else if (neighbor instanceof INetworkBlockEntityNode node) {
                        visited.add(neighborPos);
                        switch (node.getNodeType()) {
                            case PRODUCER -> network.addProducer(neighborPos);
                            case CONSUMER -> network.addConsumer(neighborPos);
                            case STORAGE -> network.addStorage(neighborPos);
                        }
                    }
                }
            }
        }
        return network;
    }


    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 管道
        ListTag pipeList = new ListTag();
        for (BlockPos pos : pipes) {
            pipeList.add(writePos(pos));
        }
        tag.put("Pipes", pipeList);

        // 生产机
        ListTag producerList = new ListTag();
        for (BlockPos pos : producers) {
            producerList.add(writePos(pos));
        }
        tag.put("Producers", producerList);

        // 存储器
        ListTag storageList = new ListTag();
        for (BlockPos pos : storages) {
            storageList.add(writePos(pos));
        }
        tag.put("Storages", storageList);

        // 消费者
        ListTag consumerList = new ListTag();
        for (BlockPos pos : consumers) {
            consumerList.add(writePos(pos));
        }
        tag.put("Consumers", consumerList);

        return tag;
    }

    public static DirtElementNetwork load(CompoundTag tag) {
        DirtElementNetwork network = new DirtElementNetwork();

        // 管道
        ListTag pipeList = tag.getList("Pipes", Tag.TAG_COMPOUND);
        for (Tag t : pipeList) {
            if (t instanceof CompoundTag posTag) {
                network.addPipe(readPos(posTag));
            }
        }

        // 生产机
        ListTag producerList = tag.getList("Producers", Tag.TAG_COMPOUND);
        for (Tag t : producerList) {
            if (t instanceof CompoundTag posTag) {
                network.addProducer(readPos(posTag));
            }
        }

        // 存储器
        ListTag storageList = tag.getList("Storages", Tag.TAG_COMPOUND);
        for (Tag t : storageList) {
            if (t instanceof CompoundTag posTag) {
                network.addStorage(readPos(posTag));
            }
        }

        // 消费者
        ListTag consumerList = tag.getList("Consumers", Tag.TAG_COMPOUND);
        for (Tag t : consumerList) {
            if (t instanceof CompoundTag posTag) {
                network.addConsumer(readPos(posTag));
            }
        }

        return network;
    }


    private static CompoundTag writePos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    private static BlockPos readPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }
}
