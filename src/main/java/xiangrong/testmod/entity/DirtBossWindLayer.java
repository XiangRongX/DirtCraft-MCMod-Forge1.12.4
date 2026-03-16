package xiangrong.testmod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import xiangrong.testmod.TestMod;

import static xiangrong.testmod.entity.ModModelLayers.DIRT_BOSS_WIND;

public class DirtBossWindLayer extends RenderLayer<DirtBossRenderState, DirtBossModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"textures/entity/dirt_boss_wind.png");
    private final DirtBossWindModel model;


    public DirtBossWindLayer(EntityRendererProvider.Context pContext, RenderLayerParent<DirtBossRenderState, DirtBossModel> pRenderer) {
        super(pRenderer);
        this.model = new DirtBossWindModel(pContext.bakeLayer(DIRT_BOSS_WIND));
    }

    @Override
    public void render(PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight,
                       DirtBossRenderState state,
                       float limbSwing,
                       float limbSwingAmount) {

        // --- 加入 PoseStack 缩放和位置调整 ---
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.5F, 0.0F); // 往上抬一格，防止风埋到地里
        poseStack.scale(1.6F, 1.2F, 1.6F);     // 放大风层两倍，你可以调整这个值

        VertexConsumer vertexConsumer =
                buffer.getBuffer(RenderType.breezeWind(
                        TEXTURE_LOCATION,
                        this.xOffset(state.ageInTicks) % 1.0F,
                        0.0F
                ));
        this.model.setupAnim(state);
        DirtBossRenderer.enable(this.model, this.model.wind())
                .renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private float xOffset(float pTickCount) {
        return pTickCount * 0.02F;
    }
}
