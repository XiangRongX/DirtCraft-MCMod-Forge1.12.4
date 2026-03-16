package xiangrong.testmod.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import xiangrong.testmod.TestMod;

public class ModDimensions {
    public static final ResourceKey<LevelStem> DIRT_DIM_KEY = ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_dimension"));

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        var dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        var noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        context.register(DIRT_DIM_KEY,
                new LevelStem(
                        // 引用你的 dimension_type
                        dimensionTypes.getOrThrow(
                                ResourceKey.create(Registries.DIMENSION_TYPE,
                                        ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "dirt_dimension_type"))
                        ),
                        // 这里不写生成器！因为你已经在 data/testmod/dimension/dirt_dimension.json 里写了
                        // Minecraft 会自动读取 JSON 里的 generator
                        null
                )
        );
    }


}
