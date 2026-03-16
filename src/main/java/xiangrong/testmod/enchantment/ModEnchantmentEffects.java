package xiangrong.testmod.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModEnchantmentEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentLocationBasedEffect>> LOCATION_BASED_ENCHANTMENT_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, TestMod.MODID);

    public static final RegistryObject<MapCodec<? extends EnchantmentEntityEffect>> DIRT_DESTROYER = LOCATION_BASED_ENCHANTMENT_EFFECTS.register("dirt_destroyer",
            () -> EnchantmentDirtDestroyer.CODEC);
}
