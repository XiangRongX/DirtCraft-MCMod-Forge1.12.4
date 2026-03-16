package xiangrong.testmod.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import xiangrong.testmod.item.ModItems;

public class EntityDirtBall extends ThrowableItemProjectile {
    public EntityDirtBall(EntityType<? extends EntityDirtBall> type, Level world) {
        super(type, world);
    }

    public EntityDirtBall(EntityType<? extends EntityDirtBall> type,
                          LivingEntity thrower, Level level, ItemStack stack) {
        super(type, thrower, level, stack);
    }

    public EntityDirtBall(Level pLevel, LivingEntity pOwner, ItemStack pItem) {
        super(ModEntities.DIRT_BALL.get(), pOwner, pLevel, pItem);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.DIRT_BALL.get();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (!this.level().isClientSide) {
            if (hitResult.getEntity() instanceof LivingEntity target) {
                target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
            }

            // 播放炸开粒子
            ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                    this.getX(), this.getY(), this.getZ(),
                    20, // 数量
                    0.25D, 0.25D, 0.25D, // 偏移范围
                    0.05D // 速度
            );

            discard();
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        super.onHitBlock(hitResult);

        if (!this.level().isClientSide) {
            // 播放声音
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ROOTED_DIRT_FALL, SoundSource.BLOCKS, 0.5F, 1.0F);

            // 播放炸开粒子
            ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                    this.getX(), this.getY(), this.getZ(),
                    20,
                    0.25D, 0.25D, 0.25D,
                    0.05D
            );

            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

}
