package xiangrong.testmod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.RenderShape;

public class AbsorbedBlockRenderer extends EntityRenderer<EntityAbsorbedBlock, AbsorbedBlockRenderState> {

    public AbsorbedBlockRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public AbsorbedBlockRenderState createRenderState() {
        return new AbsorbedBlockRenderState();
    }

    @Override
    public void render(AbsorbedBlockRenderState pRenderState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight) {
        if (pRenderState.blockState == null || pRenderState.blockState.getRenderShape() == RenderShape.INVISIBLE) {
            // 打印调试信息，方便查为什么是 null
            System.err.println("============================================================");
            System.err.println("[AbsorbedBlockRenderer] blockState is null, skip rendering.");
            System.err.println("===========================================================");
            return;
        }

        pPoseStack.pushPose();

        pPoseStack.translate(-0.5D, 0.0D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                pRenderState.blockState,
                pPoseStack,
                pBufferSource,
                pPackedLight,
                OverlayTexture.NO_OVERLAY
        );

        pPoseStack.popPose();
    }

    @Override
    public void extractRenderState(EntityAbsorbedBlock pEntity, AbsorbedBlockRenderState pReusedState, float pPartialTick) {
        super.extractRenderState(pEntity, pReusedState, pPartialTick);
        pReusedState.blockState = pEntity.getBlockStateToRender();
    }
}
