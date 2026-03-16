package xiangrong.testmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.BlockDirtPortal;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.item.ModItems;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemInWaterHandler {
    private static final Set<ItemEntity> trackedItems = new HashSet<>();

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.getItem() == ModItems.DIRT_STAR.get()) { // 替换为你的激活物品
            trackedItems.add(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 在服务器端每 tick 检查被跟踪的物品
            Iterator<ItemEntity> iterator = trackedItems.iterator();

            while (iterator.hasNext()) {
                ItemEntity itemEntity = iterator.next();

                // 如果物品已经被移除，停止跟踪
                if (!itemEntity.isAlive()) {
                    iterator.remove();
                    continue;
                }

                // 检查物品是否在水中
                if (itemEntity.isInWater()) {
                    Level level = itemEntity.level();
                    BlockPos pos = itemEntity.blockPosition();
                    BlockPos innerStart = findValidPortalFrame(level, pos);
                    if (innerStart!=null){
                        BlockDirtPortal.createPortalAt((ServerLevel) level, innerStart);
                        level.levelEvent(1038, itemEntity.blockPosition(), 0);
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                        if (lightning != null) {
                            lightning.moveTo(Vec3.atBottomCenterOf(itemEntity.blockPosition())); // 定位到门位置
                            level.addFreshEntity(lightning);
                        }
                        itemEntity.discard();
                    }
                }
            }
        }
    }

    @Nullable
    private static BlockPos findValidPortalFrame(Level level, BlockPos itemPos) {
        int y = itemPos.getY();
        int[][] offsets = {{0,0},{0,-1},{-1,0},{-1,-1}}; // 四种情况

        for (int[] offset : offsets) {
            BlockPos innerStart = itemPos.offset(offset[0], 0, offset[1]); // 内层2x2左上角
            boolean valid = true;

            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    BlockPos pos = innerStart.offset(x - 1, 0, z - 1); // 外框区域
                    boolean isEdge = x == 0 || x == 3 || z == 0 || z == 3;
                    BlockState state = level.getBlockState(pos);

                    if (isEdge) {
                        if (!state.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())) {
                            valid = false;
                            break;
                        }
                    } else {
                        if (!state.getFluidState().is(Fluids.WATER)) {
                            valid = false;
                            break;
                        }
                    }
                }
                if (!valid) break;
            }

            if (valid) {
                return innerStart; // 找到就返回这一个
            }
        }

        return null; // 没找到
    }



}
