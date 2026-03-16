package xiangrong.testmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.enchantment.ModEnchantments;
import xiangrong.testmod.item.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EntityDirtBoss extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(EntityDirtBoss.class, EntityDataSerializers.INT);
    private static final int INVULNERABLE_TICKS = 220;
    private int absorbCooldown = 40; // 每 2 秒吸收一次（20 tick = 1 秒）
    private int absorbedCount = 0;   // 累积吸收数量
    private long lastAbsorbFinishedTick = 0;
    private int destroyBlocksTick;
    private boolean isAbsorbing = false;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS
    )
            .setDarkenScreen(true);

    protected EntityDirtBoss(EntityType<? extends EntityDirtBoss> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
        this.setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level p_186262_) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, p_186262_);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        return flyingpathnavigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new EntityDirtBoss.DoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332370_) {
        super.defineSynchedData(p_332370_);
        p_332370_.define(DATA_ID_INV, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Invul", this.getInvulnerableTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setInvulnerableTicks(pCompound.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    // 追击速度插值
    private double chaseSpeedFactor = 0.0; // 0~1，逐渐增长

    @Override
    public void aiStep() {
        Vec3 motion = this.getDeltaMovement();

        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            double desiredY;

            // 🟢 1. 无敌状态贴地悬停
            if (this.getInvulnerableTicks() > 0) {
                double groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        this.blockPosition().getX(),
                        this.blockPosition().getZ());
                desiredY = groundY + 0.5; // 几乎贴地
            }
            // 🟢 2. 有目标时悬浮到目标附近
            else if (target != null && target.isAlive()) {
                double targetY = target.getY();
                double maxAllowedY = targetY + 6.0;
                desiredY = Math.min(maxAllowedY, targetY + 3.0);
            }
            // 🟢 3. Idle 状态：悬浮在地面上方并缓慢上下浮动
            else {
                double groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        this.blockPosition().getX(),
                        this.blockPosition().getZ());
                // 增加一个小的上下浮动，周期大约 80 tick
                double floatOffset = Math.sin(this.tickCount / 20.0) * 0.3;
                desiredY = groundY + 3.5 + floatOffset;
            }

            // 计算垂直速度
            double dy = desiredY - this.getY();
            double verticalSpeed = dy * 0.1;
            verticalSpeed = Mth.clamp(verticalSpeed, -0.2, 0.2);
            motion = new Vec3(motion.x, verticalSpeed, motion.z);

            // 🟢 处理水平移动
            if (target != null && target.isAlive() && this.getInvulnerableTicks() <= 0) {
                Vec3 diff = new Vec3(target.getX() - this.getX(), 0, target.getZ() - this.getZ());
                double horizontalDist = diff.length();
                double desiredDist = 6.0;
                double deltaDist = horizontalDist - desiredDist;

                Vec3 dir = diff.lengthSqr() > 1e-4 ? diff.normalize() : Vec3.ZERO;

                if (deltaDist > 0.5) {
                    // 靠近玩家，限制最大速度
                    double maxSpeed = 0.12;  // 🟢 调小这里防止瞬移，可调 0.08-0.15
                    double approachSpeed = Math.min(deltaDist * 0.02, maxSpeed);
                    motion = motion.add(dir.scale(approachSpeed));
                } else if (deltaDist < -0.5) {
                    // 太近时缓慢后退
                    double backSpeed = Math.min(-deltaDist * 0.01, 0.05);
                    motion = motion.subtract(dir.scale(backSpeed));
                }

                // 给水平方向加阻尼，让它不会抖动
                motion = new Vec3(motion.x * 0.9, motion.y, motion.z * 0.9);

                // 平滑朝向玩家
                if (horizontalDist > 0.001) {
                    float yaw = (float) (Math.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0F;
                    this.yBodyRot = rotlerp(this.yBodyRot, yaw, 4.0F);
                    this.setYRot(this.yBodyRot);
                }
            }
            else if (this.getInvulnerableTicks() <= 0) {
                // Idle 随机水平漂浮
                if (this.tickCount % 40 == 0) {
                    double dx = (this.random.nextDouble() - 0.5) * 0.2;
                    double dz = (this.random.nextDouble() - 0.5) * 0.2;
                    motion = motion.add(dx, 0, dz);
                }
                // 给 Idle 状态加轻微阻尼，防止无限漂移
                motion = new Vec3(motion.x * 0.95, motion.y, motion.z * 0.95);

                // 朝向当前移动方向
                if (motion.horizontalDistanceSqr() > 1e-4) {
                    float yaw = (float)(Mth.atan2(motion.z, motion.x) * (180.0 / Math.PI)) - 90.0F;
                    this.yBodyRot = rotlerp(this.yBodyRot, yaw, 3.0F);
                    this.setYRot(this.yBodyRot);
                }
            }
        }

        this.setNoGravity(true);
        this.setDeltaMovement(motion);
        super.aiStep();

        // 🟢 无敌状态粒子
        if (this.getInvulnerableTicks() > 0) {
            float f3 = 3.3F * this.getScale();
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.9F),
                        this.getX() + this.random.nextGaussian(),
                        this.getY() + (double) (this.random.nextFloat() * f3),
                        this.getZ() + this.random.nextGaussian(),
                        0.0, 0.0, 0.0
                );
            }
        }
    }



    @Override
    protected void customServerAiStep(ServerLevel level) {
        // 如果有无敌阶段，执行倒计时逻辑
        if (this.getInvulnerableTicks() > 0) {
            int ticks = this.getInvulnerableTicks() - 1;
            this.bossEvent.setProgress(1.0F - (float) ticks / 220.0F);

            if (ticks <= 0) {
                // 无敌结束 -> 爆炸（可调威力）
                level.explode(this, this.getX(), this.getEyeY(), this.getZ(), 5.0F, false, Level.ExplosionInteraction.MOB);
                if (!this.isSilent()) {
                    level.globalLevelEvent(1023, this.blockPosition(), 0);
                }
            }

            this.setInvulnerableTicks(ticks);

            // 每 10 tick 回血
            if (this.tickCount % 10 == 0) {
                this.heal(10.0F);
            }
            return;
        }

        // 普通 AI
        super.customServerAiStep(level);

        // 不需要多头目标，只更新 Boss 血条
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // 可选：周期性回血
        if (this.tickCount % 20 == 0) {
            this.heal(1.0F);
        }

        // 可选：破坏附近方块
        if (this.destroyBlocksTick > 0) {
            this.destroyBlocksTick--;
            if (this.destroyBlocksTick == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this)) {
                boolean flag = false;
                int radius = Mth.floor(this.getBbWidth() / 2.0F + 1.0F);
                int height = Mth.floor(this.getBbHeight());

                for (BlockPos pos : BlockPos.betweenClosed(
                        this.getBlockX() - radius, this.getBlockY(), this.getBlockZ() - radius,
                        this.getBlockX() + radius, this.getBlockY() + height, this.getBlockZ() + radius
                )) {
                    BlockState state = level.getBlockState(pos);
                    if (state.canEntityDestroy(level, pos, this) &&
                            net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this, pos, state)) {
                        flag = level.destroyBlock(pos, true, this) || flag;
                    }
                }

                if (flag) {
                    level.levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }

        //吸方块召唤
        if(!isAbsorbing&&(this.tickCount - lastAbsorbFinishedTick>=absorbCooldown)){
            BlockPos target = findNearbyBlock();
            if(target!=null){
                isAbsorbing=true;
                startAbsorb(target);
            }
        }
    }

    private void startAbsorb(BlockPos target) {
        BlockState state = level().getBlockState(target);
        if(state.isAir()) return;

        Vec3 start = Vec3.atLowerCornerOf(target);
        Vec3 end = this.position().add(0,this.getBbHeight()*0.5,0);
        EntityAbsorbedBlock absorbedBlock = new EntityAbsorbedBlock(ModEntities.ABSORBED_BLOCK.get(), level());
        absorbedBlock.init(state,start,end,300,0.3,this);
        //absorbedBlock.setBlockStateToRender(level().getBlockState(target));
        level().addFreshEntity(absorbedBlock);

        level().removeBlock(target, false);
    }

    private BlockPos findNearbyBlock() {
        BlockPos origin = this.blockPosition();
        int radius = 10;
        List<BlockPos> candidates = new ArrayList<>();

        // 第一步：收集候选方块
        for (int y = origin.getY() + 3; y >= origin.getY() - 3; y--) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, y, origin.getZ() + z);
                    BlockState state = level().getBlockState(pos);

                    if (state.isAir()) continue; // 跳过空气

                    BlockPos above = pos.above();
                    if (!level().getBlockState(above).isAir()) continue; // 上面被挡住就不是地表

                    if(state.is(ModBlocks.COMPRESSED_DIRT_BLOCK.get())||state.is(Blocks.DIRT)||state.is(Blocks.GRASS_BLOCK)){
                        candidates.add(pos);
                    }

                }
            }
        }

        // 第二步：随机选一个
        if (candidates.isEmpty()) return null;
        return candidates.get(level().random.nextInt(candidates.size()));
    }

    public void onAbsorbFinished() {
        isAbsorbing = false;
        absorbedCount++;
        lastAbsorbFinishedTick=tickCount;
        if(absorbedCount==5){
            absorbedCount=0;
            summonMinions();
        }
    }

    private void summonMinions() {
        BlockPos basePos = this.blockPosition(); // BOSS 的当前位置
        Level level = this.level();

        // 找到地面高度
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                basePos.getX(), basePos.getZ());

        // 创建小弟实体
        EntityDirtGolem minion = new EntityDirtGolem(ModEntities.DIRT_GOLEM.get(), level());
        minion.moveTo(getX() + random.nextDouble() - 0.5,
                groundY + 1.0, // 离地面 1 格，避免卡地形
                getZ() + random.nextDouble() - 0.5,
                random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(minion);

        // 特效：粒子
        for (int i = 0; i < 20; i++) {
            double px = minion.getX() + 0.5 + (random.nextDouble() - 0.5);
            double py = minion.getY() + random.nextDouble();
            double pz = minion.getZ() + 0.5 + (random.nextDouble() - 0.5);
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    px, py, pz, 0, 0.2, 0
            );
        }

        // 声音
        level.playSound(null, minion, SoundEvents.GRAVEL_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);

        // 短暂效果
        minion.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false, true));
        minion.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false, true));
    }

    public void makeInvulnerable() {
        this.setInvulnerableTicks(220);
        this.bossEvent.setProgress(0.0F);
        this.setHealth(this.getMaxHealth() / 3.0F);
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity pTarget) {
        if (pTarget != null) {
            // 如果是自己召唤的小弟，直接忽略
            if (pTarget.getType() == ModEntities.DIRT_GOLEM.get()) {
                return;
            }
        }
        super.setTarget(pTarget);
    }

    @Override
    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    private double getHeadX(int pHead) {
        return this.getX();
    }

    private double getHeadY(int pHead) {
        return this.getY() + 2.5 * this.getScale();
    }

    private double getHeadZ(int pHead) {
        return this.getZ();
    }

    private float rotlerp(float pAngle, float pTargetAngle, float pMax) {
        float f = Mth.wrapDegrees(pTargetAngle - pAngle);
        if (f > pMax) {
            f = pMax;
        }

        if (f < -pMax) {
            f = -pMax;
        }

        return pAngle + f;
    }

    private void performRangedAttack(int pHead, LivingEntity pTarget) {
        this.performRangedAttack(
                pHead,
                pTarget.getX(),
                pTarget.getY() + (double)pTarget.getEyeHeight() * 0.5,
                pTarget.getZ()
        );
    }

    private void performRangedAttack(int pHead, double pX, double pY, double pZ) {
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1024, this.blockPosition(), 0);
        }

        double d0 = this.getHeadX(pHead);
        double d1 = this.getHeadY(pHead);
        double d2 = this.getHeadZ(pHead);
        double d3 = pX - d0;
        double d4 = pY - d1;
        double d5 = pZ - d2;
        Vec3 vec3 = new Vec3(d3, d4, d5);
        EntityDirtBossSkull skull = new EntityDirtBossSkull(this.level(), this, vec3.normalize());
        skull.setOwner(this);

        skull.setPos(d0, d1, d2);
        this.level().addFreshEntity(skull);
    }

    @Override
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        this.performRangedAttack(0, pTarget);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 通用免疫
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }

        // 无敌阶段免疫伤害
        if (this.getInvulnerableTicks() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        // 受伤后触发破坏方块倒计时（可选）
        if (this.destroyBlocksTick <= 0) {
            this.destroyBlocksTick = 20;
        }

        // 真正掉血
        return super.hurtServer(level, source, amount);
    }


    @Override
    protected void dropCustomDeathLoot(ServerLevel p_342980_, DamageSource p_31464_, boolean p_31466_) {
        super.dropCustomDeathLoot(p_342980_, p_31464_, p_31466_);

        ItemStack dirtStar = new ItemStack(ModItems.DIRT_STAR.get());
        this.spawnAtLocation(p_342980_, dirtStar);
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance p_182397_, @Nullable Entity p_182398_) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6F)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 4.0);
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int pInvulnerableTicks) {
        this.entityData.set(DATA_ID_INV, pInvulnerableTicks);
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean p_342371_) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.is(MobEffects.WITHER) ? false : super.canBeAffected(pPotioneffect);
    }

    class DoNothingGoal extends Goal {
        public DoNothingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return EntityDirtBoss.this.getInvulnerableTicks() > 0;
        }
    }

    @Override
    public boolean onGround() {
        return false;
    }


}
