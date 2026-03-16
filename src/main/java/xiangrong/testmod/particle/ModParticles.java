package xiangrong.testmod.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xiangrong.testmod.TestMod;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TestMod.MODID);

    public static final RegistryObject<SimpleParticleType> DIRT_PORTAL_PARTICLE = PARTICLES.register("dirt_portal_particle",
            () -> new SimpleParticleType(false));
}
