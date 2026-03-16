package xiangrong.testmod.potion;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.effect.ModEffects;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, TestMod.MODID);

    public static final RegistryObject<Potion> DIRT_PROTECTION_POTION = POTIONS.register("dirt_protection_potion",
            ()->new Potion("dirt_protection_potion",new MobEffectInstance(ModEffects.DIRT_PROTECTION_EFFECT.getHolder().get(),3600)));
}
