package xiangrong.testmod.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.ModBlocks;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> COMPRESSED_DIRT_KEY = registerKey("compressed_dirt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DIRT_ESSENCE_ORE_KEY = registerKey("dirt_essence_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest compressedDirtReplaceables = new BlockMatchTest(Blocks.DIRT);
        RuleTest dirtEssenceOreReplaceables = new BlockMatchTest(Blocks.DIRT);

        register(context, COMPRESSED_DIRT_KEY, Feature.ORE, new OreConfiguration(compressedDirtReplaceables, ModBlocks.COMPRESSED_DIRT_BLOCK.get().defaultBlockState(), 9));
        register(context, DIRT_ESSENCE_ORE_KEY, Feature.ORE, new OreConfiguration(dirtEssenceOreReplaceables, ModBlocks.DIRT_ESSENCE_ORE.get().defaultBlockState(), 3));

    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name){
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, name));
    }
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
