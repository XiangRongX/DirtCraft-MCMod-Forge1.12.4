package xiangrong.testmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class EntityAbsorbedBlock extends Entity {

    // --- 同步字段 ---
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.BLOCK_STATE);

    private static final EntityDataAccessor<Float> DATA_START_X =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_START_Y =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_START_Z =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_END_X =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_SPEED =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_MAX_LIFE =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(EntityAbsorbedBlock.class, EntityDataSerializers.INT);

    private int age;
    private EntityDirtBoss owner; // 不同步，只在服务端存在

    public EntityAbsorbedBlock(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 初始化 (服务端调用)
    public void init(BlockState state, Vec3 start, Vec3 end, int maxLife, double speed, EntityDirtBoss owner) {
        this.owner = owner;
        this.age = 0;

        this.entityData.set(DATA_BLOCK_STATE, state);

        this.entityData.set(DATA_START_X, (float) start.x);
        this.entityData.set(DATA_START_Y, (float) start.y);
        this.entityData.set(DATA_START_Z, (float) start.z);

        this.entityData.set(DATA_END_X, (float) end.x);
        this.entityData.set(DATA_END_Y, (float) end.y);
        this.entityData.set(DATA_END_Z, (float) end.z);

        this.entityData.set(DATA_SPEED, (float) speed);
        this.entityData.set(DATA_MAX_LIFE, maxLife);

        this.entityData.set(DATA_OWNER_ID, owner.getId());

        this.moveTo(start.x, start.y, start.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.STONE.defaultBlockState());

        builder.define(DATA_START_X, 0f);
        builder.define(DATA_START_Y, 0f);
        builder.define(DATA_START_Z, 0f);

        builder.define(DATA_END_X, 0f);
        builder.define(DATA_END_Y, 0f);
        builder.define(DATA_END_Z, 0f);

        builder.define(DATA_SPEED, 0.1f);
        builder.define(DATA_MAX_LIFE, 100);

        builder.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age >= getMaxLife()) {
            discard();
            notifyOwner();
            return;
        }

        EntityDirtBoss ownerEntity = getOwner();
        if (ownerEntity == null) {
            discard();
            return;
        }

        Vec3 currentPos = this.position();
        Vec3 end = ownerEntity.position().add(0, ownerEntity.getBbHeight() * 0.5, 0);

        Vec3 dir = end.subtract(currentPos);
        double distance = dir.length();

        // 🟢 速度曲线：从 0.2 开始，平滑加速
        double lifeProgress = (double) age / (double) getMaxLife(); // 0.0 ~ 1.0
        double startSpeed = 0.2;   // 初速度
        double maxSpeed = getSpeed(); // 最大速度（建议设成 0.4~0.5）
        double dynamicSpeed = startSpeed + (maxSpeed - startSpeed) * lifeProgress;

        // 限制最大速度，避免瞬移感
        dynamicSpeed = Math.min(dynamicSpeed, maxSpeed);

        if (distance <= dynamicSpeed) {
            setPos(end);
            if (!level().isClientSide) notifyOwner();
            discard();
            return;
        }

        Vec3 move = dir.normalize().scale(dynamicSpeed);
        setPos(currentPos.add(move));

        if (level().isClientSide) {
            level().addParticle(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                    getX(), getY(), getZ(),
                    0, 0.05, 0
            );
        }
    }




    private void notifyOwner() {
        if (owner != null && !owner.isRemoved()) {
            owner.onAbsorbFinished();
        }
    }

    public BlockState getBlockStateToRender() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    public Vec3 getStart() {
        return new Vec3(entityData.get(DATA_START_X),
                entityData.get(DATA_START_Y),
                entityData.get(DATA_START_Z));
    }

    public Vec3 getEnd() {
        return new Vec3(entityData.get(DATA_END_X),
                entityData.get(DATA_END_Y),
                entityData.get(DATA_END_Z));
    }

    public double getSpeed() {
        return entityData.get(DATA_SPEED);
    }

    public int getMaxLife() {
        return entityData.get(DATA_MAX_LIFE);
    }

    public void setBlockStateToRender(BlockState state) {
        if (state == null) state = Blocks.STONE.defaultBlockState();
        this.entityData.set(DATA_BLOCK_STATE, state);
    }

    @Nullable
    public EntityDirtBoss getOwner() {
        if (this.owner != null) return this.owner;
        int id = this.entityData.get(DATA_OWNER_ID);
        if (id != -1 && this.level() != null) {
            Entity e = this.level().getEntity(id);
            if (e instanceof EntityDirtBoss boss) {
                this.owner = boss;
                return boss;
            }
        }
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel pLevel, DamageSource pDamageSource, float pAmount) {
        return false;
    }

    // --- NBT 持久化 ---
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setBlockStateToRender(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));

        this.entityData.set(DATA_START_X, tag.getFloat("StartX"));
        this.entityData.set(DATA_START_Y, tag.getFloat("StartY"));
        this.entityData.set(DATA_START_Z, tag.getFloat("StartZ"));

        this.entityData.set(DATA_END_X, tag.getFloat("EndX"));
        this.entityData.set(DATA_END_Y, tag.getFloat("EndY"));
        this.entityData.set(DATA_END_Z, tag.getFloat("EndZ"));

        this.entityData.set(DATA_SPEED, tag.getFloat("Speed"));
        this.entityData.set(DATA_MAX_LIFE, tag.getInt("MaxLife"));
        this.age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockStateToRender()));

        tag.putFloat("StartX", entityData.get(DATA_START_X));
        tag.putFloat("StartY", entityData.get(DATA_START_Y));
        tag.putFloat("StartZ", entityData.get(DATA_START_Z));

        tag.putFloat("EndX", entityData.get(DATA_END_X));
        tag.putFloat("EndY", entityData.get(DATA_END_Y));
        tag.putFloat("EndZ", entityData.get(DATA_END_Z));

        tag.putFloat("Speed", entityData.get(DATA_SPEED));
        tag.putInt("MaxLife", entityData.get(DATA_MAX_LIFE));
        tag.putInt("Age", age);
    }
}