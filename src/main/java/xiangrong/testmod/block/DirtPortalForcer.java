package xiangrong.testmod.block;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import xiangrong.testmod.villager.ModVillagers;

import java.util.Comparator;
import java.util.Optional;

public class DirtPortalForcer {
    public static final int TICKET_RADIUS = 5;
    private static final int PORTAL_RADIUS = 32;
    private static final int FRAME_OUTER_SIZE  = 4;
    private static final int PORTAL_INNER_SIZE  = 2;
    private static final int NOTHING_FOUND = -1;
    protected final ServerLevel level;

    public DirtPortalForcer(ServerLevel pLevel) {
        this.level = pLevel;
    }

    public Optional<BlockPos> findClosestPortalPosition(BlockPos exitPos, boolean isDirtWorld, WorldBorder worldBorder) {
        PoiManager poimanager = this.level.getPoiManager();
        poimanager.ensureLoadedAndValid(this.level, exitPos, PORTAL_RADIUS);
        return poimanager.getInSquare(poiTypeHolder -> poiTypeHolder.is(ModVillagers.DIRT_PORTAL_POI.getHolder().get()), exitPos, PORTAL_RADIUS, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(worldBorder::isWithinBounds)
                .min(Comparator.<BlockPos>comparingDouble(blockPos -> blockPos.distSqr(exitPos)).thenComparingInt(Vec3i::getY));
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos targetPos, Direction.Axis axis) {
        // axis 在水平门上其实可以忽略，但保留签名以兼容调用方
        double bestDist = -1.0;
        BlockPos bestInnerBase = null;
        double secondDist = -1.0;
        BlockPos secondInnerBase = null;

        WorldBorder worldBorder = this.level.getWorldBorder();
        // i：可放置的最大高度 (类似原版)
        int maxY = Math.min(this.level.getMaxY(), this.level.getMinY() + this.level.getLogicalHeight() - 1);

        // 在 targetPos 周围螺旋搜索（radius 16，方向 EAST,SOUTH）
        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(targetPos, 16, Direction.EAST, Direction.SOUTH)) {
            // 获取该 x,z 的地表高度（motion_blocking）
            int surfaceY = Math.min(maxY, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, candidate.getX(), candidate.getZ()));
            if (!worldBorder.isWithinBounds(candidate) || !worldBorder.isWithinBounds(candidate.above())) continue;

            // 从 surfaceY 向下搜寻合适的 y（寻找可替换区域直到底下为非替换块）
            BlockPos.MutableBlockPos check = candidate.mutable();
            for (int y = surfaceY; y >= this.level.getMinY(); y--) {
                check.setY(y);

                // 要能放下内层 portal 区域（2x2），且该位置下面应有足够支撑（框架需要支撑）
                if (canPlaceInnerPortalAt(check)) {
                    // 确保下面一格不是无限下降（我们尝试把 portal 放在“地面上”，即下面应是实心）
                    // 如果下面全是可替换方块（像空气），那就向下移动直到遇到实心或到 minY
                    BlockPos.MutableBlockPos downProbe = check.mutable();
                    int probeY = y;
                    while (probeY > this.level.getMinY() && allInnerReplaceable(downProbe.move(Direction.DOWN))) {
                        probeY--;
                    }
                    check.setY(probeY);

                    // 在这个位置，检测框架是否能被支撑、内部是否可替换（边框下为实心、边框本体可替换）
                    if (canHostFrameAt(check)) {
                        double distSqr = check.distSqr(targetPos);
                        if (bestDist == -1.0 || distSqr < bestDist) {
                            bestDist = distSqr;
                            bestInnerBase = check.immutable();
                        } else if (secondDist == -1.0 || distSqr < secondDist) {
                            secondDist = distSqr;
                            secondInnerBase = check.immutable();
                        }
                    }
                }
            }
        }

        // 如果没找到首选，尝试用次选
        if (bestInnerBase == null && secondInnerBase != null) {
            bestInnerBase = secondInnerBase;
            bestDist = secondDist;
        }

