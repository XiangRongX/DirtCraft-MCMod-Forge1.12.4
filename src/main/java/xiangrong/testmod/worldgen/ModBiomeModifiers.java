package xiangrong.testmod.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import xiangrong.testmod.TestMod;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_COMPRESSED_DIRT = registerKey("add_compressed_dirt");
    public static final ResourceKey<BiomeModifier> ADD_DIRT_ESSENCE_ORE = registerKey("add_dirt_essence_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context){
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_COMPRESSED_DIRT, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"dirt_world_biome")))),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.COMPRESSED_DIRT_PLACED_KEY)),
                GenerationStep.Decoration.RAW_GENERATION
        ));
        context.register(ADD_DIRT_ESSENCE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"dirt_world_biome")))),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.DIRT_ESSENCE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name){
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, name));
    }
}
