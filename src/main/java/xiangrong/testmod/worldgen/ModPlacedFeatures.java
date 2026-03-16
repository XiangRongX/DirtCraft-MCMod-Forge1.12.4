package xiangrong.testmod.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import xiangrong.testmod.TestMod;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> COMPRESSED_DIRT_PLACED_KEY = registerKey("compressed_dirt_placed");
    public static final ResourceKey<PlacedFeature> DIRT_ESSENCE_ORE_PLACED_KEY = registerKey("dirt_essence_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> compressedDirtConfigured =
                configuredFeatures.getOrThrow(ModConfiguredFeatures.COMPRESSED_DIRT_KEY);
        Holder<ConfiguredFeature<?, ?>> dirtEssenceOreConfigured =
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DIRT_ESSENCE_ORE_KEY);


        List<PlacementModifier> compressedDirtPlacement = ModOrePlacement.commonOrePlacement(
                256,
                HeightRangePlacement.uniform(
                        VerticalAnchor.absolute(6),
                        VerticalAnchor.absolute(128)
                )
        );
        List<PlacementModifier> dirtEssenceOrePlacement = ModOrePlacement.commonOrePlacement(
                10,
                HeightRangePlacement.uniform(
                        VerticalAnchor.absolute(6),
                        VerticalAnchor.absolute(32)
                )
        );


        register(context, COMPRESSED_DIRT_PLACED_KEY, compressedDirtConfigured, compressedDirtPlacement);
        register(context, DIRT_ESSENCE_ORE_PLACED_KEY, dirtEssenceOreConfigured, dirtEssenceOrePlacement);
    }

    private static ResourceKey<PlacedFeature> registerKey(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, name));
    }

   private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
   }
}
