package xiangrong.testmod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import xiangrong.testmod.TestMod;

import static xiangrong.testmod.entity.ModModelLayers.DIRT_BOSS_SKULL;

public class DirtBossSkullRenderer extends EntityRenderer<EntityDirtBossSkull, DirtBossSkullRenderState> {
    private static final ResourceLocation DIRT_BOSS_SKULL_LOCATION = ResourceLocation.fromNamespaceAndPath(TestMod.MODID,"textures/entity/dirt_boss_head.png");

    private final SkullModel model;


    public DirtBossSkullRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new SkullModel(pContext.bakeLayer(DIRT_BOSS_SKULL));
    }

    @Override
    public DirtBossSkullRenderState createRenderState() {
        return new DirtBossSkullRenderState();
    }

    public static LayerDefinition createSkullLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(DirtBossSkullRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(DIRT_BOSS_SKULL_LOCATION));
        this.model.setupAnim(0.0F, state.yRot, state.xRot);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
    }

    public void extractRenderState(EntityDirtBossSkull p_360946_, DirtBossSkullRenderState p_363928_, float p_365976_) {
        super.extractRenderState(p_360946_, p_363928_, p_365976_);
        p_363928_.yRot = p_360946_.getYRot(p_365976_);
        p_363928_.xRot = p_360946_.getXRot(p_365976_);
    }
}
