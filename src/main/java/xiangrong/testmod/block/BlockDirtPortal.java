package xiangrong.testmod.block;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.particle.ModParticles;
import xiangrong.testmod.villager.ModVillagers;

import java.util.Optional;


public class BlockDirtPortal extends Block implements Portal {
    public static final ResourceKey<Level> DIRT_WORLD_KEY = ResourceKey.create(Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_world"));
    protected static final VoxelShape HORIZONTAL_PORTAL_AABB = Block.box(0.0, 6, 0.0,16.0, 10.0, 16.0);

    public BlockDirtPortal(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return HORIZONTAL_PORTAL_AABB;
    }

    @Override
    protected void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pEntity.canUsePortal(false)) {
            pEntity.setAsInsidePortal(this, pPos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel p_342064_, Entity p_344634_) {
        return 1;
    }


    @Nullable
    @Override
    public TeleportTransition getPortalDestination(ServerLevel originLevel, Entity entity, BlockPos portalPos) {
        // 1) 确定目标维度：如果在我们自定义维度就回主世界，否则去自定义维度
        ResourceKey<Level> targetKey = originLevel.dimension() == DIRT_WORLD_KEY ? Level.OVERWORLD : DIRT_WORLD_KEY;
        ServerLevel targetLevel = originLevel.getServer().getLevel(targetKey);
        if (targetLevel == null) return null;

        // 2) 目标 worldborder
        WorldBorder worldborder = targetLevel.getWorldBorder();

        // 3) 如果不同维度，按比例缩放位置（如果你的维度不缩放可把 d0 = 1.0）
        double scale = DimensionType.getTeleportationScale(originLevel.dimensionType(), targetLevel.dimensionType());
        BlockPos scaled = worldborder.clampToBounds(
                Mth.floor(entity.getX() * scale),
                Mth.floor(entity.getY()),
                Mth.floor(entity.getZ() * scale)
        );

        boolean isNetherLike = targetLevel.dimension() == Level.NETHER; // 原版用这个决定搜索半径，保持兼容
        return this.getExitPortal(targetLevel, entity, portalPos, scaled, isNetherLike, worldborder);
    }

    @Nullable
    private TeleportTransition getExitPortal(
            ServerLevel targetLevel,
            Entity entity,
            BlockPos originPortalPos,
            BlockPos exitSearchPos,
            boolean isNetherLike,
            WorldBorder worldborder
    ) {
        DirtPortalForcer portalForcer = new DirtPortalForcer(targetLevel);
        // 先尝试找到最近的已有 portal（通过 PortalForcer 的 POI 或自定义查找）
        Optional<BlockPos> maybePortalPos = portalForcer.findClosestPortalPosition(exitSearchPos, isNetherLike, worldborder);

        BlockUtil.FoundRectangle foundRect;
        TeleportTransition.PostTeleportTransition postTransition;

        if (maybePortalPos.isPresent()) {
            BlockPos portalBlockPos = maybePortalPos.get();
            BlockState portalBlockState = targetLevel.getBlockState(portalBlockPos);

            // 获取整个矩形范围 — 注意：这里第一个 axis 参数跟你的门方向语义有关
            // 原版使用 HORIZONTAL_AXIS + Y 做为两个维度。我们门是水平的 (在 XZ 平面)，
            // 但为了兼容 BlockUtil.getLargestRectangleAround 的签名我们还是传入 Direction.Axis.X 作为主轴，
            // 以及 Direction.Axis.Y 作为第二轴（这个调用只是用来返回矩形，后续我们会忽略 axis 参数的垂直含义）。
            Direction.Axis guessedAxis = portalBlockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
                    ? portalBlockState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                    : Direction.Axis.X;

            foundRect = BlockUtil.getLargestRectangleAround(
                    portalBlockPos,
                    guessedAxis,
                    21,                 // 搜索扩展：与原版一致的样式
                    Direction.Axis.Y,
                    21,
                    pos -> targetLevel.getBlockState(pos) == portalBlockState
            );

            // 找到已有门，postTransition：播放音效并 place portal ticket（原版行为）
            postTransition = TeleportTransition.PLAY_PORTAL_SOUND.then(t -> t.placePortalTicket(portalBlockPos));
        } else {
            // 没找到已有门，则尝试在目标位置创建一个门（使用 targetLevel.getPortalForcer().createPortal）
            // 这里 directionAxis 用 origin 方块的 AXIS（如果存在）作为偏好，否则默认 X
            Direction.Axis preferAxis = Direction.Axis.X;
            Optional<BlockUtil.FoundRectangle> created = portalForcer.createPortal(exitSearchPos, preferAxis);

            if (created.isEmpty()) {
                //LOGGER.error("Unable to create a dirt portal (likely target out of worldborder)");
                return null;
            }

            foundRect = created.get();
            postTransition = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        }

        // 有了 foundRect（表示目标维度中 portal 的矩形），接下来计算落点并返回 TeleportTransition
        return getDimensionTransitionFromExit(entity, originPortalPos, foundRect, targetLevel, postTransition);
    }

    private static TeleportTransition getDimensionTransitionFromExit(
            Entity entity,
            BlockPos originPortalBlockPos,
            BlockUtil.FoundRectangle targetRect,
            ServerLevel targetLevel,
            TeleportTransition.PostTeleportTransition postTeleportTransition
    ) {
        // 要根据 originPortalBlockPos 处的方块 state 推断玩家从哪个方向进来的（用于保持朝向）
        BlockState originState = entity.level().getBlockState(originPortalBlockPos);
        Direction.Axis originAxis = originState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
                ? originState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                : Direction.Axis.X;

        // 计算实体在原门内的相对位置（若可能）
        BlockUtil.FoundRectangle entranceRect = BlockUtil.getLargestRectangleAround(
                originPortalBlockPos,
                originAxis,
                21,
                Direction.Axis.Y,
                21,
                p -> entity.level().getBlockState(p) == originState
        );

        // 得到实体在门内的相对坐标 (0..1 range)
        Vec3 relative = entity.getRelativePortalPosition(originAxis, entranceRect);

        // 现在把计算结果传给 createDimensionTransition（会尝试找门外空格）
        return createDimensionTransition(targetLevel, targetRect, originAxis, relative, entity, postTeleportTransition);
    }

    private static TeleportTransition createDimensionTransition(
            ServerLevel targetLevel,
            BlockUtil.FoundRectangle pRectangle,
            Direction.Axis fromAxis,
            Vec3 pOffset,
            Entity entity,
            TeleportTransition.PostTeleportTransition postTeleportTransition
    ) {
        BlockPos rectMin = pRectangle.minCorner; // 内层最小角（inner 2x2 的 min corner）
        BlockState rectState = targetLevel.getBlockState(rectMin);
        Direction.Axis rectAxis = rectState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);

        double axis1Size = (double) pRectangle.axis1Size;
        double axis2Size = (double) pRectangle.axis2Size;

        // 实体的尺寸（用于检测安全位）
        EntityDimensions dims = entity.getDimensions(entity.getPose());

        // 我们希望玩家不出生在 portal 内部，而在 portal 外侧最近的安全空格
        // 首先列出 portal 外围候选点（按优先方向：N, S, W, E），这些点基于内层区域的边缘
        BlockPos[] candidates = new BlockPos[] {
                // west side (x = minX - 1) across middle z
                new BlockPos(rectMin.getX() - 1, rectMin.getY(), rectMin.getZ() + (int)(axis2Size / 2.0)),
                // east side (x = minX + axis1Size) across middle z
                new BlockPos(rectMin.getX() + (int)axis1Size, rectMin.getY(), rectMin.getZ() + (int)(axis2Size / 2.0)),
                // north side (z = minZ - 1)
                new BlockPos(rectMin.getX() + (int)(axis1Size / 2.0), rectMin.getY(), rectMin.getZ() - 1),
                // south side (z = minZ + axis2Size)
                new BlockPos(rectMin.getX() + (int)(axis1Size / 2.0), rectMin.getY(), rectMin.getZ() + (int)axis2Size)
        };

        // 扩展候选点：沿着每个方向在长度上扫一段（保证 2x2 门周边都能覆盖）
        java.util.List<BlockPos> expanded = new java.util.ArrayList<>();
        for (BlockPos c : candidates) {
            // 对每个主候选，再在相邻两格范围内也尝试（例如门宽多格时）
            int radius = Math.max(1, (int)Math.ceil(Math.max(axis1Size, axis2Size) / 2.0));
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    expanded.add(c.offset(dx, 0, dz));
                }
            }
        }

        // 找到第一个“可站立”的候选点：下面为实心或可站立方块，上方两个格为空或可立足
        BlockPos selected = null;
        for (BlockPos cand : expanded) {
            if (isSafeSpawnPos(targetLevel, cand)) {
                selected = cand;
                break;
            }
        }


        Vec3 desired = new Vec3((double)selected.getX() + 0.5, (double)selected.getY(), (double)selected.getZ() + 0.5);

        int yawDelta = (fromAxis == rectAxis) ? 0 : 90;

        return new TeleportTransition(targetLevel, desired, Vec3.ZERO, (float) yawDelta, 0.0F, Relative.union(Relative.DELTA, Relative.ROTATION), postTeleportTransition);
    }

    private static boolean isSafeSpawnPos(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState at = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());

        boolean groundSolid = below.isSolid();
        boolean spaceFree = (at.isAir() || !at.isSolidRender());
        boolean spaceAboveFree = (above.isAir() || !above.isSolidRender());

        return groundSolid && spaceFree && spaceAboveFree;
    }


    public static void createPortalAt(ServerLevel level, BlockPos base) {
        // base 作为 2x2 门区域的 min-corner（center inside）
        // 我们把 4x4 框的位置设置为 base.offset(-1,0,-1) .. base.offset(2,0,2)
        BlockPos frameOrigin = base.offset(-1, 0, -1);
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos p = frameOrigin.offset(x, 0, z);
                // 如果是外围（框），放压缩泥土；否则放 portal
                if (x == 0 || x == 3 || z == 0 || z == 3) {
                    level.setBlock(p, ModBlocks.COMPRESSED_DIRT_BLOCK.get().defaultBlockState(), 18);
                } else {
                    level.setBlock(p, ModBlocks.DIRT_PORTAL_BLOCK.get().defaultBlockState(), 18);
                    level.getPoiManager().add(p, ModVillagers.DIRT_PORTAL_POI.getHolder().get());
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(120) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                    0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 6; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.6; // 从门表面往上
            double z = pos.getZ() + random.nextDouble();
            double vx = (random.nextDouble() - 0.5) * 0.1;
            double vy = random.nextDouble() * 0.15;
            double vz = (random.nextDouble() - 0.5) * 0.1;
            level.addParticle(ModParticles.DIRT_PORTAL_PARTICLE.get(), x, y, z, vx, vy, vz);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader p_310044_, BlockPos p_54912_, BlockState p_54913_, boolean p_376456_) {
        return ItemStack.EMPTY;
    }

    private boolean isValidFrame(Level level, BlockPos center) {
        int minX = center.getX() - 1;
        int minZ = center.getZ() - 1;
        int y = center.getY();

        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                boolean isEdge = x == 0 || x == 3 || z == 0 || z == 3;
                BlockState state = level.getBlockState(pos);

                if (isEdge) {
                    if (!state.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())) return false; // 框必须是压缩泥土
                } else {
                    if (!state.getFluidState().is(Fluids.WATER)) return false; // 中间必须是水
                }
            }
        }

        return true;
    }


    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction dir,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {

        if (!neighborState.is(this)) {
            if (!isValidPortalFrame(level, pos)) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, level, tickAccess, pos, dir, neighborPos, neighborState, random);

    }
    private boolean isValidPortalFrame(LevelReader level, BlockPos portalPos) {
        int y = portalPos.getY();

        // 四种可能的左上角偏移
        int[][] offsets = {
                {-1, -1}, // portalPos 在右下角
                {-2, -1}, // portalPos 在右上角
                {-1, -2}, // portalPos 在左下角
                {-2, -2}  // portalPos 在左上角
        };

        for (int[] off : offsets) {
            int baseX = portalPos.getX() + off[0];
            int baseZ = portalPos.getZ() + off[1];

            if (checkFrame(level, baseX, y, baseZ)) {
                return true; // 任意一种情况成立就算有效
            }
        }

        return false;
    }
    private boolean checkFrame(LevelReader level, int baseX, int y, int baseZ) {
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                BlockPos checkPos = new BlockPos(baseX + x, y, baseZ + z);
                boolean isEdge = (x == 0 || x == 3 || z == 0 || z == 3);
                BlockState checkState = level.getBlockState(checkPos);

                if (isEdge) {
                    if (!checkState.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())) {
                        return false; // 框架缺失
                    }
                } else {
                    if (!checkState.is(ModBlocks.DIRT_PORTAL_BLOCK.get())) {
                        return false; // 内部必须是 portal
                    }
                }
            }
        }
        return true;
    }
}