        // 如果仍没找到，使用兜底生成（在 targetPos 附近生成一个默认的 4x4 框）
        if (bestInnerBase == null) {
            int minAllowedY = Math.max(this.level.getMinY() + 1, 1); // 保守选择
            int maxAllowedY = maxY - 5; // 留一点空间

            if (maxAllowedY < minAllowedY) return Optional.empty();

            int clampedY = Mth.clamp(targetPos.getY(), minAllowedY, maxAllowedY);
            // 将 innerBase 设为 targetPos 的偏移，使 frame 居中（把 innerBase 置为 targetPos.x-1, z-1）
            BlockPos defaultInner = new BlockPos(targetPos.getX() - 1, clampedY, targetPos.getZ() - 1);
            // 把 defaultInner 限制到 world border 内
            defaultInner = worldBorder.clampToBounds(defaultInner);

            // 在 defaultInner 位置尝试生成一个空地并放置框架（参照原版）
            // 生成预备区域：把外框位置放成压缩泥土，内部 2x2 放 portal 方块
            // 但要先把上方空间清理成空气（尽量模拟原版 createPortal 的行为）
            // 这里我们清理一个 4x4 区域向上 1-2 格
            BlockPos.MutableBlockPos mut = defaultInner.mutable();
            for (int dx = -1; dx <= FRAME_OUTER_SIZE - 2; dx++) { // dx -1..2
                for (int dz = -1; dz <= FRAME_OUTER_SIZE - 2; dz++) {
                    for (int dy = 0; dy < 2; dy++) {
                        mut.setWithOffset(defaultInner, dx, dy, dz);
                        this.level.setBlockAndUpdate(mut, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // 设 bestInnerBase
            bestInnerBase = defaultInner;
        }

        // 到这儿我们有了 bestInnerBase（2x2 内部左上角）
        // 把周围 4x4 写入：外框压缩泥土，内层放 DIRT_PORTAL
        BlockPos innerBase = bestInnerBase.immutable();
        BlockPos frameOrigin = innerBase.offset(-1, 0, -1); // frame 从 innerBase 左上角的外框开始 (4x4)
        // 外框：压缩泥土；内部：portal 方块
        BlockState frameState = ModBlocks.COMPRESSED_DIRT_BLOCK.get().defaultBlockState();
        BlockState portalState = ModBlocks.DIRT_PORTAL_BLOCK.get().defaultBlockState();
        // 生成：注意使用与原版相同的标志 3/18
        BlockPos.MutableBlockPos write = frameOrigin.mutable();
        for (int fx = 0; fx < FRAME_OUTER_SIZE; fx++) {
            for (int fz = 0; fz < FRAME_OUTER_SIZE; fz++) {
                write.setWithOffset(frameOrigin, fx, 0, fz);
                boolean isEdge = (fx == 0 || fx == FRAME_OUTER_SIZE - 1 || fz == 0 || fz == FRAME_OUTER_SIZE - 1);
                if (isEdge) {
                    // 框
                    this.level.setBlock(write, frameState, 18);
                } else {
                    // 内层 portal（2x2）
                    this.level.setBlock(write, portalState, 18);
                    this.level.getPoiManager().add(write, ModVillagers.DIRT_PORTAL_POI.getHolder().get());
                }
            }
        }

        // 返回 innerBase 和 2x2 的矩形（BlockUtil.FoundRectangle 以 innerBase、axis1、axis2 表示）
        return Optional.of(new BlockUtil.FoundRectangle(innerBase, PORTAL_INNER_SIZE, PORTAL_INNER_SIZE));
    }

    /**
     * 判断内层 2x2 的每一格是否可替换（用于判断能否把 portal 放在 innerBase）
     */
    private boolean canPlaceInnerPortalAt(BlockPos.MutableBlockPos innerBase) {
        // innerBase 代表内层左上角（minX,minY,minZ）
        for (int dx = 0; dx < PORTAL_INNER_SIZE; dx++) {
            for (int dz = 0; dz < PORTAL_INNER_SIZE; dz++) {
                BlockPos pos = innerBase.offset(dx, 0, dz);
                if (!canPortalReplaceBlock(pos.mutable())) return false;
            }
        }
        return true;
    }

    private boolean allInnerReplaceable(BlockPos.MutableBlockPos base) {
        for (int dx = 0; dx < PORTAL_INNER_SIZE; dx++) {
            for (int dz = 0; dz < PORTAL_INNER_SIZE; dz++) {
                if (!canPortalReplaceBlock(base.offset(dx, 0, dz).mutable())) return false;
            }
        }
        return true;
    }

    private boolean canHostFrameAt(BlockPos.MutableBlockPos innerBase) {
        // frameOrigin = innerBase.offset(-1, 0, -1)
        BlockPos.MutableBlockPos probe = innerBase.mutable();

        for (int fx = -1; fx <= PORTAL_INNER_SIZE; fx++) { // -1 .. 2 for 4x4
            for (int fz = -1; fz <= PORTAL_INNER_SIZE; fz++) {
                probe.setWithOffset(innerBase, fx, 0, fz);
                boolean isEdge = (fx == -1 || fx == PORTAL_INNER_SIZE || fz == -1 || fz == PORTAL_INNER_SIZE);

                if (isEdge) {
                    // 框位置：下方必须是实心（支撑）
                    BlockState below = this.level.getBlockState(probe.below());
                    if (!below.isSolid()) {
                        return false;
                    }
                    // 框位置本身必须可替换（我们要放压缩泥土）
                    if (!canPortalReplaceBlock(probe)) {
                        return false;
                    }
                } else {
                    // 内层：必须可替换，用于放 portal
                    if (!canPortalReplaceBlock(probe)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }
}
