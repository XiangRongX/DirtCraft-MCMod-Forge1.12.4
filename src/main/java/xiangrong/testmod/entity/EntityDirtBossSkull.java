package xiangrong.testmod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityDirtBossSkull extends AbstractHurtingProjectile {
    public EntityDirtBossSkull(EntityType<? extends EntityDirtBossSkull> type, Level level) {
        super(type, level);
    }

    public EntityDirtBossSkull(Level level, LivingEntity owner, Vec3 movement) {
        super(ModEntities.DIRT_BOSS_SKULL.get(), owner, movement, level);
    }

    @Override
    protected float getInertia() {
        return 0.9F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = pResult.getEntity();
            boolean flag;

            if (this.getOwner() instanceof LivingEntity livingentity) {
                // 用普通爆炸伤害，不是凋零头伤害
                DamageSource damagesource = this.damageSources().explosion(this, livingentity);
                flag = entity.hurtServer(serverlevel, damagesource, 8.0F);

                if (flag) {
                    if (entity.isAlive()) {
                        // 触发附魔效果，比如火焰附加、击退等
                        EnchantmentHelper.doPostAttackEffects(serverlevel, entity, damagesource);
                    } else {
                        // 目标死了，射手回血
                        livingentity.heal(5.0F);
                    }
                }
            } else {
                entity.hurtServer(serverlevel, this.damageSources().explosion(this, null), 5.0F);
            }

        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }
}
