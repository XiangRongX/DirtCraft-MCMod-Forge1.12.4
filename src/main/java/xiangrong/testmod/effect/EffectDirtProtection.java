package xiangrong.testmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectDirtProtection extends MobEffect {
    public EffectDirtProtection() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public boolean applyEffectTick(ServerLevel pLevel, LivingEntity pEntity, int pAmplifier) {
        return false;
    }

}
