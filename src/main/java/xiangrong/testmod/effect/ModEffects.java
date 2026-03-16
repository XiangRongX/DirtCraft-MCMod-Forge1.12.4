package xiangrong.testmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TestMod.MODID);

    public static final RegistryObject<MobEffect> DIRT_PROTECTION_EFFECT =
            MOB_EFFECTS.register("dirt_protection_effect", EffectDirtProtection::new);
}
