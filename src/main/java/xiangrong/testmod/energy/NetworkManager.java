package xiangrong.testmod.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import xiangrong.testmod.blockentity.BlockEntityDirtElementPipe;

import javax.annotation.Nullable;
import java.util.*;

public class NetworkManager extends SavedData {
    public final List<DirtElementNetwork> networks = new ArrayList<>();
    private static final String DATA_NAME = "dirt_element_networks";

    public NetworkManager() {}

    public static NetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        NetworkManager::new,   // 新建时调用
                        NetworkManager::load,  // 从NBT加载
                        null                   // 可选 codec，这里不用
                ),
                DATA_NAME
        );
    }

    @Nullable
    public DirtElementNetwork getNetwork(BlockPos pos) {
        for (DirtElementNetwork network : networks) {
            if (network.getPipes().contains(pos)
                    || network.getProducers().contains(pos)
                    || network.getStorages().contains(pos)
                    || network.getConsumers().contains(pos)) {
                return network;
            }
        }
        return null;
    }

    public void addNetwork(DirtElementNetwork network) {
        networks.add(network);
        setDirty();
    }

    public void removeNetwork(DirtElementNetwork network) {
        networks.remove(network);
        setDirty();
    }

    public List<DirtElementNetwork> getNetworks() {
        return networks;
    }

    public void tick(Level level) {
        for(DirtElementNetwork network : networks) {
            network.tick(level);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag networkList = new ListTag();
        for (DirtElementNetwork network : networks) {
            networkList.add(network.save()); // 调用 DirtElementNetwork.save()
        }
        tag.put("Networks", networkList);
        return tag;
    }

    public static NetworkManager load(CompoundTag tag, HolderLookup.Provider provider) {
        NetworkManager manager = new NetworkManager();

        ListTag networkList = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (Tag t : networkList) {
            if (t instanceof CompoundTag nbt) {
                manager.networks.add(DirtElementNetwork.load(nbt));
            }
        }

        return manager;
    }

    public void refreshNetwork(Level level, BlockPos changedPos) {
        BlockEntity blockEntity = level.getBlockEntity(changedPos);
        if (!(blockEntity instanceof BlockEntityDirtElementPipe)) return;

        Set<DirtElementNetwork> adjacentNetworks = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(changedPos.relative(dir));
            if (neighbor instanceof BlockEntityDirtElementPipe pipe) {
                if (pipe.getNetwork() != null) {
                    adjacentNetworks.add(pipe.getNetwork());
                }
            }
        }

        if (adjacentNetworks.isEmpty()) {
            DirtElementNetwork built = DirtElementNetwork.buildNetwork(level, changedPos);
            this.networks.add(built);
        } else if (adjacentNetworks.size() == 1) {
            DirtElementNetwork existing = adjacentNetworks.iterator().next();
            DirtElementNetwork built = DirtElementNetwork.buildNetwork(level, changedPos);
            this.networks.remove(existing);
            this.networks.add(built);
        } else {
            // 多个网络 → 合并
            DirtElementNetwork merged = new DirtElementNetwork();
            for (DirtElementNetwork network : adjacentNetworks) {
                merged.getPipes().addAll(network.getPipes());
                merged.getProducers().addAll(network.getProducers());
                merged.getStorages().addAll(network.getStorages());
                merged.getConsumers().addAll(network.getConsumers());
                this.networks.remove(network);
            }
            DirtElementNetwork built = DirtElementNetwork.buildNetwork(level, changedPos);
            merged.getPipes().addAll(built.getPipes());
            merged.getProducers().addAll(built.getProducers());
            merged.getStorages().addAll(built.getStorages());
            merged.getConsumers().addAll(built.getConsumers());
            this.networks.add(merged);
        }

        setDirty(); // 必须标记脏，才能存盘
    }}
