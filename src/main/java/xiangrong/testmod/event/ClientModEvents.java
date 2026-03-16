package xiangrong.testmod.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xiangrong.testmod.TestMod;
import xiangrong.testmod.block.BlockDirtGolemHead;
import xiangrong.testmod.block.DirtGolemHeadModel;
import xiangrong.testmod.entity.*;
import xiangrong.testmod.menu.ModMenuTypes;
import xiangrong.testmod.menu.ScreenDirtElementGenerator;
import xiangrong.testmod.menu.ScreenDirtElementStorage;
import xiangrong.testmod.menu.ScreenDirtEssenceExtractor;
import xiangrong.testmod.particle.ModParticles;
import xiangrong.testmod.particle.ParticleDirtPortal;

@Mod.EventBusSubscriber(modid = TestMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DIRT_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DIRT_GOLEM.get(), DirtGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.DIRT_BOSS.get(), DirtBossRenderer::new);
        event.registerEntityRenderer(ModEntities.DIRT_BOSS_SKULL.get(), DirtBossSkullRenderer::new);
        event.registerEntityRenderer(ModEntities.ABSORBED_BLOCK.get(), AbsorbedBlockRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DirtGolemHeadModel.DIRT_GOLEM_HEAD, DirtGolemHeadModel::createSnowGolemHeadLayer);
        event.registerLayerDefinition(ModModelLayers.DIRT_BOSS_SKULL, DirtBossSkullRenderer::createSkullLayer);
        event.registerLayerDefinition(ModModelLayers.DIRT_BOSS, () -> DirtBossModel.createBodyLayer(64, 64));
        event.registerLayerDefinition(ModModelLayers.DIRT_BOSS_WIND, () -> DirtBossWindModel.createWindLayer(128, 128));
    }

    @SubscribeEvent
    public static void onCreateSkullModel(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(BlockDirtGolemHead.Types.DIRT_GOLEM, modelSet -> new DirtGolemHeadModel(modelSet.bakeLayer(DirtGolemHeadModel.DIRT_GOLEM_HEAD)));
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DIRT_PORTAL_PARTICLE.get(), ParticleDirtPortal.Provider::new);
    }

    @SubscribeEvent
    public static void setup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ImmutableMap.Builder<SkullBlock.Type, ResourceLocation> builder = ImmutableMap.builder();
            builder.put(BlockDirtGolemHead.Types.DIRT_GOLEM, ResourceLocation.fromNamespaceAndPath(TestMod.MODID, "textures/entity/dirt_golem.png"));
            SkullBlockRenderer.SKIN_BY_TYPE.putAll(builder.build());
        });

        MenuScreens.register(ModMenuTypes.DIRT_ELEMENT_GENERATOR_MENU.get(), ScreenDirtElementGenerator::new);
        MenuScreens.register(ModMenuTypes.DIRT_ELEMENT_STORAGE_MENU.get(), ScreenDirtElementStorage::new);
        MenuScreens.register(ModMenuTypes.DIRT_ESSENCE_EXTRACTOR_MENU.get(), ScreenDirtEssenceExtractor::new);
    }

}
