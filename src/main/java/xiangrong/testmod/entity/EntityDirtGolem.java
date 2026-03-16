package xiangrong.testmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import xiangrong.testmod.block.ModBlocks;
import xiangrong.testmod.item.ModItems;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class EntityDirtGolem extends AbstractGolem implements RangedAttackMob, Shearable {
    private static final EntityDataAccessor<Byte> DATA_HEAD = SynchedEntityData.defineId(EntityDirtGolem.class, EntityDataSerializers.BYTE);


    public EntityDirtGolem(EntityType<? extends EntityDirtGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 15.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        double d0 = pTarget.getX() - this.getX();
        double d1 = pTarget.getEyeY() - 1.1F;
        double d2 = pTarget.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        if (this.level() instanceof ServerLevel serverlevel) {
            ItemStack itemstack = new ItemStack(ModItems.DIRT_BALL.get());
            Projectile.spawnProjectile(
                    new EntityDirtBall(serverlevel, this, itemstack),
                    serverlevel,
                    itemstack,
                    p_375116_ -> p_375116_.shoot(d0, d1 + d3 - p_375116_.getY(), d2, 1.6F, 12.0F)
            );
        }

        playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GRASS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GRAVEL_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_331373_) {
        super.defineSynchedData(p_331373_);
        p_331373_.define(DATA_HEAD, (byte)16);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("Pumpkin")) {
            this.setPumpkin(pCompound.getBoolean("Pumpkin"));
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (source.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(level, ModBlocks.DIRT_GOLEM_HEAD_ITEM.get());
        }
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (false && itemstack.is(Items.SHEARS) && this.readyForShearing()) { //Forge: Moved to onSheared
            if (this.level() instanceof ServerLevel serverlevel) {
                this.shear(serverlevel, SoundSource.PLAYERS, itemstack);
                this.gameEvent(GameEvent.SHEAR, pPlayer);
                itemstack.hurtAndBreak(1, pPlayer, getSlotForHand(pHand));
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void shear(ServerLevel p_365069_, SoundSource p_29907_, ItemStack p_369140_) {
        p_365069_.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, p_29907_, 1.0F, 1.0F);
        this.setPumpkin(false);
        this.dropFromShearingLootTable(p_365069_, BuiltInLootTables.SHEAR_SNOW_GOLEM, p_369140_, (p_359192_, p_359193_) -> this.spawnAtLocation(p_359192_, p_359193_, this.getEyeHeight()));
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && this.hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (this.entityData.get(DATA_HEAD) & 16) != 0;
    }

    public void setPumpkin(boolean pPumpkinEquipped) {
        byte b0 = this.entityData.get(DATA_HEAD);
        if (pPumpkinEquipped) {
            this.entityData.set(DATA_HEAD, (byte)(b0 | 16));
        } else {
            this.entityData.set(DATA_HEAD, (byte)(b0 & -17));
        }
    }

    @Override
    public java.util.List<ItemStack> onSheared(@Nullable Player player, @org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
        world.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
        this.gameEvent(GameEvent.SHEAR, player);
        if (!world.isClientSide() && world instanceof ServerLevel server) {
            setPumpkin(false);
            var ret = new java.util.ArrayList<ItemStack>();
            this.dropFromShearingLootTable(server, BuiltInLootTables.SHEAR_SNOW_GOLEM, item, (slevel, stack) -> ret.add(stack));
            return ret;
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(
                0.0,
                (double)(0.75F * this.getEyeHeight()),
                (double)(this.getBbWidth() * 0.4F)
        );
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity pTarget) {
        if (pTarget != null) {
            // 如果是自己召唤的小弟，直接忽略
            if (pTarget.getType() == ModEntities.DIRT_BOSS.get()) {
                return;
            }
        }
        super.setTarget(pTarget);
    }

}
